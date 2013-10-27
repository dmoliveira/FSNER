package lbd.FSNER.Model;

import java.util.ArrayList;
import java.util.List;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.SimpleFilterProbability;
import lbd.FSNER.Filter.ScoreCalculatorModel.FSCMMultiFilter;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public abstract class AbstractMetaFilter extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	public enum MetaFilterType {Multi, Translated, Window};

	protected List<AbstractFilter> mFilterList;
	protected boolean mIsToLoadAsSimpleFilter;
	protected String mIdTag;

	public AbstractMetaFilter(String pActivityName, int pPreprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel pScoreCalculator) {

		super(pActivityName, pPreprocessingTypeNameIndex, pScoreCalculator);

		mFilterList = new ArrayList<AbstractFilter>();
		mIsToLoadAsSimpleFilter = false;
		mIdTag = pActivityName;

		if(pScoreCalculator instanceof FSCMMultiFilter) {
			((FSCMMultiFilter)pScoreCalculator).setFilterList(mFilterList);
		}
	}

	public AbstractMetaFilter(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator,
			List<AbstractFilter> filterList) {

		super(generateActivityName(filterList), preprocessingTypeNameIndex, scoreCalculator);

		this.mFilterList = filterList;
		mFilterClassName = generateCommonFilterName(filterList);

		if(scoreCalculator instanceof FSCMMultiFilter) {
			((FSCMMultiFilter)scoreCalculator).setFilterList(filterList);
		}
	}

	public void addFilter(AbstractFilter filter) {
		mFilterList.add(filter);
	}

	public void startMetaFilter(String dataProcessorName) {

		this.mActivityName = mIdTag + generateActivityName(mFilterList) +
				Symbol.CURLY_BRACKET_LEFT + "Fs:" +
				mFilterList.size() + Symbol.CURLY_BRACKET_RIGHT;
		this.mFilterClassName = generateCommonFilterName(mFilterList);
		this.setProbabilityFilter(new SimpleFilterProbability());

		setPreprocessingTypeName(dataProcessorName);
	}

	@Override
	public void initialize() {
		if(mIsToLoadAsSimpleFilter) {
			for(AbstractFilter filter : mFilterList) {
				filter.initialize();
			}
		}
	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		if(mIsToLoadAsSimpleFilter) {
			for(AbstractFilter filter : mFilterList) {
				filter.loadActionBeforeSequenceSetIteration();
			}
		}
	}

	@Override
	public void loadActionBeforeSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		if(mIsToLoadAsSimpleFilter) {
			for(AbstractFilter filter : mFilterList) {
				filter.loadActionBeforeSequenceIteration(sequenceLabelProcessed);
			}
		}
	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {
		if(mIsToLoadAsSimpleFilter) {
			for(AbstractFilter filter : mFilterList) {
				filter.loadTermSequence(sequenceLabelProcessed, index);
			}
		}
	}

	@Override
	public void loadActionAfterSequenceIteration(SequenceLabel sequenceLabelProcessed) {
		if(mIsToLoadAsSimpleFilter) {
			for(AbstractFilter filter : mFilterList) {
				filter.loadActionAfterSequenceIteration(sequenceLabelProcessed);
			}
		}

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		if(mIsToLoadAsSimpleFilter) {
			for(AbstractFilter filter : mFilterList) {
				filter.loadActionAfterSequenceSetIteration();
			}
		}

	}

	@Override
	public void adjust(SequenceLabel sequenceProcessedLabel) {
		if(mIsToLoadAsSimpleFilter) {
			for(AbstractFilter filter : mFilterList) {
				filter.adjust(sequenceProcessedLabel);
			}
		}
	}

	@Override
	protected String getSequenceInstanceIdSub(ISequence pSequence, SequenceLabel pSequenceLabelProcessed, int pIndex) {

		int vNumberFilterActivated = 0;

		String vId = Symbol.EMPTY;
		String vIntermediaryId = Symbol.EMPTY;
		String vInstanceId = Symbol.EMPTY;

		for(AbstractFilter cFilter : mFilterList) {

			vInstanceId = cFilter.getSequenceInstanceId(pSequence, pSequenceLabelProcessed, pIndex);

			if(!vInstanceId.isEmpty()) {
				vIntermediaryId += vInstanceId + Symbol.PLUS;
				vNumberFilterActivated++;
			}
		}

		if(vNumberFilterActivated == mFilterList.size()) {
			vId = vIntermediaryId.substring(0, vIntermediaryId.length()-1);
		}

		return (vId);
	}

	protected static String generateCommonFilterName(List<AbstractFilter> filterList) {

		String commonFilterName = Symbol.EMPTY;

		for(int i = 0; i < filterList.size(); i++) {
			commonFilterName += filterList.get(i).getFilterClassName() +
					((i < filterList.size()-1)? Symbol.PLUS : Symbol.EMPTY);
		}

		return(Symbol.PARENTHESE_LEFT + commonFilterName + Symbol.PARENTHESE_RIGHT);
	}

	protected static String generateActivityName(List<AbstractFilter> filterList) {

		String activityName = Symbol.EMPTY;

		for(int i = 0; i < filterList.size(); i++) {
			activityName += filterList.get(i).getActivityName() +
					((i < filterList.size()-1)? Symbol.PLUS : Symbol.EMPTY);
		}

		return(Symbol.PARENTHESE_LEFT + activityName + Symbol.PARENTHESE_RIGHT);
	}

	public static String getFilterClassNames(List<AbstractFilter> pFilterList) {
		String vFilterClassNames = Symbol.EMPTY;

		for(AbstractFilter cFilter : pFilterList) {
			vFilterClassNames += cFilter.getActivityName() + ",";
		}

		return (vFilterClassNames.isEmpty())? vFilterClassNames : vFilterClassNames.substring(0, vFilterClassNames.length() - 1);
	}

	public void setLoadAsSimpleFilter(boolean loadAsSimpleFilter) {
		this.mIsToLoadAsSimpleFilter = loadAsSimpleFilter;
	}

	public boolean isLoadasSimpleFilter() {
		return(mIsToLoadAsSimpleFilter);
	}
}
