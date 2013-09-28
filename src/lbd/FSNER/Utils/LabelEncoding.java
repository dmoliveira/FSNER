package lbd.FSNER.Utils;

public class LabelEncoding {

	public static enum EncodingType {BILOU, BIO, IO, BILOUUnitContext, BILOUContext};

	public static enum BILOU {Beginning, Inside, Last, Outside, UnitToken;

	public static boolean isEntity(String pLabel) {
		return (pLabel.equalsIgnoreCase(Beginning.name()) ||
				pLabel.equalsIgnoreCase(Inside.name()) ||
				pLabel.equalsIgnoreCase(Last.name()) ||
				pLabel.equalsIgnoreCase(UnitToken.name()));
	}
	}

	public static enum BIO {Beginning, Inside, Outside;

	public static boolean isEntity(String pLabel) {
		return (pLabel.equalsIgnoreCase(Beginning.name()) ||
				pLabel.equalsIgnoreCase(Inside.name()));
	}
	}

	public static enum IO {Inside, Outside;

	public static boolean isEntity(String pLabel) {
		return (pLabel.equalsIgnoreCase(Inside.name()));
	}
	}

	public static enum BILOUUnitContext{Beginning, Inside, Last, Outside, UnitToken, Context;

	public static boolean isEntity(String pLabel) {
		return (pLabel.equalsIgnoreCase(Beginning.name()) ||
				pLabel.equalsIgnoreCase(Inside.name()) ||
				pLabel.equalsIgnoreCase(Last.name()) ||
				pLabel.equalsIgnoreCase(UnitToken.name()));
	}
	}

	public static enum BILOUContext{Beginning, Inside, Last, Outside, UnitToken,
		inputContext, SharedContext, outputContext;

	public static boolean isEntity(String pLabel) {
		return (pLabel.equalsIgnoreCase(Beginning.name()) ||
				pLabel.equalsIgnoreCase(Inside.name()) ||
				pLabel.equalsIgnoreCase(Last.name()) ||
				pLabel.equalsIgnoreCase(UnitToken.name()));
	}
	}

	protected static EncodingType sEncodingType = null;

	public static boolean isEntity(int label) {

		boolean isEntity = false;

		if(sEncodingType == null) {
			new Throwable("-- Error: Set one of the Encoding Type available before start the NERModel.");
		}

		if(sEncodingType == EncodingType.BILOU) {
			isEntity = isEntityBILOU(label);
		} else if(sEncodingType == EncodingType.BIO) {
			isEntity = isEntityBIO(label);
		} else if(sEncodingType == EncodingType.IO) {
			isEntity = isEntityIO(label);
		} else if(sEncodingType == EncodingType.BILOUUnitContext) {
			isEntity = isEntityBILOUUnitContext(label);
		} else if(sEncodingType == EncodingType.BILOUContext) {
			isEntity = isEntityBILOUContext(label);
		}

		return(isEntity);
	}

	protected static boolean isEntityBILOU(int label) {
		return(label > -1 && label != BILOU.Outside.ordinal() && label < BILOU.values().length);
	}

	protected static boolean isEntityBIO(int label) {
		return(label > -1 && label != BIO.Outside.ordinal() && label < BIO.values().length);
	}

	protected static boolean isEntityIO(int label) {
		return(label > -1 && label != IO.Outside.ordinal() && label < IO.values().length);
	}

	protected static boolean isEntityBILOUUnitContext(int label) {
		return(label > -1 && label != BILOUUnitContext.Outside.ordinal() &&
				label != BILOUUnitContext.Context.ordinal() && label < IO.values().length);
	}

	protected static boolean isEntityBILOUContext(int label) {
		return(label > -1 && label != BILOUContext.Outside.ordinal() &&
				label != BILOUContext.inputContext.ordinal() &&
				label != BILOUContext.SharedContext.ordinal() &&
				label !=BILOUContext.outputContext.ordinal() &&
				label < BILOUContext.values().length);
	}

	public static void setEncodingType(EncodingType encodingType) {
		LabelEncoding.sEncodingType = encodingType;
	}

	public static EncodingType getEncodingType() {
		return(sEncodingType);
	}

	public static int getAlphabetSize() {

		int alphabetSize = -1;

		if(sEncodingType == EncodingType.BILOU) {
			alphabetSize = BILOU.values().length;
		} else if(sEncodingType == EncodingType.BIO) {
			alphabetSize = BIO.values().length;
		} else if(sEncodingType == EncodingType.IO) {
			alphabetSize = IO.values().length;
		} else if(sEncodingType == EncodingType.BILOUUnitContext) {
			alphabetSize = BILOUUnitContext.values().length;
		} else if(sEncodingType == EncodingType.BILOUContext) {
			alphabetSize = BILOUContext.values().length;
		}

		return(alphabetSize);
	}

	public static int getOutsideLabel() {

		int outsideLabel = -1;

		if(sEncodingType == EncodingType.BILOU) {
			outsideLabel = BILOU.Outside.ordinal();
		} else if(sEncodingType == EncodingType.BIO) {
			outsideLabel = BIO.Outside.ordinal();
		} else if(sEncodingType == EncodingType.IO) {
			outsideLabel = IO.Outside.ordinal();
		} else if(sEncodingType == EncodingType.BILOUUnitContext) {
			outsideLabel = BILOUUnitContext.Outside.ordinal();
		} else if(sEncodingType == EncodingType.BILOUContext) {
			outsideLabel = BILOUContext.Outside.ordinal();
		}

		return(outsideLabel);

	}


}
