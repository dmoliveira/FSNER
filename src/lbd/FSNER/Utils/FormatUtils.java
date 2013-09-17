package lbd.FSNER.Utils;

import java.text.DecimalFormat;

public class FormatUtils {

	public static String toDecimal(double pValue) {
		return (new DecimalFormat("#.##")).format(pValue);
	}

	public static String toDecimal(double pValue, int pDecimalPrecision) {
		String vMask = "#.#";

		for(int cMask = 1; cMask < pDecimalPrecision; cMask++) {
			vMask += "#";
		}

		if(Double.isNaN(pValue)) {
			pValue = 0.0;
		}

		return (new DecimalFormat(vMask)).format(pValue);
	}
}
