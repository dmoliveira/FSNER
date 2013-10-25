package lbd.FSNER.Filter;

import java.util.HashMap;
import java.util.Map;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.DataSequence;

public class FtrAffix extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	public static enum AffixType {Prefix, Suffix, Infix};
	protected AffixType mAffixType;

	protected Map<String, Object> mAffixMap;
	protected int mAffixSize;

	protected static final int MINIMUM_TERM_SIZE = Parameters.Filter.Affix.minimumTermSize;

	public FtrAffix(int pPreprocessingTypeNameIndex, AbstractFilterScoreCalculatorModel pScoreCalculator,
			AffixType pAffixType, int pAffixSize) {

		super(ClassName.getSingleName(FtrAffix.class.getName()) + ".AfxTp:" + pAffixType.name() + ".AfxSz:"+pAffixSize,
				pPreprocessingTypeNameIndex, pScoreCalculator);

		mAffixMap = new HashMap<String, Object>();
		mAffixType = pAffixType;
		mAffixSize = pAffixSize;
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
	public void loadActionBeforeSequenceIteration(SequenceLabel pSequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(SequenceLabel pSequenceLabelProcessed, int pIndex) {
		if(LabelEncoding.isEntity(pSequenceLabelProcessed.getLabel(pIndex))) {
			generateAffixes(pSequenceLabelProcessed.getTerm(pIndex));
		}
	}

	public void generateAffixes(String pTerm) {

		String vTermAffix = generateAffix(mAffixType, pTerm, mAffixSize);

		if(vTermAffix.length() == mAffixSize) {
			mAffixMap.put(vTermAffix, null);
		}
	}

	public static String generateAffix(AffixType pAffixType, String pTerm, int pAffixSize) {

		if(pTerm.length() >= MINIMUM_TERM_SIZE && pTerm.length() > pAffixSize + 2) {
			if(pAffixType == AffixType.Prefix) {
				return(generatePrefix(pTerm, pAffixSize));
			} else if(pAffixType == AffixType.Suffix) {
				return(generateSuffix(pTerm, pAffixSize));
			} else if(pAffixType == AffixType.Infix && 2 * pAffixSize < pTerm.length()) {
				return(generateInfix(pTerm, pAffixSize));
			}
		}

		return(Symbol.EMPTY);
	}

	public static String generatePrefix(String pTerm, int pAffixSize) {
		return(pTerm.substring(0, pAffixSize));
	}

	public static String generateSuffix(String term, int affixSize) {
		return(term.substring(term.length() - affixSize));
	}

	public static String generateInfix(String pTerm, int pAffixSize) {

		int vMeanIndex = pTerm.length()/2;

		return(pTerm.substring(vMeanIndex - pAffixSize, vMeanIndex + pAffixSize));
	}

	@Override
	public void loadActionAfterSequenceIteration(SequenceLabel pSequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(SequenceLabel pSequenceProcessedLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(DataSequence pSequence,
			SequenceLabel pSequenceLabelProcessed, int pIndex) {

		String vId = Symbol.EMPTY;
		String vTerm = pSequenceLabelProcessed.getTerm(pIndex);
		String vTermAffix = generateAffix(mAffixType, vTerm, mAffixSize);

		if(mAffixMap.containsKey(vTermAffix)) {
			vId = "id:" + this.mId + ".Afx:" + vTermAffix + ".AfxTp:" + mAffixType.name();
		}

		return (vId);
	}
}
