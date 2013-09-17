package lbd.GenTT;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class SequenceSplit {
	
	protected static final String ENCODE_USED = "ISO-8859-1";
	
	public static void generateSplitSequence(String inputFilenameAddress, int [] splitFactor) {
		
		try {
			
			BufferedReader in;
			Writer out;
			int sequenceNumber = 0;
			int splitFactorIndex = 0;
			String line = "";
			
			in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilenameAddress), ENCODE_USED));
			
			while(line != null) {
				
				if(splitFactorIndex < splitFactor.length) {
					
					out = new OutputStreamWriter(new FileOutputStream(generateOutputFilename(inputFilenameAddress, 
							splitFactor[splitFactorIndex])), ENCODE_USED);
					
					while((line = in.readLine()) != null) {
						
						if(!line.equals(""))
							out.write(line + "\n");
						else {
							sequenceNumber++;
							out.write("\n");
						}
							
						if(sequenceNumber == splitFactor[splitFactorIndex]) {
							splitFactorIndex++;
							sequenceNumber = 0;
							break;
						}
							
					}
						
					out.flush();
					out.close();
				} else
					break;
			}
			
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static String generateOutputFilename(String inputFilenameAddress, int splitFactor) {
		
		int endIndexInputFilename = inputFilenameAddress.lastIndexOf(".");
		String outputFilenameAddress = inputFilenameAddress.substring(0, endIndexInputFilename);
		outputFilenameAddress += "-SplitF" + splitFactor;
		outputFilenameAddress += inputFilenameAddress.substring(endIndexInputFilename);
		
		return(outputFilenameAddress);
	}

}
