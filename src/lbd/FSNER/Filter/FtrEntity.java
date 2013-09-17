package lbd.FSNER.Filter;

import java.util.ArrayList;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;

public class FtrEntity extends AbstractFilter{

	private static final long serialVersionUID = 1L;
	
	protected ArrayList<String> entityList;
	
	public FtrEntity(int preprocessingTypeNameIndex, 
			AbstractFilterScoreCalculatorModel scoreCalculator) {
		
		super(ClassName.getSingleName(FtrEntity.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);
		
		entityList = new ArrayList<String>();
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
	public void loadActionBeforeSequenceIteration(SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProccessed, int index) {
		
		if(LabelEncoding.isEntity(sequenceLabelProccessed.getLabel(index)))
			entityList.add(sequenceLabelProccessed.getTerm(index));
	}
	
	@Override
	public void loadActionAfterSequenceIteration(SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void adjust(SequenceLabel sequenceLabel) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getSequenceInstanceIdSub(SequenceLabel sequenceLabelProcessed, int index) {		
		
		return ("id:"+this.mId+"-"+sequenceLabelProcessed.getTerm(index));
	}
	
	public ArrayList<String> getEntityList() {
		return(entityList);
	}
}
