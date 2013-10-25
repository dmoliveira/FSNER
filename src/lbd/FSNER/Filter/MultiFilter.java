package lbd.FSNER.Filter;

import java.util.List;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Model.AbstractMetaFilter;

public class MultiFilter extends AbstractMetaFilter{

	private static final long serialVersionUID = 1L;

	public MultiFilter(int preprocessingTypeNameIndex, AbstractFilterScoreCalculatorModel scoreCalculator) {
		super("Multi", preprocessingTypeNameIndex, scoreCalculator);
	}

	public MultiFilter(int preprocessingTypeNameIndex, AbstractFilterScoreCalculatorModel scoreCalculator,
			List<AbstractFilter> filterList) {
		super(preprocessingTypeNameIndex, scoreCalculator, filterList);
	}
}
