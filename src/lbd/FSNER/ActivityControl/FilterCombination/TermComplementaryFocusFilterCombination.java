package lbd.FSNER.ActivityControl.FilterCombination;

import java.util.ArrayList;
import java.util.HashMap;

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

	@Override
	protected void generateMultiFilters(int[] indexList, int indexDepthPosition, ArrayList<AbstractFilter> filterList,
			HashMap<String, Integer> nextFilterClassNameMap) {

		HashMap<String, Integer> firstFilterClassNameMap = generateStartFilterClassNameMap(filterList, nextFilterClassNameMap);
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
