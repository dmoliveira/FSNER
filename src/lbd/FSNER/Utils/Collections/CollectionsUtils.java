package lbd.FSNER.Utils.Collections;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lbd.FSNER.Utils.Symbol;

public class CollectionsUtils {

	public static int sumIntegerValueInMap(Map<String, Integer> pMap) {
		Iterator<Entry<String, Integer>> cIte = pMap.entrySet().iterator();
		int vTotalSum = 0;

		while(cIte.hasNext()) {
			vTotalSum += cIte.next().getValue();
		}

		return vTotalSum;
	}

	public static String GetTerms(List<String> pList) {
		String vTermList = Symbol.EMPTY;

		for(String cTerm : pList) {
			vTermList += cTerm + Symbol.COMMA + Symbol.SPACE;
		}

		if(!vTermList.isEmpty()){
			vTermList = vTermList.substring(0, vTermList.length() -
					(Symbol.COMMA.length() + Symbol.SPACE.length()));
		}

		return vTermList;
	}

}
