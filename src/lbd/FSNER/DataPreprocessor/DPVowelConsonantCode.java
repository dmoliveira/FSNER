package lbd.FSNER.DataPreprocessor;

import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;

public class DPVowelConsonantCode extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;

	protected int blockSize;
	protected HashMap<String, Integer> blockMap;
	protected ArrayList<String> blockCodeList;

	protected HashMap<Character, Object> consonantMap;
	protected Character [] consonantList = {'B', 'C', 'D', 'F', 'G', 'H', 'J',
			'K', 'L', 'M', 'N', 'P', 'Q', 'R',
			'S', 'T', 'V', 'W', 'Y', 'X', 'Z'};

	protected HashMap<Character, Object> vowelMap;
	protected Character [] vowelList = {'A', 'E', 'I', 'O', 'U'};

	public DPVowelConsonantCode(int blockSize) {
		super(ClassName.getSingleName(DPVowelConsonantCode.class.getName())+".BlcSz:" + blockSize, null);

		this.blockSize = blockSize;
	}

	@Override
	public void initialize() {

		consonantMap = new HashMap<Character, Object>();
		for(int i = 0; i < consonantList.length; i++) {
			consonantMap.put(consonantList[i], null);
		}

		vowelMap = new HashMap<Character, Object>();
		for(int i = 0; i < vowelList.length; i++) {
			vowelMap.put(vowelList[i], null);
		}

		generateBlockCodeList();

		generateBlockMap();
	}

	protected void generateBlockCodeList() {

		blockCodeList = new ArrayList<String>();
		blockCodeList.add("0"); // Consonant
		blockCodeList.add("1"); // Vowel

	}

	protected void generateBlockMap() {

		ArrayList<String> blockList = new ArrayList<String>();
		int blockListSize = 0;

		for(String blockCode : blockCodeList) {
			blockList.add(blockCode);
		}

		for(int b = 2; b <= blockSize; b++) {

			blockListSize = blockList.size();

			for(int i = 0; i < blockListSize; i++) {

				for(String blockCode : blockCodeList) {
					blockList.add(blockList.get(i) + blockCode);
					//System.out.println(blockList.get(i) + blockCode);
				}
			}
		}

		int id = 0;
		blockMap = new HashMap<String, Integer>();

		for(String block : blockList) {
			blockMap.put(block, ++id);
		}
	}

	@Override
	public String preprocessingToken(String pToken, int pLabel) {

		String vProcessedTerm = "";
		int vCandidateBlockSize;

		for(int i = 0; i < pToken.length(); i++) {

			vCandidateBlockSize = (pToken.length()-i > blockSize)? blockSize : pToken.length()-i;
			vProcessedTerm += convertToCode(pToken.substring(i, i+vCandidateBlockSize)) + Symbol.DOT;
			//System.out.println("Bs:" + candidateBlockSize + " " + term.substring(i, i+candidateBlockSize) + " " + processedTerm);

			i += vCandidateBlockSize-1;
		}

		return vProcessedTerm;
	}

	//-- Only Vowel Consonant for now
	public String convertToCode(String term) {

		String termEncoded = "";
		String termUpperCase = term.toUpperCase();

		for(int i = 0; i < termUpperCase.length(); i++) {
			if(isConsonant(termUpperCase, i)) {
				termEncoded += "0";
			} else {
				termEncoded += "1";
			}
		}

		return("" + blockMap.get(termEncoded));

	}

	public boolean isConsonant(String termUpperCase, int index) {

		return(consonantMap.containsKey(termUpperCase.charAt(index)));
	}

	public boolean isVowel(String termUpperCase, int index) {

		return(vowelMap.containsKey(termUpperCase.charAt(index)));
	}

}
