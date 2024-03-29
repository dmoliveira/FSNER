package lbd.FSNER.ActivityControl;

import java.util.List;
import java.util.Map;

import lbd.FSNER.Model.AbstractCombineFiltersInActiveControl;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractMetaFilter.MetaFilterType;

public class SimpleFilterCombination extends AbstractCombineFiltersInActiveControl{

	private static final long serialVersionUID = 1L;

	@Override
	protected void generateMultiFilters(int[] indexList,
			int indexDepthPosition, List<AbstractFilter> filterList,
			Map<String, Integer> nextFilterClassNameMap) {

		do {
			indexList[indexDepthPosition] = getNextAvaibleIndex(filterList, indexList, indexDepthPosition, nextFilterClassNameMap);

			if(indexList[indexDepthPosition] < currentFilterListSize) {

				if(indexDepthPosition > 0) {
					addMetaFilterToFilterList(MetaFilterType.Multi, filterList, indexList, indexDepthPosition, 0);
				}

				if(indexDepthPosition < indexList.length-1) {
					indexList[indexDepthPosition + 1] = indexList[indexDepthPosition];
					generateMultiFilters(indexList, indexDepthPosition + 1, filterList, nextFilterClassNameMap);
				}
			}
		}while(indexList[indexDepthPosition] < currentFilterListSize);
	}
}
