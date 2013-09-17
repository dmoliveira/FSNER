package lbd.Model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class VerbalSuffixHashMap implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int id;
	public HashMap<String, Integer> verbalSuffixHashMap;
	
	public VerbalSuffixHashMap() {
		verbalSuffixHashMap = new HashMap<String, Integer>();
		id = 0;
	}
	
	public int getId(String token) {
		return(verbalSuffixHashMap.get(token));
	}
	
	public int getId() {
		return ++id;
	}
	
	public void readVerbalSuffixObject(String filename, VerbalSuffixHashMap target) throws IOException, ClassNotFoundException {
    	
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		VerbalSuffixHashMap verbalSuffixList = (VerbalSuffixHashMap) in.readObject();
		cloneVerbalSuffixList(target, verbalSuffixList);
		
		in.close();
    }
	
	private void cloneVerbalSuffixList (VerbalSuffixHashMap target, VerbalSuffixHashMap clone) {
		
		target.id = clone.id;
		target.verbalSuffixHashMap = clone.verbalSuffixHashMap;
		
	}
	
	public void writeVerbalSuffixObject(String filename) throws IOException {
    	
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
    }
}
