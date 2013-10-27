package lbd.FSNER;

import java.util.ArrayList;

import lbd.FSNER.Collection.CollectionDefinition.CollectionName;
import lbd.FSNER.Configuration.FilterParameters;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.FileUtils;

public class Main {
	public static void main(String [] args) {

		ArrayList<FilterParameters> vFilterParametersList = FilterParameters.loadFilterConfiguration(
				FileUtils.getListOfOnlyFiles(Parameters.Directory.mFilterConfiguration).get(0));

		for(FilterParameters cFilterParameters : vFilterParametersList) {
			System.out.println("-- FilterConfiguration: " + cFilterParameters);
			for(CollectionName cCollection : CollectionName.values()) {
				FSNER vFSNER = new FSNER();
				vFSNER.runFSNER(cCollection, cFilterParameters);
			}
		}
	}
}
