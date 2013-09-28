package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import lbd.Utils.Utils;

public class InputContextFeature extends ContextFeature {

	private static final long serialVersionUID = 1L;

	public InputContextFeature(FeatureGenImpl fgen, SupportContext supportContext, float weight) {
		super(fgen, supportContext);
		this.weight = weight;
	}

	public InputContextFeature(FeatureGenImpl fgen, SupportContext supportContext) {
		super(fgen, supportContext);
		weight=1f;
	}

	public InputContextFeature(FeatureGenImpl fgen, SupportContext supportContext, String featureName) {
		super(fgen, supportContext, featureName);
	}

	private void prepareToWriteIContextUnknownLog() {

		String inputFilename = supportContext.getInputFilenameAddress();
		String outputContextAnalysis = inputFilename.substring(0, inputFilename.lastIndexOf(".")) + ".iCxt";

		if(supportContext.isTest && outputUnknownContext == null) {
			try {
				outputUnknownContext = new OutputStreamWriter(new FileOutputStream(outputContextAnalysis), ENCODE_USED);
				outputUnknownContext.write("LOG: Unknown Input Context found in " + inputFilename + "\n");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {

		isFeatureAdded = false;
		currentState = -1;

		if(sequence != data) {
			sequence = data;
			sequenceList = Utils.convertSequenceToLowerCase(data, data.length());
		}

		//contextToken = supportContext.existPrefixInSequence(sequenceList, pos);
		//Date date = new Date();

		ContextToken contextToken = null;

		/*if(supportContext.isTest) {
			contextToken = supportContext.existContextZeroInSequenceContextZeroHashMap(sequenceList[pos]);

			if(contextToken == null)
				contextToken = supportContext.existContextInSequenceContextHashMap(sequenceList, pos);

		} if(!supportContext.isTest || contextToken == null)*/
		if(!supportContext.isTest || supportContext.existContextInSequenceContextHashMap(sequenceList, pos) == null)
		{
			contextToken = supportContext.existPrefixInSequenceRestrictedPrefixContextHashMap(sequenceList, pos);
			//else
			//contextToken = null;
			//System.out.println("CxtPrefixTime: " + ((new Date()).getTime() - date.getTime()));
		}

		/*if(supportContext.isTest && contextToken == null) {
			ExtendsAccentVariabilityInTweet eXAVIT = new ExtendsAccentVariabilityInTweet();
			String[][]sequenceTokenAccentVariability = eXAVIT.generateSequenceAccentVariation(supportContext, sequenceList, pos);

			if(sequenceTokenAccentVariability != null) {
				for(int i = 0; i < sequenceTokenAccentVariability.length; i++) {
					contextToken = supportContext.existPrefixInSequenceRestrictedPrefixContextHashMap(sequenceList, pos);

					if(contextToken != null)
						break;
				}
			}
		}*/

		hasFoundContext = (contextToken != null);

		if(hasFoundContext) {

			idContextFeature = contextToken.getContextTokenID();
			//currentState = contextToken.getToken().getState();
			previousState = -1;
			featureName = "iCxt("+supportContext.getWindowSize()+"){";

			if(contextToken.getPrefixSize() > 0) {
				previousState = contextToken.getPrefix(0).getState();
				featureName += contextToken.getPrefix(0).getValue().toUpperCase() + ",";
			}

			featureName += contextToken.getTokenValue() + "}";

			advance();

		} else if(supportContext.isTest) {

			if(outputUnknownContext == null) {
				prepareToWriteIContextUnknownLog();
			}

			try {

				if(sequence.y(pos) != 3) {
					outputUnknownContext.write("\n{");

					for(int i = pos - 1; i > 0 && (pos - i) <= supportContext.getWindowSize(); i--) {
						outputUnknownContext.write(sequenceList[i] + ((i > 1 && (pos - i) < supportContext.getWindowSize())?", ":""));
					}

					outputUnknownContext.write("} " + sequenceList[pos] + " {");

					for(int i = pos + 1; i < sequenceList.length && (i - pos) <= supportContext.getWindowSize(); i++) {
						outputUnknownContext.write(sequenceList[i] + (((i - pos) < supportContext.getWindowSize() && i < sequenceList.length-1)?" ":""));
					}

					outputUnknownContext.write("}");


					outputUnknownContext.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return(hasFoundContext);
	}
}
