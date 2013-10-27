package lbd.FSNER.Configuration;

import lbd.FSNER.Evaluation.Component.EvaluationLevel;
import lbd.FSNER.Model.AbstractLabelEncoding;
import lbd.FSNER.Utils.FileUtils;
import lbd.FSNER.Utils.Symbol;
import lbd.FSNER.Utils.Annotations.DefaultValue;
import lbd.data.handler.sequenceSetHandler.AbstractSequenceSetHandler;
import lbd.data.handler.sequenceSetHandler.SequenceSetHandlerLine;
import lbd.data.tokenizer.AbstractTokenizer;
import lbd.data.tokenizer.TokenizerAnyTokenBySpace;
import lbd.fsner.label.encoding.BILOU;

public class Parameters {

	public enum SaveOption {Real, Simulation};

	public static class DataHandler {
		@DefaultValue(value=Constants.CharsetEncoding.ISO88591)
		public static String mDataEncoding = Constants.CharsetEncoding.UTF8;

		@DefaultValue(value="BILOU()")
		public static AbstractLabelEncoding mLabelEncoding = new BILOU();

		@DefaultValue(value="TokenizerAnyTokenBySpace(null)")
		public static AbstractTokenizer mTokenizer = new TokenizerAnyTokenBySpace(null);

		@DefaultValue(value="SequenceSetHandlerTokenPerLine()")
		public static AbstractSequenceSetHandler mSequenceSetHandler = new SequenceSetHandlerLine();

		@DefaultValue(value="\\|")
		public static String mSplitTokenLabelDelimiter = Symbol.SPECIAL_SPLIT_TOKEN_LABEL;

		@DefaultValue(value="|")
		public static String mDelimiterTokenLabel = Symbol.DELIMITER_LABEL;
	}

	public static class FSNERExecution {
		public static int mTrainFileIteration = 1;// default #5
	}

	public static class Filter {

		public static class Context {
			@DefaultValue(value="4")
			public static int mMaximumContextSize = 4;

			@DefaultValue(value="4")
			public static int mMaximumEntitySizeToSearch = 4;
		}

		public static class Gazetter {
			@DefaultValue(value="3")
			public static int mMinimumAcceptedDictionaryTermEntry = 3;
		}

		public static class Affix {
			@DefaultValue(value="4")
			public static int mMinimumTermSize = 5;
		}
	}

	public static class Save {

		@DefaultValue(value="SaveOption.Simulation")
		public static SaveOption mSaveOption = SaveOption.Real;

		@DefaultValue(value="")
		public static String mOptionalDirectory = "";

		@DefaultValue(value="true")
		public static boolean mIsToSaveNERModel = false;
	}

	public static class SimpleActivityControl {
		@DefaultValue(value="true")
		public static boolean isToCombineFilters = false;

		@DefaultValue(value="false")
		public static boolean mIsItUpdate = false;

		@DefaultValue(value="false")
		public static int mSubSequenceLabelSize = 3;
	}

	public static class Evaluator {
		@DefaultValue(value="true")
		public static EvaluationLevel mTokenLv1 = EvaluationLevel.TokenLv1;

		@DefaultValue(value="true")
		public static EvaluationLevel mLabelLv2 = EvaluationLevel.LabelLv2;

		@DefaultValue(value="true")
		public static EvaluationLevel mEntityLv3 = EvaluationLevel.EntityLv3;

		@DefaultValue(value="true")
		public static boolean mIsToEvaluateOnTokenLevel = true;

		@DefaultValue(value="false")
		public static final boolean mIsToUseInvalidResults = false;

		@DefaultValue(value="true")
		public static final boolean mIsToConsiderType = true;
	}

	public static class Directory {
		@DefaultValue(value="../FS-NER/Data/Collections/")
		public static final String mCollection = "./Data/Collections/";

		@DefaultValue(value="../FS-NER/Data/Saves/")
		public static final String mSave = "./Data/Saves/";

		@DefaultValue(value= mSave + "Real/")
		public static final String mSaveReal = mSave + "Real/";

		@DefaultValue(value= mSave + "Temp/")
		public static final String mSaveTemp = mSave + "Temp/";

		@DefaultValue(value= mSave + "Models/")
		public static final String mModels = mSave + "Models/";

		@DefaultValue(value="./Data/Clusters/")
		public static final String mCluster = "./Data/Clusters/";

		@DefaultValue(value="./Data/Dictionaries/")
		public static final String mDictionary = "./Data/Dictionaries/";

		@DefaultValue(value="./Data/Configurations/Filters/")
		public static final String mFilterConfiguration = "./Data/Configurations/SimpleFilter/";
	}

	public static boolean isRealSaveOption() {
		return Save.mSaveOption == SaveOption.Real;
	}

	public static String getOutputDirectory() {
		String vOptionalDirectory = Save.mOptionalDirectory + ((Save.mOptionalDirectory.isEmpty())? "" : "/");
		String vOutputDirectory =  (isRealSaveOption())? Directory.mSaveReal + vOptionalDirectory : Directory.mSaveTemp;

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
