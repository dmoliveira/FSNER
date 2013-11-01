package lbd.fsner.labelFile.level2;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractLabelFile;
import lbd.data.handler.ISequence;

public abstract class AbstractLabelFileLevel2 {

	public abstract void trainLabelSequenceLevel2(List<ISequence> pGoldSequenceList,
			Map<String, Set<AbstractFilter>> pClassNameSingleFilterMap, AbstractLabelFile pLabelFile);

	public abstract ISequence labelSequenceLevel2(ISequence pSequence);
}
