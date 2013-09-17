package lbd.GenTT;

import iitb.CRF.DataSequence;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;

import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.LabelMap;
import lbd.CRF.Sequence;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.Model.ContextToken;
import lbd.Model.SupportContext;
import lbd.Utils.Utils;

public class SelectSampleByBagOfWordsContext {
	
	//-- The encode ISO-8859-1 accepts accent
	private final String ENCODE_USED = "ISO-8859-1";
	private final String DELIMITER_LABEL = "|";
	private final String ACRONYM = "-SelSampleByBagOfWords";
	
	private double threshould;
	
	//-- The inputFilename e outputFilename address
	private String sourceFilenameAddress;
	private String inputFilenameAddress;
	private String outputFilenameAddress; 
	
	//-- Object with the support context
	private SupportContext supportContext;
	
	//-- Total and relative sequence number
	private int totalSequenceNumber;
	private int relativeSequenceNumber;
	private int sequenceNumber;
	
	private int windowSize;
	private boolean considerOnlyEntities;
	
	private ArrayList<ArrayList<String>> sequenceOutsideList;
	
	//-- @DMZDebug start time count
	private Date startTime;
	
	public SelectSampleByBagOfWordsContext(String sourceFilenameAddress, int windowSize, boolean considerOnlyEntities, double threshould) {
		this.sourceFilenameAddress = sourceFilenameAddress;	
		this.threshould = threshould;
		this.windowSize = windowSize;
		this.considerOnlyEntities = considerOnlyEntities;
		sequenceOutsideList = new ArrayList<ArrayList<String>>();
	}
	
	public void executeSelection(String inputFilenameAddress) {
		
		this.inputFilenameAddress = inputFilenameAddress;
		this.outputFilenameAddress = generateOutputFilenameAddress(inputFilenameAddress);
		String [] sequenceList;
		
		Sequence sequence;
		
		boolean isReliableSequence;
		double correctHit;
		
		//-- Load outside list
		loadOutsideList(sourceFilenameAddress);
		
		SequenceSet trainingtSet = HandlingSequenceSet.transformFileInSequenceSet(inputFilenameAddress, FileType.TRAINING, false);
		
		totalSequenceNumber = trainingtSet.size();
		relativeSequenceNumber = 0;
		sequenceNumber = 0;
		
		try {
			
			Writer out = new OutputStreamWriter(new FileOutputStream(outputFilenameAddress), ENCODE_USED);
			
			//@DMZDebug
			startTime = new Date(); 
			
			for(trainingtSet.startScan(); trainingtSet.hasNext();) {
				sequence = (Sequence) trainingtSet.next();
				sequenceList = Utils.convertSequenceToLowerCase(sequence, sequence.size());
				
				isReliableSequence = false;
				sequenceNumber++;
				
				for(ArrayList<String> outsideSequence : sequenceOutsideList) {
					
					correctHit = 0;
					
					for(int i = 0; i < sequence.size(); i++) {
						if(sequence.y(i) == 3 && 
								(outsideSequence.contains(sequenceList[i])))
							correctHit++;
						
						if(correctHit/outsideSequence.size() >= threshould) {
							isReliableSequence = true;
							break;
						}
					}
					
					if(isReliableSequence)
						break;
				}
				
				if(isReliableSequence)
					writeSequence(out, sequence);
			}
			
			out.flush();
			out.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 		
	}
	
	private void loadSupportContext() {
		supportContext = new SupportContext(windowSize,"BILOU",considerOnlyEntities);
		
		SequenceSet sourceSet = HandlingSequenceSet.transformFileInSequenceSet(sourceFilenameAddress, FileType.TRAINING, false);
		supportContext.generateContext(sourceSet);
	}
	
	private void loadOutsideList(String sourceFilenameAddress) {
		
		String[] sequenceList;
		ArrayList<String> outsideSequence;
		
		Sequence sequence;
		SequenceSet sourceSet = HandlingSequenceSet.transformFileInSequenceSet(sourceFilenameAddress, FileType.TRAINING, false);
		
		for(sourceSet.startScan(); sourceSet.hasNext();) {
			
			sequence = (Sequence) sourceSet.next();
			sequenceList = Utils.convertSequenceToLowerCase(sequence, sequence.size());
			
			outsideSequence = new ArrayList<String>();
			
			for(int i = 0; i < sequence.size(); i++)
				if(sequence.y(i) == 3)
					outsideSequence.add(sequenceList[i]);
			
			if(outsideSequence.size() > 0)
				sequenceOutsideList.add(outsideSequence);
		}
	}
	
	private void writeSequence(Writer out, DataSequence sequence) {
		try {
			
			//@DMZDebug
			String printLog = "# " + (++relativeSequenceNumber);
			printLog += ", Sequence " + sequenceNumber;
			printLog += ", Relative Selection " + (100.0 * relativeSequenceNumber)/totalSequenceNumber + "% (";
			printLog += ((int)((new Date()).getTime() - startTime.getTime())) + "ms)";
			System.out.println(printLog);
			
			for(int i = 0; i < sequence.length(); i++)
				out.write(sequence.x(i) + DELIMITER_LABEL + (LabelMap.getLabelNameBILOU(sequence.y(i))) + "\n");
			
			out.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected String generateOutputFilenameAddress(String inputFilenameAddress) {
		
		String inputFile = inputFilenameAddress.substring(inputFilenameAddress.lastIndexOf("/"));
		inputFile = "./samples/data/bcs2010/GenTT/Output" + inputFile;
		
		int endInputFilenameIndex = inputFile.lastIndexOf(".");
		String outputFilenameAddress = inputFile.substring(0, endInputFilenameIndex);
		outputFilenameAddress += ACRONYM + "T(" + threshould + ")";
		outputFilenameAddress += inputFile.substring(endInputFilenameIndex);
		
		return(outputFilenameAddress);
	}
	

}
