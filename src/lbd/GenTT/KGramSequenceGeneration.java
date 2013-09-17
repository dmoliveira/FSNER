package lbd.GenTT;

import iitb.CRF.DataSequence;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.LabelMap;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;

public class KGramSequenceGeneration {

	//-- The encode ISO-8859-1 accepts accent
	public final String ENCODE_USED = "ISO-8859-1";
	
	protected final String DELIMITER = "|";
	protected final String ACRONYM = "-kGram";
	
	protected int kGram;
	
	public void executeKGramSequenceGeneration(String inputFilenameAddress, int kGram) {
		
		this.kGram = kGram;
		String outputFilenameAddress = generateOutputFilenameAddress(inputFilenameAddress);
		
		try {
			
			Writer out = new OutputStreamWriter(new FileOutputStream(outputFilenameAddress), ENCODE_USED);
			
			SequenceSet sequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(inputFilenameAddress,
					FileType.TRAINING, false);
			
			for (sequenceSet.startScan(); sequenceSet.hasNext();)
				writeKGramSequence(out, sequenceSet.next());
			
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
	
	private void writeKGramSequence(Writer out, DataSequence sequence) {
		
		try {
			
			for(int i = 0; i < sequence.length(); i++) {
				for(int j = 0; j < kGram && i + j < sequence.length(); j++) {
					out.write(sequence.x(i + j) + DELIMITER + LabelMap.getLabelNameBILOU(sequence.y(i + j)) + "\n");
				}	
				
				out.write("\n");
				
				if(i + kGram >= sequence.length())
					break;
			}			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	protected String generateOutputFilenameAddress(String inputFilenameAddress) {
		
		int endIndexFilename = inputFilenameAddress.lastIndexOf(".");
		
		String outputFilenameAddress = inputFilenameAddress.substring(0, endIndexFilename);
		outputFilenameAddress += ACRONYM + kGram + inputFilenameAddress.substring(endIndexFilename);
		outputFilenameAddress = outputFilenameAddress.replace("/Input/", "/Output/");
		
		return(outputFilenameAddress);
	}
	
}
