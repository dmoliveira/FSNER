package lbd.AutoTagger;

import iitb.CRF.DataSequence;

import java.io.IOException;
import java.io.Writer;

import lbd.CRF.LabelMap;
import lbd.Model.ContextToken;
import lbd.Model.SupportContext;

public class SelectEntitiesByContextExtended extends SelectEntitiesByContext {
	
	protected SupportContext supportContextWindowTwo;
	protected SupportContext supportContextWindowTree;
	
	public SelectEntitiesByContextExtended(String inputFilenameAddress) {
		super(inputFilenameAddress, 1, false);
		
		acronym = "SEByCxtExt";
		//supportContextWindowTwo = loadSupportContext(inputFilenameAddress, 2, false);
		supportContextWindowTree = loadSupportContext(inputFilenameAddress, 1, false);
	}
	
	protected void writeSequence(String [] sequence, DataSequence seq,
			Writer out, SupportContext supportContext) throws IOException {
		
		ContextToken context = null;
		String label = "";
		int defaultLabel = 3;
		
		for(int i = 0; i < sequence.length; i++) {
			
			context = supportContextWindowTree.existContextInSequenceContextHashMap(sequence, i);
			
			//if(context == null)
				//context = supportContextWindowTwo.existContextInSequenceContextHashMap(sequence, i);
			//if(context == null)
			//	context = supportContext.existContextInSequenceContextHashMap(sequence, i);
			
			//if(context == null)
				//context = supportContextWindowTree.existPrefixInSequencePrefixContextHashMap(sequence, i);
			//if(context == null)
				//context = supportContextWindowTree.existSuffixInSequenceSuffixContextHashMap(sequence, i);
			
			label = ((context != null)? LabelMap.getLabelNameBILOU(context.getToken().getState()) :
				LabelMap.getLabelNameBILOU(defaultLabel));
			
			out.write(sequence[i] + "|" + label + "\n");
		}
		
		out.write("\n");
	}

}
