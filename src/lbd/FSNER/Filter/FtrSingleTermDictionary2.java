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

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrSingleTermDictionary2 extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected static final String DICTIONARY_UNIVERSAL_DIRECTORY = "./samples/data/bcs2010/AutoTagger/Dictionary/";

	protected static ArrayList<HashMap<String, Object>> mDictionaryList;
	protected static ArrayList<String> mDictionaryNameList;
	protected static HashMap<String, Boolean> dictionaryLoadMap;
	protected int dictionaryNameIndex;

	protected AbstractDataPreprocessor dataProcessor;

	public FtrSingleTermDictionary2(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, AbstractDataPreprocessor dataProcessor, int dictionaryNameIndex) {

		super(ClassName.getSingleName(FtrSingleTermDictionary2.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		if(mDictionaryList == null) {
			mDictionaryList = new ArrayList<HashMap<String,Object>>();
			mDictionaryNameList = new ArrayList<String>();
			dictionaryLoadMap = new HashMap<String, Boolean>();
		}

		this.dataProcessor = dataProcessor;
		this.dictionaryNameIndex = dictionaryNameIndex;
	}

	@Override
	public void initialize() {
		if(mDictionaryList.size() == 0) {
			//-- Load Dictionary Name List
			loadDictionaryNameList();

			//-- Load all dictionaries from the universal directory
			loadAllDictionary();
		}

		mActivityName += Symbol.SQUARE_BRACKET_LEFT + mDictionaryNameList.get(dictionaryNameIndex).substring(
				mDictionaryNameList.get(dictionaryNameIndex).lastIndexOf(Symbol.SLASH) + 1) + Symbol.SQUARE_BRACKET_RIGHT;
	}

	protected void loadDictionaryNameList() {

		File folder = new File(DICTIONARY_UNIVERSAL_DIRECTORY);
		File[] listOfFiles = folder.listFiles();

		String dictionaryFilenameAddress;

		for (int i = 0; i < listOfFiles.length; i++) {

			dictionaryFilenameAddress = DICTIONARY_UNIVERSAL_DIRECTORY + listOfFiles[i].getName();

			if (listOfFiles[i].isFile() && !dictionaryLoadMap.containsKey(dictionaryFilenameAddress)) {
				mDictionaryNameList.add(dictionaryFilenameAddress);
				dictionaryLoadMap.put(dictionaryFilenameAddress, false);
				//System.out.println("--" + listOfFiles[i].getName());
			}
		}
	}

	protected void loadAllDictionary() {

		for(String dictionaryFilenameAddress : mDictionaryNameList) {
			if(!dictionaryLoadMap.get(dictionaryFilenameAddress)) {
				loadDictionary(dictionaryFilenameAddress);
				dictionaryLoadMap.put(dictionaryFilenameAddress, true);
			}
		}
	}

	protected void loadDictionary(String dictionaryFilenameAddress) {

		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(dictionaryFilenameAddress), Parameters.DataHandler.mDataEncoding));

			mDictionaryList.add(new HashMap<String, Object> ());

			String entry;
			String [] entryElement;
			String entryPreprocessed;

			HashMap<String, Object> dictionaryMap = mDictionaryList.get(mDictionaryList.size() - 1);

			while((entry = in.readLine()) != null) {

				entryElement = entry.split(Symbol.SPACE);

				for(int i = 0; i < entryElement.length; i++) {

					entryPreprocessed = dataProcessor.preprocessingToken(entryElement[i], -1);
					dictionaryMap.put(entryPreprocessed, null);
					//System.out.println(entryPreprocessed);
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
	public void loadActionBeforeSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

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
		HashMap<String, Object> vDictionaryMap;

		for(int i = 0; i < mDictionaryNameList.size(); i++) {

			vDictionaryMap = mDictionaryList.get(i);

			if(vDictionaryMap.containsKey(pPreprocessedSequence.getToken(pIndex))) {
				vId = "id:" + this.mId + ".dic:" + mDictionaryNameList.get(i);
				break;
			}
		}

		return (vId);
	}

	public static int getDictionaryListNumber() {

		File vFolder = new File(DICTIONARY_UNIVERSAL_DIRECTORY);
		File[] vListOfFiles = vFolder.listFiles();

		int vDictionaryNumberList = 0;

		for (int i = 0; i < vListOfFiles.length; i++) {
			if (vListOfFiles[i].isFile()) {
				vDictionaryNumberList++;
			}
		}

		return(vDictionaryNumberList);
	}

}
