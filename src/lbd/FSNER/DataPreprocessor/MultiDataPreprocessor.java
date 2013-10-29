package lbd.FSNER.DataPreprocessor;

import java.util.ArrayList;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.DataProcessor.Component.PreprocessData;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;
import lbd.data.handler.SequenceSegment;

public class MultiDataPreprocessor extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;

	protected ArrayList<AbstractDataPreprocessor> mDataPreproccessorList;

	public MultiDataPreprocessor(ArrayList<AbstractDataPreprocessor> dataPreproccessorList) {

		super(getActivityName(dataPreproccessorList), null);

		this.mDataPreproccessorList = dataPreproccessorList;
	}

	@Override
	public ISequence preprocessingSequence(ISequence pSequence) {

		ISequence sequenceLabel = new SequenceSegment();

		for(int i = 0; i < pSequence.length(); i++) {
			if(!pSequence.getToken(i).isEmpty()) {
				sequenceLabel.add(pSequence.getToken(i), pSequence.getLabel(i));
			} else {
				sequenceLabel.add(Symbol.EMPTY, Parameters.DataHandler.mLabelEncoding.getOutsideLabel().ordinal());
			}
		}

		return (sequenceLabel);
	}

	@Override
	public String preprocessingToken(String pTerm, int pLabel) {

		String vPreproccessedTerm = pTerm;

		for(AbstractDataPreprocessor cDataProcessor : mDataPreproccessorList) {
			vPreproccessedTerm = PreprocessData.preproccessTerm(vPreproccessedTerm, pLabel, mDataPreproccessorList, cDataProcessor.getActivityName());
		}

		return vPreproccessedTerm;
	}

	@Override
	public void initialize() {

		for(int i = 0; i < mDataPreproccessorList.size(); i++) {
			mDataPreproccessorList.get(i).initialize();
		}
	}

	public static String getActivityName(ArrayList<AbstractDataPreprocessor> dataPreproccessorList) {

		String activityName = "";

		for(AbstractDataPreprocessor dataPreprocessor : dataPreproccessorList) {
			activityName += dataPreprocessor.getActivityName()+"+";
		}

		return(activityName.substring(0, activityName.length()-1));
	}

}
