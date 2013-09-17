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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.LabelMap;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.Model.ContextToken;
import lbd.Model.SupportContext;
import lbd.Utils.POSTag;
import lbd.Utils.QuickSort;

public class SelectByPOSTag extends SelectEntitiesByContext implements Serializable {
	
	private static final long serialVersionUID = 1L;
	protected HashMap<String, POSTag> posTagContextMap;
	protected ArrayList<POSTag> posTagList;
	
	protected double threshould;
	protected double maxCorrectHits;
	
	public SelectByPOSTag(SupportContext supportContext, double threshould) {
		
		super(supportContext);
		
		acronym = "-SelByPOSTag";
		this.threshould = threshould;
	}
	
	public SelectByPOSTag(String inputFilenameAddress, int windowSize,
			boolean considerOnlyEntities, double threshould) {
		super(inputFilenameAddress, windowSize, considerOnlyEntities);
		acronym = "-SelByPOSTag";
		this.threshould = threshould;
	}
	
	public SelectByPOSTag(int windowSize, boolean considerOnlyEntities, double threshould) {
		super(windowSize, considerOnlyEntities);
		acronym = "-SelByPOSTag";
		this.threshould = threshould;
	}
	
	public String analyzePOSTagContext (String inputFilenameAddress, String testFilenameAddress) {

		this.inputFilenameAddress = inputFilenameAddress;
		outputFilenameAddress = generateOutputFilenameAddress(inputFilenameAddress);	
		logOutputFilenameAddress = generateLogOutputFilenameAddress(outputFilenameAddress);
			
		//-- Generate the possibles POSTag Contexts from the inputFilenameAddress
		generatePOSTagContext(inputFilenameAddress);
		
		//-- Analyze the test filename with the POS tag was assigned correct the entities terms
		analyzePOSTagInTestFile(testFilenameAddress);
		
		//-- Sort the posTag elements by decrescent correct hits
		posTagList = sortByCorrectHitsPOSTagContexts(posTagList);
		
		//-- Write the log file with the analysis of posTag
		//writeAnalysisPOSTagContext(logOutputFilenameAddress);
		
		return(outputFilenameAddress);
	}
	
	protected void generatePOSTagContext(String inputFilenameAddress) {
		
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(inputFilenameAddress,
				FileType.TRAINING, false);
		
		DataSequence sequence;
		posTagContextMap = new HashMap<String, POSTag>();
		ContextToken context = null;
		POSTag posTag = null;
		String posTagKey;
		
		for (inputSequenceSet.startScan(); inputSequenceSet.hasNext();) {
			
			sequence = inputSequenceSet.next();
			
			for(int i = 0; i < sequence.length(); i++) {
				
				//posTagKey = supportContext.generateLeftPrefixPOSTagKeyFromSequence(sequence, i);
				posTagKey = supportContext.generatePOSTagKeyFromSequence(sequence, i);
				context = supportContext.getFastAccessPrefixPosTagList().get(posTagKey);
				
				if(context != null) {
					if((posTag = posTagContextMap.get(posTagKey)) != null)
						posTag.addFrequency();
					else
						posTagContextMap.put(posTagKey, new POSTag(posTagKey, context.getTokenValue(), 1));
				}
			}
		}
		
	}
	
	protected ArrayList<POSTag> sortByCorrectHitsPOSTagContexts(ArrayList<POSTag> posTagList) {
		
		int posTagIndex = 0;
		
		Double [] correctHitsPOSTagContext = new Double[posTagContextMap.size()];
		ContextToken context;
		posTagList = new ArrayList<POSTag>();
		POSTag posTag;
		
		Set<Entry<String, POSTag>> c = posTagContextMap.entrySet();
		Iterator<Entry<String, POSTag>>  ite = c.iterator();
		
		while(ite.hasNext()) {
		
			posTag = ite.next().getValue();
			
			correctHitsPOSTagContext[posTagIndex++] = (100.0*posTag.getCorrectHits())/(posTag.getCorrectHits()+posTag.getWrongHits());
			posTag.setPercentageCorrectHits(correctHitsPOSTagContext[posTagIndex -1]);
			posTag.setPercentageWrongHits(100.0 - correctHitsPOSTagContext[posTagIndex -1]);
			
			posTagList.add(posTag);
			
			if(posTag.getCorrectHits() > maxCorrectHits && posTag.getPercentageCorrectHits() >= threshould * 100)
				maxCorrectHits = posTag.getCorrectHits();
		}		
		
		//QuickSort.sort(correctHitsPOSTagContext, posTagList);
		
		return(posTagList);
	}
	
	protected void analyzePOSTagInTestFile(String testFilenameAddress) {
		
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(inputFilenameAddress,
				FileType.TRAINING, false);
		
		int stateSequence;
		
		DataSequence sequence;
		ContextToken context = null;
		POSTag posTag = null;
		String posTagKey;
		
		for (inputSequenceSet.startScan(); inputSequenceSet.hasNext();) {
			
			sequence = inputSequenceSet.next();
			
			for(int i = 0; i < sequence.length(); i++) {
				
				//posTagKey = supportContext.generateLeftPrefixPOSTagKeyFromSequence(sequence, i);
				posTagKey = supportContext.generatePOSTagKeyFromSequence(sequence, i);
				context = supportContext.getFastAccessPrefixPosTagList().get(posTagKey);
				
				if(context != null) {
					
					posTag = posTagContextMap.get(posTagKey);
					stateSequence = sequence.y(i);
					
					//BILOU Entities (0-B, 1-I, 2-L and 4-U)
					if(stateSequence >= 0 && stateSequence <= 4 && stateSequence != 3)
						posTag.addCorrectHit();
					else if(stateSequence == 3)
						posTag.addWrongHit();
				}
			}
		}
	}
	
	protected void writeAnalysisPOSTagContext(String logOutputFilenameAddress) {
		
		POSTag posTag;
		ContextToken context;
		String logOutputMessage;
		DecimalFormat decFormat = new DecimalFormat("#.##");
		
		int posTagNumber = 0;
		int correctHits;
		int wrongHits;
		String percentageCorrectHits;
		String percentageWrongHits;
		
		try {
			
			logOut = new OutputStreamWriter(new FileOutputStream(logOutputFilenameAddress), ENCODE_USED);
			
			for(int i = posTagList.size()-1; i >= 0; i--) {
				
				posTag = posTagList.get(i);
				context = supportContext.getFastAccessPrefixPosTagList().get(posTag.getPosTagKey());
				
				correctHits = posTag.getCorrectHits();
				wrongHits = posTag.getWrongHits();
				percentageCorrectHits = decFormat.format((100.0*correctHits)/(correctHits+wrongHits));
				percentageWrongHits = decFormat.format((100.0*wrongHits)/(correctHits+wrongHits));
				
				logOutputMessage = ++posTagNumber + ". ";
				logOutputMessage += posTag.getPosTagKey() + " [" + context.getTokenValue() + "](";
				logOutputMessage += context.getAllPrefixInOrder() + ") F: " + posTag.getFrequency();
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
		
		POSTag posTag;
		String posTagKey;
		ContextToken context = null;
		String label = "";
		
		for(int i = 0; i < sequence.length; i++) {
		
			posTagKey = supportContext.generateLeftPrefixPOSTagKeyFromSequence(seq, i);
			context = supportContext.getFastAccessPrefixPosTagList().get(posTagKey);
			
			if(context != null && seq.y(i) == 3 && isPOSTagAboveOrEqualsThreshould(posTagKey)) {
				
				posTag = posTagContextMap.get(posTagKey);
				
				label = LabelMap.getLabelNamePOSTagPTBR(context.getToken().getState());
				numberEntitiesSelected++;
				
				logOut.write(numberEntitiesSelected + ". " + posTagKey + " [" + context.getTokenValue()  + "] (");
				logOut.write(context.getAllPrefixInOrder()  +  ") " + sequence[i] + "|" + label);
				logOut.write(" F: " + posTag.getFrequency());
				logOut.write(", C: " + posTag.getCorrectHits() + "(" + posTag.getPercentageCorrectHits() + "%)");
				logOut.write(", W: " + posTag.getWrongHits() + "(" + posTag.getPercentageWrongHits() + "%)\n");
				//logOut.write(" OldLabel: " + LabelMap.getLabelNamePOSTagPTBR(seq.y(i)) + "\n");
			} else {
				label = LabelMap.getLabelNamePOSTagPTBR(seq.y(i));
			}
			
			out.write(sequence[i] + "|" + label + "\n");
		}
		
		out.write("\n");
	}
	
	public boolean isPOSTagAboveOrEqualsThreshould(String posTagKey) {
		
		POSTag posTag = posTagContextMap.get(posTagKey);
		
		if(posTag != null) {
			double cH = posTag.getPercentageCorrectHits();
			double cHMax = posTag.getCorrectHits()/maxCorrectHits;
		}
		//System.out.println("Key : " + posTagKey + " Ratio : " + cH * cHMax);
		
		/*return(posTag != null && posTag.getPercentageCorrectHits() >= (threshould * 100.0) &&
				posTag.getCorrectHits() >= 5);*/
		
		return(posTag != null && posTag.getPercentageCorrectHits() >= (threshould * 100.0) &&
				posTag.getCorrectHits() >= 5);
		
		/*return(posTag != null && cH * cHMax >= (threshould * 100.0) &&
				posTag.getCorrectHits() > 0);*/
		
	}
	
	public String generatePrefixPOSTagKey(DataSequence seq, int index) {
		return (supportContext.generateLeftPrefixPOSTagKeyFromSequence(seq, index));
	}
	
	public String generatePOSTagKey(DataSequence seq, int index) {
		return (supportContext.generatePOSTagKeyFromSequence(seq, index));
	}
	
	public ContextToken getContextAssociatedWithPOSTag(String posTagKey) {
		return (supportContext.getFastAccessPrefixPosTagList().get(posTagKey));
	}
	
	public void readSelectByPOSTagObject (String filename, SelectByPOSTag target) throws IOException, ClassNotFoundException {
    	
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		SelectByPOSTag clone = (SelectByPOSTag) in.readObject();
		cloneSelectByPOSTag(target, clone);
		
		in.close();
    }
	
	private void cloneSelectByPOSTag (SelectByPOSTag target, SelectByPOSTag clone) {
		
		target.inputFilenameAddress = clone.inputFilenameAddress;
	    target.outputFilenameAddress = clone.outputFilenameAddress;
	    target.logOutputFilenameAddress = clone.logOutputFilenameAddress;
	    target.posTagContextMap = clone.posTagContextMap;
	    target.posTagList = clone.posTagList;
	    target.threshould = clone.threshould;
	    target.maxCorrectHits = clone.maxCorrectHits;
	    target.supportContext = clone.supportContext;
	    target.numberEntitiesSelected = clone.numberEntitiesSelected;
	    target.windowSize = clone.windowSize;
	    target.considerOnlyEntities = target.considerOnlyEntities;
	}
	
	
    
	/**
	 * writeSupportContextObject(): Serializable 
	 * method to write the object in the output file
	 * @param filename The output filename.
	 * @throws IOException
	 */
    public void writeSelectByPOSTagObject (String filename) throws IOException {
    	
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
    }

	public HashMap<String, POSTag> getPosTagContextMap() {
		return posTagContextMap;
	}
	
	public POSTag getPOSTagElement(String posTagKey) {
		return posTagContextMap.get(posTagKey);
	}

	public void setPosTagContextMap(HashMap<String, POSTag> posTagContextMap) {
		this.posTagContextMap = posTagContextMap;
	}

	public ArrayList<POSTag> getPosTagList() {
		return posTagList;
	}

	public void setPosTagList(ArrayList<POSTag> posTagList) {
		this.posTagList = posTagList;
	}
}
