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
import java.util.Map;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrSingleTermDictionary extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected final String DICTIONARY_UNIVERSAL_DIRECTORY = "./samples/data/bcs2010/AutoTagger/Dictionary/";

	protected static ArrayList<HashMap<String, Object>> dictionaryList;
	protected static ArrayList<String> dictionaryNameList;
	protected static HashMap<String, Boolean> dictionaryLoadMap;

	protected AbstractDataPreprocessor dataProcessor;

	public FtrSingleTermDictionary(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, AbstractDataPreprocessor dataProcessor) {

		super(ClassName.getSingleName(FtrSingleTermDictionary.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		if(dictionaryList == null) {
			dictionaryList = new ArrayList<HashMap<String,Object>>();
			dictionaryNameList = new ArrayList<String>();
			dictionaryLoadMap = new HashMap<String, Boolean>();
		}

		this.dataProcessor = dataProcessor;
	}

	@Override
	public void initialize() {

		//-- Load Dictionary Name List
		loadDictionaryNameList();

		//-- Load all dictionaries from the universal directory
		loadAllDictionary();
	}

	protected void loadDictionaryNameList() {

		File folder = new File(DICTIONARY_UNIVERSAL_DIRECTORY);
		File[] listOfFiles = folder.listFiles();

		String dictionaryFilenameAddress;

		for (int i = 0; i < listOfFiles.length; i++) {

			dictionaryFilenameAddress = DICTIONARY_UNIVERSAL_DIRECTORY + listOfFiles[i].getName();

			if (listOfFiles[i].isFile() && !dictionaryLoadMap.containsKey(dictionaryFilenameAddress)) {
				dictionaryNameList.add(dictionaryFilenameAddress);
				dictionaryLoadMap.put(dictionaryFilenameAddress, false);
				//System.out.println("--" + listOfFiles[i].getName());
			}
		}
	}

	protected void loadAllDictionary() {

		for(String dictionaryFilenameAddress : dictionaryNameList) {
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

			dictionaryList.add(new HashMap<String, Object> ());

			String entry;
			String [] entryElement;
			String entryPreprocessed;

			HashMap<String, Object> dictionaryMap = dictionaryList.get(dictionaryList.size() - 1);

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
		Map<String, Object> vDictionaryMap;

		for(int i = 0; i < dictionaryNameList.size(); i++) {

			vDictionaryMap = dictionaryList.get(i);

			if(vDictionaryMap.containsKey(pPreprocessedSequence.getToken(pIndex))) {
				vId = "id:" + this.mId + ".dic:" + dictionaryNameList.get(i);
				break;
			}
		}

		return vId;
	}

}
