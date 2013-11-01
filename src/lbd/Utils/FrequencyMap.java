package lbd.Utils;

import java.util.HashMap;
import java.util.Map;

public class FrequencyMap<T> {

	protected Map<T, Frequency<T>> mFrequencyMap;

	public FrequencyMap() {
		mFrequencyMap = new HashMap<T, Frequency<T>>();
	}

	public void add(T pItem) {
		if(!mFrequencyMap.containsKey(pItem)) {
			mFrequencyMap.put(pItem, new Frequency<T>(pItem));
		}

		mFrequencyMap.get(pItem).addFrequency();
	}

	public T getMax() {

		int vMaxFrequency = 0;
		T vId = null;

		for(Frequency<T> cFrequency : mFrequencyMap.values()) {
			if(vMaxFrequency < cFrequency.getFrequency()) {
				vMaxFrequency = cFrequency.getFrequency();
				vId = cFrequency.getId();
			}
		}

		return vId;
	}

	public void clear() {
		mFrequencyMap.clear();
	}

}

