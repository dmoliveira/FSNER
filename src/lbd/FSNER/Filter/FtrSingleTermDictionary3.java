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
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.FSNER.Utils.Annotations.Comment;

@Comment(message="Official Filter")
public class FtrSingleTermDictionary3 extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	public static String sDictionaryDirectory;
	protected final int WINDOW_SIZE = 3;

	protected static ArrayList<HashMap<String, HashMap<String, Object>>> sDictionaryList;
	protected static ArrayList<String> sDictionaryNameList;
	protected static HashMap<String, Boolean> sDictionaryLoadMap;

	protected int mDictionaryNameIndex;

	protected AbstractDataPreprocessor mDataProcessor;

	//-- To optimize generateTermWindowSize
	protected static SequenceLabel sCurrentSequence;
	protected static int sCurrentIndex;
	protected static String [] sTermsWindowSizeList;

	public FtrSingleTermDictionary3(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, AbstractDataPreprocessor dataProcessor, int dictionaryNameIndex) {

		super(ClassName.getSingleName(FtrSingleTermDictionary3.class.getName()) +
				".dicIndex:" + dictionaryNameIndex,
				preprocessingTypeNameIndex, scoreCalculator);

		if(sDictionaryList == null) {
			sDictionaryList = new ArrayList<HashMap<String,HashMap<String, Object>>>();
			sDictionaryNameList = new ArrayList<String>();
			sDictionaryLoadMap = new HashMap<String, Boolean>();
		}

		//this.commonFilterName = "Dic";

		//this.commonFilterName = "Wrd" + preprocessingTypeNameIndex;

		this.mDataProcessor = dataProcessor;
		this.mDictionaryNameIndex = dictionaryNameIndex;
	}

	@Override
	public void initialize() {
		if(sDictionaryList.size() == 0) {
			//-- Load Dictionary Name List
			loadDictionaryNameList();

			//-- Load all dictionaries from the universal directory
			loadAllDictionary();
		}

		mActivityName += Symbol.SQUARE_BRACKET_LEFT + sDictionaryNameList.get(mDictionaryNameIndex).substring(
				sDictionaryNameList.get(mDictionaryNameIndex).lastIndexOf(Symbol.SLASH) + 1) + Symbol.SQUARE_BRACKET_RIGHT;
	}

	protected void loadDictionaryNameList() {

		File folder = new File(sDictionaryDirectory);
		File[] listOfFiles = folder.listFiles();

		String dictionaryFilenameAddress;

		for (int i = 0; i < listOfFiles.length; i++) {

			dictionaryFilenameAddress = sDictionaryDirectory + listOfFiles[i].getName();

			if (listOfFiles[i].isFile() && !sDictionaryLoadMap.containsKey(dictionaryFilenameAddress)) {
				sDictionaryNameList.add(dictionaryFilenameAddress);
				sDictionaryLoadMap.put(dictionaryFilenameAddress, false);
				//System.out.println("--" + listOfFiles[i].getName());
			}
		}
	}

	protected void loadAllDictionary() {

		for(String dictionaryFilenameAddress : sDictionaryNameList) {
			if(!sDictionaryLoadMap.get(dictionaryFilenameAddress)) {
				loadDictionary(dictionaryFilenameAddress);
				sDictionaryLoadMap.put(dictionaryFilenameAddress, true);
			}
		}
	}

	protected void loadDictionary(String dictionaryFilenameAddress) {

		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(dictionaryFilenameAddress), Parameters.dataEncoding));

			sDictionaryList.add(new HashMap<String, HashMap<String, Object>> ());
			HashMap<String, HashMap<String, Object>> dictionaryMap = sDictionaryList.get(sDictionaryList.size() - 1);

			String entry;
			String [] entryElement;

			String entryPreprocessed = Symbol.EMPTY;

			while((entry = in.readLine()) != null) {

				entryElement = entry.split(Symbol.SPACE);
				entryPreprocessed = Symbol.EMPTY;

				//-- Preprocess entry
				for(int i = 0; i < entryElement.length; i++) {

					entryElement[i] = mDataProcessor.preprocessingTerm(
							entryElement[i], -1).getTerm();

					entryPreprocessed +=  entryElement[i] + Symbol.SPACE;
				}

				if(entryPreprocessed.length() > 0) {

					entryPreprocessed = entryPreprocessed.substring(0, entryPreprocessed.length()-1);

					if(!dictionaryMap.containsKey(entryElement[0])) {
						dictionaryMap.put(entryElement[0], new HashMap<String, Object>());
					}

					dictionaryMap.get(entryElement[0]).put(entryPreprocessed, null);
				}
			}

			in.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

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
			SequenceLabel sequenceLabelProcessed, int index) {

		String id = Symbol.EMPTY;

		if(sCurrentSequence != sequenceLabelProcessed || sCurrentIndex != index) {
			sTermsWindowSizeList = generateTermsWindowSize(sequenceLabelProcessed, index);

			sCurrentSequence = sequenceLabelProcessed;
			sCurrentIndex = index;
		}

		HashMap<String, Object> entryDictionaryMap = sDictionaryList.get(
				mDictionaryNameIndex).get(sequenceLabelProcessed.getTerm(index));

		if(entryDictionaryMap != null) {
			for(int j = sTermsWindowSizeList.length - 1; j >= 0; j--) {

				if(sTermsWindowSizeList[j] == null) {
					continue;
				}

				/*if(LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index)))
					System.out.println("Ent: " + sequenceLabelProcessed.getTerm(index) + " dic: " + termsWindowSizeList[j]);*/

				if(entryDictionaryMap.containsKey(sTermsWindowSizeList[j])) {
					id = "id:" + this.mId + ".dic:" + mDictionaryNameIndex;
					break;
				}
			}
		}

		/*if(filterMode == FilterMode.inLabel && LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index))) {
			System.out.println(sequenceLabelProcessed.getTerm(index));
		}*/


		return (id);
	}

	protected String[] generateTermsWindowSize(SequenceLabel sequenceLabelProcessed, int index) {

		String term;
		String [] termsWindowSizeList = new String[WINDOW_SIZE + 1];

		for(int i = 0; i < termsWindowSizeList.length; i++) {

			termsWindowSizeList[i] = Symbol.EMPTY;

			for(int j = index; j < index + i + 1 && j < index + termsWindowSizeList.length && j < sequenceLabelProcessed.size(); j++) {

				term = sequenceLabelProcessed.getTerm(j);

				if(term.length() > 0) {
					termsWindowSizeList[i] += ((j != index && !sequenceLabelProcessed.getTerm(j).isEmpty())?
							Symbol.SPACE : Symbol.EMPTY) + sequenceLabelProcessed.getTerm(j);
				}
			}

			if(i > 0 && termsWindowSizeList[i - 1].equals(termsWindowSizeList[i])) {
				termsWindowSizeList[i] = null;
				break;
			}

			/*if(filterMode == FilterMode.inLabel && LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index)))
				System.out.println("*" + termsWindowSizeList[i] + "*");*/
		}

		return(termsWindowSizeList);
	}

	public static int getDictionaryListNumber() {

		File folder = new File(sDictionaryDirectory);
		File[] listOfFiles = folder.listFiles();

		int dictionaryNumberList = 0;

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				dictionaryNumberList++;
			}
		}

		return(dictionaryNumberList);
	}

	public static void Clear() {
		sDictionaryDirectory = null;
		sDictionaryList = null;
		sDictionaryNameList = null;
		sDictionaryLoadMap = null;
		sCurrentSequence = null;
		sCurrentIndex = 0;
		sTermsWindowSizeList = null;
	}

}
