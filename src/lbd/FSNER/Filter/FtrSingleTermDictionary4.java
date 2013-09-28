package lbd.FSNER.Filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Filter.Component.DictionaryFtr4;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;
import lbd.FSNER.Utils.Annotations.DefaultValue;

public class FtrSingleTermDictionary4 extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected int mDictionaryNameIndex;

	public static String sDictionaryDirectory;

	protected AbstractDataPreprocessor mDataProcessor;
	protected HashMap<String, DictionaryFtr4> sDictionaryList;

	protected ArrayList<String> sDictionaryNameList;
	protected HashMap<String, Boolean> sDictionaryLoadMap;
	protected SequenceLabel mSequenceLabelProcessed;

	//To utilize entity terms in training set in dictionary
	@DefaultValue(value="false")
	public final boolean sIsToUseTrainingEntityTermAsDictionary = false;
	public String mTrainingFileDictionary = "Internal@TrainingFileDictionary";

	//To optimize dictionary search
	protected final int MAX_ENTRY_WINDOW = 6; //DEFAULT: 6
	protected int mNextIndexToSearchInDictionary;

	public FtrSingleTermDictionary4(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator,
			AbstractDataPreprocessor dataProcessor, int dictionaryNameIndex) {

		super(ClassName.getSingleName(FtrSingleTermDictionary4.class.getName()) +
				".dicIndex:" + dictionaryNameIndex,
				preprocessingTypeNameIndex, scoreCalculator);

		if(sDictionaryList == null) {
			sDictionaryList = new HashMap<String, DictionaryFtr4>();
			sDictionaryNameList = new ArrayList<String>();
			sDictionaryLoadMap = new HashMap<String, Boolean>();
		}

		//this.commonFilterName = "Dic";
		//this.commonFilterName = "Wrd" + preprocessingTypeNameIndex;

		this.mDataProcessor = dataProcessor;
		this.mDictionaryNameIndex = dictionaryNameIndex;
	}

	public FtrSingleTermDictionary4(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator,
			AbstractDataPreprocessor dataProcessor, int dictionaryNameIndex, FtrSingleTermDictionary4 pLastDictionaryFilter) {

		this(preprocessingTypeNameIndex, scoreCalculator, dataProcessor, dictionaryNameIndex);

		if(pLastDictionaryFilter != null) {
			this.sDictionaryList = pLastDictionaryFilter.sDictionaryList;
			this.sDictionaryNameList = pLastDictionaryFilter.sDictionaryNameList;
			this.sDictionaryLoadMap = pLastDictionaryFilter.sDictionaryLoadMap;
			this.mTrainingFileDictionary = pLastDictionaryFilter.mTrainingFileDictionary;
		}
	}

	@Override
	public void initialize() {
		if(sDictionaryList.size() == 0) {
			loadDictionaryNameList();
			loadAllDictionary();
		}

		if(sIsToUseTrainingEntityTermAsDictionary) {
			sDictionaryList.put(mTrainingFileDictionary, new DictionaryFtr4());
		}

		mActivityName += Symbol.SQUARE_BRACKET_LEFT + sDictionaryNameList.get(mDictionaryNameIndex).substring(
				sDictionaryNameList.get(mDictionaryNameIndex).lastIndexOf(Symbol.SLASH) + 1) + Symbol.SQUARE_BRACKET_RIGHT;
	}

	protected void loadDictionaryNameList() {

		File vFolder = new File(sDictionaryDirectory);
		File[] vFileList = vFolder.listFiles();

		String vDictionaryFilenameAddress;

		for(File cFile : vFileList) {
			vDictionaryFilenameAddress = sDictionaryDirectory + cFile.getName();

			if (cFile.isFile() && !sDictionaryLoadMap.containsKey(vDictionaryFilenameAddress)) {
				sDictionaryNameList.add(vDictionaryFilenameAddress);
				sDictionaryLoadMap.put(vDictionaryFilenameAddress, false);
			}
		}

		if(sIsToUseTrainingEntityTermAsDictionary) {
			sDictionaryNameList.add(mTrainingFileDictionary);
			sDictionaryLoadMap.put(mTrainingFileDictionary, true);
		}
	}

	protected void loadAllDictionary() {
		for(String cDictionaryFilenameAddress : sDictionaryNameList) {
			if(!sDictionaryLoadMap.get(cDictionaryFilenameAddress)) {
				loadDictionary(cDictionaryFilenameAddress);
				sDictionaryLoadMap.put(cDictionaryFilenameAddress, true);
			}
		}
	}

	protected void loadDictionary(String pDictionaryFilenameAddress) {
		try {
			BufferedReader vInputReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(pDictionaryFilenameAddress), Parameters.dataEncoding));

			DictionaryFtr4 vDictionary = new DictionaryFtr4();

			String vEntry;
			String vEntryPreprocessed = Symbol.EMPTY;

			while((vEntry = vInputReader.readLine()) != null) {
				vEntryPreprocessed = preprocessEntry(vEntry.trim().split(Symbol.SPACE));

				if(!vEntryPreprocessed.isEmpty()) {
					vDictionary.addEntry(vEntryPreprocessed);
				}
			}

			sDictionaryList.put(pDictionaryFilenameAddress, vDictionary);

			vInputReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected String preprocessEntry(String[] pEntryElement) {
		String vPreprocessedEntry = Symbol.EMPTY;

		for(String cTerm : pEntryElement) {
			cTerm = mDataProcessor.preprocessingTerm(cTerm.trim(), -1).getTerm();
			vPreprocessedEntry +=  cTerm + Symbol.SPACE;
		}

		if(!vPreprocessedEntry.isEmpty()) {
			vPreprocessedEntry = vPreprocessedEntry.trim();
		}
		return vPreprocessedEntry;
	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		if(sIsToUseTrainingEntityTermAsDictionary) {
			DictionaryFtr4 vDictionary = sDictionaryList.get(mTrainingFileDictionary);

			for(String cEntity : getEntityListInSequence(sequenceLabelProcessed)) {
				vDictionary.addEntry(cEntity);
			}
		}
	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(SequenceLabel sequenceProcessedLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(
			SequenceLabel pSequenceLabelProcessed, int pIndex) {
		String vId = Symbol.EMPTY;

		if(pSequenceLabelProcessed != mSequenceLabelProcessed) {
			mSequenceLabelProcessed = pSequenceLabelProcessed;
			mNextIndexToSearchInDictionary = 0;
		}

		if(mNextIndexToSearchInDictionary <= pIndex) {
			String [] vCandidateEntryList = generateEntryPossibilities(pSequenceLabelProcessed, pIndex);
			int vEntrySize = existEntryInDictionary(vCandidateEntryList);

			if(vEntrySize > 0) {
				vId = generateId();
				mNextIndexToSearchInDictionary = pIndex + vEntrySize;
			}
		} else {
			vId = generateId();
		}

		return (vId);
	}

	protected String generateId() {
		return "id:" + this.mId + ".dic:" + mDictionaryNameIndex;
	}

	protected String [] generateEntryPossibilities(SequenceLabel pSequenceLabelProcessed, int pIndex) {
		int vLength = Math.min(pSequenceLabelProcessed.size(), pIndex + MAX_ENTRY_WINDOW);
		int vEntryPossibilitiesNumber = vLength - pIndex;
		String [] vEntryVariationsList = new String[vEntryPossibilitiesNumber];

		for(int cStartPosition = pIndex; cStartPosition < vLength; cStartPosition++) {
			for(int cEntry = 0; cEntry < vEntryPossibilitiesNumber; cEntry++) {
				if(cStartPosition == pIndex) {
					vEntryVariationsList[cEntry] = Symbol.EMPTY;
				}

				if(cEntry >= cStartPosition - pIndex) {
					vEntryVariationsList[cEntry] +=  mDataProcessor.preprocessingTerm(
							pSequenceLabelProcessed.getTerm(cStartPosition).trim(), -1).getTerm()
							+ ((cEntry > cStartPosition - pIndex)? Symbol.SPACE : Symbol.EMPTY);
				}
			}
		}

		return vEntryVariationsList;
	}

	protected int existEntryInDictionary(String [] pCandidateEntryList) {
		int vEntrySizeFound = 0;

		for(String cCandidateEntry : pCandidateEntryList) {
			if(sDictionaryList.get(sDictionaryNameList.get(mDictionaryNameIndex)).hasEntry(cCandidateEntry)) {
				vEntrySizeFound = cCandidateEntry.split(Symbol.SPACE).length;
				break;
			}
		}

		return vEntrySizeFound;
	}

	public static int getDictionaryListNumber() {
		File[] vFileList = (new File(sDictionaryDirectory)).listFiles();
		int vDictionaryFileNumbers = 0;

		for (File cFile : vFileList) {
			if (cFile.isFile()) {
				vDictionaryFileNumbers++;
			}
		}

		return(vDictionaryFileNumbers);
	}

	public ArrayList<String> getEntityListInSequence(SequenceLabel pSequence) {
		ArrayList<String> pEntityList = new ArrayList<String>();
		int cEntity = 0;

		for(int i = 0; i < pSequence.size(); i++) {
			if(pSequence.getLabel(i) == LabelEncoding.BILOU.Beginning.ordinal()) {
				pEntityList.add(pSequence.getTerm(i));
			} else if(pSequence.getLabel(i) == LabelEncoding.BILOU.Inside.ordinal()) {
				pEntityList.add(cEntity, pEntityList.get(cEntity) + Symbol.SPACE + pSequence.getTerm(i));
			} else if(pSequence.getLabel(i) == LabelEncoding.BILOU.Last.ordinal()) {
				pEntityList.add(cEntity, pEntityList.get(cEntity) + Symbol.SPACE + pSequence.getTerm(i));
				cEntity++;
			} else if(pSequence.getLabel(i) == LabelEncoding.BILOU.UnitToken.ordinal()) {
				pEntityList.add(pSequence.getTerm(i));
				cEntity++;
			}
		}

		return pEntityList;
	}

	public static void Clear() {
		/*sDictionaryDirectory = null;
		sDictionaryList = null;
		sDictionaryNameList = null;
		sDictionaryLoadMap = null;*/
	}

}
