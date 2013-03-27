import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableInt;



import util.BuildUtils;
import util.CSVParser.MetaData;
import util.CSVParser.Row;
import util.Fmt;
import util.TaskUtils;
import util.CSVParser;



/**
 * @author Yanni Antonellis antonellis@odesk.com
 */
public class ContractorRecommendationsTraining{

	private static Logger log = Logger.getLogger(ContractorRecommendationsTraining.class.toString());

	public static enum ColName {
		OPENING_TITLE, CONTRACTOR_TITLE, CONTRACTOR_BLURB
//		OPENING_TITLE, OPENING_DESC, OPENING_EST_DURATION, OPENING_SKILLS, OPENING_TYPE,
//		CONTRACTOR_NAME, CONTRACTOR_TITLE, CONTRACTOR_BLURB, CONTRACTOR_SKILLS, CONTRACTOR_RATE, CONTRACTOR_HOURS_BILLED, CONTRACTOR_COUNTRY, CONTRACTOR_LAST_ACTIVE, CONTRACTOR_NO_TESTS, CONTRACTOR_FEEDBACK, CONTRACTOR_HAS_IMAGE,
	};

	protected Map<String, String> params;
	protected String inputPath;
	protected int maxPositiveExamples;
	protected int maxNegativeExamples;
	protected String outputPath;
	protected String tmpPath;
	protected String dictionaryFile;

	protected boolean outputModel;
	protected boolean loadIdf;
	
	
	protected List<OpeningContractorPair> positiveTrainingExamples;
	protected List<OpeningContractorPair> negativeTrainingExamples;

	
	
	public ContractorRecommendationsTraining(String xml) {
		
		
		this.positiveTrainingExamples = new ArrayList<OpeningContractorPair>();
		this.negativeTrainingExamples = new ArrayList<OpeningContractorPair>();
		
		
		params = TaskUtils.parseXmlParameter(xml, "ContractorRecommendationsTraining");
		Map boundParams = TaskUtils.bindBeanWithParameters(this, params);
		log.info("bound params " + boundParams.toString());
		
	}

	public static void main(String[] argv) {
		String xml = "<ContractorRecommendationsTraining>"
				+ "<inputPath>data/test/</inputPath>"
				+ "<tmpPath>/tmp/</tmpPath>"
				+ "<outputPath>data/test/output/</outputPath>"
				+ "<maxPositiveExamples>-1</maxPositiveExamples>"
				+ "<maxNegativeExamples>-1</maxNegativeExamples>"
				+ "</ContractorRecommendationsTraining>";

		ContractorRecommendationsTraining contractorRecommendationsTrainer = new ContractorRecommendationsTraining(xml);

		contractorRecommendationsTrainer.doTask();

		
		
		System.exit(0);
	}

	protected void doTask() {
		long t0 = System.currentTimeMillis();
		log.info("Training...");

		try {
			iterateTrainingExamples(inputPath, maxPositiveExamples, maxNegativeExamples, tmpPath, outputPath);
			
			printTrainingExamples();
			printSvmTrainingExamples(false);

			log.info(TaskUtils.getMemoryStatus());
			log.info("Cleaning up memory");
			log.info("Finished scaling training data...");

		}
		catch (Exception e) {
			log.log(Level.SEVERE, Fmt.S(e));
			throw new RuntimeException(e);
		}

	}


	public void iterateTrainingExamples(String path, int maxPositiveExamples, int maxNegativeExamples, String tmpPath, String outputPath) throws IOException{
		log.info("Iterating over " + (maxPositiveExamples > 0 ? ("at most " + maxPositiveExamples) : "all") + " available positive training examples "
									+ (maxNegativeExamples > 0 ? ("at most " + maxNegativeExamples) : "all") + " available negative training examples.");

		
		iteratePositiveTrainingExamplesFromFile(path + "positive.csv", maxPositiveExamples, tmpPath, outputPath);
		iterateNegativeTrainingExamplesFromFile(path + "negative.csv", maxNegativeExamples, tmpPath, outputPath);
		
		
		log.info("Done iterating over training examples.");
	}

	private void iteratePositiveTrainingExamplesFromFile(String filePath, int maxExamples, String tmpPath, String outputPath) throws IOException {
		log.info("Reading positive training examples from file " + filePath + " ...");

		CSVParser parser = new CSVParser();
		parser.setParseHeader(true);
		parser.setQuoted(true);
		parser.setDelimiter(',');

		PositiveCsvHandler handler = new PositiveCsvHandler();
		handler.setMaxExamples(maxExamples);

		parser.addHandler(handler);

		parser.parse(new File(filePath));
		log.info("Done reading positive training examples.");
	}
	
	private void iterateNegativeTrainingExamplesFromFile(String filePath, int maxExamples, String tmpPath, String outputPath) throws IOException {
		log.info("Reading positive training examples from file " + filePath + " ...");

		CSVParser parser = new CSVParser();
		parser.setParseHeader(true);
		parser.setQuoted(true);
		parser.setDelimiter(',');

		NegativeCsvHandler handler = new NegativeCsvHandler();
		handler.setMaxExamples(maxExamples);

		parser.addHandler(handler);

		parser.parse(new File(filePath));
		log.info("Done reading positive training examples.");
	}
	
	

	public void printTrainingExamples() {
		this.printPositiveExamples();
		this.printNegativeExamples();
	}

	public void printPositiveExamples() {
		BufferedWriter out = null;
		
		log.info("ready to print " + this.positiveTrainingExamples.size() + "positive examples");
		try {
			FileWriter fstream = new FileWriter(this.outputPath + "positive");
			out = new BufferedWriter(fstream);

			for (int k = 0; k < this.positiveTrainingExamples.size(); k++) {
				out.write(this.positiveTrainingExamples.get(k).openingTitle + " " + this.positiveTrainingExamples.get(k).contractorTitle + "    ");
				
				for (Entry<String, ContractorRecommendationsFeature> entry : this.positiveTrainingExamples.get(k).features.entrySet()) {
					out.write(entry.getKey() + " " + entry.getValue().value + " ");
				}

				out.write("\n");
			}
		}
		catch (IOException e) {
			log.log(Level.SEVERE, "Exception when printing positive training examples: " + Fmt.S(e));
			throw new RuntimeException(e);
		}
		finally {
			IOUtils.closeQuietly(out);
		}
	}
	

	public void printNegativeExamples() {
		BufferedWriter out = null;
		
		log.info("ready to print " + this.negativeTrainingExamples.size() + "negative examples");
		try {
			FileWriter fstream = new FileWriter(this.outputPath + "negative");
			out = new BufferedWriter(fstream);

			for (int k = 0; k < this.negativeTrainingExamples.size(); k++) {
				out.write(this.negativeTrainingExamples.get(k).openingTitle + " " + this.negativeTrainingExamples.get(k).contractorTitle + "    ");


				for (Entry<String, ContractorRecommendationsFeature> entry : this.negativeTrainingExamples.get(k).features.entrySet()) {
					out.write(entry.getKey() + " " + entry.getValue().value + " ");
				}
				out.write("\n");
			}
		}
		catch (IOException e) {
			log.log(Level.SEVERE, "Exception when printing negative training examples: " + Fmt.S(e));
			throw new RuntimeException(e);
		}
		finally {
			IOUtils.closeQuietly(out);
		}
		
	}

	
	
	/**
	 * Convert training data to liblinear's sparse representation
	 */

	public void printSvmTrainingExamples(boolean sampleImbalanced) {
		List<OpeningContractorPair> trainingExamples = new ArrayList<OpeningContractorPair>(positiveTrainingExamples);
		if (sampleImbalanced) {
			trainingExamples.addAll(reservoirSample(negativeTrainingExamples, positiveTrainingExamples.size()));
		}
		else {
			trainingExamples.addAll(negativeTrainingExamples);
		}

		// at this point I no longer need the positive and training negative examples.
		positiveTrainingExamples.clear();
		negativeTrainingExamples.clear();

		// this is: String -> <featureValue, position>
		Map<String, Map<String, Integer>> features = new HashMap<String, Map<String, Integer>>();

		// this is: String -> currentPosition
		Map<String, MutableInt> count = new HashMap<String, MutableInt>();

		// this is: String -> currentPosition
		Map<String, Integer> featureIds = new HashMap<String, Integer>();
		Map<Integer, String> featureIdsInverse = new TreeMap<Integer, String>();

		// loop through all training examples to buffer values of categorical features
		for (int k = 0; k < trainingExamples.size(); k++) {
			for (Entry<String, ContractorRecommendationsFeature> entry : trainingExamples.get(k).features.entrySet()) {
				
				if (!entry.getValue().isCategorical || entry.getValue().value.equals("")) {
					continue;
				}

				String featureNameTemp = entry.getKey();

				if (k == 0 || !features.containsKey(featureNameTemp)) {
					log.info("Initializing hash for feature " + featureNameTemp + "a");
					features.put(featureNameTemp, new HashMap<String, Integer>());
				}

				log.info("a" + featureNameTemp + "a" + entry.getValue().value +" " +features.get(featureNameTemp) +"a");
				if (features.get(featureNameTemp).containsKey(entry.getValue().value)) {
					continue;
				}

				MutableInt featureValuePos = BuildUtils.getOrDefault(count, featureNameTemp, MutableInt.class);

				features.get(featureNameTemp).put(entry.getValue().value, featureValuePos.intValue());
				featureValuePos.increment();
			}
		}

		int prev = 0;
		int id = 1;
		for (Entry<String, MutableInt> entry : count.entrySet()) {
			featureIds.put(entry.getKey(), id);
			featureIdsInverse.put(id, entry.getKey());
			prev = entry.getValue().intValue();
			log.info("COUNTS          " + entry.getKey() + "=" + entry.getValue());
			log.info("IDS          " + entry.getKey() + "=" + id);
			id += prev;
			
		}
		
		BufferedWriter out = null;
		try {
			FileWriter fstream = new FileWriter(this.outputPath + "svn_training");
			out = new BufferedWriter(fstream);
			
			
			for (int k = 0; k < trainingExamples.size(); k++) {

				if (trainingExamples.get(k).isPositive)
					out.write("1 ");
				else
					out.write("-1 ");

				
				/*
				for (Entry<String, ContractorRecommendationsFeature> entry : trainingExamples.get(k).features.entrySet()) {
					if (entry.getValue().isCategorical && !entry.getValue().value.equals("")) {
						int tmpCount = featureIds.get(entry.getKey()).intValue();
						out.write(tmpCount + ":1 ");
					}
					else {
						if (!entry.getValue().value.equals("")) {
							int tmpCount = featureIds.get(entry.getKey()).intValue();
							out.write(tmpCount + ":" + entry.getValue().value + " ");
							
						}
					}
				}*/
				
				
				
				for (Entry<Integer, String> e : featureIdsInverse.entrySet()) {
					String featureName = e.getValue();
					Integer featureId = e.getKey();
					
					ContractorRecommendationsFeature feature = trainingExamples.get(k).features.get(featureName);
					if (feature != null) {
						if (feature.isCategorical && !feature.value.equals("")) {
							out.write(featureId + ":1 ");
						}
						else {
							if (!feature.value.equals("")) {
								out.write(featureId + ":" + feature.value + " ");
								
							}
						}	
					}
					
				}
				
				out.write("\n");
			}

		}
		catch (IOException e) {
			log.log(Level.SEVERE, "Exception when svn formatted training examples: " + Fmt.S(e));
			throw new RuntimeException(e);
		}
		finally {
			IOUtils.closeQuietly(out);
		}

		//now serialize the features to be used when generating the testing data
		ObjectOutputStream out2 = null;
		try {
			FileOutputStream fstream = new FileOutputStream(this.outputPath + "features");
			out2 = new ObjectOutputStream(fstream);

			out2.writeObject(features);

		}
		catch (IOException e) {
			log.log(Level.SEVERE, "Exception when svn formatted training examples: " + Fmt.S(e));
			throw new RuntimeException(e);
		}
		finally {
			IOUtils.closeQuietly(out);
		}
		
		
	}
	
	
	/*
	 * Reservoir sampling
	 */
	public static <T> List<T> reservoirSample(Iterable<T> items, int m) {
		Random rnd = new Random(System.currentTimeMillis());
		ArrayList<T> res = new ArrayList<T>(m);
		int count = 0;
		for (T item : items) {
			count++;
			if (count <= m) {
				res.add(item);
			}
			else {
				int r = rnd.nextInt(count);
				
				if (r < m)
					res.set(r, item);

			}
		}
		return res;

	}
	
	
	public void processPositiveExample(OpeningContractorPair pair) {
		log.info("processing a positive example");
		this.positiveTrainingExamples.add(pair);
		
	}
	
	public void processNegativeExample(OpeningContractorPair pair) {
		log.info("processing a positive example");
		this.negativeTrainingExamples.add(pair);
		
	}
	

	
	private class NegativeCsvHandler implements CSVParser.Handler {
		private Map<String, Integer> columnIdx = new HashMap<String, Integer>();
		private int maxExamples = 0;
		private int ctr = 0;

		public void parseColumnNames(MetaData md) {
			for (int idx = 0; idx < md.getColumnNames().length; ++idx) {
				columnIdx.put(canonicalColName(md.getColumnNames()[idx]), idx);
			}
		}

		private String canonicalColName(String colName) {
			return colName.trim().toLowerCase();
		}

		private String canonicalColName(ColName colName) {
			return canonicalColName(colName.toString());
		}

		private String getField(ColName colName, Row row) {
			String canonicalColName = canonicalColName(colName);
			return columnIdx.containsKey(canonicalColName) ? row
					.getValue(columnIdx.get(canonicalColName)) : "";
		}		

		@Override
		public void endDocument() {
		}

		@Override
		public void row(Row row) {
			if (maxExamples > 0 && ctr > maxExamples) {
				return;
			}
			++ctr;
			
			OpeningContractorPair pair = new OpeningContractorPair(
						getField(ColName.OPENING_TITLE, row),
						getField(ColName.CONTRACTOR_TITLE, row),
						getField(ColName.CONTRACTOR_BLURB, row),
						false
						);

			processNegativeExample(pair);
		}

		public void setMaxExamples(int maxExamples) {
			this.maxExamples = maxExamples;
		}

		@Override
		public void startDocument(MetaData metaData) {
			parseColumnNames(metaData);
			
		}


	}

	
	
	private class PositiveCsvHandler implements CSVParser.Handler {
		private Map<String, Integer> columnIdx = new HashMap<String, Integer>();
		private int maxExamples = 0;
		private int ctr = 0;

		public void parseColumnNames(MetaData md) {
			for (int idx = 0; idx < md.getColumnNames().length; ++idx) {
				columnIdx.put(canonicalColName(md.getColumnNames()[idx]), idx);
			}
		}

		private String canonicalColName(String colName) {
			return colName.trim().toLowerCase();
		}

		private String canonicalColName(ColName colName) {
			return canonicalColName(colName.toString());
		}

		private String getField(ColName colName, Row row) {
			String canonicalColName = canonicalColName(colName);
			return columnIdx.containsKey(canonicalColName) ? row
					.getValue(columnIdx.get(canonicalColName)) : "";
		}		

		@Override
		public void endDocument() {
		}

		@Override
		public void row(Row row) {
			if (maxExamples > 0 && ctr > maxExamples) {
				return;
			}
			++ctr;
			
			OpeningContractorPair pair = new OpeningContractorPair(
						getField(ColName.OPENING_TITLE, row),
						getField(ColName.CONTRACTOR_TITLE, row),
						getField(ColName.CONTRACTOR_BLURB, row),
						true
						);

			processPositiveExample(pair);
		}

		public void setMaxExamples(int maxExamples) {
			this.maxExamples = maxExamples;
		}

		@Override
		public void startDocument(MetaData metaData) {
			parseColumnNames(metaData);
			
		}


	}



	public void setOutputModel(boolean outputModel) {
		this.outputModel = outputModel;
	}

	public void setLoadIdf(boolean loadIdf) {
		this.loadIdf = loadIdf;
	}



	public void setInputPath(String inputPath) {
		this.inputPath = inputPath;
	}

	public void setDictionaryFile(String dictionaryFile) {
		this.dictionaryFile = dictionaryFile;
	}
	
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public void setTmpPath(String tmpPath) {
		this.tmpPath = tmpPath;
	}

}
