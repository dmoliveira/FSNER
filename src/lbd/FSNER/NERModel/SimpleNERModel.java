package lbd.FSNER.NERModel;

import java.util.ArrayList;
import java.util.List;

import lbd.FSNER.Collection.DataCollection;
import lbd.FSNER.Component.Statistic.SimpleFilterProbability;
import lbd.FSNER.Configuration.FilterParameters.FilterType;
import lbd.FSNER.DataPreprocessor.DPLowerCase;
import lbd.FSNER.DataPreprocessor.DPPlainSequence;
import lbd.FSNER.DataPreprocessor.DPStopWord;
import lbd.FSNER.Filter.FtrAffix;
import lbd.FSNER.Filter.FtrEntityContext;
import lbd.FSNER.Filter.FtrEntityTerm;
import lbd.FSNER.Filter.FtrGazetteer;
import lbd.FSNER.Filter.FtrState;
import lbd.FSNER.Filter.FtrToken;
import lbd.FSNER.Filter.FtrTokenLength;
import lbd.FSNER.Filter.FtrWindow;
import lbd.FSNER.Filter.FtrWindow.WindowType;
import lbd.FSNER.Filter.FtrWordType;
import lbd.FSNER.Filter.MultiFilter;
import lbd.FSNER.Filter.ScoreCalculatorModel.FSCMNoScore;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Model.AbstractNERModel;
import lbd.FSNER.Utils.Symbol;
import lbd.FSNER.Utils.Annotations.DefaultValue;

public class SimpleNERModel extends AbstractNERModel {

	private static final long serialVersionUID = 1L;

	protected boolean displayGeneralizationStatistics = false;

	public SimpleNERModel(DataCollection pDataCollection) {
		super(pDataCollection);
	}

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
			if (mFilterParameters.isFilterActive(FilterType.EntityTerm)) {
				addEntityTermFilter(i);
			}

			/** Dictionary Filter **/
			if (i == 0 && mFilterParameters.isFilterActive(FilterType.Dictionary)) {
				addGazetteerFilters(i);
			}

			if (mFilterParameters.isFilterActive(FilterType.Context)) {
				//addOldContextFilters(i);
				addContextFilters(i);
			}

			/** Orthographic Filter Level **/
			// -- Affix Filter (CommonClass:Afx)
			if (mFilterParameters.isFilterActive(FilterType.Affix)) {
				addOrthographicFilters(i);
			}

			// -- Spell Filters
			if (i == 0 && mFilterParameters.isFilterActive(FilterType.WordType)) {
				addSpellFilters(i);
			}

			/** Token Length **/
			//addTokenLengthFilter(i);

			/** State Filter **/
			if (mFilterParameters.isFilterActive(FilterType.State)) {
				addStateFilter(i);
			}

			/** WindowFilter **/
			if(mFilterParameters.isFilterActive(FilterType.Window)) {
				//List<AbstractFilter> vTokenWindowFilterList = createWindowFilters(i, FtrToken.class.getName(), 2, WindowType.Both);
				//List<AbstractFilter> vAfixWindowFilterList = createWindowFilters(i, FtrAffix.class.getName(), 2, WindowType.Both);
				List<AbstractFilter> vWordTypeWindowFilterList = createWindowFilters(i, FtrWordType.class.getName(), 2, WindowType.Both);
				//List<AbstractFilter> vTokenLengthWindowFilterList = createWindowFilters(i, FtrTokenLength.class.getName(), 2, WindowType.Both);
				List<AbstractFilter> vStateLengthWindowFilterList = createWindowFilters(i, FtrState.class.getName(), 0, WindowType.Prefix);
				//List<AbstractFilter> vContextFilterList = createWindowFilters(i, FtrEntityContext.class.getName(), 0, WindowType.Prefix);

				//-- MetaFilters
				//addMetaFilters(i, FtrWordType.class.getName(), vContextFilterList);
				//List<AbstractFilter> vMultiFilterWordTypeAndContext = createMetaFilters(i, FtrWordType.class.getName(), vContextFilterList);

				//mActivityControl.addActivity(new ArrayList<AbstractActivity>(vMultiFilterWordTypeAndContext));

			}
		}

		// -- Add probability filter element
		for (AbstractFilter filter : mActivityControl.getFilterList()) {
			filter.setProbabilityFilter(new SimpleFilterProbability());
		}
	}

	private List<AbstractFilter> createMetaFilters(int i, String pFilterClassName, List<AbstractFilter> pFilterListToCombine) {
		List<AbstractFilter> vMetaFiltersList = new ArrayList<AbstractFilter>();
		for(AbstractFilter cBasicFilter : mActivityControl.getFiltersByClassName(pFilterClassName)) {
			for(AbstractFilter cFilterToCombine : pFilterListToCombine) {
				List<AbstractFilter> vFilterList = new ArrayList<AbstractFilter>();
				vFilterList.add(cBasicFilter);
				vFilterList.add(cFilterToCombine);
				vMetaFiltersList.add(new MultiFilter(i, new FSCMNoScore(), vFilterList));
			}
		}

		return vMetaFiltersList;
	}

	private List<AbstractFilter> createWindowFilters(int i, String pFilterClassName, int pWindowSize, WindowType pWindowType) {

		List<AbstractFilter> vWindowFilterList = new ArrayList<AbstractFilter>();

		for(AbstractFilter cFilter : mActivityControl.getFiltersByClassName(pFilterClassName)) {

			List<AbstractFilter> vFilterList = new ArrayList<AbstractFilter>();
			vFilterList.add(cFilter);

			if(pWindowType != WindowType.Suffix) {
				AbstractFilter vWindowFilter = new FtrWindow(i, new FSCMNoScore(), pWindowSize, WindowType.Prefix, vFilterList);
				vWindowFilter.setFilterState(FilterState.Auxiliary);
				vWindowFilterList.add(vWindowFilter);
			}

			if(pWindowType != WindowType.Prefix) {
				AbstractFilter vWindowFilter = new FtrWindow(i, new FSCMNoScore(), pWindowSize, WindowType.Suffix, vFilterList);
				vWindowFilter.setFilterState(FilterState.Auxiliary);
				vWindowFilterList.add(vWindowFilter);
			}

			if(pWindowType == WindowType.Both) {
				AbstractFilter vWindowFilter = new FtrWindow(i, new FSCMNoScore(), pWindowSize, WindowType.Both, vFilterList);
				vWindowFilter.setFilterState(FilterState.Auxiliary);
				vWindowFilterList.add(vWindowFilter);
			}
		}

		return vWindowFilterList;
	}

	protected void addEntityTermFilter(int i) {
		mActivityControl.addActivity(new FtrEntityTerm(i));

		AbstractFilter vTokenFilter = new FtrToken(i);
		vTokenFilter.setFilterState(FilterState.Auxiliary);
		mActivityControl.addActivity(vTokenFilter);
	}

	protected void addGazetteerFilters(int i) {

		String vDictionaryDirectory = mDataCollection.mDictionaryAddress + mDataCollection.mDictionaryName + Symbol.SLASH;

		for (String cFilenameAddress : lbd.FSNER.Utils.FileUtils.getFileList(vDictionaryDirectory)) {
			AbstractFilter vFilter = new FtrGazetteer(vDictionaryDirectory + cFilenameAddress, i, new FSCMNoScore());
			vFilter.setFilterState(FilterState.Auxiliary);
			mActivityControl.addActivity(vFilter);
		}
	}

	protected void addContextFilters(int i) {
		//mActivityControl.addActivity(new FtrEntityContext(i, 1));
		mActivityControl.addActivity(new FtrEntityContext(i, 2));
		mActivityControl.addActivity(new FtrEntityContext(i, 3));
	}

	@DefaultValue(value="affixTypeIndex = 0; affixSize = 3; affixSize > 1")
	protected void addOrthographicFilters(int i) {
		for (int affixTypeIndex = 0; affixTypeIndex < FtrAffix.AffixType
				.values().length; affixTypeIndex++) {
			for (int affixSize = 4; affixSize > 1; affixSize--) {

				mActivityControl
				.addActivity(new FtrAffix(i, new FSCMNoScore(),
						FtrAffix.AffixType.values()[affixTypeIndex], affixSize));

				mActivityControl.getFilterList()
				.get(mActivityControl.getFilterList().size() - 1)
				.setFilterState(FilterState.Auxiliary);
			}
		}
	}

	protected void addSpellFilters(int i) {
		ArrayList<AbstractFilter> spellFilters = new ArrayList<AbstractFilter>();
		spellFilters.add(new FtrWordType(i, new FSCMNoScore()));
		//spellFilters.add(new FtrCapitalizedTerms(i, new FSCMNoScore()));
		//spellFilters.add(new FtrAllCapitalized(i, new FSCMNoScore()));
		//spellFilters.add(new FtrCapitalizedPossibleTerms(i, new
		//		FSCMNoScore()));
		//spellFilters.add(new FtrHasDigit(i, new FSCMNoScore()));
		//spellFilters.add(new FtrHasPontuaction(i, new FSCMNoScore()));

		//activityControl.addActivity(new FtrPosition(i, new FSCMNoScore()));
		//activityControl.getFilterList().get(activityControl.getFilterList().size()-1).setFilterState(FilterState.Auxiliary);
		//activityControl.addActivity(new FtrTermLength(i, new FSCMNoScore()));

		for (AbstractFilter filter : spellFilters) {
			mActivityControl.addActivity(filter);
			//mActivityControl.getFilterList().get(mActivityControl.getFilterList().size() - 1).setFilterState(FilterState.Auxiliary);
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

	protected void addTokenLengthFilter(int i) {
		FtrTokenLength vFilter = new FtrTokenLength(i);
		vFilter.setFilterState(FilterState.Auxiliary);
		mActivityControl.addActivity(vFilter);
	}

	@DefaultValue(value="vNumberPreviousState: {1,2,3}; vIsToConsiderStatePerTerm: {true}")
	protected void addStateFilter(int i) {

		int [] vNumberPreviousState = {3};
		boolean [] vIsToConsiderStatePerTerm = {false};

		for(int cNumberPreviousState : vNumberPreviousState) {
			for(boolean cIsToConsiderStatePerTerm : vIsToConsiderStatePerTerm) {

				mActivityControl
				.addActivity(new FtrState(i, cNumberPreviousState, cIsToConsiderStatePerTerm, new FSCMNoScore()));

				mActivityControl.getFilterList()
				.get(mActivityControl.getFilterList().size() - 1)
				.setFilterState(FilterState.Auxiliary);
			}
		}
	}

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
