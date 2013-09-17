package lbd.AutoTagger;

import iitb.CRF.DataSequence;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.LabelMap;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.Model.ContextToken;
import lbd.Model.SupportContext;
import lbd.Utils.POSTag;
import lbd.Utils.QuickSort;
import lbd.Utils.TFIDF;
import lbd.Utils.Utils;
import lbd.Utils.TFIDF.TFIDFElement;

public class SelectByContext extends SelectEntitiesByContext implements Serializable {

	private static final long serialVersionUID = 1L;
	protected SelectByPOSTag selByPOSTag;
	protected HashMap<String, POSTag> posTagMap;
	
	protected ArrayList<ContextToken> contextUsedList;
	protected ArrayList<String> termList;
	protected TFIDF tFIDF; 
	
	protected double threshould;
	protected double maxCorrectHits;
	
	public SelectByContext(SupportContext supportContext, double threshould) {
		super(supportContext);
		acronym = "-SelByCxt";
		this.threshould = threshould;
		tFIDF = new TFIDF();
		termList = new ArrayList<String>();
		
		selByPOSTag = new SelectByPOSTag(supportContext, threshould);
	}
	
	public SelectByContext(String inputFilenameAddress, int windowSize,
			boolean considerOnlyEntities, double threshould) {
		super(inputFilenameAddress, windowSize, considerOnlyEntities);
		acronym = "-SelByCxt";
		this.threshould = threshould;
		tFIDF = new TFIDF();
		termList = new ArrayList<String>();
		
		selByPOSTag = new SelectByPOSTag(inputFilenameAddress, windowSize, considerOnlyEntities, threshould);
	}
	
	public String analyzeContext (String inputFilenameAddress, String testFilenameAddress) {

		this.inputFilenameAddress = inputFilenameAddress;
		outputFilenameAddress = generateOutputFilenameAddress(inputFilenameAddress);	
		logOutputFilenameAddress = generateLogOutputFilenameAddress(outputFilenameAddress);
		
		//-- Generate POSTag
		selByPOSTag.analyzePOSTagContext(inputFilenameAddress, testFilenameAddress);
		
		//-- Analyze the test filename with the POS tag was assigned correct the entities terms
		analyzeContextInTestFile(testFilenameAddress);
		
		//-- Sort the posTag elements by decrescent correct hits
		contextUsedList = new ArrayList<ContextToken>();
		sortByCorrectHitsContext(contextUsedList);
		
		//-- Write the log file with the analysis of posTag
		//writeAnalysisContext(logOutputFilenameAddress);
		
		return(outputFilenameAddress);
	}
	
	protected void analyzeContextInTestFile(String testFilenameAddress) {
		
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(inputFilenameAddress,
				FileType.TRAINING, false);
		
		int stateSequence;
		
		String[] sequenceList;
		DataSequence sequence;
		ContextToken context = null;
		String contextKey;
		
		for (inputSequenceSet.startScan(); inputSequenceSet.hasNext();) {
			
			sequence = inputSequenceSet.next();
			sequenceList = Utils.convertSequenceToLowerCase(sequence, sequence.length());
			
			for(int i = 0; i < sequence.length(); i++) {
				
				/** Consider only prefix **/
				/*contextKey = supportContext.generateLeftPrefixKeyFromSequence(sequenceList, i, false);
				context = supportContext.getFastAccessPrefixContextList().get(contextKey);*/
				
				/** Consider prefix and suffix **/
				contextKey = supportContext.generateKeyFromSequence(sequenceList, i, false);
				context = supportContext.getFastAccessContextList().get(contextKey);
				
				if(context != null) {
					
					context.addFrequency();
					stateSequence = sequence.y(i);
					
					//BILOU Entities (0-B, 1-I, 2-L and 4-U)
					if(stateSequence >= 0 && stateSequence <= 4 && stateSequence != 3)
						context.addCorrectHit();
					else if(stateSequence == 3)
						context.addWrongHit();
				}
			}
		}
	}
	
	protected void sortByCorrectHitsContext(ArrayList<ContextToken> contextList) {
		
		int contextIndex = 0;
		
		Double [] correctHitsContext = new Double[supportContext.getFastAccessPrefixContextList().size()];
		ContextToken context;
		
		Collection<Entry<String, ContextToken>> collection = supportContext.getFastAccessPrefixContextList().entrySet();
		Iterator<Entry<String, ContextToken>> ite = collection.iterator();
		
		//for(ContextToken context : supportContext.getContextList()) {
		while(ite.hasNext()) {
			
			context = ite.next().getValue();
			
			if(context.getPrefixSize() > 0) {
				correctHitsContext[contextIndex++] = (100.0*context.getCorrectHits())/(context.getCorrectHits()+context.getWrongHits());
				context.setPercentageCorrectHits(correctHitsContext[contextIndex -1]);
				context.setPercentageWrongHits(100.0 - correctHitsContext[contextIndex -1]);
				
				if(context.getCorrectHits() > maxCorrectHits && context.getPercentageCorrectHits() >= threshould * 100.0)
					maxCorrectHits = context.getCorrectHits();
			}
			
			contextList.add(context);
		}		
		
		//QuickSort.sort(correctHitsContext, contextList);
	}
	
	protected void writeAnalysisContext(String logOutputFilenameAddress) {
		
		ContextToken context;
		String logOutputMessage;
		DecimalFormat decFormat = new DecimalFormat("#.##");
		
		int contextNumber = 0;
		int correctHits;
		int wrongHits;
		String percentageCorrectHits;
		String percentageWrongHits;
		
		try {
			
			logOut = new OutputStreamWriter(new FileOutputStream(logOutputFilenameAddress), ENCODE_USED);
			
			for(int i = contextUsedList.size()-1; i >= 0; i--) {
				
				context = contextUsedList.get(i);
				
				correctHits = context.getCorrectHits();
				wrongHits = context.getWrongHits();
				percentageCorrectHits = decFormat.format((100.0*correctHits)/(correctHits+wrongHits));
				percentageWrongHits = decFormat.format((100.0*wrongHits)/(correctHits+wrongHits));
				
				logOutputMessage = ++contextNumber + ". ";
				logOutputMessage += context.getKey() + " [" + context.getTokenValue() + "](";
				logOutputMessage += context.getAllPrefixInOrder() + ") F: " + context.getFrequency();
				logOutputMessage += ", C: " + correctHits + "(" + percentageCorrectHits + "%)";
				logOutputMessage += ", W: " + wrongHits + "(" + percentageWrongHits + "%)";
				
				logOut.write(logOutputMessage + "\n");
			}
			
			logOut.flush();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void writeSequence(String [] sequence, DataSequence seq,
			Writer out, SupportContext supportContext) throws IOException {
		
		String contextKey;
		String posTagKey;
		ContextToken context = null;
		
		String label = "";
		String [] sequenceList = Utils.convertSequenceToLowerCase(seq, seq.length());
		
		for(int i = 0; i < sequence.length; i++) {
			
			posTagKey = supportContext.generateLeftPrefixPOSTagKeyFromSequence(seq, i);
			contextKey = supportContext.generateLeftPrefixKeyFromSequence(sequenceList, i, false);
			context = supportContext.getFastAccessPrefixContextList().get(contextKey);
			
			if(context != null && seq.y(i) == 3 && isContextAboveOrEqualsThreshould(context, posTagKey)) {
				
				label = LabelMap.getLabelNamePOSTagPTBR(context.getToken().getState());
				numberEntitiesSelected++;
				
				termList.add(sequenceList[i]);
				
				logOut.write(numberEntitiesSelected + ". " + contextKey + " [" + context.getTokenValue()  + "] (");
				logOut.write(context.getAllPrefixInOrder()  +  ") " + sequence[i] + "|" + label);
				logOut.write(" F: " + context.getFrequency());
				logOut.write(", C: " + context.getCorrectHits() + "(" + context.getPercentageCorrectHits() + "%)");
				logOut.write(", W: " + context.getWrongHits() + "(" + context.getPercentageWrongHits() + "%)\n");
				//logOut.write(" OldLabel: " + LabelMap.getLabelNamePOSTagPTBR(seq.y(i)) + "\n");
			} else {
				label = LabelMap.getLabelNamePOSTagPTBR(seq.y(i));
			}
			
			out.write(sequence[i] + "|" + label + "\n");
		}
		
		out.write("\n");
	}
	
	protected boolean isContextAboveOrEqualsThreshould(ContextToken context, String posTagKey) {
		
		/*return(context.getPercentageCorrectHits() >= (threshould * 100.0) &&
				context.getCorrectHits() > 0 && 
				selByPOSTag.getPOSTagElement(posTagKey) != null);*/
		
		double cH = context.getPercentageCorrectHits();
		double cHMax = context.getCorrectHits()/maxCorrectHits;
		System.out.println("Key : " + posTagKey + " Ratio : " + cH * cHMax);
		
		return(cH * cHMax >= (threshould * 100.0) && 
				context.getCorrectHits() > 0 && 
				selByPOSTag.getPOSTagElement(posTagKey) != null);
		
	}
	
	public boolean isContextAboveOrEqualsThreshould(ContextToken context) {
		
		double cH = context.getPercentageCorrectHits();
		double cHMax = context.getCorrectHits()/maxCorrectHits;
		
		return(context.getCorrectHits() >= 2);
		//return(cH * cHMax >= (threshould * 100.0) && context.getCorrectHits() >= 0);
		//return(cH * cHMax >= (threshould * 100.0) && context.getCorrectHits() >= 5);
	}
	
	protected void finalizeSelection() {
		
		try {
			logOut.write("\n*** Showing the imparse ***\n");
			
			tFIDF.analyzeStrategy(termList, this.inputFilenameAddress);
			
			for(TFIDFElement tfIDFElement : tFIDF.gettFIDFList())
				logOut.write(tfIDFElement.getTerm() + " : " + tfIDFElement.getFrequency()  + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readSelectByContextObject (String filename, SelectByContext target) throws IOException, ClassNotFoundException {
    	
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		SelectByContext clone = (SelectByContext) in.readObject();
		cloneSelectByContext(target, clone);
		
		in.close();
    }
	
	private void cloneSelectByContext(SelectByContext target, SelectByContext clone) {
		
		target.inputFilenameAddress = clone.inputFilenameAddress;
	    target.outputFilenameAddress = clone.outputFilenameAddress;
	    target.logOutputFilenameAddress = clone.logOutputFilenameAddress;
	    target.contextUsedList = clone.contextUsedList;
	    target.supportContext = clone.supportContext;
	    target.termList = clone.termList;
	    target.threshould = clone.threshould;
	    target.maxCorrectHits = clone.maxCorrectHits;
	    target.tFIDF = clone.tFIDF;
	    target.posTagMap = clone.posTagMap;
	    target.selByPOSTag = clone.selByPOSTag;
	    target.numberEntitiesSelected = clone.numberEntitiesSelected;
	}
	
	
    
	/**
	 * writeSupportContextObject(): Serializable 
	 * method to write the object in the output file
	 * @param filename The output filename.
	 * @throws IOException
	 */
    public void writeSelectByContextObject (String filename) throws IOException {
    	
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
    }

}
