package lbd.FSNER.ActivityControl.FilterCombination;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lbd.FSNER.Filter.FtrAffix;
import lbd.FSNER.Filter.FtrEntityContext;
import lbd.FSNER.Filter.FtrEntityTerm;
import lbd.FSNER.Filter.FtrGazetteer;
import lbd.FSNER.Filter.FtrState;
import lbd.FSNER.Filter.FtrToken;
import lbd.FSNER.Filter.FtrWordType;
import lbd.FSNER.Model.AbstractCombineFiltersInActiveControl;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractMetaFilter.MetaFilterType;

public class TermComplementaryFocusFilterCombination extends AbstractCombineFiltersInActiveControl{

	private static final long serialVersionUID = 1L;

	//TODO: Implement a list for Labeled Terms by FSNER. Then I can use to implement state filter correctly.
	@Override
	protected void generateMultiFilters(int[] pIndexList, int pIndexDepthPosition, List<AbstractFilter> pFilterList,
			Map<String, Integer> pNextFilterClassNameMap) {

		Map<String, Integer> vFirstFilterClassNameMap = generateStartFilterClassNameMap(pFilterList, pNextFilterClassNameMap);
		List<String[]> mFilterClassNameList = new ArrayList<String[]>();

		//-- Original
		//mFilterClassNameList.add(new String [] {FtrToken.class.getName(), FtrState.class.getName()});
		//mFilterClassNameList.add(new String [] {FtrAffix.class.getName(), FtrContext.class.getName(), FtrState.class.getName()});
		//mFilterClassNameList.add(new String [] {FtrSingleTermDictionary4.class.getName(), FtrCapitalizedTerms.class.getName(), FtrState.class.getName()});

		mFilterClassNameList.add(new String [] {FtrToken.class.getName(), FtrState.class.getName()});
		mFilterClassNameList.add(new String [] {FtrEntityTerm.class.getName(), FtrState.class.getName()});
		mFilterClassNameList.add(new String [] {FtrAffix.class.getName(), FtrEntityContext.class.getName(), FtrState.class.getName()});
		mFilterClassNameList.add(new String [] {FtrGazetteer.class.getName(), FtrWordType.class.getName(), FtrState.class.getName()});

		for(String [] cFilterList : mFilterClassNameList) {
			generateMetaFiltersBySpecificCombinations(new int [cFilterList.length], 0, pFilterList,
					vFirstFilterClassNameMap, pNextFilterClassNameMap, cFilterList, MetaFilterType.Multi, 0);
		}

	}

}
