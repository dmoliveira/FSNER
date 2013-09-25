package lbd.FSNER.Configuration;

import lbd.FSNER.Utils.Annotations.DefaultValue;

public interface Debug {
	public interface ActivityControl {

		@DefaultValue(value="false")
		public boolean printFilterStatistics = false;

		@DefaultValue(value="false")
		public boolean printFilterInstanceStatistics = false;

		@DefaultValue(value="false")
		public boolean showElapsedTime = false;

		@DefaultValue(value="false")
		public boolean showGeneratedFiltersNumber = false;
	}

	public interface NERModel {
		@DefaultValue(value="true")
		public boolean writeNERModelSpecification = false;
	}

	public interface LabelFile {
		@DefaultValue(value="false")
		public boolean printFilterStatistics = false;

		@DefaultValue(value="false")
		public boolean printCorrectEntityTermIdentifiedByFilter = false;

		@DefaultValue(value="false")
		public boolean printMissedEntityTermIdentifiedByFilter = false;

		@DefaultValue(value="false")
		public boolean printWrongEntityTermIdentifiedByFilter = false;

		@DefaultValue(value="false")
		public boolean printTermIdentifiedAsEntity = false;

		@DefaultValue(value="false")
		public boolean printTermsLabeledAsEntity = false;

		@DefaultValue(value="false")
		public boolean printWrongTermsLabeledAsEntity = false;

		@DefaultValue(value="false")
		public boolean printMissedEntityTerms = false;

		@DefaultValue(value="false")
		public boolean showElapsedTime = false;

		//Not working well.
		@DefaultValue(value="false")
		public boolean showUnknownTerms = false;
	}

	public interface Evaluator {
		@DefaultValue(value="false")
		public boolean isToPrintParcialStatistics = false;

		@DefaultValue(value="false")
		public boolean isToWriteParcialStatistics = false;

		@DefaultValue(value="true")
		public boolean isToPrintStatistics = true;

		//TODO: Diego - Despite it not write, the folder was created.
		@DefaultValue(value="false")
		public boolean isToWriteStatistics = false;
	}
}
