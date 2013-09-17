package lbd.AutoTagger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;
import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.LabelMap;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.Model.ContextToken;
import lbd.Model.SupportContext;
import lbd.Utils.Utils;

public class SelectEntitiesByContext implements Serializable {
	
	private static final long serialVersionUID = 1L;
	protected static final String ENCODE_USED = "ISO-8859-1";
	protected String acronym = "-SEByCxt";
	protected String inputFilenameAddress;
	protected String outputFilenameAddress;
	protected String logOutputFilenameAddress;
	
	protected int windowSize;
	protected boolean considerOnlyEntities;
	
	protected int numberEntitiesSelected;
	
	protected SupportContext supportContext;
	
	protected transient Writer out;
	protected transient Writer logOut;
	
	public SelectEntitiesByContext( SupportContext supportContext) {
		this.supportContext = supportContext;
	}
	
	public SelectEntitiesByContext(String inputFilenameAddress, int windowSize, boolean considerOnlyEntities) {
		
		this.inputFilenameAddress = inputFilenameAddress;
		this.windowSize = windowSize;
		this.considerOnlyEntities = considerOnlyEntities;
		
		supportContext = loadSupportContext(inputFilenameAddress, windowSize, considerOnlyEntities);
	}
	
	public SelectEntitiesByContext(int windowSize, boolean considerOnlyEntities) {
		this.windowSize = windowSize;
		this.considerOnlyEntities = considerOnlyEntities;
		
		supportContext = new SupportContext(windowSize, "BILOU", considerOnlyEntities);
	}
	
	public String selectEntities(String inputFilenameAddress) {
		
		DataSequence sequence;
		String [] sequenceList;
		
		numberEntitiesSelected = 0;
		
		this.inputFilenameAddress = inputFilenameAddress;		
		outputFilenameAddress = generateOutputFilenameAddress(inputFilenameAddress);	
		logOutputFilenameAddress = generateLogOutputFilenameAddress(outputFilenameAddress);
		
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(inputFilenameAddress,
				FileType.TRAINING, false);
		
		try {
			out = new OutputStreamWriter(new FileOutputStream(outputFilenameAddress), ENCODE_USED);
			
			if(logOut == null)
				logOut = new OutputStreamWriter(new FileOutputStream(logOutputFilenameAddress), ENCODE_USED);
			
			logOut.write("\n*********** Write Sequence ***********\n\n");
			
			for (inputSequenceSet.startScan(); inputSequenceSet.hasNext();) {
				
				sequence = inputSequenceSet.next();
				sequenceList = Utils.convertSequenceToLowerCase(sequence, sequence.length());
				
				writeSequence(sequenceList, sequence, out, supportContext);
			}
			
			//@DMZDebug
			System.out.println("Number of Entities selected: " + numberEntitiesSelected);
			
			finalizeSelection();
			
			out.flush();
			out.close();
			
			if(logOut != null) {
				logOut.write("\n");
				logOut.flush();
				logOut.close();
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		return(outputFilenameAddress);
	}
	
	protected void writeSequence(String [] sequence, DataSequence seq,
			Writer out, SupportContext supportContext) throws IOException {
		
		ContextToken context = null;
		String label = "";
		
		for(int i = 0; i < sequence.length; i++) {
			
			context = supportContext.existContextInSequenceContextHashMap(sequence, i);
			label = ((context != null)? LabelMap.getLabelNameBILOU(context.getToken().getState()) :
				LabelMap.getLabelNameBILOU(seq.y(i)));
			
			out.write(sequence[i] + "|" + label + "\n");
		}
		
		out.write("\n");
	}
	
	//-- Used in children class 
	protected void finalizeSelection() {
		
	}
	
	protected SupportContext loadSupportContext(String inputFilenameAddress, int windowSize, boolean considerOnlyEntities) {
		
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(inputFilenameAddress,
				FileType.TRAINING, false);
		
		SupportContext supportContext = new SupportContext(windowSize, "BILOU", considerOnlyEntities);
		
		supportContext.generateContext(inputSequenceSet);
		
		return(supportContext);
	}
	
	public void loadSupportContext(DataIter sequenceSet) {
		
		supportContext.generateContext(sequenceSet);
	}
	
	protected String generateOutputFilenameAddress(String inputFilenameAddress) {
		
		String inputFile = inputFilenameAddress.substring(inputFilenameAddress.lastIndexOf("/"));
		inputFile = "./samples/data/bcs2010/AutoTagger/Output" + inputFile;
		
		int endInputFilenameIndex = inputFile.lastIndexOf(".");
		String outputFilenameAddress = inputFile.substring(0, endInputFilenameIndex);
		outputFilenameAddress += acronym + inputFile.substring(endInputFilenameIndex);
		
		return(outputFilenameAddress);
	}
	
	protected String generateLogOutputFilenameAddress(String outputFilenameAddress) {
		int endFilenameIndex = outputFilenameAddress.lastIndexOf(".");
		String logOutputFilenameAddress = outputFilenameAddress.substring(0, endFilenameIndex);
		logOutputFilenameAddress += ".log";
		
		return(logOutputFilenameAddress);
	}
	
	public SupportContext getSupportContext() {
		return(supportContext);
	}

}
