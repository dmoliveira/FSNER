package lbd.Report;

public class StatisticalEntry {

	private String id;
	private double truePositive;
	private double falsePositive;
	private double falseNegative;
	private double trueNegative;
	private double specificity;
	private double accuracy;
	private double alpha;
	private double beta;
	private double likelihoodRatioPositive;
	private double likelihoodRatioNegative;
	private double precision;
	private double recall;
	private double fMeasure;
	
	public StatisticalEntry(String id) {
		this.id = id;
	}
	
	public void calculateStatisticalMeasures(double truePositive, double falsePositive,
			double falseNegative, double trueNegative) {
		
		setTruePositive(truePositive);
		setFalsePositive(falsePositive);
		setFalseNegative(falseNegative);
		setTrueNegative(trueNegative);
		
		calculateStatisticalMeasures();
	}
	
	public void addTruePositive() {
		truePositive++;
	}
	
	public void addFalsePositive() {
		falsePositive++;
	}
	
	public void addFalseNegative() {
		falseNegative++;
	}
	
	public void addTrueNegative() {
		trueNegative++;
	}

	public void calculateStatisticalMeasures() {
		calculatePrecision();
		calculateRecall();
		calculateFMeasure();
		calculateSpecificity();
		calculateAccuracy();
		calculateAlpha();
		calculateBeta();
		calculateLikelihoodRatioPositive();
		calculateLikelihoodRatioNegative();
	}
	
	public double calculatePrecision() {
		precision = truePositive/(truePositive + falsePositive);
		return(precision);
	}
	
	public double calculateRecall(){
		recall = truePositive/(truePositive + falseNegative);
		return(recall);
	}
	
	public double calculateFMeasure(){
		fMeasure = (2 * precision * recall) / (precision + recall);
		return(fMeasure);
	}
	
	public double calculateSpecificity(){
		specificity = trueNegative / (trueNegative + falsePositive);
		return(specificity);
	}
	
	public double calculateAccuracy(){
		accuracy = (truePositive + trueNegative) / (truePositive + falsePositive + falseNegative + trueNegative);
		return(accuracy);
	}
	
	public double calculateAlpha(){
		alpha = 1 - specificity;
		return(alpha);
	}
	
	public double calculateBeta(){
		beta = 1 - recall;
		return(beta);
	}
	
	public double calculateLikelihoodRatioPositive(){
		likelihoodRatioPositive = recall / (1 - specificity);
		return(likelihoodRatioPositive);
	}
	
	public double calculateLikelihoodRatioNegative(){
		likelihoodRatioNegative = (1 - recall) / specificity;
		return(likelihoodRatioNegative);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getTruePositive() {
		return truePositive;
	}

	public void setTruePositive(double truePositive) {
		this.truePositive = truePositive;
	}

	public double getFalsePositive() {
		return falsePositive;
	}

	public void setFalsePositive(double falsePositive) {
		this.falsePositive = falsePositive;
	}

	public double getFalseNegative() {
		return falseNegative;
	}

	public void setFalseNegative(double falseNegative) {
		this.falseNegative = falseNegative;
	}

	public double getTrueNegative() {
		return trueNegative;
	}

	public void setTrueNegative(double trueNegative) {
		this.trueNegative = trueNegative;
	}

	public double getSpecificity() {
		return specificity;
	}

	public void setSpecificity(double specificity) {
		this.specificity = specificity;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getBeta() {
		return beta;
	}

	public void setBeta(double beta) {
		this.beta = beta;
	}

	public double getLikelihoodRatioPositive() {
		return likelihoodRatioPositive;
	}

	public void setLikelihoodRatioPositive(double likelihoodRatioPositive) {
		this.likelihoodRatioPositive = likelihoodRatioPositive;
	}

	public double getLikelihoodRatioNegative() {
		return likelihoodRatioNegative;
	}

	public void setLikelihoodRatioNegative(double likelihoodRatioNegative) {
		this.likelihoodRatioNegative = likelihoodRatioNegative;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getfMeasure() {
		return fMeasure;
	}

	public void setfMeasure(double fMeasure) {
		this.fMeasure = fMeasure;
	}
	
}
