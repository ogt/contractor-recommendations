package util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.Reader;

import java.util.logging.Logger;


/**
 * This class creates a Unicode Reader that uses a BOM (byte-order mark)
 * to determine what encoding to use. If no BOM is found, then the specified
 * default encoding is used. The system default encoding is used if no BOM
 * is found, and no default encoding is specified.
 *
 * The Java InputStreamReader only handles a BOM in UTF-16 files, not UTF-8.
 *
 * BOMs:
 *	00 00 FE FF = UTF-32, big-endian
 *	FF FE 00 00 = UTF-32, little-endian
 *	FE FF       = UTF-16, big-endian
 *	FF FE       = UTF-16, little-endian
 *	EF BB BF    = UTF-8
 *
 * Win2k Notepad:
 *	Unicode format = UTF-16LE
 *
 * Usage:
 * 	String dfltEncoding = "ISO-8859-1"; // or null to use system default
 * 	FileInputStream fis = new FileInputStream(file);
 * 	Reader in = new UnicodeReader(fis, dfltEncoding);
 *
 * See http://www.unicode.org/unicode/faq/utf_bom.html
 * Original pseudocode: Thomas Weidenfeller
 *
 */
public class UnicodeReader extends Reader {

	private static final Logger log = Logger.getLogger(UnicodeReader.class.toString());

	private static final int BOM_SIZE = 4;

	private PushbackInputStream internalPbis;
	private InputStreamReader internalIsr;
	private String dfltEncoding;


	public UnicodeReader(InputStream in, String dfltEncoding)
			throws IOException {
		this.internalPbis = new PushbackInputStream(in, BOM_SIZE);
		this.dfltEncoding = dfltEncoding;
		init();
	}
	
	public UnicodeReader(InputStream in) throws IOException {
		this(in, null);
	}

	public String getDfltEncoding() {
		return this.dfltEncoding;
	}

	public String getEncoding() {
		return (internalIsr == null) ? null
			: internalIsr.getEncoding();
	}

	/**
	 * Read-ahead four bytes and check for BOM marks. Extra bytes are
	 * unread back to the stream, only BOM bytes are skipped.
	 */
	protected void init() throws IOException {
		if (internalIsr != null) {
			return;
		}

		String encoding;
		byte bom[] = new byte[BOM_SIZE];
		int unread;
		int bytesRead;

		bytesRead = internalPbis.read(bom, 0, bom.length);

		if ((bom[0] == (byte) 0xEF) 
			  && (bom[1] == (byte) 0xBB) 
			  && (bom[2] == (byte) 0xBF)) {
			encoding = "UTF-8";
			unread = bytesRead - 3;
		} else if ((bom[0] == (byte) 0xFE) 
			  && (bom[1] == (byte) 0xFF)) {
			encoding = "UTF-16BE";
			unread = bytesRead - 2;
		} else if ((bom[0] == (byte) 0xFF) 
			  && (bom[1] == (byte) 0xFE)) {
			encoding = "UTF-16LE";
			unread = bytesRead - 2;
		} else if ((bom[0] == (byte) 0x00) 
			  && (bom[1] == (byte) 0x00) 
			  && (bom[2] == (byte) 0xFE) 
			  && (bom[3] == (byte) 0xFF)) {
			encoding = "UTF-32BE";
			unread = bytesRead - 4;
		} else if ((bom[0] == (byte) 0xFF) 
			  && (bom[1] == (byte) 0xFE) 
			  && (bom[2] == (byte) 0x00) 
			  && (bom[3] == (byte) 0x00)) {
			encoding = "UTF-32LE";
			unread = bytesRead - 4;
		} else {
			// Unicode BOM mark not found, unread all bytes
			encoding = this.dfltEncoding;
			unread = bytesRead;
		}

		log.info("UnicodeReader: encoding = " + encoding);
			
		//System.out.println("read=" + n + ", unread=" + unread);

		// Pushback the unread bytes.
		if (unread > 0) {
			internalPbis.unread(bom, (bytesRead - unread), unread);
		}

		// Use the given encoding.
		if (encoding == null) {
			internalIsr = new InputStreamReader(internalPbis);
		} else {
			internalIsr = new InputStreamReader(internalPbis, encoding);
		}
	}

	/**
	 * Close this UnicodeReader.
	 */
	public void close() throws IOException {
		this.internalIsr.close();
	}

	/**
	 * Reads up to <code>len</code> bytes from the Reader.
	 */
	public int read(char[] cbuf, int offset, int len)
			throws IOException {
		return this.internalIsr.read(cbuf, offset, len);
	}
}
