package lbd.OutOfVocabularySupport;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.NewModels.Context.ContextManager;

public class OOVSupportContext implements Serializable{

	private static final long serialVersionUID = 1L;

	protected HashMap<String, HashMap<String, Integer>> oovSupportContextMap;

	protected transient double inVocab = 0;
	protected transient double outVocab = 0;

	protected static transient boolean isDebugActive = false;

	public OOVSupportContext() {
		oovSupportContextMap = new HashMap<String, HashMap<String, Integer>>();
	}

	public void addContext(String [] sequence, int pos) {

		String mainTerm = sequence[pos];
		String encodedContext = ContextManager.getEncodedContext(sequence, pos);

		ArrayList<String> affix = new ArrayList<String>();
		affix.add(getPrefixEncodedWindow3(encodedContext, pos));
		affix.add(getSuffixEncodedWindow3(encodedContext, pos));

		for(String affixEncodedWindow : affix) {
			if(!affixEncodedWindow.isEmpty()) {

				if(!oovSupportContextMap.containsKey(affixEncodedWindow)) {
					oovSupportContextMap.put(affixEncodedWindow, new HashMap<String, Integer>());
				}

				HashMap<String, Integer> contextMap = oovSupportContextMap.get(affixEncodedWindow);

				if(!contextMap.containsKey(mainTerm)) {
					contextMap.put(mainTerm, 1);
				}
				else {
					contextMap.put(mainTerm, 1); //-- counter not implemented
				}
			}
		}

	}

	protected static String getPrefixEncodedWindow3 (String encodedContext, int pos) {

		String prefixContext = "";

		if(encodedContext.charAt(0) != ContextManager.DELIMITER_AFFIX.charAt(0)) {

			String [] prefixElement = (encodedContext.split(ContextManager.DELIMITER_AFFIX)[0]).split(ContextManager.DELIMITER_SPLIT);

			if(prefixElement.length > 2) {
				for(int i = prefixElement.length-3; i < prefixElement.length; i++) {
					prefixContext += prefixElement[i].toLowerCase() + ContextManager.DELIMITER_TERM;
				}
			}

		}

		if(isDebugActive && !prefixContext.isEmpty()) {
			System.out.println("Prefix Context: " + prefixContext);
		}

		return(prefixContext);
	}

	protected static String getSuffixEncodedWindow3 (String encodedContext, int pos) {

		String suffixContext = "";

		if(encodedContext.charAt(encodedContext.length()-1) != ContextManager.DELIMITER_AFFIX.charAt(0)) {

			String [] suffixElement = (encodedContext.split(ContextManager.DELIMITER_AFFIX)[1]).split(ContextManager.DELIMITER_SPLIT);

			if(suffixElement.length > 2) {
				for(int i = pos+1; i < suffixElement.length && i < pos+3; i++) {
					suffixContext += suffixElement[i].toLowerCase() + ContextManager.DELIMITER_TERM;
				}
			}

		}

		if(isDebugActive && !suffixContext.isEmpty()) {
			System.out.println("Suffix Context: " + suffixContext);
		}

		return(suffixContext);
	}

	public HashMap<String, Integer> getSimilarTerm(String [] sequence, int pos) {

		String encodedContext = ContextManager.getEncodedContext(sequence, pos);
		String prefixEncodedWindow3 = getPrefixEncodedWindow3(encodedContext, pos);
		String suffixEncodedWindow3 = getSuffixEncodedWindow3(encodedContext, pos);

		HashMap<String, Integer> candidateTermsList = (!prefixEncodedWindow3.isEmpty())?
				oovSupportContextMap.get(prefixEncodedWindow3) : null;

				if(!suffixEncodedWindow3.isEmpty()) {
					if(candidateTermsList != null) {
						candidateTermsList.putAll(oovSupportContextMap.get(prefixEncodedWindow3));
					} else {
						candidateTermsList = oovSupportContextMap.get(suffixEncodedWindow3);
					}
				}

				if(candidateTermsList != null && (candidateTermsList.size() > 1 ||
						(!candidateTermsList.containsKey(sequence[pos].toLowerCase())))) {
					inVocab++;
				} else {
					outVocab++;
				}

				return(candidateTermsList);
	}

	/*******************************************************************
	 * 
	 * Read and Write Object
	 * 
	 *******************************************************************/

	public void readContextAnalysisObject2(String filename, OOVSupportContext target) throws IOException, ClassNotFoundException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		OOVSupportContext oovSupportContext = (OOVSupportContext) in.readObject();
		cloneOOVSupportContext(target, oovSupportContext);

		in.close();
	}

	public void readContextAnalysisObject(String filename) throws IOException, ClassNotFoundException {

		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "ISO-8859-1"));

		String line;
		String [] lineElement;
		String [] termElement;
		String [] termUnitElement;

		HashMap<String, Integer> context;

		while((line = in.readLine()) != null && !line.isEmpty()) {

			lineElement = line.split("   ");

			oovSupportContextMap.put(lineElement[0], new HashMap<String, Integer>());
			context = oovSupportContextMap.get(lineElement[0]);

			termElement = lineElement[1].split("  ");

			for(int i = 0; i < termElement.length; i++) {

				termUnitElement = termElement[i].split(" ");
				context.put(termUnitElement[0], Integer.parseInt(termUnitElement[1]));
			}
		}

		in.close();
	}

	private void cloneOOVSupportContext(OOVSupportContext target, OOVSupportContext clone) {

		target.oovSupportContextMap = clone.oovSupportContextMap;
	}

	public void writeContextAnalysisObject2(String filename) throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();

		System.out.println("OOVSCxt OutVocab: "+ (int)outVocab + "(" + (new DecimalFormat("#.##")).format(100*outVocab/(inVocab+outVocab)) + "%)" +
				", InVocab: " + (int)inVocab + "(" + (new DecimalFormat("#.##")).format(100*inVocab/(inVocab+outVocab)) + "%)");
	}

	public void writeContextAnalysisObject(String filename) throws IOException {

		Writer out = new OutputStreamWriter(new FileOutputStream(filename), "ISO-8859-1");


		Iterator<Entry<String, HashMap<String, Integer>>> iteCxtMap = oovSupportContextMap.entrySet().iterator();
		Entry<String, HashMap<String, Integer>> entry1;

		while(iteCxtMap.hasNext()) {

			entry1 = iteCxtMap.next();
			out.write(entry1.getKey() + "   ");

			Iterator<Entry<String, Integer>> iteCxt = entry1.getValue().entrySet().iterator();
			Entry<String, Integer> entry2;

			while(iteCxt.hasNext()) {

				entry2 = iteCxt.next();
				out.write(entry2.getKey().trim() + " " + entry2.getValue() + "  ");
			}

			out.write("\n");
		}

		out.flush();
		out.close();

		System.out.println("OOVSCxt OutVocab: "+ (int)outVocab + "(" + (new DecimalFormat("#.##")).format(100*outVocab/(inVocab+outVocab)) + "%)" +
				", InVocab: " + (int)inVocab + "(" + (new DecimalFormat("#.##")).format(100*inVocab/(inVocab+outVocab)) + "%)");
	}

}
