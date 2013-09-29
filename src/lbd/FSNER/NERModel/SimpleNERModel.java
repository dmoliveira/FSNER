package lbd.FSNER.NERModel;

import java.util.ArrayList;

import lbd.FSNER.Component.Statistic.FPESimple;
import lbd.FSNER.Configuration.FilterParameters.FilterType;
import lbd.FSNER.DataPreprocessor.DPLowerCase;
import lbd.FSNER.DataPreprocessor.DPPlainSequence;
import lbd.FSNER.DataPreprocessor.DPStopWord;
import lbd.FSNER.Filter.FtrAffix;
import lbd.FSNER.Filter.FtrCapitalizedTerms;
import lbd.FSNER.Filter.FtrContext;
import lbd.FSNER.Filter.FtrEntityProbability;
import lbd.FSNER.Filter.FtrSingleTermDictionary;
import lbd.FSNER.Filter.FtrSingleTermDictionary4;
import lbd.FSNER.Filter.FtrState;
import lbd.FSNER.Filter.MultiFilter;
import lbd.FSNER.Filter.Component.Context.ContextType;
import lbd.FSNER.Filter.ScoreCalculatorModel.FSCMMultiFilter;
import lbd.FSNER.Filter.ScoreCalculatorModel.FSCMNoScore;
import lbd.FSNER.Model.AbstractActivityControl;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Model.AbstractNERModel;
import lbd.FSNER.Utils.CommonEnum.Flexibility;

public class SimpleNERModel extends AbstractNERModel {

	private static final long serialVersionUID = 1L;

	protected boolean displayGeneralizationStatistics = false;

	@Override
	protected void allocModelSub(String[] initializeFilenameList) {

		/** Data Preprocessor **/
		ArrayList<AbstractDataPreprocessor> dataPreprocessorList = new ArrayList<AbstractDataPreprocessor>();
		ArrayList<AbstractDataPreprocessor> dpLowerCaseStopword = new ArrayList<AbstractDataPreprocessor>();

		dpLowerCaseStopword.add(new DPLowerCase());
		dpLowerCaseStopword.add(new DPStopWord(initializeFilenameList[0]));

		// -- Data Preprocessor List

		// -- Base DP
		dataPreprocessorList.add(new DPPlainSequence());
		//dataPreprocessorList.add(new DPLowerCase());
		//dataPreprocessorList.add(new DPStopWord(initializeFilenameList[0]));
		// dataPreprocessorList.add(new
		// MultiDataPreprocessor(dpLowerCaseStopword));

		// -- Support DP
		// dataPreprocessorList.add(new DPSummarizedPattern());
		/*
		 * dataPreprocessorList.add(new DPCapitalizationTermsOnly());
		 * dataPreprocessorList.add(new DPSemiCapitalizationTermsOnly());
		 * dataPreprocessorList.add(new DPOnlyConsonant());
		 * dataPreprocessorList.add(new DPOnlyVowel());
		 */
		// dataPreprocessorList.add(new DPVowelConsonantCode(3));
		// for(int i = 3; i >= 0; i--) dataPreprocessorList.add(new
		// DPLength(i));
		// for(int i = 10; i > 8; i--) dataPreprocessorList.add(new
		// DPBrownCluster(initializeFilenameList[1], i));
		// dataPreprocessorList.add(new DPAffix(0,4)); // Prefix
		// dataPreprocessorList.add(new DPAffix(5,8)); // Suffix
		// for(int i = 0; i < 3; i++) for(int j = i+3; j < 6; j++)
		// dataPreprocessorList.add(new DPAffix(i,j)); // Prefix
		/*
		 * dataPreprocessorList.add(new
		 * DPBrownCluster(initializeFilenameList[1], 4));
		 * dataPreprocessorList.add(new
		 * DPBrownCluster(initializeFilenameList[1], 8));
		 * dataPreprocessorList.add(new
		 * DPBrownCluster(initializeFilenameList[1], 12));
		 */

		for (AbstractDataPreprocessor dataPreprocessor : dataPreprocessorList) {
			mActivityControl.addActivity(dataPreprocessor);
		}

		/** Filter **/
		// -- Variation All Data Preprocessor over the Filters
		for (int i = 0; i < dataPreprocessorList.size(); i++) {

			/** Filter Word Level **/
			// -- Probabilistic Entity Filter (CommonClass:Wrd)
			if (mFilterParameters.isFilterActive(FilterType.EntityProbability)) {
				addEntityProbability(i);
			}

			/** Dictionary Filter **/
			if (mFilterParameters.isFilterActive(FilterType.Dictionary)) {
				addDictionaryFilters(i);
			}

			// activityControl.addActivity(new FtrSingleTermDictionary(i, new
			// FSCMNoScore(),
			// activityControl.getDataPreprocessorList().get(i)));
			// activityControl.getFilterList().get(activityControl.getFilterList().size()-1).setFilterState(FilterState.Auxiliary);
			// activityControl.addActivity(new FtrFullTermDictionary(i, new
			// FSCMNoScore(),
			// activityControl.getDataPreprocessorList().get(i)));
			// activityControl.addActivity(new FtrSimpleDictionary(i, new
			// FSCMNoScore(),
			// activityControl.getDataPreprocessorList().get(i)));

			/** Context Filter Level **/
			// -- Context Filter (Variation in Flexibility[Restricted, Partial,
			// Total], ContextType[AllContext, Prefix, Suffix],
			// WindowSize[1..5])
			if (mFilterParameters.isFilterActive(FilterType.Context)) {
				addContextFilters(i);
			}

			// -- Vocab Filter
			// activityControl.addActivity(new FtrVocab(i, new FSCMNoScore(),
			// 0.25, 2));
			// activityControl.getFilterList().get(activityControl.getFilterList().size()-1).setFilterState(FilterState.Auxiliary);

			/*
			 * if(i == 1) { int windowSideSize = 10;//10 AbstractFilter filter =
			 * new FtrVocab2(i, new FSCMNoScore(), 10);
			 * filter.setFilterState(FilterState.Auxiliary);
			 * 
			 * activityControl.addActivity(filter);
			 * 
			 * for(int s = 1; s <= windowSideSize; s++) {
			 * //activityControl.addActivity(new FtrShiftFilterPosition(i, new
			 * FSCMNoScore(), filter, s));
			 * //activityControl.getFilterList().get(
			 * activityControl.getFilterList
			 * ().size()-1).setFilterState(FilterState.Auxiliary);
			 * 
			 * activityControl.addActivity(new FtrShiftFilterPosition(i, new
			 * FSCMNoScore(), filter, -s));
			 * activityControl.getFilterList().get(activityControl
			 * .getFilterList().size()-1).setFilterState(FilterState.Auxiliary);
			 * 
			 * } }
			 */

			// -- BagOfWords Filter
			/*
			 * for(int flexIndex = 0; flexIndex < Flexibility.values().length;
			 * flexIndex++) { for(int genUseIndex = 0; genUseIndex < 2;
			 * genUseIndex++) { activityControl.addActivity(new FtrBagOfWords(i,
			 * new FSCMNoScore(), Flexibility.values()[flexIndex], (genUseIndex
			 * != 0), 0.7)); } }
			 */

			// -- Combine Terms in Sequence Filter
			/*
			 * for(int blockSizeIndex = 4; blockSizeIndex > 0;
			 * blockSizeIndex--){ //for(double threshold = 1; threshold > 0.8;
			 * threshold-= 0.1) { activityControl.addActivity(new
			 * FtrCombineTermsInSequence(i, new FSCMNoScore(), blockSizeIndex,
			 * 0.8));//3, 0.8 //} }
			 */

			/** Orthographic Filter Level **/
			// -- Affix Filter (CommonClass:Afx)
			if (i < 4 && mFilterParameters.isFilterActive(FilterType.Affix)) {
				addOrthographicFilters(i);
			}

			// -- Spell Filters
			if (i == 0
					&& mFilterParameters
					.isFilterActive(FilterType.CapitalizedTerms)) {
				addSpellFilters(i);
			}

			/** State Filter **/
			if (mFilterParameters.isFilterActive(FilterType.State)) {
				addStateFilter(i);
			}

			// -- MultiFilters (Filters Combination in an AND manner)
			// aggregateFilter(activityControl, i);
		}

		// -- Add probability filter element
		for (AbstractFilter filter : mActivityControl.getFilterList()) {
			filter.setProbabilityFilterElement(new FPESimple());
			// if(filter.getFilterPreprocessingTypeNameIndex() > 0)
			// filter.setFilterState(FilterState.Auxiliary);
			// filter.setFilterState(FilterState.Auxiliary);
		}
	}

	protected void addEntityProbability(int i) {
		mActivityControl.addActivity(new FtrEntityProbability(i,
				new FSCMNoScore()));
		//activityControl.getFilterList().get(activityControl.getFilterList().size()-1).setFilterState(FilterState.Auxiliary);
		// activityControl.getFilterList().get(activityControl.getFilterList().size()-1).setUseFilterInUnrealiableSituation(false);
	}

	protected void addDictionaryFilters(int i) {
		ArrayList<AbstractFilter> dictionaryFilters = new ArrayList<AbstractFilter>();
		int dictionaryListNumber = FtrSingleTermDictionary4
				.getDictionaryListNumber();

		for (int j = 0; j < dictionaryListNumber; j++) {

			dictionaryFilters.add(new FtrSingleTermDictionary4(i,
					new FSCMNoScore(), mActivityControl
					.getDataPreprocessorList().get(i), j,
					(j <= 0)? null : (FtrSingleTermDictionary4)dictionaryFilters.get(j-1)));
		}
		// Utilizar apenas quando ativado 'adicionar conjunto de treino'
		// dictionaryFilters.add(new FtrSingleTermDictionary4(i, new
		// FSCMNoScore(), activityControl.getDataPreprocessorList().get(i),
		// dictionaryFilters.size()));

		for (AbstractFilter filter : dictionaryFilters) {
			mActivityControl.addActivity(filter);
			mActivityControl.getFilterList()
			.get(mActivityControl.getFilterList().size() - 1)
			.setFilterState(FilterState.Auxiliary);
		}
	}

	protected void addContextFilters(int i) {
		for (int flexibilityIndex = Flexibility.values().length - 3; flexibilityIndex >= 0; flexibilityIndex--) {
			for (int windowSize = 3; windowSize > 2; windowSize--) {//DEFAULT: WindowSize = 3, > 2;
				// (current)
				// 3; > 2
				for (int contextTypeIndex = 1; contextTypeIndex < ContextType
						.values().length; contextTypeIndex++) {
					if (flexibilityIndex > 1 || windowSize > 2) {// > 1 > 2
						mActivityControl
						.addActivity(new FtrContext(
								i,
								new FSCMNoScore(),
								ContextType.values()[contextTypeIndex],
								windowSize
								+ ((ContextType.values()[contextTypeIndex] == ContextType.AllContext) ? -1
										: 0), Flexibility
										.values()[flexibilityIndex]));
						/*activityControl
						.getFilterList()
						.get(activityControl.getFilterList().size() - 1)
						.setFilterState(FilterState.Auxiliary);*/
					} else {
						mActivityControl
						.addActivity(new FtrContext(
								i,
								new FSCMNoScore(),
								ContextType.values()[contextTypeIndex],
								windowSize
								+ ((ContextType.values()[contextTypeIndex] == ContextType.AllContext) ? -1
										: 0), Flexibility
										.values()[flexibilityIndex]));
						mActivityControl
						.getFilterList()
						.get(mActivityControl.getFilterList().size() - 1)
						.setFilterState(FilterState.Auxiliary);
					}
				}
			}
		}
	}

	protected void addOrthographicFilters(int i) {
		for (int affixTypeIndex = 0; affixTypeIndex < FtrAffix.AffixType
				.values().length; affixTypeIndex++) {
			for (int affixSize = 3; affixSize > 1; affixSize--) {//Default: (afxSize=3, > 1)
				mActivityControl
				.addActivity(new FtrAffix(i, new FSCMNoScore(),
						FtrAffix.AffixType.values()[affixTypeIndex],
						affixSize));
				mActivityControl.getFilterList()
				.get(mActivityControl.getFilterList().size() - 1)
				.setFilterState(FilterState.Auxiliary);
			}
		}
	}

	protected void addSpellFilters(int i) {
		ArrayList<AbstractFilter> spellFilters = new ArrayList<AbstractFilter>();
		spellFilters.add(new FtrCapitalizedTerms(i, new FSCMNoScore()));
		// spellFilters.add(new FtrAllCapitalized(i, new FSCMNoScore()));
		// spellFilters.add(new FtrCapitalizedPossibleTerms(i, new
		// FSCMNoScore()));
		// spellFilters.add(new FtrHasDigit(i, new FSCMNoScore()));
		// spellFilters.add(new FtrHasPontuaction(i, new FSCMNoScore()));

		// activityControl.addActivity(new FtrPosition(i, new FSCMNoScore()));
		// activityControl.getFilterList().get(activityControl.getFilterList().size()-1).setFilterState(FilterState.Auxiliary);
		// activityControl.addActivity(new FtrTermLength(i, new FSCMNoScore()));

		for (AbstractFilter filter : spellFilters) {
			mActivityControl.addActivity(filter);
			mActivityControl.getFilterList()
			.get(mActivityControl.getFilterList().size() - 1)
			.setFilterState(FilterState.Auxiliary);
		}

		/*
		 * int shiftSize = 2; for(int s = 1; s <= shiftSize; s++) {
		 * for(AbstractFilter filter : spellFilters) {
		 * activityControl.addActivity(new FtrShiftFilterPosition(i, new
		 * FSCMNoScore(), filter, s));
		 * activityControl.getFilterList().get(activityControl
		 * .getFilterList().size()-1).setFilterState(FilterState.Auxiliary);
		 * 
		 * //activityControl.addActivity(new FtrShiftFilterPosition(i, new
		 * FSCMNoScore(), filter, -s));
		 * //activityControl.getFilterList().get(activityControl
		 * .getFilterList().size()-1).setFilterState(FilterState.Auxiliary); } }
		 */
	}

	protected void addStateFilter(int i) {
		mActivityControl.addActivity(new FtrState(i,
				new FSCMNoScore()));
		mActivityControl.getFilterList().get(mActivityControl.getFilterList().size()-1).setFilterState(FilterState.Auxiliary);
	}

	protected void aggregateFilter(AbstractActivityControl activityControl,
			int dataProcessorIndex) {

		ArrayList<MultiFilter> multiFilterList = new ArrayList<MultiFilter>();

		// -- Example of filters
		// filters.add(new FtrCapitalizedTerms(dataProcessorIndex, new
		// FSCMNoScore()));
		// filters.add(new FtrSimilarSequence(dataProcessorIndex, new
		// FSCMNoScore(), 1));
		// filters.add(new FtrCombineTermsInSequence(dataProcessorIndex, new
		// FSCMNoScore(), 3, 0.2));
		// filters.add(new FtrDictionary(dataProcessorIndex, new FSCMNoScore(),
		// activityControl.getDataPreprocessorList().get(dataProcessorIndex)));

		// -- Prefix (2) + Suffix (2)
		/*
		 * ArrayList<AbstractFilter> filters1 = new ArrayList<AbstractFilter>();
		 * filters1.add(new FtrContext(dataProcessorIndex, new FSCMNoScore(),
		 * ContextType.Prefix, 2, Flexibility.Total)); filters1.add(new
		 * FtrContext(dataProcessorIndex, new FSCMNoScore(), ContextType.Suffix,
		 * 2, Flexibility.Total)); multiFilterList.add(new
		 * MultiFilter(dataProcessorIndex, new FSCMMultiFilter(), filters1));
		 */

		// -- Prefix (1) + Combine Terms In Sequence
		/*
		 * ArrayList<AbstractFilter> filters2 = new ArrayList<AbstractFilter>();
		 * filters2.add(new FtrContext(dataProcessorIndex, new FSCMNoScore(),
		 * ContextType.Prefix, 1, Flexibility.Total)); filters2.add(new
		 * FtrCombineTermsInSequence(dataProcessorIndex, new FSCMNoScore(), 3,
		 * 0.2)); multiFilterList.add(new MultiFilter(dataProcessorIndex, new
		 * FSCMMultiFilter(), filters2));
		 */

		// -- Prefix (1) + Dictionary + Affix(Pfx+Sfx(3))
		/*
		 * ArrayList<AbstractFilter> filters3 = new ArrayList<AbstractFilter>();
		 * filters3.add(new FtrContext(dataProcessorIndex, new FSCMNoScore(),
		 * ContextType.Prefix, 1, Flexibility.Total)); filters3.add(new
		 * FtrSingleTermDictionary(dataProcessorIndex, new FSCMNoScore(),
		 * activityControl.getDataPreprocessorList().get(dataProcessorIndex)));
		 * filters3.add(new FtrAffix(dataProcessorIndex, new FSCMNoScore(),
		 * FtrAffix.AffixType.Prefix, 3)); filters3.add(new
		 * FtrAffix(dataProcessorIndex, new FSCMNoScore(),
		 * FtrAffix.AffixType.Suffix, 3)); multiFilterList.add(new
		 * MultiFilter(dataProcessorIndex, new FSCMMultiFilter(), filters3));
		 */

		// -- CombineTermsInSequence + Dictionary + Affix(Pfx+Sfx(3))
		/*
		 * ArrayList<AbstractFilter> filters4 = new ArrayList<AbstractFilter>();
		 * filters4.add(new FtrCombineTermsInSequence(dataProcessorIndex, new
		 * FSCMNoScore(), 1, 0.2)); filters4.add(new
		 * FtrSingleTermDictionary(dataProcessorIndex, new FSCMNoScore(),
		 * activityControl.getDataPreprocessorList().get(dataProcessorIndex)));
		 * filters4.add(new FtrAffix(dataProcessorIndex, new FSCMNoScore(),
		 * FtrAffix.AffixType.Prefix, 3)); filters4.add(new
		 * FtrAffix(dataProcessorIndex, new FSCMNoScore(),
		 * FtrAffix.AffixType.Suffix, 4)); multiFilterList.add(new
		 * MultiFilter(dataProcessorIndex, new FSCMMultiFilter(), filters4));
		 */

		// -- EntityProbability + Dictionary
		/*
		 * ArrayList<AbstractFilter> filters5 = new ArrayList<AbstractFilter>();
		 * filters5.add(new FtrEntityProbability(dataProcessorIndex, new
		 * FSCMNoScore())); filters5.add(new
		 * FtrSingleTermDictionary(dataProcessorIndex, new FSCMNoScore(),
		 * activityControl.getDataPreprocessorList().get(dataProcessorIndex)));
		 * multiFilterList.add(new MultiFilter(dataProcessorIndex, new
		 * FSCMMultiFilter(), filters5));
		 */

		// -- Prefix (1) + Affix(Sfx(1)) + Dictionary
		ArrayList<AbstractFilter> filters6 = new ArrayList<AbstractFilter>();
		filters6.add(new FtrContext(dataProcessorIndex, new FSCMNoScore(),
				ContextType.Prefix, 1, Flexibility.Total));
		filters6.add(new FtrAffix(dataProcessorIndex, new FSCMNoScore(),
				FtrAffix.AffixType.Suffix, 1));
		filters6.add(new FtrSingleTermDictionary(dataProcessorIndex,
				new FSCMNoScore(), activityControl.getDataPreprocessorList()
				.get(dataProcessorIndex)));
		multiFilterList.add(new MultiFilter(dataProcessorIndex,
				new FSCMMultiFilter(), filters6));

		for (MultiFilter multiFilter : multiFilterList) {
			// multiFilter.setConsiderFilterProbability(false);
			// multiFilter.setUseFilterInUnrealiableSituation(false);
			multiFilter.setLoadAsSimpleFilter(true);
			activityControl.addActivity(multiFilter);
		}
	}

	/*
	 * @Override public void evaluate() { if(displayGeneralizationStatistics)
	 * System.out.println("-- Number of New Entities [" +
	 * entityGeneralizedNumber + "] (= Generalization)"); }
	 */

	@Override
	protected void updateSub(String updateSource) {

		/*
		 * System.out.println("-- Update Model adding (" +
		 * updateControl.getUpdateListSize() + ") sequences from \"" +
		 * updateSource + "\"");
		 */

		mActivityControl.update(mUpdateControl.getSequenceListToUpdate());
	}
}
