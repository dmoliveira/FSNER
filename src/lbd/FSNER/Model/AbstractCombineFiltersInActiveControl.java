package lbd.FSNER.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lbd.FSNER.Filter.FtrTranslate;
import lbd.FSNER.Filter.FtrWindow;
import lbd.FSNER.Filter.MultiFilter;
import lbd.FSNER.Filter.ScoreCalculatorModel.FSCMNoScore;
import lbd.FSNER.Model.AbstractMetaFilter.MetaFilterType;
import lbd.FSNER.Utils.Symbol;

public abstract class AbstractCombineFiltersInActiveControl implements Serializable {

	private static final long serialVersionUID = 1L;

	//-- Set the maximum depth of filter combination (e.g., 4 indicates the combination of 2, 3 and 4 filters at same time)
	protected final int MAX_FILTER_DEPTH = 2;

	protected List<AbstractFilter> mGlobalFilterList;
	protected Map<String, List<AbstractFilter>> mGlobalFilterListPerDataPreprocessor;

	protected String mCurrentDataProcessorName;
	protected int currentFilterListSize;

	public void combineAllFilters(List<AbstractFilter> globalFilterList,
			final Map<String, List<AbstractFilter>> globalFilterListPerDataPreprocessor) {

		this.mGlobalFilterList = globalFilterList;
		this.mGlobalFilterListPerDataPreprocessor = globalFilterListPerDataPreprocessor;

		Iterator<Entry<String, List<AbstractFilter>>> ite = globalFilterListPerDataPreprocessor.entrySet().iterator();
		Entry<String, List<AbstractFilter>> entry;

		List<AbstractFilter> filterList;
		Map<String, Integer> nextFilterClassNameMap;

		int [] indexList;
		int numberFilterPerClass;

		while(ite.hasNext()) {

			entry = ite.next();
			filterList = entry.getValue();
			//filterList = globalFilterList;

			mCurrentDataProcessorName = entry.getKey();
			currentFilterListSize = filterList.size();

			nextFilterClassNameMap = generateNextFilterClassNameMap(filterList);
			numberFilterPerClass = nextFilterClassNameMap.size();

			indexList = new int[((MAX_FILTER_DEPTH < numberFilterPerClass)? MAX_FILTER_DEPTH : numberFilterPerClass)];
			indexList[0] = -1;

			generateMultiFilters(indexList, 0, filterList, nextFilterClassNameMap);
		}
	}

	protected Map<String, Integer> generateStartFilterClassNameMap(List<AbstractFilter> filterList, Map<String, Integer> nextFilterClassNameMap) {

		Map<String, Integer> startFilterClassNameMap = new HashMap<String, Integer>();

		Iterator<Entry<String, Integer>> ite = nextFilterClassNameMap.entrySet().iterator();
		Entry<String, Integer> entry;

		while(ite.hasNext()) {
			entry = ite.next();

			if(entry.getValue() < filterList.size()) {
				startFilterClassNameMap.put(filterList.get(entry.getValue()).getFilterClassName(), entry.getValue());
			}
		}

		//-- First FilterClass are always skipped, for this:
		if(filterList.size() > 0) {
			startFilterClassNameMap.put(filterList.get(0).getFilterClassName(), 0);
		}


		return(startFilterClassNameMap);
	}

	protected Map<String, Integer> generateNextFilterClassNameMap(List<AbstractFilter> filterList) {

		Map<String, Integer> nextFilterClassNameMap = new HashMap<String, Integer>();
		nextFilterClassNameMap.put(filterList.get(0).getFilterClassName(), 0);

		for(int i = 1; i < currentFilterListSize; i++) {
			if(!nextFilterClassNameMap.containsKey(filterList.get(i).getFilterClassName())) {
				nextFilterClassNameMap.put(filterList.get(i-1).getFilterClassName(), i);
				nextFilterClassNameMap.put(filterList.get(i).getFilterClassName(), i);
			}
		}

		nextFilterClassNameMap.put(filterList.get(filterList.size()-1).getFilterClassName(), filterList.size());

		return(nextFilterClassNameMap);
	}

	protected abstract void generateMultiFilters(int [] indexList, int indexDepthPosition,
			List<AbstractFilter> filterList, Map<String, Integer> nextFilterClassNameMap);

	public int getNextAvaibleIndex(List<AbstractFilter> filterList,
			int[] indexList, int indexDepthPosition, Map<String, Integer> nextFilterClassNameMap) {

		String classCommonName;
		String nextClassCommonName;

		int nextAvaibleIndex = indexList[indexDepthPosition] + 1;

		if(nextAvaibleIndex < currentFilterListSize) {

			classCommonName = (indexDepthPosition > 0 && indexList[indexDepthPosition] > -1)?
					filterList.get(indexList[indexDepthPosition - 1]).getFilterClassName() : Symbol.EMPTY;

					nextClassCommonName = filterList.get(nextAvaibleIndex).getFilterClassName();

					if(classCommonName.equals(nextClassCommonName)) {
						nextAvaibleIndex = nextFilterClassNameMap.get(nextClassCommonName);
					}
		}

		return(nextAvaibleIndex);
	}

	public void generateMetaFiltersBySpecificCombinations(int[] filterIndexList, int indexDepthPosition,
			List<AbstractFilter> filterList, Map<String, Integer> startFilterClassNameMap,
			Map<String, Integer> nextFilterClassNameMap,
			String [] filterClassNameList, MetaFilterType pMetaFilterType, int pMetaFilterWindowOrTraslatedPosition) {

		int startFilterIndex = startFilterClassNameMap.get(filterClassNameList[indexDepthPosition]);
		int endFilterIndex = nextFilterClassNameMap.get(filterClassNameList[indexDepthPosition]) - 1;

		for (int currentFilterIndex = startFilterIndex; currentFilterIndex <= endFilterIndex; currentFilterIndex++) {
			filterIndexList[indexDepthPosition] = currentFilterIndex;

			if (indexDepthPosition + 1 < filterIndexList.length) {
				generateMetaFiltersBySpecificCombinations(filterIndexList, indexDepthPosition + 1, filterList,
						startFilterClassNameMap, nextFilterClassNameMap, filterClassNameList, pMetaFilterType, pMetaFilterWindowOrTraslatedPosition);
			} else {
				addMetaFilterToFilterList(pMetaFilterType, filterList, filterIndexList, indexDepthPosition, pMetaFilterWindowOrTraslatedPosition);
			}
		}
	}

	protected void addMetaFilterToFilterList(MetaFilterType pMetaFilterType, List<AbstractFilter> pFilterList,
			int [] pIndexList, int pMetaFilterSize, int pMetaFilterWindowOrTraslatedPosition) {

		//-- It is always zero for security
		mGlobalFilterList.add(getNewMetaFilter(pMetaFilterType, pFilterList, pIndexList, pMetaFilterSize, pMetaFilterWindowOrTraslatedPosition));
		mGlobalFilterListPerDataPreprocessor.get(mCurrentDataProcessorName).add(mGlobalFilterList.get(mGlobalFilterList.size() - 1));

		AbstractMetaFilter vMetaFilter = (AbstractMetaFilter) mGlobalFilterList.get(mGlobalFilterList.size() - 1);

		for(int i = 0; i <= pMetaFilterSize; i++) {
			vMetaFilter.addFilter(pFilterList.get(pIndexList[i]));
		}

		vMetaFilter.startMetaFilter(mCurrentDataProcessorName);
	}

	protected AbstractMetaFilter getNewMetaFilter(MetaFilterType pMetaFilterType, List<AbstractFilter> pFilterList,
			int [] pIndexList, int pMultiFilterSize, int pMetaFilterWindowOrTraslatedPosition) {

		AbstractMetaFilter vMetaFilter = null;

		if(pMetaFilterType == MetaFilterType.Multi) {
			vMetaFilter = getNewMultiFilter(pFilterList, pIndexList, pMultiFilterSize);
		} else if (pMetaFilterType == MetaFilterType.Translated) {
			vMetaFilter = getNewTranslatedFilter(pFilterList, pIndexList, pMultiFilterSize, pMetaFilterWindowOrTraslatedPosition);
		} else if (pMetaFilterType == MetaFilterType.Window) {
			vMetaFilter = getNewWindowFilter(pFilterList, pIndexList, pMultiFilterSize, pMetaFilterWindowOrTraslatedPosition);
		}

		return vMetaFilter;
	}

	protected AbstractMetaFilter getNewMultiFilter(List<AbstractFilter> pFilterList, int [] pIndexList, int pMultiFilterSize) {
		//-- It is always zero for security
		return new MultiFilter(pFilterList.get(pIndexList[0])
				.getFilterPreprocessingTypeIndex(), new FSCMNoScore());
	}

	protected AbstractMetaFilter getNewTranslatedFilter(List<AbstractFilter> pFilterList, int [] pIndexList,
			int pMultiFilterSize, int pTranslatedPosition) {
		//-- It is always zero for security
		return new FtrTranslate(pFilterList.get(pIndexList[0])
				.getFilterPreprocessingTypeIndex(), new FSCMNoScore(), pTranslatedPosition);
	}

	protected AbstractMetaFilter getNewWindowFilter(List<AbstractFilter> pFilterList, int [] pIndexList,
			int pMultiFilterSize, int pWindowSize) {
		//-- It is always zero for security
		return new FtrWindow(pFilterList.get(pIndexList[0])
				.getFilterPreprocessingTypeIndex(), new FSCMNoScore(), pWindowSize);
	}
}
