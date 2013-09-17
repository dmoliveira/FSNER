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

public class MergeDifferentFiles {

	//-- The encode ISO-8859-1 accepts accent
	private final String ENCODE_USED = "ISO-8859-1";
	
	//-- Delimiter used to generate the token in the sequence in the format DOCRF
	private final String DELIMITER = "|";
	
	public void mergeFiles(String inputLongestFile, String inputShortestFile, String output) {
		
		try {
			Writer out = new OutputStreamWriter(new FileOutputStream(output), ENCODE_USED);
			
			SequenceSet inputLongest =  HandlingSequenceSet.transformFileInSequenceSet(inputLongestFile,
					FileType.TRAINING, false);
			
			SequenceSet inputShortest =  HandlingSequenceSet.transformFileInSequenceSet(inputShortestFile,
					FileType.TRAINING, false);
			
			for (inputLongest.startScan(); inputLongest.hasNext();) {
				
				writeSequence(out, inputLongest.next());
				
				if(inputShortest.hasNext())
					writeSequence(out, inputShortest.next());
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
	
	private void writeSequence(Writer out, DataSequence seq) {
		
		String label = "";
		
		try {
			
			for(int i = 0; i < seq.length(); i++) {
				
				label = LabelMap.getLabelNamePOSTagPTBR(seq.y(i));
				out.write(seq.x(i) + DELIMITER + label + "\n");
			}
			
			out.write("\n");
			
		} catch(Exception e) {}
		
	}	
}
