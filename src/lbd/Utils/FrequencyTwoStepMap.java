package lbd.Utils;

import java.util.HashMap;
import java.util.Map;

public class FrequencyTwoStepMap<T> {

	protected Map<T, Map<T, Frequency<T>>> mFrequencyMap;

	public FrequencyTwoStepMap() {
		mFrequencyMap = new HashMap<T, Map<T, Frequency<T>>>();
	}

	public void add(T pId, T pResult) {
		if(!mFrequencyMap.containsKey(pId)) {
			mFrequencyMap.put(pId, new HashMap<T, Frequency<T>>());
		}

		if(!mFrequencyMap.get(pId).containsKey(pResult)) {
			mFrequencyMap.get(pId).put(pResult, new Frequency<T>(pResult));
		}

		mFrequencyMap.get(pId).get(pResult).addFrequency();
	}

	public T getMax(String pId) {
		if(!mFrequencyMap.containsKey(pId)) {
			return null;
		}

		int vMaxFrequency = 0;
		T vId = null;

		for(Frequency<T> cFrequency : mFrequencyMap.get(pId).values()) {
			if(vMaxFrequency < cFrequency.getFrequency()) {
				vMaxFrequency = cFrequency.getFrequency();
				vId = cFrequency.getId();
			}
		}

		return vId;
	}

}
