package lbd.NewModels.Token;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class NGram implements Serializable {

	private static final long serialVersionUID = 1L;

	protected int nGramId;
	protected HashMap<String, Integer> nGramMap;

	public NGram() {

		nGramMap = new HashMap<String, Integer>();

		nGramId = 0;
	}

	public void addToken(String token) {
		addAllAnyNGram(token.toLowerCase());
	}

	protected void addAllAnyNGram(String token) {

		String ngram;

		for(int i = 0; i < token.length(); i++) {
			for(int j = i+1; j <= token.length(); j++) {
				ngram = token.substring(i,j);

				if(!nGramMap.containsKey(ngram)) {
					nGramMap.put(ngram, ++nGramId);
				}
			}
		}
	}

	public int getNGramId(String token, int start, int end) {

		int id = -1;

		String ngram = "";

		if(token.length() > start && token.length() >= end) {
			ngram = token.substring(start, end);
		}

		if(!ngram.isEmpty() && nGramMap.containsKey(ngram)) {
			id = nGramMap.get(ngram);
		}

		return(id);
	}

	/*************************************************************************
	 * 
	 * Auxiliary Methods
	 * 
	 *************************************************************************/

	public void readNGramObject(String filename, NGram target) throws IOException, ClassNotFoundException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		NGram nGram = (NGram) in.readObject();
		cloneNGram(target, nGram);

		in.close();
	}

	private void cloneNGram(NGram target, NGram clone) {

		target.nGramMap = clone.nGramMap;
	}

	public void writeNGramObject(String filename) throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
	}

}
