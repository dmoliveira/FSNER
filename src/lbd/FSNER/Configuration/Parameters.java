package lbd.FSNER.Configuration;

import lbd.FSNER.Utils.FileUtils;
import lbd.FSNER.Utils.Annotations.DefaultValue;

public class Parameters {
	public enum SaveOption {Real, Simulation};

	@DefaultValue(value=Constants.CharsetEncoding.ISO88591)
	public static String dataEncoding = Constants.CharsetEncoding.ISO88591;

	public static class FSNERExecution {
		public static int trainFileIteration = 5;// default #5
	}

	public static class Save {

		@DefaultValue(value="SaveOption.Simulation")
		public static SaveOption saveOption = SaveOption.Real;

		@DefaultValue(value="")
		public static String optionalDirectory = "";

		@DefaultValue(value="true")
		public static boolean isToSaveNERModel = false;
	}

	public static class SimpleActivityControl {
		@DefaultValue(value="true")
		public static boolean isToCombineFilters = true;

		@DefaultValue(value="false")
		public static boolean isItUpdate = false;
	}

	public static class Evaluator {
		@DefaultValue(value="false")
		public static boolean isToEvaluateOnTokenLevel = false;

		@DefaultValue(value="false")
		public static final boolean isToUseInvalidResults = false;
	}

	public static class Directory {
		@DefaultValue(value="../FS-NER/Data/Collections/")
		public static final String collection = "../FS-NER/Data/Collections/";

		@DefaultValue(value="../FS-NER/Data/Saves/")
		public static final String save = "../FS-NER/Data/Saves/";

		@DefaultValue(value=save + "Real/")
		public static final String saveReal = save + "Real/";

		@DefaultValue(value=save + "Temp/")
		public static final String saveTemp = save + "Temp/";

		@DefaultValue(value="../FS-NER/Data/Clusters/")
		public static final String cluster = "../FS-NER/Data/Clusters/";

		@DefaultValue(value="./Data/Dictionaries/")
		public static final String dictionary = "../FS-NER/Data/Dictionaries/";

		@DefaultValue(value="../FS-NER/Data/Configurations/Filters/")
		public static final String filterConfiguration = "./Data/Configurations/SimpleFilter/";
	}

	public static boolean isRealSaveOption() {
		return Save.saveOption == SaveOption.Real;
	}

	public static String getOutputDirectory() {
		String vOptionalDirectory = Save.optionalDirectory + ((Save.optionalDirectory.isEmpty())? "" : "/");
		String vOutputDirectory =  (isRealSaveOption())? Directory.saveReal + vOptionalDirectory : Directory.saveTemp;

		return vOutputDirectory;
	}

	public static String generateOutputFilenameAddress(String pInputFilenameAddress, String pFileExtension) {
		String vOutputDirectory = getOutputDirectory();
		String vOutputFilename = FileUtils.getOnlyFilename(pInputFilenameAddress);

		return vOutputDirectory + vOutputFilename + pFileExtension;
	}

	public interface Display {
		public int NUMBERS_CHARACTERS_FOR_WRAP = 80;
	}
}
