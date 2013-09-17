package lbd.AutoTagger;

import iitb.CRF.DataSequence;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.LabelMap;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.GenTT.SelectSampleByBalancedContext;
import lbd.Model.ContextToken;
import lbd.Utils.Utils;

public class SelectEntitiesByBalancedContext extends SelectSampleByBalancedContext {

	public SelectEntitiesByBalancedContext(int windowSize) {
		super(windowSize);
		acronym = "-SEntByBalCxt";
	}
	
	public void selectEntities(String inputFilenameAddress, double threshold) {
		
		String extention = "{w="+windowSize+",T" + (new DecimalFormat("#.##")).format(threshold) + acronym + "}";
		
		this.tetaThreshold = threshold; 
		this.inputFilenameAddress = inputFilenameAddress;
		this.outputFilenameAddress = Utils.generateOutputFilenameAddress(inputFilenameAddress, extention);
		this.logOutputFilenameAddress = Utils.generateOutputFilenameAddress(inputFilenameAddress, extention, ".log");
		totalSequenceNumber = Utils.countSequenceInFile(inputFilenameAddress);
		
		outputFilenameAddress = outputFilenameAddress.replace("/Input/", "/Output/");
		logOutputFilenameAddress = logOutputFilenameAddress.replace("/Input/", "/Output/");
		
		try {
			
			logOutput = new OutputStreamWriter(new FileOutputStream(logOutputFilenameAddress), ENCODE_USED);
			
			loadSupportContext();
			
			//-- Calculate the Alpha, Beta e Teta metrics
			calculateMetrics();
			
			//--
			System.out.print("Starting to Select Entities");
			selectEntitiesByTetaThreshould();
			
			logOutput.flush();
			logOutput.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	protected void selectEntitiesByTetaThreshould() throws IOException {
		
		try {
			Date startTime = new Date();
			
			Writer output = new OutputStreamWriter(new FileOutputStream(outputFilenameAddress), ENCODE_USED);
			ContextToken context;
			int defaultState = 3;// Outside
			
			DataSequence sequence;
			String [] sequenceList;	
			int [] labelList;
			
			SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(inputFilenameAddress,
					FileType.TRAINING, false);
			
			for (inputSequenceSet.startScan(); inputSequenceSet.hasNext();) {
				
				sequence = inputSequenceSet.next();
				sequenceList = Utils.convertSequenceToLowerCase(sequence, sequence.length());
				labelList = new int [sequence.length()];
				
				for(int i = 0; i < sequenceList.length; i++) {
					
					context = supportContext.getFastAccessContextList().get(supportContext.generateKeyFromSequence(sequenceList, i, false));
					
					if(context != null && alphaContextFrequency[supportContext.getContextList().indexOf(context)] >= tetaThreshold)
						labelList[i] = context.getToken().getState();
					else
						labelList[i] = defaultState;
				}
				
				writeSequence(sequenceList, labelList, output);
			}
			
			System.out.println("\nTotal Elipsed Time: " + ((new Date()).getTime() - startTime.getTime())/1000 + "s");
			
			output.flush();
			output.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	protected void writeSequence(String [] sequenceList, int [] labelList, Writer output) {
		
		try {
			
			for(int i = 0; i < sequenceList.length; i++)		
				output.write(sequenceList[i] + DELIMITER_DOCRF + LabelMap.getLabelNameBILOU(labelList[i]) + "\n");
			
			output.write("\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
