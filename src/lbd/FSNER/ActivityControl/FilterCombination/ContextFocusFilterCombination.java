package lbd.FSNER.ActivityControl.FilterCombination;

import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Filter.FtrAffix;
import lbd.FSNER.Filter.FtrCapitalizedTerms;
import lbd.FSNER.Filter.FtrContext;
import lbd.FSNER.Filter.FtrSingleTermDictionary3;
import lbd.FSNER.Model.AbstractCombineFiltersInActiveControl;
import lbd.FSNER.Model.AbstractFilter;

public class ContextFocusFilterCombination extends AbstractCombineFiltersInActiveControl{

	@Override
	protected void generateMultiFilters(int[] indexList, int indexDepthPosition, ArrayList<AbstractFilter> filterList,
			HashMap<String, Integer> nextFilterClassNameMap) {
		
		HashMap<String, Integer> firstFilterClassNameMap = generateStartFilterClassNameMap(filterList, nextFilterClassNameMap);
		String [] filterClassNameList;

		//-- Generate Combinations of filters
		filterClassNameList = new String [] {FtrContext.class.getName(), FtrAffix.class.getName()};
		generateMultiFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList);

		filterClassNameList = new String [] {FtrContext.class.getName(), FtrCapitalizedTerms.class.getName()};
		generateMultiFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList);
		
		filterClassNameList = new String [] {FtrContext.class.getName(), FtrSingleTermDictionary3.class.getName()};
		generateMultiFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList);
		
		filterClassNameList = new String [] {FtrContext.class.getName(), FtrAffix.class.getName(), FtrSingleTermDictionary3.class.getName()};
		generateMultiFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList);
		
		filterClassNameList = new String [] {FtrContext.class.getName(), FtrAffix.class.getName(), FtrCapitalizedTerms.class.getName()};
		generateMultiFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList);
		
		filterClassNameList = new String [] {FtrContext.class.getName(), FtrSingleTermDictionary3.class.getName(), FtrCapitalizedTerms.class.getName()};
		generateMultiFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList);
		
		filterClassNameList = new String [] {FtrContext.class.getName(), FtrAffix.class.getName(), FtrSingleTermDictionary3.class.getName(), FtrCapitalizedTerms.class.getName()};
		generateMultiFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList);
	}

}