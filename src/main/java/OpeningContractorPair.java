
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang3.mutable.MutableInt;



import util.BuildUtils;
import util.JRegex;

/**
 * @author Yanni Antonellis
 */

public class OpeningContractorPair {

	private static Logger log = Logger.getLogger(OpeningContractorPair.class.toString());
	
	private static String SplitRegrex =
		"\\s+|,|\\(|\\)|:|\\/|\\#|\\+|\\*|\\_|\\xa0|\\[|\\]|\\?|\\'|\\&|\\||\\|\\=|\\;|\\x00b0|\\x00c2|\\x20|\\x00c3|\\x00c2|\\x00ab|\\xc3|\\xc2|\\xab";

	private static Pattern splitPtn = Pattern.compile(SplitRegrex);
	private static JRegex characterReplace = new JRegex("[\\[a-z\\]\\[A-Z\\]]", "*");
	private static JRegex digitReplace = new JRegex("\\d", "#");
	
	
	public String openingTitle;
	public String contractorTitle;
	public String contractorBlurb;
	public boolean isPositive;

	
	public Map<String, ContractorRecommendationsFeature> features;
	


		
	public OpeningContractorPair(String openingTitle,
									String contractorTitle,
									String contractorBlurb,
									boolean isPositive
									) {
		
		this.openingTitle = openingTitle != null ? openingTitle : "";
		this.contractorTitle = contractorTitle != null ? contractorTitle : "";
		this.contractorBlurb = contractorBlurb != null ? contractorBlurb : "";
		this.isPositive = isPositive;
		
		
		// we want order-consistent iteration over the features and we use TreeMap
		this.features = new TreeMap<String, ContractorRecommendationsFeature>();
		
		this.computeLocalFeatures();
		
	}
	
	private void computeLocalFeatures() {
		computeOpeningTitleTermsFeatures();
		computeContractorTitleTermsFeatures();
		computeContractorBlurbTermsFeatures();
		computeDifferentTitleTermsFeature();
	}
	
	
	private void computeOpeningTitleTermsFeatures() {

		String[] openingTitleTokens = extractTokens(this.openingTitle);
		//log.info("computeOpeningTitleTermsFeatures: " + openingTitle  + "title tokens size " + openingTitleTokens.length + " " + openingTitleTokens[0]);
		for (String token: openingTitleTokens) {
			ContractorRecommendationsFeature tmpFeature =  
					new ContractorRecommendationsFeature("INOPENINGTITLE_" + token, token, false, true);
			addFeature(tmpFeature);
			//log.info("Adding feature " + "INTITLE_" + token);
		}
	}
	
	private void computeContractorTitleTermsFeatures() {

		String[] openingTitleTokens = extractTokens(this.contractorTitle);
		
		for (String token: openingTitleTokens) {
			ContractorRecommendationsFeature tmpFeature =  
					new ContractorRecommendationsFeature("INCONTRACTORTITLE_" + token, token, false, true);
			addFeature(tmpFeature);
			//log.info("Adding feature " + "INCONTRACTORTITLE_" + token);
		}
	}
	
	private void computeContractorBlurbTermsFeatures() {

		String[] openingTitleTokens = extractTokens(this.contractorBlurb);
		
		for (String token: openingTitleTokens) {
			ContractorRecommendationsFeature tmpFeature =  
					new ContractorRecommendationsFeature("INCONTRACTORBLURB_" + token, token, false, true);
			addFeature(tmpFeature);
			//log.info("Adding feature " + "INCONTRACTORBLURB_" + token);
		}
	}
	
	
	private void computeDifferentTitleTermsFeature() {

		ArrayList<String> tokens = differentTokens(this.openingTitle, this.contractorTitle);
		if (tokens.size() > 0) {
			for (String token: tokens) {
				ContractorRecommendationsFeature tmpFeature =  
					new ContractorRecommendationsFeature("DIFFERENTTERMINTITLES_" + token, token, false, true);
				addFeature(tmpFeature);
				//log.info("Adding feature " + "DIFFERENTTERMINTITLES_" + token);
			}
		}
	}

	
	public ArrayList<String> differentTokens(String name1, String name2) {
		String [] tokens1 = extractTokens(name1);
		String [] tokens2 = extractTokens(name2);
		
		ArrayList<String> returnValue = new ArrayList<String>();
		
		Map <String, MutableInt> commonCount = new HashMap<String, MutableInt>();
		
		
		for (String token: tokens1) {
			if (!commonCount.containsKey(token)) {
				commonCount.put(token, new MutableInt(new Integer("1")));
			}
		}

		for (String token: tokens2) {
			if (!commonCount.containsKey(token)) {
				commonCount.put(token, new MutableInt(new Integer("1")));
			}
			else {
				BuildUtils.getOrDefault(commonCount, token, MutableInt.class).increment();
			}
		}
			
		if (commonCount.size() > 0) {
			//log.info(commonCount.size() + "");
			for (Entry<String, MutableInt> entry : commonCount.entrySet()) {
				String key = entry.getKey();
				if (!"".equals(key) && key != null && commonCount.get(key) != null && commonCount.get(key).intValue() == 1) {
					returnValue.add(key);
				}
			}
		}
		return (returnValue);
	}
	
	public double computeJaccard(String name1, String name2) {
		String [] tokens1 = extractTokens(name1);
		String [] tokens2 = extractTokens(name2);
		
		Map <String, MutableInt> commonCount = new HashMap<String, MutableInt>();
		
		
		for (String token: tokens1) {
			if (!commonCount.containsKey(token)) {
				commonCount.put(token, new MutableInt(new Integer("1")));
			}
		}

		for (String token: tokens2) {
			if (!commonCount.containsKey(token)) {
				commonCount.put(token, new MutableInt(new Integer("1")));
			}
			else {
				BuildUtils.getOrDefault(commonCount, token, MutableInt.class).increment();
			}
		}
		
		int union = commonCount.size();
		int intersection = 0;
		
		for (Entry<String, MutableInt> entry : commonCount.entrySet()) {
			if (commonCount.get(entry.getKey()).intValue() > 1) {
				intersection++;
			}
		}
		
		
		return (intersection + 0.0)/(union + 0.0);
	}
	
	
	
	public double computeWeightedJaccard(String name1, String name2, Map<String, Double> idfGlobal) {
		String [] tokens1 = extractTokens(name1);
		String [] tokens2 = extractTokens(name2);
		
		Map <String, MutableInt> commonCount = new HashMap<String, MutableInt>();
		
		
		for (String token: tokens1) {
			if (!commonCount.containsKey(token)) {
				commonCount.put(token, new MutableInt(new Integer("1")));
			}
		}

		for (String token: tokens2) {
			if (!commonCount.containsKey(token)) {
				commonCount.put(token, new MutableInt(new Integer("1")));
			}
			else {
				BuildUtils.getOrDefault(commonCount, token, MutableInt.class).increment();
			}
		}
		
		double union = 0.0;
		double intersection = 0.0;
		
		for (Entry<String, MutableInt> entry : commonCount.entrySet()) {
			String token = entry.getKey();
			if (commonCount.get(token).intValue() > 1) {
				if (idfGlobal.containsKey(token)) {
					intersection += idfGlobal.get(token);
					union += idfGlobal.get(token);
				}
				else {
					intersection += 1.0;
					union += 1.0;
				}
			}
			else {
				if (idfGlobal.containsKey(token)) {
					union += idfGlobal.get(token);
				}
				else {
					union += 1.0;
				}
			}
		}
		
		
		return (intersection)/(union);
	}
	
	
	
	public static String[] extractTokens(String someString) {
		String[] tmpTokens = splitPtn.split(someString);
		//log.info("extractTokens:  size " + tmpTokens.length + " " + someString + " " + tmpTokens[0]);
		
		String [] clearTokens = new String[tmpTokens.length];
		int j = 0;
		for (String tmpToken : tmpTokens) {
			if (!"".equals(tmpToken)) {
				clearTokens[j] = tmpToken;
				j++;
			}
		}
		
		return clearTokens;
	}
	
	public void addFeature(ContractorRecommendationsFeature feature) {
		
		if (!features.containsKey(feature.name)) {
			features.put(feature.name, feature);
		}
	}
	
	public static void addExtendedFeature(OpeningContractorPair p, ContractorRecommendationsFeature f) {
		p.addFeature(f);
		double fVal = Double.valueOf(f.value);
		String f2 = f.name + "_LOG";
		String v2 = Math.log(fVal + 1) + "";
		p.addFeature(new ContractorRecommendationsFeature(f2, v2, f.isGlobal, f.isCategorical));

		String f3 = f.name+ "_SQUARE";
		String v3 = Math.pow(fVal, 2.0) + "";
		p.addFeature(new ContractorRecommendationsFeature(f3, v3, f.isGlobal, f.isCategorical));
	}
	
	
	public boolean isPositive() {
		if (this.isPositive)
			return true;
		else
			return false;
	}

	public boolean isNegative() {
		if (this.isPositive)
			return false;
		else
			return true;
	}

}
