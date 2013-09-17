package lbd.FSNER.Filter;

import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;

public class FtrAffix extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	public static enum AffixType {Prefix, Suffix, Infix};
	protected AffixType mAffixType;

	protected HashMap<String, Object> mAffixMap;
	protected int mAffixSize;

	protected static final int MINIMUM_TERM_SIZE = 4;//4 (Standard)

	public FtrAffix(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator,
			AffixType affixType, int affixSize) {

		super(ClassName.getSingleName(FtrAffix.class.getName()) +
				".AfxTp:" + affixType.name() + ".AfxSz:"+affixSize,
				preprocessingTypeNameIndex, scoreCalculator);

		mAffixMap = new HashMap<String, Object>();
		//this.commonFilterName = "Ort" + preprocessingTypeNameIndex;
		//this.commonFilterName = "Wrd" + preprocessingTypeNameIndex;

		this.mAffixType = affixType;
		this.mAffixSize = affixSize;
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
		if(LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index))) {
			generateAffixes(sequenceLabelProcessed.getTerm(index));
		}
	}

	public void generateAffixes(String term) {

		String termAffix = generateAffix(mAffixType, term, mAffixSize);

		if(termAffix.length() == mAffixSize) {
			mAffixMap.put(termAffix, null);
		}
	}

	public static String generateAffix(AffixType affixType, String term, int affixSize) {

		if(term.length() >= MINIMUM_TERM_SIZE && term.length() > affixSize + 2) {
			if(affixType == AffixType.Prefix) {
				return(generatePrefix(term, affixSize));
			} else if(affixType == AffixType.Suffix) {
				return(generateSuffix(term, affixSize));
			} else if(affixType == AffixType.Infix && 2 * affixSize < term.length()) {
				return(generateInfix(term, affixSize));
			}
		}

		return(Symbol.EMPTY);
	}

	public static String generatePrefix(String term, int affixSize) {
		return(term.substring(0, affixSize));
	}

	public static String generateSuffix(String term, int affixSize) {
		return(term.substring(term.length() - affixSize));
	}

	public static String generateInfix(String term, int affixSize) {

		int meanIndex = term.length()/2;

		return(term.substring(meanIndex - affixSize, meanIndex + affixSize));
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
