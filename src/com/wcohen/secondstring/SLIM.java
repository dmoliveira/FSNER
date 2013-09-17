package com.wcohen.secondstring;
import java.util.*;

/**
 * The same-letter index mixture distance.
 * <p>
 * Experimental, invented by William Cohen. Ahem. Ahem. Which is mine.
 * <p>
 * Represents a pair S=a1...aK, T=b1...bK as a set of index distances
 * D = { i-j : ai=bj }, and models this set D as a mixture of two
 * Gaussians, one with small variance (so i~=j) and one with large
 * variance (so i!=j).  The degree of overlap for two chars ai and bj
 * such that ai=bj is the posterior probability of the small-variance
 * Gaussian, given i-j.  Distance is average overlap between the two
 * strings.
 */

public class SLIM extends AbstractStringDistance
{
	// trace output
	private static final boolean TRACE = false;

	// max iterations for EM
	private static final int MAX_EM_ITERATIONS = 20;

	// minimum change of any parameter to continue EM
	// (less than this is considered convergence)
	private static final double MIN_PARAMETER_CHANGE = 0.01;

	// before modeling, all indices are modified by adding 
	// random Gaussian noise with mean of zero and this SD
	private static final double RANDOM_GAUSSIAN_SD = 1.0;

	// prior weight, in pseudo-counts, of the prior for the
	// small-variance gaussian in the mixture model
	private static double parameterPriorWeight = 0.0;
	// parameters of the prior for the small gaussian
	private static double priorMean = 0.0;
	private static double priorVar = 1.0;

	// prior probability of drawing from the small gaussian,
	// and a weight for this in pseudo-counts
	private static double mixturePriorWeight = 0.0;
	private static double mixturePrior = 0.5;

	// if true, approximate the uniform distribution with a second
	// high-variance gaussian
	private static final boolean USE_TWO_GAUSSIANS = false;

	// if true, use a trivial 'model' which gives everything to the
	// gaussian, rather than a mixture model
	private static final boolean USE_TRIVIAL_MODEL = true;

	//
	// constructor
	//

	public SLIM() { }

	public String toString() { return "[SLIM]"; }

	//
	// real code
	//

	public double score(StringWrapper s,StringWrapper t) 
	{
		String str1 = s.unwrap().toLowerCase();
		String str2 = t.unwrap().toLowerCase();
		if (TRACE) System.out.println("SLIM: '"+str1+"' ~ '"+str2+"'");
		if (str1.equals(str2)) return 1.0;
		Histogram hist = new Histogram(str1,str2);
		double sOverlap,tOverlap;
		if (hist.getTotalCount() <= 1) { // can't build a model from this
			sOverlap = ((double)hist.getTotalCount()) / str1.length();
			tOverlap = ((double)hist.getTotalCount()) / str2.length();
		} else {
			MixtureModel model = new MixtureModel(hist);
			sOverlap = overlap(str1,str2,model);
			tOverlap = overlap(str2,str1,model);
		}
		double score = 0.5 * (sOverlap + tOverlap);
		return score;
	}

	public String explainScore(StringWrapper s, StringWrapper t)	
	{

		String str1 = s.unwrap().toLowerCase();
		String str2 = t.unwrap().toLowerCase();

		if (str1.equals(str2)) {
			return "Strings are equal: score = 1.0";
		}
		
		StringBuffer buf = new StringBuffer("");
		// print the same-letter matrix
		buf.append("Co-occurence matrix:\n");
		buf.append(new MatchMemoMatrix(s,t).toString());
		buf.append("\n\n");

		Histogram hist = new Histogram(str1,str2);
		double sOverlap,tOverlap;
		if (hist.getTotalCount() <= 1) { // can't build a model from this
			buf.append("No mixture model for "+hist.getTotalCount()+" common letter(s)\n");
			sOverlap = ((double)hist.getTotalCount()) / str1.length();
			tOverlap = ((double)hist.getTotalCount()) / str2.length();
		} else {
			// print the mixture model, and posterior, histogram
			MixtureModel model = new MixtureModel(hist);
			buf.append("Mixture model "+model.toString()+"\n");
			PrintfFormat fmt = new PrintfFormat("%3d Pr(%3d): ");
			for (int d=hist.getMinDiff(); d<=hist.getMaxDiff(); d++) {
				double p = model.posteriorProbOfGaussian(d);
				buf.append(fmt.sprintf(new Object[]{new Integer(d),new Integer((int)(100*p))}));
				for (int j=0; j<hist.getCount(d); j++) buf.append('*');
				buf.append("\n");
			}
			buf.append("\nPosterior probability of Gaussian:\n");
			buf.append(new MixMemoMatrix(s,t,model).toString());
			buf.append("\n\n");
			sOverlap = overlap(str1,str2,model);
			tOverlap = overlap(str2,str1,model);
		}
		double score = 0.5 * (sOverlap + tOverlap);
		buf.append("sOverlap="+sOverlap+" tOverlap="+tOverlap+" score="+score+"\n");
		buf.append("Score="+score(s,t)+"\n");
		return buf.toString();
	}

	/** Figure out how much overlap there is between the first n
	 * characters of s and t according to the mixture model.  Char ai of
	 * s overlaps with t if there is some bj in t such that bj=ai and
	 * i-j is "explained nicely" by the mixture model---ie, if i-j has a
	 * high posterior probability of being generated by the
	 * small-variance Gaussian.  Votes from the different bj's are
	 * combined using a noisy-or function.
	 */
	private double overlap(String s,String t,MixtureModel model,int len)
	{
		double n = 0.0;
		for (int i=0; i<len; i++) {
			double thisCharWeight = 1.0;
			for (int j=0; j<t.length(); j++) {
				if (s.charAt(i)==t.charAt(j)) {
					thisCharWeight *= (1.0 - model.posteriorProbOfGaussian(i-j));
				}
			}
			thisCharWeight = 1.0 - thisCharWeight;
			n += thisCharWeight;
		}
		return n/s.length();
	}

	/** Defaults the length parameter n to s.length(). */
	private double overlap(String s,String t,MixtureModel model) {
		return overlap(s,t,model,s.length());
	}

	/** Counts how many times a distance d=i-j occurs when s[i]==t[j].
	 */
	private static class Histogram
	{
		int maxLength;
		int[] count;
		int totalCount;
		public Histogram(String s,String t) {
			maxLength = Math.max(s.length(),t.length());
			count = new int[2*maxLength+1];
			totalCount = 0;
			for (int i=0; i<s.length(); i++) {
				for (int j=0; j<t.length(); j++) {
					if (s.charAt(i) == t.charAt(j)) {
						int diff = i-j;
						count[ diff + maxLength ]++;
						totalCount++;
					}
				}
			}
		}
		public int getMinDiff() { return -maxLength; }
		public int getMaxDiff() { return +maxLength; }
		public int getCount(int diff) { return count[ diff + maxLength ];	}
		public int getTotalCount() { return totalCount; }
		public String toString() {
			StringBuffer buf = new StringBuffer();
			PrintfFormat fmt = new PrintfFormat("%3d: ");
			for (int d=getMinDiff(); d<=getMaxDiff(); d++) {
				buf.append(fmt.sprintf(d));
				for (int i=0; i<getCount(d); i++) buf.append('*');
				buf.append("\n");
			}
			return buf.toString();
		}
		/** Convert to a 'sample' of doubles, with noise added. */
		public double[] toSample() {
			double[] sample = new double[totalCount];
			Random rand = new Random(0);
			int k = 0;
			for (int d=getMinDiff(); d<=getMaxDiff(); d++) {
				for (int i=0; i<getCount(d); i++) {
					sample[ k++ ] = d + rand.nextGaussian() * RANDOM_GAUSSIAN_SD; 
				}
			}
			return sample;
		}
	}

	/** Model a histogram as a mixture of a Gaussian and a "uniform"
	 * probability.  The "uniform probability" is modeled as another
	 * Gaussian, with a fixed mean of 0 and a large fixed variance.
	 */
	private static class MixtureModel 
	{
		double[] z; // Prob(s[i] from gaussian | s[i])
		double[] s; // sample
		double probGaussian; // prob of drawing from Gaussian vs uniform
		double bigVariance;  // variance of wide gaussian, which approximates uniform
		double bigMean;      // wide gaussian's mean
		double mean; // mean of Gaussian
		double var;  // variance of Gaussian
		double spread; // range of possible values

		public MixtureModel(Histogram hist) 
		{
			if (USE_TRIVIAL_MODEL) return;

			// draw the sample s
			s = hist.toSample();

			// initialize a model
			spread = hist.getMaxDiff()-hist.getMinDiff();
			if (USE_TWO_GAUSSIANS) {
				bigVariance = spread*spread/4.0;
				bigMean = (hist.getMaxDiff() + hist.getMinDiff())/2.0;
			}
			mean = priorMean;
			var = priorVar;
			probGaussian = mixturePrior;

			// run E/M
			z = new double[s.length];
			for (int t=0; t<MAX_EM_ITERATIONS; t++) {
				// E step: estimate the posterior probability of the small
				// gaussian having generated each sample point
				for (int i=0; i<s.length; i++) {
					z[i] = posteriorProbOfGaussian( s[i] );
				}
				// save old model
				double oldMean=mean, oldVar=var, oldProbGaussian=probGaussian;
				// M step: estimate parameters of the new gaussian
				double weightedSum = 0.0;
				double weightedCount = 0.0;
				for (int i=0; i<s.length; i++) {
					weightedCount += z[i];
					weightedSum += s[i]*z[i];
				}
				// posterior estimate of mean: ML estimate would be weightedSum/weightedCount;
				mean = (weightedSum + priorMean*parameterPriorWeight) / (weightedCount + parameterPriorWeight);
				// posterior estimate of probability of drawing from the gaussian
				probGaussian = (weightedCount + mixturePriorWeight*mixturePrior) / (s.length + mixturePriorWeight);
				// posterior estimate of variance
				double sumVar = 0.0;
				for (int i=0; i<s.length; i++) {
					sumVar += (s[i]-mean) * (s[i]-mean) * z[i];
				}
				var = (sumVar+priorVar*parameterPriorWeight)/ (weightedCount + parameterPriorWeight);
				if (TRACE) System.out.println("Iteration "+t+":  "+toString()+"lambda="+probGaussian+" z="+toString(z));
				// stop E/M if converged
				if (!changed(mean,oldMean) && !changed(var,oldVar) && !changed(probGaussian,oldProbGaussian)) break;
			}// iteration t
		}
		// has a parameter changed much?
		private boolean changed(double newV,double oldV) {
			double diff = oldV-newV;
			if (diff<0) diff = -diff;
			return diff > MIN_PARAMETER_CHANGE;
		}
		// print an array of doubles
		private String toString(double[] z)
		{
			StringBuffer buf = new StringBuffer();
			PrintfFormat fmt = new PrintfFormat(" %.2g");
			for (int i=0; i<z.length; i++) {
				buf.append(fmt.sprintf(z[i]));
			}
			return buf.toString();
		}
		// log prob of generating s from a Gaussian, i.e. log P(s|N(mean,var))
		private double logPGaussian(double s,double mean, double var) 
		{
			double d = s - mean;
			return -2*Math.log(2.0*3.1415927*var) - (d*d) /(2.0*var);
		}

		// find z[i] = Prob( Gaussian | s )  =
		//   1/c * Prob(s[i] | gaussian)Prob(gaussian)
		//   where c =  Prob(s[i] | gaussian)Prob(gaussian) + Prob(s[i]|uniform)(1-Prob(gaussian))
		public double posteriorProbOfGaussian(double s)
		{
			if (USE_TRIVIAL_MODEL) return 1.0;
			double logProbSGivenGaussian = logPGaussian( s, mean, var);
			double probSGivenGaussian = Math.exp( logProbSGivenGaussian );
			double logProbSGivenUniform = 
				USE_TWO_GAUSSIANS ? logPGaussian( s, bigMean, bigVariance) : -Math.log( spread );
			double probSGivenUniform = Math.exp( logProbSGivenUniform );
			double normalizer = probSGivenGaussian*probGaussian + probSGivenUniform*(1-probGaussian);
			/*
			System.out.println("post "+s+": P(s|N)="+probSGivenGaussian+" P(s|U)="+probSGivenUniform
												 +"\nnorm="+normalizer
												 +" result = "+probSGivenGaussian*probGaussian/normalizer);
			*/
			if (normalizer>0) {
				double result = probSGivenGaussian*probGaussian / normalizer;
				return Math.max( result, 0.0 );  // catch underflow to small negative #'s
			} else { 		// underflow
				return (logProbSGivenGaussian > logProbSGivenUniform) ? 0.99999 : 0.00001;
			}
		}
		// show the mixture model
		public String toString() 
		{
			if (USE_TRIVIAL_MODEL) {
				return "[mix:trivial]";
			} else if (USE_TWO_GAUSSIANS) {
				PrintfFormat fmt = new PrintfFormat("[mix: N(mu=%.3g;sd=%.3g)*%.3f + N(mu=%.3g;sd=%.3g)*%.3f]");
				return fmt.sprintf(
					new Object[] {   
						new Double(mean), new Double(Math.sqrt(var)),	new Double(probGaussian),
						new Double(bigMean), new Double(Math.sqrt(bigVariance)), new Double(1-probGaussian) 
					});
			} else {
				PrintfFormat fmt = new PrintfFormat("[mix: N(mu=%.3g;sd=%.3g)*%.3f + Uniform(1/%g)*%.3f]");
				return fmt.sprintf(
					new Object[] {   
						new Double(mean), new Double(Math.sqrt(var)),	new Double(probGaussian),
						new Double(spread), new Double(1-probGaussian) 
					});
				
			}
		}
	}

	// graphically show positions where ai=bj
	private static class MatchMemoMatrix extends MemoMatrix {
		public MatchMemoMatrix(StringWrapper s,StringWrapper t) { super(s,t); }
		double compute(int i,int j) { return sAt(i)==tAt(j) ? 1 : 0; }
	}

	// graphically show posterior probability of positions
	private static class MixMemoMatrix extends MemoMatrix {
		private MixtureModel model;
		public MixMemoMatrix(StringWrapper s,StringWrapper t,MixtureModel model) { 
			super(s,t); 
			this.model = model;
		}
		double compute(int i,int j) { 
			if (sAt(i)!=tAt(j)) return 0;
			else {
				double p = model.posteriorProbOfGaussian(i-j);
				if (p==1) return 99;
				else return (int)(p*100);
			}
		}
	}


	public StringWrapper prepare(String s) { return new StringWrapper(s);	}

	static public void main(String[] argv) {
		doMain(new SLIM(), argv);
	}
}