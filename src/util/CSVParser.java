package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;

/**
 * an event-driven CSV parser.
 *
 *
 */
public class CSVParser
{

	public static final int NEWLINE_UNIX = 1;
	public static final int NEWLINE_WINDOWS = 2;
	public static final int NEWLINE_MAC = 3;
	public static final int NEWLINE_ANY = 4;


	/**
	 * interface to implement for receiving CSV parse events.
	 */
	public static interface Handler
	{
		public void startDocument(MetaData metaData);
		public void row(Row row);
		public void endDocument();
	}

	/**
	 * utility impl of {@link nextag.util.CSVParser.Handler} that has empty function defs.
	 */
	public static class HandlerAdapter implements Handler
	{
		public void startDocument(MetaData metaData) {}
		public void row(Row row) {}
		public void endDocument() {}
	}

	/**
	 * class representing global data about the current file.
	 */
	public static class MetaData
	{
		private char delimiter;
		private boolean quoted;
		private String[] columns;

		private MetaData(char delimiter, boolean quoted, String[] columns)
		{
			this.delimiter = delimiter;
			this.quoted = quoted;
			this.columns = columns;
		}

		/**
		 * returns the delimiter character...
		 */
		public char getDelimiter()
		{
			return delimiter;
		}

		/**
		 * returns whether the row entries are being unquoted.
		 */
		public boolean isQuoted()
		{
			return quoted;
		}

		/**
		 * returns an array of all the column names.
		 */
		public String[] getColumnNames()
		{
			return columns;
		}
	}

	/**
	 * class representing a single row from the current file.
	 */
	public static class Row
	{
		private MetaData metaData;
		private String[] values;
		private String rawRow;
		private int lineNumber;

		private Row(MetaData metaData, String[] values, String rawRow, int lineNumber)
		{
			this.metaData = metaData;
			this.values = values;
			this.rawRow = rawRow;
			this.lineNumber = lineNumber;
		}

		/**
		 * returns the meta data for the whole file.
		 */ 
		public MetaData getMetaData()
		{
			return metaData;
		}

		/**
		 * returns all of the row values.
		 */
		public String[] getValues()
		{
			return values;
		}

		/**
		 * returns a single value corresponding to column <code>columnName</code>.
		 * returns null if no column named <code>columnName</code> exists.
		 */
		public String getValue(String columnName)
		{
			if(columnName == null) return null;

			String[] columns = metaData.getColumnNames();
			for(int i = 0; i < columns.length; i++) {
				if(columnName.equals(columns[i])) {
					return values[i];
				}
			}

			return null;
		}

		/**
		 * returns a single value retrieved by column index (0-based).
		 * @throws java.lang.ArrayIndexOutOfBoundsException if index is not valid
		 */
		public String getValue(int index)
		{
			return values[index];
		}


		/**
		 * returns the raw input for this line.
		 */
		public String getRawRow()
		{
			return rawRow;
		}

		/**
		 * returns the line number for this row.
		 */
		public int getLineNumber()
		{
			return lineNumber;
		}

		@Override
		public String toString()
		{
			return "Row{" +
				"values=" + (values == null ? null : Arrays.asList(values)) +
				"}";
		}
	}

	/**
	 * reader class that is buffered and can be configured to break on
	 * any system's newline type.  this differs from {@link BufferedReader}, which
	 * breaks on any newline type, regardless of host system.
	 */
	private class SysDependentBufferedReader extends Reader
	{
		char[] buffer;
		int pushbackChar;
		int bufferIndex;
		int bufferSize;
		Reader reader;

		SysDependentBufferedReader(Reader reader) throws IOException
		{
			this(reader, 4096);
		}

		SysDependentBufferedReader(Reader reader, int sz) throws IOException
		{
			this.reader = reader;

			buffer = new char[sz];
			bufferIndex = 0;
			pushbackChar = -1;

			refillBuffer();
		}

		public String readLine() throws IOException
		{
			StringBuffer buf = new StringBuffer();

			int c;
			while((c = read()) != -1) {
				if(isNewline((char) c)) {
					while((c = read()) != -1 && isNewline((char) c)) ;
					pushback(c);
					break;
				}
				else {
					buf.append((char) c);
				}
			}

			// the last line may not have a newline attached to it, so return
			// the last string buffer iff there are characters in it...
			return (buf.length() > 0 ? buf.toString() : null);
		}

		@Override
		public int read(char[] buf, int offset, int length) throws IOException
		{
			int charsRead = 0;

			for(int i = offset; i < length; i++) {
				int readChar = read();
				if(readChar == -1) break;

				buf[i] = (char) readChar;
				charsRead++;
			}

			return charsRead;
		}

		@Override
		public int read() throws IOException
		{
			if(pushbackChar != -1) {
				int retVal = pushbackChar;
				pushbackChar = -1;
				return retVal;
			}

			if(bufferSize == 0) return -1;

			if(bufferIndex < bufferSize) {
				return buffer[bufferIndex++];
			}

			refillBuffer();
			return read();
		}

		public void pushback(int c)
		{
			pushbackChar = c;
		}

        @Override
        public void close() throws IOException
		{
			reader.close();
		}


		private void refillBuffer() throws IOException
		{
			bufferIndex = 0;
			bufferSize = reader.read(buffer);

			if(bufferSize == -1) {
				bufferSize = 0;
			}
		}

		private boolean isNewline(char c) throws IOException
		{
			if(mNewlineType == NEWLINE_WINDOWS) {
				if(c == '\r') {
					c = (char) read();
					if(c != '\n') {
						pushback(c);
						return false;
					}

					return true;
				}
			}
			if(mNewlineType == NEWLINE_ANY && (c == '\r' || c == '\n')) return true;
			if(mNewlineType == NEWLINE_MAC && c == '\r') return true;
			if(mNewlineType == NEWLINE_UNIX && c == '\n') return true;

			return false;
		}
	}



	private MetaData mMetaData;

	private char mDelimiter;
	private boolean mParseHeader;
	private boolean mQuoted;
	private int mNewlineType;
	private ArrayList mHandlers;


	/**
	 * creates a new instance with no registered handlers breaking lines on commas.
	 */
	public CSVParser()
	{
		this(',');
	}

	/**
	 * creates a new instance with no registered handlers breaking lines on <code>delimiter</code>.
	 */
	public CSVParser(char delimiter)
	{
		mDelimiter = delimiter;
		mParseHeader = true;
		mNewlineType = NEWLINE_ANY;
		mHandlers = new ArrayList();
	}

	/**
	 * returns the current delimiter. this is a comma by default.
	 */
	public char getDelimiter() { return mDelimiter; }

	/**
	 * returns whether to expect quoted values.
	 * by default, the parser does not expect quoted values.
	 */
	public boolean isQuoted() { return mQuoted; }

	/**
	 * returns whether to parse the first line as a header.
	 * by default, the parser will try to parse a header.
	 */
	public boolean isParseHeader() { return mParseHeader; }

	/**
	 * returns the newline type.  by default, any newline type will be used.
	 * @return
	 */
	public int getNewlineType() { return mNewlineType; }

	/**
	 * sets the delimiter.
	 */
	public void setDelimiter(char delimiter) { mDelimiter = delimiter; }

	/**
	 * sets whether the values are quoted.
	 */
	public void setQuoted(boolean quoted) { mQuoted = quoted; }

	/**
	 * returns whether parsing the first line as a header or as data.
	 */
	public void setParseHeader(boolean parseHeader) { mParseHeader = parseHeader; }

	/**
	 * sets the newline type.
	 */
	public void setNewlineType(int newlineType) { mNewlineType = newlineType; }

	/**
	 * adds a handler to the parser if not already present.
	 * @throws java.lang.IllegalStateException if trying to add a handler while parsing
	 */
	public void addHandler(Handler handler)
	{
		if(!mHandlers.contains(handler)) {
			mHandlers.add(handler);
		}
	}

	/**
	 * remotes a handler from the parser if present.
	 * @return boolean true iff handler was found and removed
	 * @throws java.lang.IllegalStateException if trying to remove a handler while parsing
	 */
	public boolean removeHandler(Handler handler)
	{
		return mHandlers.remove(handler);
	}

	/**
	 * returns an array of registered handlers.
	 */
	public Handler[] getAllHandlers()
	{
		return (Handler[]) mHandlers.toArray(new Handler[mHandlers.size()]);
	}

	/**
	 * parse the file located at <code>file</code>, notifying all registered
	 * handlers of parse events.
	 * @param file
	 * @throws java.io.IOException if an error occurs accessing <code>file</code>
	 */
	public void parse(String file) throws IOException
	{
		parse(new File(file));
	}

	/**
	 * parse the file represented by <code>file</code>, notifying all registered
	 * handlers of parse events.
	 * @param file
	 * @throws java.io.IOException if an error occurs accessing <code>file</code>
	 */
	public void parse(File file) throws IOException
	{
		parse(file, null);
	}

	/**
	 * Parse the file represented by <code>file</code> (encoded using 
	 * <code>charsetName</code> encoding) notifying all registered
	 * handlers of parse events.
	 * @param file
	 * @param charsetName
	 * @throws java.io.IOException if an error occurs accessing <code>file</code>
	 */
	public void parse(File file, String charsetName) throws IOException
	{
		SysDependentBufferedReader in = openReader(file, charsetName);

		try {
			String line;
			boolean firstRow = false;
			int lineNumber = 0;

			while((line = in.readLine()) != null) {
				if (Thread.currentThread().isInterrupted()) {
					throw new RuntimeException("Interruption during CSV parsing");
				}
				lineNumber++;

				// if the metadata is null, then this is the first
				// row in the file and has all the column defs.
				// otherwise, it's just another row...
				//
				// in either case,  notify all the registered handlers
				// of the current event...
				if(mMetaData == null) {
					if(isParseHeader()) {
						mMetaData = parseHeader(line);
					}
					else {
						mMetaData = new MetaData(getDelimiter(), isQuoted(), new String[0]);
					}

					Iterator it = mHandlers.iterator();
					while(it.hasNext()) {
						((Handler) it.next()).startDocument(mMetaData);
					}

					firstRow = true;
				}

				if(!isParseHeader() || !firstRow) {
					Row row = parseRow(line, lineNumber);
					Iterator it = mHandlers.iterator();
					while(it.hasNext()) {
						((Handler) it.next()).row(row);
					}
				}

				firstRow = false;
			}
		}
		finally {
			in.close();
		}

		// done!  notify handlers
		Iterator it = mHandlers.iterator();
		while(it.hasNext()) {
			((Handler) it.next()).endDocument();
		}
	}

	/**
	 * utility method to parse out column names and determine
	 * whether values are quoted.
	 */
	private MetaData parseHeader(String line)
	{
		String[] columnNames = parseLine(line);

		// if the quoted value hasn't explicitly been set to true,
		// try to guess whether quoted or not...if every column name
		// is surrounded with double quotes, then assume yes
		boolean quoted = isQuoted();
		if(!quoted && columnNames.length > 0) {
			quoted = true;
			for(int i = 0; i < columnNames.length && quoted; i++) {
				quoted = isStringQuoted(columnNames[i]);
			}
		}

		if(quoted) {
			columnNames = unquote(columnNames);
		}

		return new MetaData(mDelimiter, quoted, columnNames);
	}


	/**
	 * utility method to parse out row values.
	 * unquotes as necessary...
	 */
	private Row parseRow(String line, int lineNumber)
	{
		String[] values = parseLine(line);

		// if columns were specified, then rope in the row data to the correct size.
		// otherwise, allow free-form rows.
		if(values.length != mMetaData.columns.length && mMetaData.columns.length > 0) {
			String[] newValues = new String[mMetaData.columns.length];

			final int numToCopy = Math.min(values.length, mMetaData.columns.length);
			System.arraycopy(values, 0, newValues, 0, numToCopy);
			values = newValues;

			for(int i = numToCopy; i < values.length; i++) {
				values[i] = "";
			}
		}

		if(mMetaData.quoted) {
			values = unquote(values);
		}

		return new Row(mMetaData, values, line, lineNumber);
	}


	/**
	 * tokenizes the current row based on the delimiter and returns an array of columns.
	 * configures the tokenizer to return delimiter tokens also to get the correct
	 * number of columns when there are empty values. This function should probably 
	 * be re-written to not use a StringTokenizer and to parse character by 
	 * character to properly check things like escaped quotes and delimiters within 
	 * quotes. I don't have time to re-implement it, though, so I have instead 
	 * attempted to quick-fix the StringTokenizer implementation to handle all 
	 * edge-cases I can think of in this regard.
	 */
	private String[] parseLine(String line)
	{
		StringTokenizer tokenizer = new StringTokenizer(line, Character.toString(getDelimiter()), true);
		ArrayList tokens = new ArrayList(tokenizer.countTokens());

		String last = null, delimStr = Character.toString(getDelimiter());

		boolean inQuoted = false;
		String currTot = null;
		while(tokenizer.hasMoreTokens()) {
			String curr = tokenizer.nextToken();

			// check for empty column...
			if(delimStr.equals(curr)) {
				if (inQuoted) {
					if (currTot == null) {
						currTot = curr;
					}
					else {
						currTot += curr;
					}
				}
				else if(delimStr.equals(last)) {
					tokens.add("");
				}
			}
			else {
				// This logic here is a bit wacky to handle weird edge 
				// cases like ","
				if (!inQuoted && beginsQuoted(curr) && 
						(!endsQuoted(curr) || curr.length() == 1)) {
					inQuoted = true;
				}
				// For this check we need to send the whole string we have 
				// so far into endsQuoted() because it needs full context 
				// to decide whether it ends with a quote or not
				else if (inQuoted && 
						endsQuoted((currTot == null ? "" : currTot) + curr)) {
					inQuoted = false;
				}
				if (currTot == null) {
					currTot = curr;
				}
				else {
					currTot += curr;
				}
				if (!inQuoted) {
					tokens.add(currTot);
					currTot = null;
				}
			}

			last = curr;
		}

		if(delimStr.equals(last)) {
			tokens.add("");
		}

		return (String[]) tokens.toArray(new String[tokens.size()]);
	}

	/** Returns true iff the string begins with a proper quote. This includes 
	 * checking for escaped quotes which are two subsequent quotes. So basically 
	 * this function counts the number of subsequent quotes at the beginning of 
	 * the string and if its an odd number, it decides that it begins 
	 * quoted. */
	private boolean beginsQuoted(String str) {
		if (!str.startsWith("\"")) {
			return false;
		}
		int cnt = 1;
		while (cnt < str.length() && str.charAt(cnt) == '"') {
			cnt++;
		}
		if (cnt % 2 == 1) {
			return true;
		}
		else {
			return false;
		}
	}

	/** Returns true iff the string ends with a proper quote. This is very 
	 * similar to beginsQuoted(), except that it needs the full context of 
	 * the string you are checking and not just a chunk of it to really 
	 * decide if it ends quoted or not. */
	private boolean endsQuoted(String str) {
		if (!str.endsWith("\"")) {
			return false;
		}
		boolean beginsQuoted = beginsQuoted(str);
		int cnt = 1;
		// If the string begins quoted, ignore the first quote while 
		// doing this check or else it will mess up our quote 
		// counting
		while (cnt < (beginsQuoted ? str.length() - 1 : str.length()) && 
				str.charAt(str.length() - cnt - 1) == '"') {
			cnt++;
		}
		if (cnt % 2 == 1) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * utility to return whether string is quoted.
	 */
	private boolean isStringQuoted(String string)
	{
		return string.length() >= 2 &&
			string.charAt(0) == '"' &&
			string.charAt(string.length() - 1) == '"'
		;
	}


	/**
	 * utility method to unquote an array of values.
	 */
	private String[] unquote(String[] values)
	{
		String[] unquoted = new String[values.length];
		for(int i = 0; i < values.length; i++) {
			if(isStringQuoted(values[i])) {
            	unquoted[i] = values[i].substring(1, values[i].length() - 1);
				// Check for quoted quotes (double quotes) and unquote them
				unquoted[i] = StringUtils.replace(unquoted[i], "\"\"", "\"");
			}
			else {
				unquoted[i] = values[i];
			}
		}

		return unquoted;
	}

	/**
	 * helper method to open the reader.
	 * checks for the presence of a gzip file and wraps the input stream with a
	 * gzip stream if necessary.
	 * @throws IOException if error opening the stream
	 */
	protected SysDependentBufferedReader openReader(File file, String charsetName) throws IOException
	{
		InputStream stream = new FileInputStream(file);
		UnicodeReader streamReader = null;

		byte[] buf = new byte[2];
		if((stream.read(buf)) == buf.length) {
			// check for a standard gzip header...
			stream.close();
			if(buf[0] == 0x1f && buf[1] == -117) { //0x8b) {
				stream = new GZIPInputStream(new FileInputStream(file));
			}
			else {
				stream = new FileInputStream(file);
			}
		}

		streamReader = new UnicodeReader(stream, charsetName);
		return new SysDependentBufferedReader(streamReader);
	}


}

