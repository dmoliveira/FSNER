package lbd.FSNER.ActivityControl.FilterCombination;

import java.util.List;
import java.util.Map;

import lbd.FSNER.Filter.FtrAffix;
import lbd.FSNER.Filter.FtrCapitalizedTerms;
import lbd.FSNER.Filter.FtrContext;
import lbd.FSNER.Filter.FtrEntityProbability;
import lbd.FSNER.Filter.FtrSingleTermDictionary4;
import lbd.FSNER.Filter.FtrState;
import lbd.FSNER.Model.AbstractCombineFiltersInActiveControl;
import lbd.FSNER.Model.AbstractFilter;

public class TermComplementaryFocusFilterCombination extends AbstractCombineFiltersInActiveControl{

	private static final long serialVersionUID = 1L;

	//TODO: Implement a list for Labeled Terms by FSNER. Then I can use to implement state filter correctly.
	@Override
	protected void generateMultiFilters(int[] indexList, int indexDepthPosition, List<AbstractFilter> filterList,
			Map<String, Integer> nextFilterClassNameMap) {

		Map<String, Integer> firstFilterClassNameMap = generateStartFilterClassNameMap(filterList, nextFilterClassNameMap);
		String [] filterClassNameList;

		//-- Generate Combinations of filters (TESTE STATE Filters)
		filterClassNameList = new String [] {FtrEntityProbability.class.getName(), FtrState.class.getName()};
		generateMultiFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList);

		//-- Generate Combinations of filters
		filterClassNameList = new String [] {FtrAffix.class.getName(), FtrContext.class.getName(), FtrState.class.getName()};
		generateMultiFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList);

		//filterClassNameList = new String [] {FtrSingleTermDictionary3.class.getName(), FtrCapitalizedTerms.class.getName()};
		filterClassNameList = new String [] {FtrSingleTermDictionary4.class.getName(), FtrCapitalizedTerms.class.getName(), FtrState.class.getName()};
		generateMultiFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList);
	}

}
