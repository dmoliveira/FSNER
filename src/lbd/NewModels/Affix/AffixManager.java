package lbd.NewModels.Affix;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.NewModels.Affix.Affix.AffixType;
import lbd.SummirizedPattern.SummarizedPattern;

public class AffixManager implements Serializable{

	private static final long serialVersionUID = 1L;

	protected HashMap<String, Affix> affixMap;

	protected transient SummarizedPattern summarizedPattern;

	protected static final int THRESHOLD = 0;

	public AffixManager() {
		this(null);
	}

	public AffixManager(SummarizedPattern summarizedPattern) {
		affixMap = new HashMap<String, Affix>();
		this.summarizedPattern = summarizedPattern;
	}

	public void addToken(String token) {

		String tokenLowerCase = token.toLowerCase();

		int tokenSize = tokenLowerCase.length();

		//-- Prefix Size 2
		if(tokenSize > 2) {
			addAffix(tokenLowerCase, AffixType.PrefixSize2, 2);
		}

		//-- Prefix Size 3
		if(tokenSize > 3) {
			addAffix(tokenLowerCase, AffixType.PrefixSize3, 3);
		}

		//-- Suffix Size 1
		if(tokenSize > 1) {
			addAffix(tokenLowerCase, AffixType.SuffixSize1, 1);
		}

		//-- Suffix Size 2
		if(tokenSize > 2) {
			addAffix(tokenLowerCase, AffixType.SuffixSize2, 2);
		}

		//-- Suffix Size 3
		if(tokenSize > 3) {
			addAffix(tokenLowerCase, AffixType.SuffixSize3, 3);
		}

		//-- Suffix Size 4
		if(tokenSize > 4) {
			addAffix(tokenLowerCase, AffixType.SuffixSize4, 4);
		}
	}

	public void removeAffixBelowThreshold() {

		Iterator<Entry<String, Affix>> ite = affixMap.entrySet().iterator();
		Entry<String, Affix> entry;

		ArrayList<Entry<String, Affix>> affixToRemove = new ArrayList<Entry<String,Affix>>();

		while(ite.hasNext()) {

			entry = ite.next();

			if(entry.getValue().getFrequency() < THRESHOLD) {
				affixToRemove.add(entry);
			}
		}

		for(Entry<String, Affix> entryItem : affixToRemove) {
			affixMap.remove(entryItem.getKey());
		}
	}

	protected void addAffix(String token, AffixType affixType, int affixSize) {

		String affixValue = token.substring(0, affixSize);

		if(affixMap.containsKey(affixValue)) {
			affixMap.get(affixValue).addFrequency();
		} else {
			affixMap.put(affixValue, new Affix(affixValue, affixType));
		}

		//System.out.println(token + " ("+affixType.name()+") " + affixValue);
	}

	public String getAffix(String token, AffixType affixType) {

		String affix = "";
		String tokenLowerCase = token;//.toLowerCase();

		int tokenSize = tokenLowerCase.length();

		//-- Prefix Size 2
		if(affixType == AffixType.PrefixSize2 && tokenSize > 2) {
			affix = tokenLowerCase.substring(0, 2);
		} else if(affixType == AffixType.PrefixSize3 && tokenSize > 3) {
			affix = tokenLowerCase.substring(0, 3);
		} else if(affixType == AffixType.SuffixSize1 && tokenSize > 1) {
			affix = tokenLowerCase.substring(tokenSize-1, tokenSize);
		} else if(affixType == AffixType.SuffixSize2 && tokenSize > 2) {
			affix = tokenLowerCase.substring(tokenSize-2, tokenSize);
		} else if(affixType == AffixType.SuffixSize3 && tokenSize > 3) {
			affix = tokenLowerCase.substring(tokenSize-3, tokenSize);
		} else if(affixType == AffixType.SuffixSize4 && tokenSize > 4) {
			affix = tokenLowerCase.substring(tokenSize-4, tokenSize);
		}

		//System.out.println("T: " + token + " affixType(" + affixType.name() + ") " + affix);

		return(affix);
	}

	public int getAffixId(String affixValue) {

		int id = -1;
		Affix affix = affixMap.get(affixValue);

		if(affix != null) {
			id = affix.getId();
		}

		return(id);
	}

	public void readAffixManagerObject(String filename, AffixManager target) throws IOException, ClassNotFoundException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		AffixManager affixManager = (AffixManager) in.readObject();
		cloneAffixManager(target, affixManager);

		in.close();
	}

	private void cloneAffixManager(AffixManager target, AffixManager clone) {

		target.affixMap = clone.affixMap;
	}

	public void writeAffixManagerObject(String filename) throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
	}
}
