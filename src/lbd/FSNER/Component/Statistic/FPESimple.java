package lbd.FSNER.Component.Statistic;

import lbd.FSNER.Model.AbstractFilterProbabilityElement;
import lbd.FSNER.Utils.LabelEncoding;

public class FPESimple extends AbstractFilterProbabilityElement{

	@Override
	public void addLabel(int label) {
		
		totalAssignedLabel++;
		totalAssignedEntityPerLabel[label]++;
		
		if(LabelEncoding.isEntity(label))
			totalAssignedEntityLabel++;
	}

}
