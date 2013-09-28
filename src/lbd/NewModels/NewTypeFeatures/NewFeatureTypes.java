package lbd.NewModels.NewTypeFeatures;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;
import iitb.Model.WordsInTrain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.CRF.LabelMap.BILOU;
import lbd.NewModels.Affix.AffixManager;
import lbd.NewModels.BrownHierarquicalCluster.BrownCluster;
import lbd.NewModels.Context.ContextAnalysis;
import lbd.NewModels.Context.ContextManager.ContextType;
import lbd.NewModels.Context.ContextUnit;
import lbd.OutOfVocabularySupport.OOVSupportContext;
import lbd.SummirizedPattern.SummarizedPattern;
import lbd.Thesauru.Thesaurus;
import lbd.Thesauru.ThesaurusElement;
import lbd.Thesauru.ThesaurusManager;
import lbd.Utils.Utils;

public abstract class NewFeatureTypes extends FeatureTypes{

	private static final long serialVersionUID = 1L;

	/** Id Parameters **/
	private static int featureGlobalId;
	protected int featureInternalId;
	protected int featureId;

	/** Trivial Parameters **/
	protected String featureName;

	protected int currentState;
	protected int previousState;

	protected float weight;

	/** Feature Behavior Parameters **/
	protected boolean skipOutsideState;
	protected boolean iterateOverPreviousState;
	protected boolean useOnlyWhenAllOtherFeaturesInactives;
	protected boolean tryThesaurusAlternative;

	/** Data Sequence Parameters **/
	public enum ProccessSequenceType {Plain, AllLowerCase, BrownHierarquicalCluster, SummarizedPattern};
	protected ProccessSequenceType proccessSequenceType;

	protected DataSequence sequence;
	protected String [] proccessedSequence;
	protected String term;
	protected int pos;

	/** Feature  Type, Mode and State **/
	public static enum FeatureType {BrownHierarquicalCluster, SummarizedPattern, TokenAffix,
		ContextAnalysis, TwitterSymbol, Capitalization, Dictionary, JCluster, NGram, Unknown};
		protected FeatureType featureType;
		protected FeatureType [] featureToNotRunInParallel;

		public enum FeatureMode {InTrain, InTest, Unknown};
		protected static FeatureMode featureMode;

		protected enum FeatureState {Active, Inactive};
		protected FeatureState featureState;

		/** Out of Vocabulary Object Support **/
		public transient static ThesaurusManager thesaurusManager;
		protected transient static HashMap<String, String> thesaurusTermMap;

		protected transient static String oovSCxtFile = "oovSCxtFile.bin";
		protected transient static OOVSupportContext oovSupportContext;

		public transient static ContextAnalysis contextAnalysis;

		/** Feature Statistics Objects **/
		protected transient NewFeatureTypesStatistics featureStatistics;
		protected transient static NewFeatureTypesGlobalStatistics globalFeatureStatistics;

		protected HashMap<Integer, Integer[]> stateFrequencyMap;
		protected double totalStateFrequency;


		/** Auxiliary Objects to Combine features **/
		protected static transient ArrayList<ContextAnalysis> contextAnalysisList;
		protected static transient BrownCluster brownCluster;
		protected static transient SummarizedPattern summarizedPattern;
		protected static transient AffixManager affixManager;
		protected static transient WordsInTrain dict;

		public NewFeatureTypes(FeatureGenImpl fgen) {

			super(fgen);

			if(thesaurusManager == null) {thesaurusManager = new ThesaurusManager();}
			if(thesaurusTermMap == null) {
				thesaurusTermMap = new HashMap<String, String>();
			}
			if(globalFeatureStatistics == null)
			{
				globalFeatureStatistics  = new NewFeatureTypesGlobalStatistics();
				//if(oovSupportContext == null) startOOVSupportContext();
			}

			featureStatistics = new NewFeatureTypesStatistics();

			featureInternalId = ++featureGlobalId;

			skipOutsideState = false;
			iterateOverPreviousState = false;
			useOnlyWhenAllOtherFeaturesInactives = false;
			tryThesaurusAlternative = false;

			featureType = FeatureType.Unknown;
			featureMode = FeatureMode.Unknown;

			previousState = -1;
			weight = 1f;

			stateFrequencyMap = new HashMap<Integer, Integer []>();
			totalStateFrequency = 0;

			proccessSequenceType = ProccessSequenceType.AllLowerCase;
			featureState = FeatureState.Inactive;
		}

		@Override
		public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {

			proccessSequence(data);
			this.pos = pos;

			featureId = -1;
			currentState = -1;
			previousState = -1;

			featureState = FeatureState.Inactive;

			if(canRunFeature()) {

				featureId = startFeature(data, pos);

				if(featureId <= -1 && tryThesaurusAlternative && oovSupportContext != null) {
					featureId = useOOVSupportContext(pos);
				}

				//useThesaurusOOV(pos);

				if(featureId > -1) {
					featureState = FeatureState.Active;
					currentState = 0;
					term = proccessedSequence[pos];

					if(isInTrain()) {

						Integer [] stateFrequency;

						if(!stateFrequencyMap.containsKey(featureId)) {
							stateFrequencyMap.put(featureId, new Integer[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});
						}

						stateFrequency = stateFrequencyMap.get(featureId);

						stateFrequency[data.y(pos)]++;

						totalStateFrequency++;
					}

					/*if(!isInTrain() && stateFrequencyMap.containsKey(featureId)) {
					Integer [] freq = stateFrequencyMap.get(featureId);
					double perc = ((double)freq[3])/(freq[0] + freq[1] + freq[2] + freq[3] + freq[4]);
					if(isInTrain() && perc >= 0.4 &&  perc <= 0.7){
						featureId = -1;
					}
				}*/

					//if(!isInTrain())System.out.println(data.x(pos));
				}
			}

			if(wasReachedCriteria() && (featureMode == FeatureMode.InTrain || featureMode == FeatureMode.InTest)) {
				featureStatistics.addToStatistics(data, proccessedSequence[pos], pos, featureId > -1, featureType);
				globalFeatureStatistics.addToStatistics(data, proccessedSequence[pos], pos, featureId > -1, featureType);
			}

			return(featureId > -1);
		}

		protected int improvedStartFeature(int pos) {

			String term = proccessedSequence[pos];
			int featuredId = -1;

			if(thesaurusTermMap.containsKey(proccessedSequence[pos])) {

				proccessedSequence[pos] = thesaurusTermMap.get(proccessedSequence[pos]);
				featureId = startFeature(sequence, pos);

			} else {
				featureId = useThesaurusLite(pos);
			}

			proccessedSequence[pos] = term;

			return(featuredId);
		}

		protected int useOOVSupportContext(int pos) {

			int featureId = -1;

			HashMap<String, Integer> similarTermsMap = oovSupportContext.getSimilarTerm(proccessedSequence, pos);

			if(similarTermsMap != null) {

				Iterator<Entry<String, Integer>> ite = similarTermsMap.entrySet().iterator();

				String term = proccessedSequence[pos];

				while(ite.hasNext()) {

					proccessedSequence[pos] = ite.next().getKey();

					if(!proccessedSequence[pos].equals(term.toLowerCase())) {
						featureId = startFeature(sequence, pos);

						if(featureId > -1) {
							break;
						}
					}
				}

				proccessedSequence[pos] = term;
			}

			return(featureId);
		}

		protected void useThesaurusOOV(int pos) {
			if(featureId <= -1 && tryThesaurusAlternative &&
					ThesaurusManager.isValidTerm(proccessedSequence[pos]) && proccessedSequence[pos].length() > 3) {
				featureId = improvedStartFeature(pos);
			}
		}

		protected void useContextAnalysisLikeThesaurus(DataSequence data, int pos) {

			if(featureId <= -1 && tryThesaurusAlternative && contextAnalysis != null) {
				ArrayList<ContextUnit> contextUnitList = contextAnalysis.getSimilarContextList(proccessedSequence, pos, ContextType.Prefix, 2);
				//if(contextUnitList == null) contextUnitList = contextAnalysis.getSimilarContextList(proccessedSequence, pos, ContextType.PrefixSuffix, 1);
				//if(contextUnitList == null) contextUnitList = contextAnalysis.getSimilarContextList(proccessedSequence, pos, ContextType.Prefix, 2);

				if(contextUnitList != null) {
					String term = proccessedSequence[pos];

					for(ContextUnit contextUnit : contextUnitList) {

						proccessedSequence[pos] = contextUnit.getMainTerm();

						featureId = startFeature(data, pos);

						if(featureId > -1) {
							break;
						}
					}

					proccessedSequence[pos] = term;
				}
			}

		}

		protected int useThesaurusLite(int pos) {

			int featureId = -1;

			Thesaurus thesaurus = thesaurusManager.getThesaurusLite(proccessedSequence[pos]);

			if(thesaurus != null) {
				for(String synonym : thesaurus.getSynonymList()) {

					proccessedSequence[pos] = synonym;
					featureId = startFeature(sequence, pos);

					if(featureId > -1) {
						thesaurusTermMap.put(proccessedSequence[pos], synonym);
						break;
					}
				}
			}

			return(featureId);
		}

		protected int useThesaurus(int pos) {

			int featureId = -1;

			Thesaurus thesaurus = thesaurusManager.getThesaurus(proccessedSequence[pos]);

			if(thesaurus.hasThesaurus()) {
				for(ThesaurusElement thesaurusElement : thesaurus.getThesaurusList()){
					for(String posTag : thesaurusElement.getPosTagList()) {
						if(thesaurusElement.getSynonymMap() != null && thesaurusElement.getSynonymMap().get(posTag) != null) {
							for(String synonym : thesaurusElement.getSynonymMap().get(posTag)) {
								proccessedSequence[pos] = synonym;
								featureId = startFeature(sequence, pos);

								if(featureId > -1) {
									thesaurusTermMap.put(proccessedSequence[pos], synonym);
									return(featureId);
								}
							}
						} else {
							return(featureId);
						}
					}
				}
			}

			return(featureId);
		}

		protected void startOOVSupportContext() {

			if((new File(oovSCxtFile)).exists()) {
				try {
					oovSupportContext = new OOVSupportContext();
					oovSupportContext.readContextAnalysisObject(oovSCxtFile);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				oovSupportContext = new OOVSupportContext();
			}
		}

		protected void proccessSequence(DataSequence data) {
			if(sequence != data) {

				sequence = data;

				if(proccessSequenceType == ProccessSequenceType.AllLowerCase) {

					proccessedSequence = Utils.convertSequenceToLowerCase(data, data.length());

				} else if(proccessSequenceType == ProccessSequenceType.Plain) {

					proccessedSequence = Utils.transformSequenceToArray(data, data.length());

				}
			}
		}

		protected abstract int startFeature(DataSequence data, int pos);

		protected void advance() {


			if(iterateOverPreviousState) {
				previousState++;
			}

			if(!iterateOverPreviousState || previousState >= model.numStates()) {
				if(skipOutsideState && currentState == BILOU.Last.ordinal()) {
					currentState++;
				}

				currentState++;
			}

			if(previousState >= model.numStates()) {
				previousState = -1;
			}

			/*if(iterateOverPreviousState) {
			if(pos > 0) {
				if(currentState == 0) {
					if(previousState == -1) {
						previousState = 2;
					} else if(previousState == 2) {
						previousState = 3;
					} else if(previousState == 3) {
						previousState = 4;
					}  else if(previousState == 4) {
						currentState++;
						previousState = -1;
					}
				}

				if(currentState == 1) {
					if(previousState == -1) {
						previousState = 0;
					} else if(previousState == 0) {
						previousState = 1;
					} else if(previousState == 1) {
						currentState++;
						previousState = -1;
					}
				}

				if(currentState == 2) {
					if(previousState == -1) {
						previousState = 0;
					} else if(previousState == 0) {
						previousState = 1;
					} else if(previousState == 1) {
						currentState++;
						previousState = -1;
					}
				}

				if(currentState == 3) {
					if(previousState == -1) {
						previousState = 2;
					} else if(previousState == 2) {
						previousState = 3;
					} else if(previousState == 3) {
						previousState = 4;
					} else if(previousState == 4) {
						currentState++;
						previousState = -1;
					}
				}

				if(currentState == 4) {
					if(previousState == -1) {
						previousState = 2;
					} else if(previousState == 2) {
						previousState = 3;
					} else if(previousState == 3) {
						previousState = 4;
					} else if(previousState == 4) {
						currentState++;
						previousState = -1;
					}
				}
			} else {
				currentState++;
			}
		}*/
		}

		@Override
		public boolean hasNext() {
			return (currentState > -1 && currentState < model.numStates());
		}

		@Override
		public void next(FeatureImpl f) {

			/*double newWeight = (stateFrequencyMap != null && stateFrequencyMap.containsKey(this.featureId))?
				(stateFrequencyMap.get(this.featureId)[currentState]/totalStateFrequency) : 1;*/

			int featureId = this.featureId * model.numStates() + currentState;
			featureId = (iterateOverPreviousState)? featureId * (model.numStates()+1) + (previousState+1) : featureId;

			/*setFeatureIdentifier(featureId, currentState,
				featureName + ".state[" + previousState + "," + currentState + "](" + term + ")_id["+featureId+"]",f);*/
			setFeatureIdentifier(featureId, currentState, term+"_"+featureName,f);

			f.yend = currentState;
			f.ystart = previousState;
			f.val = 1 * weight;// * ((float)newWeight);

			advance();
		}

		protected boolean canRunFeature() {
			return(!useOnlyWhenAllOtherFeaturesInactives || !isInTrain() || isFeaturesInactive(featureToNotRunInParallel));
		}

		public static void setFeatureModeInTrain() {
			featureMode = FeatureMode.InTrain;
		}

		public static void setFeatureModeInTest() {
			featureMode = FeatureMode.InTest;
		}

		public static void setFeatureModeUnknown() {
			featureMode = FeatureMode.Unknown;
		}

		protected boolean wasReachedCriteria() {
			return(true);
		}

		public static boolean isInTrain() {
			return(featureMode == FeatureMode.InTrain);
		}

		public static boolean isInTest() {
			return(featureMode == FeatureMode.InTest);
		}

		public static boolean isUnknownState() {
			return(featureMode == FeatureMode.Unknown);
		}

		public static void setContextAnalysisList(ArrayList<ContextAnalysis> contextAnalysisList) {
			NewFeatureTypes.contextAnalysisList = contextAnalysisList;
		}

		public static void setBrownCluster(BrownCluster brownCluster) {
			NewFeatureTypes.brownCluster = brownCluster;
		}

		public static void setSummarizedPattern(SummarizedPattern summarizedPattern) {
			NewFeatureTypes.summarizedPattern = summarizedPattern;
		}

		public static void setAffixManager(AffixManager affixManager) {
			NewFeatureTypes.affixManager = affixManager;
		}

		public static void setWordsInTrain(WordsInTrain dict) {
			NewFeatureTypes.dict = dict;
		}

		protected boolean isAllFeaturesInactive() {

			boolean isAllFeaturesInactive = true;

			for(Object feature : fgen.features) {
				if(feature.getClass().isInstance(this) &&
						((NewFeatureTypes)feature).featureState == FeatureState.Active) {
					isAllFeaturesInactive = false;
					break;
				}
			}

			return(isAllFeaturesInactive);
		}

		protected boolean isFeaturesInactive(FeatureType [] featureTypeList) {

			boolean isAllFeaturesInactive = true;
			NewFeatureTypes newFeatureTypes;

			for(Object feature : fgen.features) {
				if(NewFeatureTypes.class.isAssignableFrom(feature.getClass())) {

					newFeatureTypes = (NewFeatureTypes) feature;

					if(isSameFeatureType(featureTypeList, newFeatureTypes.featureType) &&
							newFeatureTypes.featureState == FeatureState.Active) {
						isAllFeaturesInactive = false;
						break;
					}
				}
			}

			return(isAllFeaturesInactive);
		}

		protected boolean isSameFeatureType(FeatureType [] featureTypeList, FeatureType featureType) {

			boolean isSameFeatureType = false;

			for(int i = 0; i < featureTypeList.length; i++) {
				if(featureType == featureTypeList[i]) {
					isSameFeatureType = true;
					break;
				}
			}

			return(isSameFeatureType);
		}

		public void writeStateFrequencyMap() {

			try {
				Writer out = new OutputStreamWriter(new FileOutputStream(featureName+"-SFMap.bin"), "ISO-8859-1");

				Iterator<Entry<Integer,Integer[]>> ite = stateFrequencyMap.entrySet().iterator();
				Entry<Integer,Integer[]> entry;

				out.write(totalStateFrequency + "\n");

				while(ite.hasNext()) {

					entry = ite.next();

					out.write(entry.getKey() + " ");

					Integer [] freq = entry.getValue();

					for(Integer fItem : freq) {
						out.write(fItem + " ");
					}

					out.write("\n");
				}

				out.flush();
				out.close();

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void readStateFrequencyMap() {

			try {
				if(new File(featureName+"-SFMap.bin").exists()) {
					BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(featureName+"-SFMap.bin"), "ISO-8859-1"));

					String line;
					String split[];

					stateFrequencyMap = new HashMap<Integer, Integer[]>();
					totalStateFrequency = Double.parseDouble(in.readLine());

					while((line = in.readLine()) != null) {

						split = line.split(" ");

						stateFrequencyMap.put(Integer.parseInt(split[0]), new Integer[5]);
						Integer [] freq = stateFrequencyMap.get(Integer.parseInt(split[0]));

						for(int i = 1; i < 6; i++) {
							freq[i-1] = Integer.parseInt(split[i]);
						}

					}

					in.close();
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		public static void restartGlobalStatistics() {
			globalFeatureStatistics = new NewFeatureTypesGlobalStatistics();
		}

		public static void addOOVSupportContext(DataSequence data) {

			if(oovSupportContext != null) {
				String [] sequence = Utils.transformSequenceToArray(data, data.length());
				for(int i = 0; i < data.length(); i++) {
					oovSupportContext.addContext(sequence, i);
				}
			}
		}

		public static void saveOOVSupportContext() {
			try {
				if(oovSupportContext != null) {
					oovSupportContext.writeContextAnalysisObject(oovSCxtFile);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void printFeatureStatistics() {
			featureStatistics.printFeatureStatistics(featureType);
		}

		public static void printGlobalFeatureStatistics() {
			globalFeatureStatistics.printFeaturesStatistics();
		}
}
