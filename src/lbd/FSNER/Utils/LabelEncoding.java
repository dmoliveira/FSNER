package lbd.FSNER.Utils;

import java.util.ArrayList;
import java.util.List;

import lbd.data.handler.DataSequence;

public class LabelEncoding {

	public static enum EncodingType {BILOU, BIO, IO};

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
		}

		return(isEntity);
	}

	@SuppressWarnings("rawtypes")
	public static Enum[] getLabels() {

		if(sEncodingType == null) {
			new Throwable("-- Error: Set one of the Encoding Type available before start the NERModel.");
		}

		Enum [] vLabelList = null;

		if(sEncodingType == EncodingType.BILOU) {
			vLabelList = BILOU.values();
		} else if(sEncodingType == EncodingType.BIO) {
			vLabelList =  BIO.values();
		} else if(sEncodingType == EncodingType.IO) {
			vLabelList =  IO.values();
		}

		return(vLabelList);
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
		}

		return(alphabetSize);
	}

	public static int getLabel(String pLabelName) {

		int vOrdinalLabel = -1;

		if(sEncodingType == EncodingType.BILOU && BILOU.valueOf(pLabelName) != null) {
			vOrdinalLabel = BILOU.valueOf(pLabelName).ordinal();
		} else if(sEncodingType == EncodingType.BIO && BIO.valueOf(pLabelName) != null) {
			vOrdinalLabel = BIO.valueOf(pLabelName).ordinal();
		} else if(sEncodingType == EncodingType.IO && IO.valueOf(pLabelName) != null) {
			vOrdinalLabel = IO.valueOf(pLabelName).ordinal();
		}

		return(vOrdinalLabel);
	}

	public static int getOutsideLabel() {

		int vOutsideLabel = -1;

		if(sEncodingType == EncodingType.BILOU) {
			vOutsideLabel = BILOU.Outside.ordinal();
		} else if(sEncodingType == EncodingType.BIO) {
			vOutsideLabel = BIO.Outside.ordinal();
		} else if(sEncodingType == EncodingType.IO) {
			vOutsideLabel = IO.Outside.ordinal();
		}

		return(vOutsideLabel);
	}

	public static List<String> getEntities(DataSequence pLabeledSequence) {
		//TODO: Implement behavior for the others encodings (IO,BIO, etc).
		return getBILOUEncodingEntities(pLabeledSequence);
	}

	public static List<String> getBILOUEncodingEntities(DataSequence pLabeledSequence) {
		List<String> vEntities = new ArrayList<String>();
		String vEntity = Symbol.EMPTY;
		BILOU vLastLabel = BILOU.Outside;

		for (int cIndex = 0; cIndex < pLabeledSequence.length(); cIndex++) {
			if (pLabeledSequence.y(cIndex) == BILOU.Beginning.ordinal()) {
				if (vLastLabel == BILOU.Beginning || vLastLabel == BILOU.Inside) {
					vEntities.add(vEntity.trim());
				}
				vEntity = (String) pLabeledSequence.x(cIndex);
				vLastLabel = BILOU.Beginning;
			} else if (pLabeledSequence.y(cIndex) == BILOU.Inside.ordinal()) {
				vEntity += Symbol.SPACE + ((String) pLabeledSequence.x(cIndex)).trim();
				vLastLabel = BILOU.Inside;
			} else if (pLabeledSequence.y(cIndex) == BILOU.Last.ordinal()) {
				vEntity += Symbol.SPACE + ((String) pLabeledSequence.x(cIndex)).trim();
				vEntities.add(vEntity.trim());
				vEntity = Symbol.EMPTY;
				vLastLabel = BILOU.Last;
			} else if (pLabeledSequence.y(cIndex) == BILOU.UnitToken.ordinal()) {
				if (vLastLabel == BILOU.Beginning || vLastLabel == BILOU.Inside) {
					vEntities.add(vEntity.trim());
				}
				vEntities.add(((String) pLabeledSequence.x(cIndex)).trim());
				vEntity = Symbol.EMPTY;
				vLastLabel = BILOU.UnitToken;
			} else if (pLabeledSequence.y(cIndex) == BILOU.Outside.ordinal()
					&& (vLastLabel == BILOU.Beginning || vLastLabel == BILOU.Inside)) {
				vEntities.add(vEntity.trim());
				vEntity = Symbol.EMPTY;
				vLastLabel = BILOU.Outside;
			}
		}

		return vEntities;
	}

	public static void checkLabelEncoding() {
		if(getEncodingType() == null) {
			throw new IllegalStateException("[Error] Need to initialized Encoding Type by setEncodingType().");
		}
	}
}
