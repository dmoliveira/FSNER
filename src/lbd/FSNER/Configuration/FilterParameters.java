package lbd.FSNER.Configuration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Utils.Symbol;


public class FilterParameters implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum FilterType {EntityTerm, Dictionary, Context, Affix, WordType, State, Window, Test};
	protected HashMap<FilterType, Boolean> mFilterTypeMap;

	public FilterParameters() {
		mFilterTypeMap = new HashMap<FilterType, Boolean>();
	}

	@SuppressWarnings("resource")
	public static ArrayList<FilterParameters> loadFilterConfiguration(String pFilterConfigurationFilename) {
		ArrayList<FilterParameters> vFilterParametersList = new ArrayList<FilterParameters>();

		try {
			BufferedReader vInput = new BufferedReader(new FileReader(pFilterConfigurationFilename));

			String cFilter;
			String [] vFilterParametersElement;
			FilterParameters vFilterParameters = new FilterParameters();

			while((cFilter = vInput.readLine()) != null) {
				if(!cFilter.isEmpty()) {
					vFilterParametersElement = cFilter.split(Symbol.COLON);
					if(vFilterParametersElement.length == 2) {
						vFilterParameters.addFilter(vFilterParametersElement[0], vFilterParametersElement[1]);
					} else {
						throw new IOException("[!] Wrong filter configuration format. Accept only 2 parameters.");
					}
				} else {
					vFilterParametersList.add(vFilterParameters);
					vFilterParameters = new FilterParameters();
				}
			}

			vInput.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return vFilterParametersList;
	}

	@SuppressWarnings("rawtypes")
	protected FilterType getFilterType(String pFilterTypeName) {
		for(Enum cFilterType : FilterType.values()) {
			if(cFilterType.name().equalsIgnoreCase(pFilterTypeName)) {
				return (FilterType) cFilterType;
			}
		}

		return null;
	}

	protected void addFilter(String pFiltertype, String pValue) {
		mFilterTypeMap.put(getFilterType(pFiltertype), Boolean.parseBoolean(pValue));
	}

	public boolean isFilterActive(FilterType pFilterType) {
		return mFilterTypeMap.get(pFilterType);
	}

	@Override
	public String toString() {
		String vToString = Symbol.EMPTY;

		for(FilterType cFilterType : FilterType.values()) {
			if(mFilterTypeMap.get(cFilterType)) {
				vToString += cFilterType.name() + Symbol.PLUS;
			}
		}

		return vToString.substring(0, vToString.length() - Symbol.PLUS.length());

	}
}
