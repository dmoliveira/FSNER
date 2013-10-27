package lbd.FSNER.Configuration;


public interface Constants {

	public String FSNERModel = "FSNER-Model";

	public String NonAlphaNumericCharacters = "\"¬'¹!@³#£$¢%¨&*()-_+=§/?€®←↓→ø´ª`{[~^]º|«»©“”,<.>·̣̣̣̣̣:;";

	public interface CharsetEncoding {
		public String ISO88591= "ISO-8859-1";
		public String UTF8 = "UTF-8";
	}

	public interface FileExtention {
		public String Tagged = ".tagged";
		public String stats = ".stats";
		public String generalizationStatistics = ".genstats";
		public String filterCombination = ".fcstats";
		public String NERModelSpecifications = ".specs";
		public String FilterConfiguration = ".fconf";
		public String FSNERModel = "fsbin";

		public interface TaggerConvert {
			public String LACWekaToFSNERFilterApplication = "lffa";
		}
	}

	public static enum FileType {TRAIN, TEST, VALIDATION, LABEL, TAGGED}

}
