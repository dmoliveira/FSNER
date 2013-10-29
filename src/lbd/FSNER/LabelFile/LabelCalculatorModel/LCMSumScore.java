package lbd.FSNER.LabelFile.LabelCalculatorModel;

import java.util.List;
import java.util.Map;

import lbd.FSNER.Component.Statistic.FilterProbabilityHandler;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Model.AbstractLabelFileLabelCalculatorModel;
import lbd.FSNER.Model.AbstractTermRestrictionChecker;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;
import lbd.fsner.label.encoding.Label;

public class LCMSumScore extends AbstractLabelFileLabelCalculatorModel{

	private static final long serialVersionUID = 1L;

	//-- Filter Restriction
	protected double vFilterProbability = 0.0;
	protected double vAlpha = 0.0; //0.8
	protected final double SCORE_THRESHOLD = 0;
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;
	protected boolean vShowProbabilityForLabel = false;

	public LCMSumScore(AbstractTermRestrictionChecker pTermRestrictionChecker) {
		super(pTermRestrictionChecker);
	}

	@Override
	public int calculateMostProbablyLabelSub(int pIndex,
			ISequence pSequence,
			Map<String, ISequence> pProccessedSequenceMap,
			List<AbstractDataPreprocessor> pDataProcessorList,
			List<AbstractFilter> pFilterList) {

		int vMostProbablyLabel = Parameters.DataHandler.mLabelEncoding.getOutsideLabel().ordinal();
		int vLastFilterPreprocessingTypeNameIndex = -1;

		boolean vWasFilterActivated = false;

		String vFilterInstanceId;
		String vTerm = Symbol.EMPTY;

		ISequence vPreprocessedSequence = null;
		FilterProbabilityHandler vFilterProbability = null;
		AbstractDataPreprocessor vDataPreprocessor = null;

		double vFilterLabelMaxProbability;
		int vFilterLabelChoosed;

		for(AbstractFilter cFilter : pFilterList) {

			if(vLastFilterPreprocessingTypeNameIndex != cFilter.getFilterPreprocessingTypeIndex()) {

				vDataPreprocessor = pDataProcessorList.get(cFilter.getFilterPreprocessingTypeIndex());
				vPreprocessedSequence = pProccessedSequenceMap.get(cFilter.getPreprocesingTypeName());
				vLastFilterPreprocessingTypeNameIndex = cFilter.getFilterPreprocessingTypeIndex();

				vTerm = vPreprocessedSequence.getToken(pIndex);
			}

			if(cFilter.getFilterState() == FilterState.Active &&
					!mIsUnrealibleSituation &&
					vDataPreprocessor.getCommonTokenProbability(vTerm) < COMMON_TERM_PERCENTAGE_THRESHOLD) {// <

				//-- Get filter instance id determined by the index
				vFilterProbability = cFilter.getFilterProbability();
				vFilterInstanceId = cFilter.getSequenceInstanceId(pSequence, vPreprocessedSequence, pIndex);

				if(!vFilterInstanceId.isEmpty() &&	vFilterProbability.getProbability(vFilterInstanceId) > this.vAlpha) {

					vFilterLabelMaxProbability = 0;
					vFilterLabelChoosed = Parameters.DataHandler.mLabelEncoding.getOutsideLabel().ordinal();
					for(int i = 0; i < Parameters.DataHandler.mLabelEncoding.getAlphabetSize(); i++) {
						//TODO: Test normalization only when prob. > 0
						mLabelProbability[i] += vFilterProbability.getProbability(vFilterInstanceId, i);
						mNormalizationFactor[i]++;
						if(vFilterLabelMaxProbability < vFilterProbability.getProbability(vFilterInstanceId, i)) {
							vFilterLabelMaxProbability = vFilterProbability.getProbability(vFilterInstanceId, i);
							vFilterLabelChoosed = i;
						}
					}

					vWasFilterActivated = true;

					vFilterProbability.addToFilterStatisticForAssignedLabels(vTerm,
							Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(vPreprocessedSequence.getLabel(pIndex))),
							Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(vFilterLabelChoosed)));
				}
			}
		}

		if(vWasFilterActivated) {
			vMostProbablyLabel = getMostProbablyLabel(vTerm, mLabelProbability, mNormalizationFactor);
		}

		return (vMostProbablyLabel);
	}

	protected int getMostProbablyLabel(String term, double [] labelProbability, int [] normalizationFactor) {

		int mostProbablyLabel = Parameters.DataHandler.mLabelEncoding.getOutsideLabel().ordinal();
		double maxProbability = -1;
		double probability = -1;

		if(vShowProbabilityForLabel && term.length() > 2) {
			System.out.println("Term: " + term);
		}

		for(int i = 0; i < Parameters.DataHandler.mLabelEncoding.getAlphabetSize(); i++) {

			probability = labelProbability[i]/normalizationFactor[i];
			if(vShowProbabilityForLabel && term.length() > 2) {
				System.out.println("Prob(" + Label.getCanonicalLabel(i).getValue() + "): " + probability);
			}

			if(probability > maxProbability) {
				maxProbability = probability;
				mostProbablyLabel = i;
			}
		}

		if(vShowProbabilityForLabel && term.length() > 2) {
			System.out.println(">> Chosen Label: " + Label.getCanonicalLabel(mostProbablyLabel).getValue());
		}
		if(vShowProbabilityForLabel && term.length() > 2) {
			System.out.println("-----------------");
		}

		return((maxProbability >= vFilterProbability)? mostProbablyLabel : Parameters.DataHandler.mLabelEncoding.getOutsideLabel().ordinal());
	}

	public void setFilterProbability(double filterProbability) {
		this.vFilterProbability = filterProbability;
	}

	public void setAlpha(double alpha) {
		this.vAlpha = alpha;
	}

}
