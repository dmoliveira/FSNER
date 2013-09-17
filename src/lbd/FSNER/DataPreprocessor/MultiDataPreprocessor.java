package lbd.FSNER.DataPreprocessor;

import java.util.ArrayList;

import iitb.CRF.DataSequence;
import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.DataProcessor.Component.PreprocessData;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.Symbol;
import lbd.FSNER.Utils.LabelEncoding;

public class MultiDataPreprocessor extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;
	
	protected ArrayList<AbstractDataPreprocessor> dataPreproccessorList;

	public MultiDataPreprocessor(ArrayList<AbstractDataPreprocessor> dataPreproccessorList) {
		
		super(getActivityName(dataPreproccessorList), null);
		
		this.dataPreproccessorList = dataPreproccessorList;
	}

	@Override
	public SequenceLabel preprocessingSequence(DataSequence sequence) {
		
		SequenceLabel sequenceLabel = new SequenceLabel();
		SequenceLabelElement sequenceLabelElement;
		
		for(int i = 0; i < sequence.length(); i++) {
			
			sequenceLabelElement = preprocessingTerm((String)sequence.x(i), sequence.y(i));
			
			if(!sequenceLabelElement.getTerm().isEmpty())
				sequenceLabel.add(sequenceLabelElement);
			else
				sequenceLabel.add(new SequenceLabelElement(Symbol.EMPTY, LabelEncoding.BILOU.Outside.ordinal()));
		}
		
		return (sequenceLabel);
	}

	@Override
	public SequenceLabelElement preprocessingTerm(String term, int label) {
		
		String preproccessedTerm = term;
		SequenceLabelElement sequenceLabelElement;
		
		for(AbstractDataPreprocessor dataProcessor : dataPreproccessorList) {
			sequenceLabelElement = PreprocessData.preproccessTerm(preproccessedTerm, label, dataPreproccessorList, dataProcessor.getActivityName());
			preproccessedTerm = sequenceLabelElement.getTerm();
		}
		
		return (new SequenceLabelElement(preproccessedTerm, label));
	}

	@Override
	public void initialize() {
		
		for(int i = 0; i < dataPreproccessorList.size(); i++)
			dataPreproccessorList.get(i).initialize();
	}
	
	public static String getActivityName(ArrayList<AbstractDataPreprocessor> dataPreproccessorList) {
		
		String activityName = "";
		
		for(AbstractDataPreprocessor dataPreprocessor : dataPreproccessorList)
			activityName += dataPreprocessor.getActivityName()+"+";
		
		return(activityName.substring(0, activityName.length()-1));
	}

}
