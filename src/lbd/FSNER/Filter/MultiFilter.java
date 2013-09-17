package lbd.FSNER.Filter;

import java.util.ArrayList;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.FPESimple;
import lbd.FSNER.Filter.ScoreCalculatorModel.FSCMMultiFilter;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.Symbol;

public class MultiFilter extends AbstractFilter{

	private static final long serialVersionUID = 1L;
	protected ArrayList<AbstractFilter> filterList;
	protected boolean loadAsSimpleFilter;

	public MultiFilter(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {

		super(Symbol.EMPTY, preprocessingTypeNameIndex, scoreCalculator);

		filterList = new ArrayList<AbstractFilter>();
		loadAsSimpleFilter = false;

		if(scoreCalculator instanceof FSCMMultiFilter) {
			((FSCMMultiFilter)scoreCalculator).setFilterList(filterList);
		}
	}

	public MultiFilter(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, ArrayList<AbstractFilter> filterList) {

		super(generateActivityName(filterList), preprocessingTypeNameIndex, scoreCalculator);

		this.filterList = filterList;
		this.mCommonFilterName = generateCommonFilterName(filterList);

		if(scoreCalculator instanceof FSCMMultiFilter) {
			((FSCMMultiFilter)scoreCalculator).setFilterList(filterList);
		}
	}

	public void addFilter(AbstractFilter filter) {
		filterList.add(filter);
	}

	public void startMultiFilter(String dataProcessorName) {

		this.mActivityName = generateActivityName(filterList) +
				Symbol.CURLY_BRACKET_LEFT +"Fs:" +
				filterList.size() + Symbol.CURLY_BRACKET_RIGHT;
		this.mCommonFilterName = generateCommonFilterName(filterList);
		this.setProbabilityFilterElement(new FPESimple());

		setPreprocessingTypeName(dataProcessorName);
	}

	@Override
	public void initialize() {
		if(loadAsSimpleFilter) {
			for(AbstractFilter filter : filterList) {
				filter.initialize();
			}
		}
	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		if(loadAsSimpleFilter) {
			for(AbstractFilter filter : filterList) {
				filter.loadActionBeforeSequenceSetIteration();
			}
		}
	}

	@Override
	public void loadActionBeforeSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		if(loadAsSimpleFilter) {
			for(AbstractFilter filter : filterList) {
				filter.loadActionBeforeSequenceIteration(sequenceLabelProcessed);
			}
		}
	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {
		if(loadAsSimpleFilter) {
			for(AbstractFilter filter : filterList) {
				filter.loadTermSequence(sequenceLabelProcessed, index);
			}
		}
	}

	@Override
	public void loadActionAfterSequenceIteration(SequenceLabel sequenceLabelProcessed) {
		if(loadAsSimpleFilter) {
			for(AbstractFilter filter : filterList) {
				filter.loadActionAfterSequenceIteration(sequenceLabelProcessed);
			}
		}

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		if(loadAsSimpleFilter) {
			for(AbstractFilter filter : filterList) {
				filter.loadActionAfterSequenceSetIteration();
			}
		}

	}

	@Override
	public void adjust(SequenceLabel sequenceProcessedLabel) {
		if(loadAsSimpleFilter) {
			for(AbstractFilter filter : filterList) {
				filter.adjust(sequenceProcessedLabel);
			}
		}
	}

	@Override
	protected String getSequenceInstanceIdSub(SequenceLabel sequenceLabelProcessed, int index) {

		int numberFilterActivated = 0;

		String id = Symbol.EMPTY;
		String intermediaryId = Symbol.EMPTY;
		String instanceId = Symbol.EMPTY;

		for(AbstractFilter filter : filterList) {

			instanceId = filter.getSequenceInstanceId(sequenceLabelProcessed, index);

			if(!instanceId.isEmpty()) {
				intermediaryId += instanceId + Symbol.PLUS;
				numberFilterActivated++;
			}
		}

		if(numberFilterActivated == filterList.size()) {
			id = "MltFltId:" + this.mId + "(" + intermediaryId.substring(0, intermediaryId.length()-1) + ")";
		}

		return (id);
	}

	protected static String generateCommonFilterName(ArrayList<AbstractFilter> filterList) {

		String commonFilterName = Symbol.EMPTY;

		for(int i = 0; i < filterList.size(); i++) {
			commonFilterName += filterList.get(i).getCommonFilterName() +
					((i < filterList.size()-1)? Symbol.PLUS : Symbol.EMPTY);
		}

		return(Symbol.PARENTHESE_LEFT + commonFilterName + Symbol.PARENTHESE_RIGHT);
	}

	protected static String generateActivityName(ArrayList<AbstractFilter> filterList) {

		String activityName = Symbol.EMPTY;

		for(int i = 0; i < filterList.size(); i++) {
			activityName += filterList.get(i).getActivityName() +
					((i < filterList.size()-1)? Symbol.PLUS : Symbol.EMPTY);
		}

		return(Symbol.PARENTHESE_LEFT + activityName + Symbol.PARENTHESE_RIGHT);
	}

	public void setLoadAsSimpleFilter(boolean loadAsSimpleFilter) {
		this.loadAsSimpleFilter = loadAsSimpleFilter;
	}

	public boolean isLoadasSimpleFilter() {
		return(loadAsSimpleFilter);
	}
}
