package lbd.FSNER.Filter.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Filter.Component.DictionaryFtr4;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.FSNER.Utils.Annotations.DefaultValue;
import lbd.data.handler.ISequence;
import lbd.fsner.label.encoding.Label;

public class FtrSingleTermDictionary4 extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected int mDictionaryNameIndex;

	public static String sDictionaryDirectory;

	protected AbstractDataPreprocessor mDataProcessor;
	protected HashMap<String, DictionaryFtr4> sDictionaryList;

	protected ArrayList<String> sDictionaryNameList;
	protected HashMap<String, Boolean> sDictionaryLoadMap;
	protected ISequence mSequenceLabelProcessed;

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
					new FileInputStream(pDictionaryFilenameAddress), Parameters.DataHandler.mDataEncoding));

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
			cTerm = mDataProcessor.preprocessingToken(cTerm.trim(), -1);
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
	public void loadActionBeforeSequenceIteration(ISequence pPreprocessedSequence) {
		if(sIsToUseTrainingEntityTermAsDictionary) {
			DictionaryFtr4 vDictionary = sDictionaryList.get(mTrainingFileDictionary);

			for(String cEntity : getEntityListInSequence(pPreprocessedSequence)) {
				vDictionary.addEntry(cEntity);
			}
		}
	}

	@Override
	public void loadTermSequence(ISequence pPreprocessedSequence, int pIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(ISequence pSequence,
			ISequence pPreprocessedSequence, int pIndex) {
		String vId = Symbol.EMPTY;

		if(pPreprocessedSequence != mSequenceLabelProcessed) {
			mSequenceLabelProcessed = pPreprocessedSequence;
			mNextIndexToSearchInDictionary = 0;
		}

		if(mNextIndexToSearchInDictionary <= pIndex) {
			String [] vCandidateEntryList = generateEntryPossibilities(pPreprocessedSequence, pIndex);
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

	protected String [] generateEntryPossibilities(ISequence pPreprocessedSequence, int pIndex) {
		int vLength = Math.min(pPreprocessedSequence.length(), pIndex + MAX_ENTRY_WINDOW);
		int vEntryPossibilitiesNumber = vLength - pIndex;
		String [] vEntryVariationsList = new String[vEntryPossibilitiesNumber];

		for(int cStartPosition = pIndex; cStartPosition < vLength; cStartPosition++) {
			for(int cEntry = 0; cEntry < vEntryPossibilitiesNumber; cEntry++) {
				if(cStartPosition == pIndex) {
					vEntryVariationsList[cEntry] = Symbol.EMPTY;
				}

				if(cEntry >= cStartPosition - pIndex) {
					vEntryVariationsList[cEntry] +=  mDataProcessor.preprocessingToken(
							pPreprocessedSequence.getToken(cStartPosition).trim(), -1)
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

	public ArrayList<String> getEntityListInSequence(ISequence pPreprocessedSequence) {
		ArrayList<String> pEntityList = new ArrayList<String>();
		int cEntity = 0;

		for(int i = 0; i < pPreprocessedSequence.length(); i++) {
			if(Label.getCanonicalLabel(pPreprocessedSequence.getLabel(i)) == Label.Beginning) {
				pEntityList.add(pPreprocessedSequence.getToken(i));
			} else if(Label.getCanonicalLabel(pPreprocessedSequence.getLabel(i)) == Label.Inside) {
				pEntityList.add(cEntity, pEntityList.get(cEntity) + Symbol.SPACE + pPreprocessedSequence.getToken(i));
			} else if(Label.getCanonicalLabel(pPreprocessedSequence.getLabel(i)) == Label.Last) {
				pEntityList.add(cEntity, pEntityList.get(cEntity) + Symbol.SPACE + pPreprocessedSequence.getToken(i));
				cEntity++;
			} else if(Label.getCanonicalLabel(pPreprocessedSequence.getLabel(i)) == Label.UnitToken) {
				pEntityList.add(pPreprocessedSequence.getToken(i));
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
