package lbd.FSNER.ActivityControl.FilterCombination;

import java.util.List;
import java.util.Map;

import lbd.FSNER.Filter.FtrCapitalizedTerms;
import lbd.FSNER.Filter.FtrEntityProbability;
import lbd.FSNER.Model.AbstractCombineFiltersInActiveControl;
import lbd.FSNER.Model.AbstractFilter;

public class MyFiltersCombination extends AbstractCombineFiltersInActiveControl{

	private static final long serialVersionUID = 1L;

	@Override
	protected void generateMultiFilters(int[] indexList, int indexDepthPosition, List<AbstractFilter> filterList,
			Map<String, Integer> nextFilterClassNameMap) {

		Map<String, Integer> firstFilterClassNameMap = generateStartFilterClassNameMap(filterList, nextFilterClassNameMap);
		String [] filterClassNameList;

		//-- Generate Combinations of filters
		filterClassNameList = new String [] {FtrEntityProbability.class.getName(), FtrCapitalizedTerms.class.getName()};
		generateMultiFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList);
	}

}
