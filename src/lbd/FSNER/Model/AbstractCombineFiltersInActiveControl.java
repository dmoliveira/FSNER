package lbd.FSNER.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lbd.FSNER.Filter.MultiFilter;
import lbd.FSNER.Filter.ScoreCalculatorModel.FSCMNoScore;
import lbd.FSNER.Utils.Symbol;

public abstract class AbstractCombineFiltersInActiveControl implements Serializable {

	private static final long serialVersionUID = 1L;

	//-- Set the maximum depth of filter combination (e.g., 4 indicates the combination of 2, 3 and 4 filters at same time)
	protected final int MAX_FILTER_DEPTH = 2;

	protected List<AbstractFilter> globalFilterList;
	protected Map<String, List<AbstractFilter>> globalFilterListPerDataPreprocessor;

	protected String currentDataProcessorName;
	protected int currentFilterListSize;

	public void combineAllFilters(List<AbstractFilter> globalFilterList,
			final Map<String, List<AbstractFilter>> globalFilterListPerDataPreprocessor) {

		this.globalFilterList = globalFilterList;
		this.globalFilterListPerDataPreprocessor = globalFilterListPerDataPreprocessor;

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

			currentDataProcessorName = entry.getKey();
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
				startFilterClassNameMap.put(filterList.get(entry.getValue()).getCommonFilterName(), entry.getValue());
			}
		}

		//-- First FilterClass are always skipped, for this:
		if(filterList.size() > 0) {
			startFilterClassNameMap.put(filterList.get(0).getCommonFilterName(), 0);
		}


		return(startFilterClassNameMap);
	}

	protected Map<String, Integer> generateNextFilterClassNameMap(List<AbstractFilter> filterList) {

		Map<String, Integer> nextFilterClassNameMap = new HashMap<String, Integer>();
		nextFilterClassNameMap.put(filterList.get(0).getCommonFilterName(), 0);

		for(int i = 1; i < currentFilterListSize; i++) {
			if(!nextFilterClassNameMap.containsKey(filterList.get(i).getCommonFilterName())) {
				nextFilterClassNameMap.put(filterList.get(i-1).getCommonFilterName(), i);
				nextFilterClassNameMap.put(filterList.get(i).getCommonFilterName(), i);
			}
		}

		nextFilterClassNameMap.put(filterList.get(filterList.size()-1).getCommonFilterName(), filterList.size());

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
					filterList.get(indexList[indexDepthPosition - 1]).getCommonFilterName() : Symbol.EMPTY;

					nextClassCommonName = filterList.get(nextAvaibleIndex).getCommonFilterName();

					if(classCommonName.equals(nextClassCommonName)) {
						nextAvaibleIndex = nextFilterClassNameMap.get(nextClassCommonName);
					}
		}

		return(nextAvaibleIndex);
	}

	public void addMultiFilterToFilterList(List<AbstractFilter> filterList, int [] indexList, int multiFilterSize) {

		//-- It is always zero for security
		globalFilterList.add(new MultiFilter(filterList.get(indexList[0]).getFilterPreprocessingTypeNameIndex(), new FSCMNoScore()));
		globalFilterListPerDataPreprocessor.get(currentDataProcessorName).add(globalFilterList.get(globalFilterList.size() - 1));

		MultiFilter multiFilter = (MultiFilter) globalFilterList.get(globalFilterList.size() - 1);

		for(int i = 0; i <= multiFilterSize; i++) {
			multiFilter.addFilter(filterList.get(indexList[i]));
		}

		multiFilter.startMultiFilter(currentDataProcessorName);
	}

	public void generateMultiFiltersBySpecificCombinations(int[] filterIndexList, int indexDepthPosition,
			List<AbstractFilter> filterList, Map<String, Integer> startFilterClassNameMap, Map<String, Integer> nextFilterClassNameMap,
			String [] filterClassNameList) {

		int startFilterIndex = startFilterClassNameMap.get(filterClassNameList[indexDepthPosition]);
		int endFilterIndex = nextFilterClassNameMap.get(filterClassNameList[indexDepthPosition]) - 1;

		for (int currentFilterIndex = startFilterIndex; currentFilterIndex <= endFilterIndex; currentFilterIndex++) {
			filterIndexList[indexDepthPosition] = currentFilterIndex;

			if (indexDepthPosition + 1 < filterIndexList.length) {
				generateMultiFiltersBySpecificCombinations(filterIndexList, indexDepthPosition + 1, filterList,
						startFilterClassNameMap, nextFilterClassNameMap, filterClassNameList);
			} else {
				addMultiFilterToFilterList(filterList, filterIndexList, indexDepthPosition);
			}
		}
	}

}
