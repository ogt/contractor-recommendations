
/**
 * @author Yanni Antonellis
 */
public class ContractorRecommendationsFeature {
	public String name;
	public String value;
	public boolean isGlobal;
	public boolean isCategorical;
	
	public ContractorRecommendationsFeature (String name, String value, boolean isGlobal, boolean isCategorical) {
		this.name = name;
		this.value = value != null ? value : "";
		this.isGlobal = isGlobal;
		this.isCategorical = isCategorical;
	}

}

