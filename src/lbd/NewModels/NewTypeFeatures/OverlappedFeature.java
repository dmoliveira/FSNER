package lbd.NewModels.NewTypeFeatures;

import java.util.HashMap;

public class OverlappedFeature {
	
	protected HashMap<String, Object> featureTypeMap;
	
	private static int globalId;
	protected int id;
	
	protected String term;
	protected int numberFeaturesOverlapped;
	
	public OverlappedFeature(String term, String featureType) {
		
		id = ++globalId;
		this.term = term;
		numberFeaturesOverlapped = 1;
		
		featureTypeMap = new HashMap<String, Object>();
		featureTypeMap.put(featureType, null);
	}
	
	public boolean hasFeatureType(String featureType) {
		return(featureTypeMap.containsKey(featureType));
	}
	
	public void addOneToFeatureOverlapped(String featureType) {
		featureTypeMap.put(featureType, null);
		numberFeaturesOverlapped++;
	}
	
	public int getNumberFeaturesOverlapped() {
		return(numberFeaturesOverlapped);
	}
}
