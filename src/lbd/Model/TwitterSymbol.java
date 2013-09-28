package lbd.Model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class TwitterSymbol implements Serializable{

	private static final long serialVersionUID = 1L;

	protected HashMap<String, Integer> twitterSymbolMap;
	protected int symbolId;

	protected final String [] TWITTER_SYMBOL = {"#", "@"};

	public TwitterSymbol() {
		twitterSymbolMap = new HashMap<String, Integer>();

		symbolId = 0;
	}

	public void addTwitterSymbol(String term) {
		if(term.startsWith(TWITTER_SYMBOL[0]) || term.startsWith(TWITTER_SYMBOL[1])) {
			twitterSymbolMap.put(term, ++symbolId);
		}
	}

	public int getTwitterSymbol(String term) {
		return(twitterSymbolMap.containsKey(term)? twitterSymbolMap.get(term) : -1);
	}

	public void readTwitterSymbolObject(String filename, TwitterSymbol target) throws IOException, ClassNotFoundException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		TwitterSymbol twitterSymbol = (TwitterSymbol) in.readObject();
		cloneTwitterSymbol(target, twitterSymbol);

		in.close();
	}

	private void cloneTwitterSymbol(TwitterSymbol target, TwitterSymbol clone) {

		target.twitterSymbolMap = clone.twitterSymbolMap;
	}

	public void writeTwitterSymbolObject(String filename) throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
	}


}
