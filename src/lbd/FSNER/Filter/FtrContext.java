package lbd.FSNER.Filter;

import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Filter.Component.Context;
import lbd.FSNER.Filter.Component.Context.ContextType;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.CommonEnum.Flexibility;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.DataSequence;

public class FtrContext extends AbstractFilter{

	private static final long serialVersionUID = 1L;
	protected static final int MAX_CONTEXT_WINDOW_SIDE_SIZE = 10;

	//-- It is common use for any Context Filter. Only to optimize memory stored.
	protected HashMap<Integer, Integer> contextDataMapForId;
	protected HashMap<Integer, Context> contextMap;

	protected Flexibility contextFlexibility;
	protected ContextType contextType;
	protected int windowSize;

	public FtrContext(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator,
			ContextType contextType, int windowSize, Flexibility contextFlexibility) {

		super(ClassName.getSingleName(FtrContext.class.getName()) +
				".Cxt:" + contextType.name() + ".Ws:" + windowSize +
				".Flx:" + contextFlexibility.name(),
				preprocessingTypeNameIndex, scoreCalculator);

		this.contextType = contextType;
		this.windowSize = windowSize;
		this.contextFlexibility = contextFlexibility;
		//this.commonFilterName = "Cxt." + contextType.name() + CommonTag.DOT + preprocessingTypeNameIndex;

		if(contextDataMapForId == null) {
			contextDataMapForId = new HashMap<Integer, Integer>();
			contextMap = new HashMap<Integer, Context>();
		}

		if(!contextMap.containsKey(getContextKey())) {
			contextDataMapForId.put(getContextKey(), mId);
			contextMap.put(getContextKey(), new Context(MAX_CONTEXT_WINDOW_SIDE_SIZE, contextFlexibility));
		}
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {

		int label = sequenceLabelProcessed.getLabel(index);

		//-- Add only one time for the context filter created with the specific affixType.
		if(contextDataMapForId.get(getContextKey()) == mId && LabelEncoding.isEntity(label)) {
			contextMap.get(getContextKey()).addAsContext(sequenceLabelProcessed, index);
		}
	}

	@Override
	public void loadActionAfterSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(SequenceLabel sequenceProcessedLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSequenceInstanceIdSub(DataSequence pSequence, SequenceLabel sequenceLabelProcessed, int index) {

		String id = Symbol.EMPTY;

		Context context = contextMap.get(getContextKey());
		int contextId = context.getContextId(sequenceLabelProcessed, index,
				contextType, windowSize, contextFlexibility);

		if(contextId > -1) {
			id = "id:" + this.mId + ".ctxId:" + contextId + ".ctxType:" +
					contextType.name() + ".wS:" + windowSize + ".flx:" + contextFlexibility;
		}

		return (id);
	}

	public int getContextKey() {
		return(mPreprocessingTypeIndex);
	}

}
