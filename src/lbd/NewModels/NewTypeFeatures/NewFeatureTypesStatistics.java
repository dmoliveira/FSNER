package lbd.NewModels.NewTypeFeatures;

import java.text.DecimalFormat;

import iitb.CRF.DataSequence;
import lbd.CRF.LabelMap.BILOU;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes.FeatureType;

public class NewFeatureTypesStatistics{
	
	protected int numberTermsOutOfVocabulary;
	protected int numberTermsInVocabulary;
	protected int numberEntityTermsOutOfVocabulary;
	protected int numberEntityTermsInVocabulary;
	
	public NewFeatureTypesStatistics() {
		
		numberTermsOutOfVocabulary = 0;
		numberTermsInVocabulary = 0;
		numberEntityTermsOutOfVocabulary = 0;
		numberEntityTermsInVocabulary = 0;
	}
	
	public void addToStatistics(DataSequence sequence, String term, int pos, boolean reachedCriteria, FeatureType featureType) {
		
		if(reachedCriteria) { 
			addToTermInVocabulary(sequence, term, pos, featureType);
		} else {  
			addToTermOutOfVocabulary(sequence, pos);
		}
	}
	
	private void addToTermOutOfVocabulary(DataSequence sequence, int pos) {
		
		numberTermsOutOfVocabulary++;
		
		if(sequence.y(pos) != BILOU.Outside.ordinal())
			numberEntityTermsOutOfVocabulary++;
	}
	
	private void addToTermInVocabulary(DataSequence sequence, String term, int pos, FeatureType featureType) {
		
		numberTermsInVocabulary++;
		addTermFeatureOverlapped(term, featureType);
		
		if(sequence.y(pos) != BILOU.Outside.ordinal()) {
			numberEntityTermsInVocabulary++;
			addEntityTermFeatureOverlapped(term, featureType);
		}
	}
	
	protected void addTermFeatureOverlapped(String term, FeatureType featureType) {}
	
	protected void addEntityTermFeatureOverlapped(String term, FeatureType featureType) {}
	
	public void printFeatureStatistics(FeatureType featureType) {
		
		System.out.println("-- " + featureType.name() + " (Statistics)");
		
		printOverviewStatitics();
	}
	
	protected void printOverviewStatitics() {
		
		double outOfVocabularyPercent = ((double)numberTermsOutOfVocabulary)/(numberTermsOutOfVocabulary + numberTermsInVocabulary);
    	double inVocabularyPercent = ((double)numberTermsInVocabulary)/(numberTermsOutOfVocabulary + numberTermsInVocabulary);
    	
    	System.out.print("\tTerms OOV: " + numberTermsOutOfVocabulary + " (" + 
    			(new DecimalFormat("#.##")).format(100*outOfVocabularyPercent) + "%)");
    	System.out.println(" Terms InVocabulary: " + numberTermsInVocabulary + " (" +
    			(new DecimalFormat("#.##")).format(100*inVocabularyPercent) +"%)");
    	
    	double outOfVocabularyEntityPercent = ((double)numberEntityTermsOutOfVocabulary);
    	outOfVocabularyEntityPercent /= (numberEntityTermsOutOfVocabulary + numberEntityTermsInVocabulary);
    	
    	double inVocabularyEntityPercent = ((double)numberEntityTermsInVocabulary);
    	inVocabularyEntityPercent /= (numberEntityTermsOutOfVocabulary + numberEntityTermsInVocabulary);
    	
    	System.out.print("\tEntity Terms OOV: " + numberEntityTermsOutOfVocabulary + 
    			" (" + (new DecimalFormat("#.##")).format(100*outOfVocabularyEntityPercent) + "%)");
    	
    	System.out.println(" Entity Terms InVocabulary: " + numberEntityTermsInVocabulary + " (" +
    			(new DecimalFormat("#.##")).format(100*inVocabularyEntityPercent) +"%)");
	}
}
