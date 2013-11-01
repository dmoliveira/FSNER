package lbd.FSNER.ActivityControl.FilterCombination;

import java.util.List;
import java.util.Map;

import lbd.FSNER.Filter.FtrAffix;
import lbd.FSNER.Filter.FtrToken;
import lbd.FSNER.Filter.FtrState;
import lbd.FSNER.Filter.old.FtrCapitalizedTerms;
import lbd.FSNER.Filter.old.FtrContext;
import lbd.FSNER.Filter.old.FtrSingleTermDictionary4;
import lbd.FSNER.Model.AbstractCombineFiltersInActiveControl;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractMetaFilter.MetaFilterType;

public class CustomFilterCombination extends AbstractCombineFiltersInActiveControl{

	private static final long serialVersionUID = 1L;

	@Override
	protected void generateMultiFilters(int[] indexList, int indexDepthPosition, List<AbstractFilter> filterList,
			Map<String, Integer> nextFilterClassNameMap) {

		Map<String, Integer> firstFilterClassNameMap = generateStartFilterClassNameMap(filterList, nextFilterClassNameMap);
		String [] filterClassNameList;

		filterClassNameList = new String [] {FtrToken.class.getName(),FtrState.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Multi, 0);

		filterClassNameList = new String [] {FtrAffix.class.getName(),FtrState.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Multi, 0);

		filterClassNameList = new String [] {FtrContext.class.getName(),FtrState.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Multi, 0);

		filterClassNameList = new String [] {FtrSingleTermDictionary4.class.getName(),FtrState.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Multi, 0);

		filterClassNameList = new String [] {FtrCapitalizedTerms.class.getName(),FtrState.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Multi, 0);

		//-- Translated Filter
		filterClassNameList = new String [] {FtrToken.class.getName(),FtrState.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, -1);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, -2);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, 1);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, 2);

		filterClassNameList = new String [] {FtrAffix.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, -1);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, -2);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, 1);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, 2);

		filterClassNameList = new String [] {FtrSingleTermDictionary4.class.getName(), FtrState.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, -1);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, -2);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, 1);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, 2);

		filterClassNameList = new String [] {FtrCapitalizedTerms.class.getName(), FtrState.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, -1);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, -2);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, 1);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Translated, 2);

		//-- Window Filter
		filterClassNameList = new String [] {FtrToken.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Window, -2);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Window, -1);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Window, 2);
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Window, 1);

		filterClassNameList = new String [] {FtrState.class.getName()};
		generateMetaFiltersBySpecificCombinations(new int [filterClassNameList.length], 0, filterList, firstFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, MetaFilterType.Window, 2);
	}
}
