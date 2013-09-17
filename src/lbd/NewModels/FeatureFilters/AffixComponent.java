package lbd.NewModels.FeatureFilters;

import iitb.CRF.DataSequence;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import lbd.FSNER.Utils.Symbol;

public class AffixComponent implements Serializable{

	private static final long serialVersionUID = 1L;
	protected int id;

	public static enum AffixType {Prefix, Suffix, Infix};
	protected AffixType affixType;

	protected HashMap<String, Integer> affixMap;
	protected int affixSize;

	protected static final int MINIMUM_TERM_SIZE = 4;//4 (Standard)
	protected String filename;

	public AffixComponent(AffixType affixType, int affixSize) {
		affixMap = new HashMap<String, Integer>();

		this.affixType = affixType;
		this.affixSize = affixSize;
		this.filename = "./Data/Temp/CRFFeaturesAsFilters/AffixComp-" + affixType.name() + "." + affixSize;
		this.id = 0;
	}

	public void addAffixes(String term) {

		String termAffix = generateAffix(affixType, term, affixSize);

		if(termAffix.length() == affixSize) {
			affixMap.put(termAffix, ++id);
		}
	}

	public static String generateAffix(AffixType affixType, String term, int affixSize) {

		if(term.length() >= MINIMUM_TERM_SIZE && term.length() > affixSize + 2) {
			if(affixType == AffixType.Prefix) {
				return(generatePrefix(term, affixSize));
			} else if(affixType == AffixType.Suffix) {
				return(generateSuffix(term, affixSize));
			} else if(affixType == AffixType.Infix && 2 * affixSize < term.length()) {
				return(generateInfix(term, affixSize));
			}
		}

		return(Symbol.EMPTY);
	}

	public static String generatePrefix(String term, int affixSize) {
		return(term.substring(0, affixSize));
	}

	public static String generateSuffix(String term, int affixSize) {
		return(term.substring(term.length() - affixSize));
	}

	public static String generateInfix(String term, int affixSize) {

		int meanIndex = term.length()/2;

		return(term.substring(meanIndex - affixSize, meanIndex + affixSize));
	}

	protected int getSequenceInstanceIdSub(DataSequence sequence, int index) {

		int id = -1;
		String term = (String)sequence.x(index);
		String termAffix = generateAffix(affixType, term, affixSize);

		if(affixMap.containsKey(termAffix)) {
			id = affixMap.get(termAffix);
		}

		return (id);
	}

	public void read(AffixComponent target) throws IOException, ClassNotFoundException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		AffixComponent clone = (AffixComponent) in.readObject();
		clone(target, clone);

		in.close();
	}

	private void clone(AffixComponent target, AffixComponent clone) {
		target.affixMap = clone.affixMap;
	}

	public void write() throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
	}

}
