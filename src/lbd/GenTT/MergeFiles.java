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

public class MergeFiles {
	
	//-- The encode ISO-8859-1 accepts accent
	private final String ENCODE_USED = "ISO-8859-1";
	
	//-- Delimiter used to generate the token in the sequence in the format DOCRF
	private final String DELIMITER = "|";
	
	private int lineNumber;
	private int numberSeq1Labeled;
	private int numberSeq2Labeled;
	
	public void mergeFiles(String inputFile1, String inputFile2, String output) {
		
		try {
			Writer out = new OutputStreamWriter(new FileOutputStream(output), ENCODE_USED);
			
			SequenceSet input1 =  HandlingSequenceSet.transformFileInSequenceSet(inputFile1,
					FileType.TRAINING, false);
			
			SequenceSet input2 =  HandlingSequenceSet.transformFileInSequenceSet(inputFile2,
					FileType.TRAINING, false);
			
			lineNumber = 0;
			numberSeq1Labeled = 0;
			numberSeq2Labeled = 0;
			
			for (input2.startScan(); input2.hasNext();)
				writeSequence(out, input1.next(), input2.next());
			
			System.out.println("# terms merged from Input1: " + numberSeq1Labeled);
			System.out.println("# terms merged from Input2: " + numberSeq2Labeled);
			
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
	
	private void writeSequence(Writer out, DataSequence seq1, DataSequence seq2) {
		
		String errorMessage ="";
		String label = "";
		
		try {
			
			lineNumber++;
			
			if(seq1.length() != seq2.length()) {
				errorMessage = "The size of the sequences are different (Line " + lineNumber + ")";
				System.err.println(errorMessage);
				//throw new Exception();
			}
			
			for(int i = 0; i < seq1.length(); i++) {
				
				if(!seq1.x(i).equals(seq2.x(i))) {
					errorMessage = "Error of diferent sequence in line " + (lineNumber + i) + ", seq1(" + seq1.x(i) + ") seq2(" + seq2.x(i) + ")";
					System.err.println(errorMessage);
					//throw new Exception();
				}
				
				if(seq1.y(i) != 3) {
					label = LabelMap.getLabelNamePOSTagPTBR(seq1.y(i));
					numberSeq1Labeled++;
				} else if(seq2.y(i) != 3) {
					label = LabelMap.getLabelNamePOSTagPTBR(seq2.y(i));
					numberSeq2Labeled++;
				} else 
					label = LabelMap.getLabelNamePOSTagPTBR(3); //Outside
				
				out.write(seq1.x(i) + DELIMITER + label + "\n");
			}
			
			out.write("\n");
			
			lineNumber += seq1.length();
			
		} catch(Exception e) {
			//System.err.println(errorMessage);
		}
		
	}

}
