package lbd.NewModels.NewTypeFeatures;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.NewModels.NewTypeFeatures.NewFeatureTypes.FeatureType;

public class NewFeatureTypesGlobalStatistics extends NewFeatureTypesStatistics{
	
	protected enum OverlappedType {Term, EntityTerm}; 
	
	protected ArrayList<HashMap<String, OverlappedFeature>> overlappedFeatureList;

	public NewFeatureTypesGlobalStatistics() {
		
		super();
		
		overlappedFeatureList = new ArrayList<HashMap<String, OverlappedFeature>>();
		for(int i = 0; i < OverlappedType.values().length; i++) { 
			overlappedFeatureList.add(new HashMap<String, OverlappedFeature>());
		}
	}
	
	protected void addTermFeatureOverlapped(String term, FeatureType featureType) {
		
		addToFeatureOverlapped(term, overlappedFeatureList.get(OverlappedType.Term.ordinal()), featureType);
	}
	
	protected void addEntityTermFeatureOverlapped(String term, FeatureType featureType) {
		
		addToFeatureOverlapped(term, overlappedFeatureList.get(OverlappedType.EntityTerm.ordinal()), featureType);
	}
	
	protected void addToFeatureOverlapped(String term, HashMap<String, OverlappedFeature> overlappedMap, FeatureType featureType) {
		
		if(overlappedMap.containsKey(term) && !overlappedMap.get(term).hasFeatureType(featureType.name()))
			overlappedMap.get(term).addOneToFeatureOverlapped(featureType.name());
		else
			overlappedMap.put(term, new OverlappedFeature(term, featureType.name()));
	}
	
	public void printFeaturesStatistics() {
		
		System.out.println("-- Overview (Statistics)");
		
		printOverviewStatitics();
		
		printOverlappedFeatureStatistics();
	}
	
	protected void printOverlappedFeatureStatistics() {
		
		final int OVERLAP_THRESHOLD = 2;
		
		for(int i = 0; i < OverlappedType.values().length; i++) {
    		
	    	int numberTermFeaturesOverlapped = 0;
	    	int numberTermFeaturesNonOverlapped = 0;
	    	
	    	OverlappedFeature overlappedFeature;
	    	
	    	Iterator<Entry<String, OverlappedFeature>> ite = overlappedFeatureList.get(i).entrySet().iterator();
	    	
	    	while(ite.hasNext()) {
	    		
	    		overlappedFeature = ite.next().getValue();
	    		
	    		if(overlappedFeature.getNumberFeaturesOverlapped() >= OVERLAP_THRESHOLD) {
	    			numberTermFeaturesOverlapped++;
	    		} else if(overlappedFeature.getNumberFeaturesOverlapped() == 1) { 
	    			numberTermFeaturesNonOverlapped++;
	    		}
	    	}
	    	
	    	double numberOverlappedFeature = numberTermFeaturesOverlapped;
	    	numberOverlappedFeature /= numberTermFeaturesOverlapped + numberTermFeaturesNonOverlapped;
	    	
	    	System.out.println("\t" + ((i == 0)?"":"Entity ") + "Terms Overlapped Feature: " + 
	    			numberTermFeaturesOverlapped + " (" + (new DecimalFormat("#.##")).format(100*numberOverlappedFeature) + "%)");
    	}
		
	}

}
