package lbd.FSNER.Filter;

import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.DataPreprocessor.DPCapitalizationTermsOnly;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;

public class FtrCapitalizedPossibleTerms extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected HashMap<String, Object> termsCapitalized;

	public FtrCapitalizedPossibleTerms(
			int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {

		super(ClassName.getSingleName(FtrCapitalizedPossibleTerms.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		termsCapitalized = new HashMap<String, Object>();
		this.mCommonFilterName = "Ort" + preprocessingTypeNameIndex;
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
		if(index > 0 && LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index)) &&
				DPCapitalizationTermsOnly.isCapitalized(sequenceLabelProcessed.getTerm(index))) {
			termsCapitalized.put(sequenceLabelProcessed.getTerm(index).toLowerCase(), null);
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
	protected String getSequenceInstanceIdSub(
			SequenceLabel sequenceLabelProcessed, int index) {

		return ((termsCapitalized.containsKey(sequenceLabelProcessed.getTerm(index).toLowerCase()))?
				"id:" + this.mId + Symbol.DOT + sequenceLabelProcessed.getTerm(index).toLowerCase() : Symbol.EMPTY);
	}

}
