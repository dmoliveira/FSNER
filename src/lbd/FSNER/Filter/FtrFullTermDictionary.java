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
import lbd.FSNER.Filter.Component.Dictionary;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrFullTermDictionary extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected final String DICTIONARY_UNIVERSAL_DIRECTORY = "./samples/data/bcs2010/AutoTagger/Dictionary/";

	protected static ArrayList<Dictionary> dictionaryList;
	protected static ArrayList<String> dictionaryNameList;
	protected static HashMap<String, Boolean> dictionaryLoadMap;

	protected AbstractDataPreprocessor dataProcessor;

	public FtrFullTermDictionary(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, AbstractDataPreprocessor dataProcessor) {

		super(ClassName.getSingleName(FtrFullTermDictionary.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		if(dictionaryList == null) {
			dictionaryList = new ArrayList<Dictionary>();
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

			dictionaryList.add(new Dictionary (dictionaryFilenameAddress));

			String entry;
			String [] entryElement;

			Dictionary dictionary = dictionaryList.get(dictionaryList.size() - 1);

			while((entry = in.readLine()) != null) {

				entryElement = entry.split(Symbol.SPACE);

				for(int i = 0; i < entryElement.length; i++) {
					entryElement[i] = dataProcessor.preprocessingTerm(entryElement[i], -1).getTerm();
				}

				dictionary.addEntry(entryElement);
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
	protected String getSequenceInstanceIdSub(ISequence pSequence,
			SequenceLabel sequenceLabelProcessed, int index) {

		String id = Symbol.EMPTY;
		Dictionary dictionary;

		for(int i = 0; i < dictionaryNameList.size(); i++) {

			dictionary = dictionaryList.get(i);

			if(dictionary.isTermPartFromEntityEntryInDictionary(sequenceLabelProcessed, index)) {
				id = "id:" + this.mId + ".dic:" + dictionaryNameList.get(i);
				break;
			}
		}

		return (id);
	}

}
