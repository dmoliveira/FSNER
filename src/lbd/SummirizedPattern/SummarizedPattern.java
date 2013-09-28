package lbd.SummirizedPattern;

import iitb.CRF.DataSequence;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class SummarizedPattern implements Serializable{

	private static final long serialVersionUID = 1L;
	protected HashMap<String, Integer> summarizedPatternMap;
	protected int summarizedPatternId;

	protected static enum PatternType {isUpperCase, isLowerCase, isDigit, isSymbol};
	protected static String [] patternTypeMap = {"A", "a", "0", "-"};
	protected static char [] alphabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	protected static HashMap<Character, Object> vowelMap;
	protected static int [] vowelIndex = {0, 4, 8, 14, 20};

	protected static HashMap<Character, Integer> alphabetMap;

	public SummarizedPattern() {

		summarizedPatternMap = new HashMap<String, Integer>();
		summarizedPatternId = 0;

		alphabetMap = new HashMap<Character, Integer>();
		for(int i = 0; i < alphabet.length; i++) {
			alphabetMap.put(alphabet[i], i+1);
		}

		vowelMap = new HashMap<Character, Object>();
		for(int i = 0; i < vowelIndex.length; i++) {
			vowelMap.put(alphabet[vowelIndex[i]], null);
		}
	}

	public void addSequence(DataSequence data) {

		String summarizedTerm;

		for(int i = 0; i < data.length(); i++) {
			summarizedTerm = getPattern((String)data.x(i));
			summarizedPatternMap.put(summarizedTerm, ++summarizedPatternId);
		}
	}

	public String getPattern(String term) {

		String summarizedTerm = "";
		PatternType lastPatternType = null;
		char letter;

		for(int i = 0; i < term.length(); i++) {

			letter = term.charAt(i);

			if(Character.isDigit(letter)) {

				summarizedTerm += patternTypeMap[PatternType.isDigit.ordinal()];
				lastPatternType = PatternType.isDigit;
			}
			else if(Character.isUpperCase(letter)) {
				letter = Character.toLowerCase(letter);
				summarizedTerm += patternTypeMap[PatternType.isUpperCase.ordinal()];
				lastPatternType = PatternType.isUpperCase;
				//summarizedTerm += (alphabetMap.containsKey(letter) && vowelMap.containsKey(letter))? "v" : "c";
				//summarizedTerm += (alphabetMap.containsKey(letter))? ((alphabetMap.get(letter) < alphabet.length/2)? 1 : 2) : "";
			}
			else if(Character.isLowerCase(letter)) {
				summarizedTerm += patternTypeMap[PatternType.isLowerCase.ordinal()];
				lastPatternType = PatternType.isLowerCase;
				//summarizedTerm += (alphabetMap.containsKey(letter) && vowelMap.containsKey(letter))? "v" : "c";
				//summarizedTerm += (alphabetMap.containsKey(letter))? ((alphabetMap.get(letter) < alphabet.length/2)? 1 : 2) : "";
			} else {
				summarizedTerm += patternTypeMap[PatternType.isSymbol.ordinal()];
			}
		}

		return(summarizedTerm);
	}

	public int getSummarizedPatternId(String term) {

		String summmarizedTerm = getPattern(term);

		return((summarizedPatternMap.containsKey(summmarizedTerm))? summarizedPatternMap.get(summmarizedTerm) : -1);
	}

	public void readSummarizedPatternObject(String filename, SummarizedPattern target) throws IOException, ClassNotFoundException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		SummarizedPattern summarizedPattern = (SummarizedPattern) in.readObject();
		cloneSummarizedPattern(target, summarizedPattern);

		in.close();
	}

	private void cloneSummarizedPattern(SummarizedPattern target, SummarizedPattern clone) {

		target.summarizedPatternMap = clone.summarizedPatternMap;
	}

	public void writeSummarizedPatternObject(String filename) throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
	}

}
