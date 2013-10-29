package lbd.fsner.labelFile.level2;

import lbd.FSNER.Model.AbstractActivityControl;
import lbd.data.handler.ISequence;

public abstract class AbstractLabelFileLevel2 {

	protected AbstractActivityControl mActivityControl;

	public AbstractLabelFileLevel2(AbstractActivityControl pActivityControl) {
		mActivityControl = pActivityControl;
	}

	public abstract ISequence labelSequenceLevel2(ISequence pSequence);
}
