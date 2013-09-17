package lbd.GenTT;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;

import iitb.CRF.DataSequence;
import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.LabelMap;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.Model.ContextToken;
import lbd.Model.SupportContext;
import lbd.Utils.Utils;

public class SelectSampleByContext {
	
	//-- The encode ISO-8859-1 accepts accent
	private final String ENCODE_USED = "ISO-8859-1";
	
	//-- Delimiter used to generate the token in the sequence in the format DOCRF
	private final String DELIMITER = "|";
	
	//-- The inputFilename e outputFilename address
	private String inputFilenameAddress;
	private String outputFilenameAddress; 
	
	//-- Object with the support context
	private SupportContext supportContext;
	
	//-- Total and relative sequence number
	private int totalSequenceNumber;
	private int relativeSequenceNumber;
	
	//-- The current number of the sequence
	private int sequenceNumber;
	
	//-- @DMZDebug start time count
	private Date startTime;
	
	/**
	 * SelectSampleByContext(Constructor):
	 */
	public SelectSampleByContext() {
		supportContext = new SupportContext(1, "BILOU", true);
	}
	
	/**
	 * selectSamples():
	 * @param inputFilenameAddress
	 */
	public void selectSamples(String inputFilenameAddress) {
		
		this.inputFilenameAddress = inputFilenameAddress;
		this.outputFilenameAddress = Utils.generateOutputFilenameAddress(inputFilenameAddress, "-SSbyCxt");
		totalSequenceNumber = Utils.countSequenceInFile(inputFilenameAddress);
		
		loadSupportContext();
		
		//@DMZDebug
		startTime = new Date(); 
		relativeSequenceNumber = 0;
		sequenceNumber = 0;
		System.out.println("File: " + inputFilenameAddress);
		
		System.out.println("Starting to Select Samples \n");
		
		selectSamplesToWrite();	
	}
	
	/**
	 * loadSupportContext():
	 */
	private void loadSupportContext() {
		
		String input = "./samples/data/bcs2010/GenTT/Input/Twitter-2000-Player-MANUAL-LABELED-BILOU-CV5-CRF-POSTag-REWE-RRT-RRL-RRWS.tagged";
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(input,
				FileType.TRAINING, false);
		
		supportContext.generateContext(inputSequenceSet);
	}
	
	/**
	 * selectSamplesToWrite():
	 */
	private void selectSamplesToWrite() {
		
		int contextID = -1;
		DataSequence sequence;
		ContextToken contextToken = null;
		String [] sequenceList;
		
		try {
			Writer out = new OutputStreamWriter(new FileOutputStream(outputFilenameAddress), ENCODE_USED);
			
			SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(inputFilenameAddress,
					FileType.TRAINING, false);
			
			for (inputSequenceSet.startScan(); inputSequenceSet.hasNext();) {
				
				sequence = inputSequenceSet.next();
				sequenceList = Utils.convertSequenceToLowerCase(sequence, sequence.length());
				sequenceNumber++;
				
				
				for(int i = 0; contextID == -1 && i < sequence.length(); i++) {
					contextToken = supportContext.existContextInSequence(sequenceList, i);
					contextID = (contextToken != null && (sequence.y(i) == 0 || sequence.y(i) == 1 ||
							sequence.y(i) == 2 || sequence.y(i) == 4))? contextToken.getContextTokenID() : -1;
				}
				
				if(contextID != -1) {
					writeSequence(sequence, out);
					
					//-- Remove repeated context
					//supportContext.removeContextToken(contextToken);
				}
				
				contextID = -1;
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
	
	/**
	 * writeSequence():
	 * @param sequence
	 * @param out
	 */
	private void writeSequence(DataSequence sequence, Writer out) {
		try {
			
			//@DMZDebug
			String printLog = "# " + (++relativeSequenceNumber);
			printLog += ", Sequence " + sequenceNumber;
			printLog += ", Relative Selection " + (100.0 * relativeSequenceNumber)/totalSequenceNumber + "% (";
			printLog += ((int)((new Date()).getTime() - startTime.getTime())) + "ms)";
			System.out.println(printLog);
			
			for(int i = 0; i < sequence.length(); i++)
				out.write(sequence.x(i) + DELIMITER + (LabelMap.getLabelNameBILOU(sequence.y(i))) + "\n");
			
			out.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
