package lbd.FSNER.Filter;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Filter.ScoreCalculatorModel.FSCMEntityless;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;

public class FtrEntityless extends FtrEntity {

	private static final long serialVersionUID = 1L;

	public FtrEntityless(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {

		super(preprocessingTypeNameIndex, scoreCalculator);
	}

	@Override
	public String getSequenceInstanceIdSub(SequenceLabel sequenceLabelProcessed,
			int index) {

		String id = Symbol.EMPTY;

		if(mFilterMode == FilterMode.inLabel) {
			for(int i = 0; i < entityList.size(); i++) {

				if(mScoreCalculator instanceof FSCMEntityless &&
						((FSCMEntityless)mScoreCalculator).calculateScoreEntityLess(sequenceLabelProcessed, index) > 0) {

					id = "id:"+this.mId+"-"+i;
					System.out.println(id);
					break;
				}
			}
		} else {
			if(LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index))) {
				for(int i = 0; i < entityList.size(); i++) {
					if(sequenceLabelProcessed.getTerm(index).equals(entityList.get(i))) {
						id = "id:"+this.mId+"-"+i;
						break;
					}
				}
			}
		}

		return (id);
	}

}
