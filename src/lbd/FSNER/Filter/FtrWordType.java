package lbd.FSNER.Filter;

import java.text.MessageFormat;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.data.handler.ISequence;

public class FtrWordType extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected enum WordType {AllCaps, Capitalized, Digit, NonAlpha, Common};

	public FtrWordType(int pPreprocessingTypeNameIndex,	AbstractFilterScoreCalculatorModel pScoreCalculator) {
		super(ClassName.getSingleName(FtrWordType.class.getName()), pPreprocessingTypeNameIndex, pScoreCalculator);
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
	public void loadActionBeforeSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(ISequence pPreprocessedSequence, int pIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(ISequence pSequence,
			ISequence pPreprocessedSequence, int pIndex) {

		String vToken = pSequence.getToken(pIndex);
		String vWordType = WordType.Common.name();

		if(!vToken.isEmpty()) {
			if(isAllCaps(vToken)) {
				vWordType = WordType.AllCaps.name();
			} else if(isCapitalized(vToken)) {
				vWordType = WordType.Capitalized.name();
			} else if(isDigit(vToken)) {
				vWordType = WordType.Digit.name();
			} else if(isNonAlpha(vToken)) {
				vWordType = WordType.NonAlpha.name();
			}
		}

		String vId = MessageFormat.format("id:{0}.WrdTp:{1}", mId, vWordType);

		return vId;
	}

	protected boolean isAllCaps(String pToken) {
		boolean vIsAllCaps = true;

		for(int i = 0; i < pToken.length(); i++) {
			if(Character.isLowerCase(pToken.charAt(i))) {
				vIsAllCaps = false;
				break;
			}
		}

		return vIsAllCaps;
	}

	protected boolean isCapitalized(String pToken) {
		boolean vIsCapitalized = true;

		if(Character.isUpperCase(pToken.charAt(0))) {
			for(int i = 1; i < pToken.length(); i++) {
				if(Character.isUpperCase(pToken.charAt(i))) {
					vIsCapitalized = false;
					break;
				}
			}
		} else {
			vIsCapitalized = false;
		}

		return vIsCapitalized;
	}

	protected boolean isDigit(String pToken) {
		if(pToken.length() == 0) {
			return false;
		}

		boolean vIsDigit = true;

		for(int i = 0; i < pToken.length(); i++) {
			if(!Character.isDigit(pToken.charAt(i)) && Character.isLetter(pToken.charAt(i))) {
				vIsDigit = false;
				break;
			}
		}

		return vIsDigit;
	}

	protected boolean isNonAlpha(String pToken) {
		if(pToken.length() == 0) {
			return false;
		}

		boolean vIsNonAlpha = true;

		for(int i = 0; i < pToken.length(); i++) {
			if(Character.isLetterOrDigit(pToken.charAt(i))) {
				vIsNonAlpha = false;
				break;
			}
		}

		return vIsNonAlpha;
	}
}
