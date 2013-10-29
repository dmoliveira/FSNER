package lbd.FSNER.Model;

import java.text.MessageFormat;

import lbd.FSNER.Component.Statistic.FilterProbabilityHandler;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Filter.ScoreCalculatorModel.FSCMNoScore;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public abstract class AbstractFilter extends AbstractActivity {

	private static final long serialVersionUID = 1L;

	protected String mFilterClassName;
	protected int mPreprocessingTypeIndex;
	protected String mPreprocessingTypeName;

	protected AbstractFilterScoreCalculatorModel mScoreCalculator;
	protected FilterProbabilityHandler mFilterProbability;

	public enum FilterState {Active, Auxiliary};
	public enum FilterStage {Train, Label, Update};

	protected FilterState mFilterState;
	protected static FilterStage mFilterStage;

	public AbstractFilter(String pActivityName, int pPreprocessingTypeIndex) {
		this(pActivityName, pPreprocessingTypeIndex, new FSCMNoScore());
	}

	public AbstractFilter(String pActivityName, int pPreprocessingTypeIndex, AbstractFilterScoreCalculatorModel pScoreCalculator) {

		super(pActivityName + Symbol.PLUS + ClassName.getSingleName(pScoreCalculator.getClass().getName()) +
				"[id"+ (_globalId+1) +"]");

		mFilterClassName = this.getClass().getName();
		mPreprocessingTypeIndex = pPreprocessingTypeIndex;
		mScoreCalculator = pScoreCalculator;
		mFilterState = FilterState.Active;
	}

	public abstract void loadActionBeforeSequenceSetIteration();

	public abstract void loadActionBeforeSequenceIteration(ISequence pSequenceLabelProcessed);

	public abstract void loadTermSequence(ISequence pSequenceLabelProcessed, int pIndex);

	public abstract void loadActionAfterSequenceIteration(ISequence pSequenceLabelProcessed);

	public abstract void loadActionAfterSequenceSetIteration();

	public abstract void adjust(ISequence pProcessedSequence);

	protected abstract String getSequenceInstanceIdSub(ISequence pSequence, ISequence pPreprocessedSequence, int pIndex);

	// pSequence contains original sequence with labels produced by FS-NER until state i - 1
	public String getSequenceInstanceId(ISequence pOriginalSequence, ISequence pPreprocessedSequence, int pIndex) {

		String vId = Symbol.EMPTY;

		if(!((String)pPreprocessedSequence.getToken(pIndex)).isEmpty()) {
			vId = getSequenceInstanceIdSub(pOriginalSequence, pPreprocessedSequence, pIndex);
		}

		return(vId);
	}

	public double calculateScore(ISequence pSequenceLabelProcessed, int pIndex) {
		return(mScoreCalculator.calculateScoreLabeling(pSequenceLabelProcessed, pIndex));
	}

	public FilterProbabilityHandler getFilterProbability() {
		return(mFilterProbability);
	}

	public void setProbabilityFilter(AbstractFilterProbability pFilterProbability) {
		mFilterProbability = new FilterProbabilityHandler(pFilterProbability);
	}

	public void printFilterProbabilityInstanceStatistics() {

		String vMessageFormat = "--- {0} --- Instances({1}) {2}";

		if(mFilterProbability.size() > 0) {

			System.out.println(MessageFormat.format(vMessageFormat, mActivityName,
					mFilterProbability.size(),
					mFilterProbability.getFilterStatisticForAssignedLabelsInTrain()));

			if(Debug.ActivityControl.printFilterInstanceStatistics) {
				mFilterProbability.printFilterInstanceStatistic(this);
			}
		}
	}

	public String getFilterClassName() {
		return(mFilterClassName);
	}

	public FilterState getFilterState() {
		return(mFilterState);
	}

	public void setFilterState(FilterState pFilterState) {
		mFilterState = pFilterState;
	}

	public static FilterStage getFilterStage() {
		return(mFilterStage);
	}

	public static void setFilterStage(FilterStage pFilterStage) {
		mFilterStage = pFilterStage;
	}

	public int getFilterPreprocessingTypeIndex() {
		return(mPreprocessingTypeIndex);
	}

	public void setPreprocessingTypeName(String pPreprocessingTypeName) {
		this.mPreprocessingTypeName = pPreprocessingTypeName;
	}

	public String getPreprocesingTypeName() {
		return(mPreprocessingTypeName);
	}

	public void clearFilterProbability() {
		mFilterProbability.clear();
	}
}
