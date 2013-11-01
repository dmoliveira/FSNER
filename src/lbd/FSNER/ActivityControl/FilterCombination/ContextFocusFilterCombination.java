package lbd.FSNER.ActivityControl.FilterCombination;

import java.util.List;
import java.util.Map;

import lbd.FSNER.Filter.FtrAffix;
import lbd.FSNER.Filter.old.FtrCapitalizedTerms;
import lbd.FSNER.Filter.old.FtrContext;
import lbd.FSNER.Filter.old.FtrSingleTermDictionary3;
import lbd.FSNER.Model.AbstractCombineFiltersInActiveControl;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractMetaFilter.MetaFilterType;

public class ContextFocusFilterCombination extends AbstractCombineFiltersInActiveControl{

	private static final long serialVersionUID = 1L;

	@Override
	protected void generateMultiFilters(int[] indexList, int indexDepthPosition, List<AbstractFilter> filterList,
			Map<String, Integer> nextFilterClassNameMap) {

		Map<String, Integer> firstFilterClassNameMap = generateStartFilterClassNameMap(filterList, nextFilterClassNameMap);
		String [] filterClassNameList;

		//-- Generate Combinations of filters
		filterClassNameList = new String [] {FtrContext.class.getName(), FtrAffix.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Multi, 0);

		filterClassNameList = new String [] {FtrContext.class.getName(), FtrCapitalizedTerms.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Multi, 0);

		filterClassNameList = new String [] {FtrContext.class.getName(), FtrSingleTermDictionary3.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Multi, 0);

		filterClassNameList = new String [] {FtrContext.class.getName(), FtrAffix.class.getName(), FtrSingleTermDictionary3.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Multi, 0);

		filterClassNameList = new String [] {FtrContext.class.getName(), FtrAffix.class.getName(), FtrCapitalizedTerms.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Multi, 0);

		filterClassNameList = new String [] {FtrContext.class.getName(), FtrSingleTermDictionary3.class.getName(), FtrCapitalizedTerms.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Multi, 0);

		filterClassNameList = new String [] {FtrContext.class.getName(), FtrAffix.class.getName(), FtrSingleTermDictionary3.class.getName(), FtrCapitalizedTerms.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Multi, 0);
	}

}
