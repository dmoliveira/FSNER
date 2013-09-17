package lbd.Evaluation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

import lbd.CRF.CRFExecutor;
import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.Sequence;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.CRF.LabelMap.BILOU;

public class VotingEvaluation {
	
	public enum VotingType {AND, OR, MAJORITY, MINORITY};
	
	public static void main(String [] args) {
		String dir = "./samples/data/bcs2010/saves/";
		String [] inputFilenameList = new String[3];
		//for(int i = 0; i < inputFilenameList.length; i++)
		//	inputFilenameList[i] = dir + "Twitter-Ner-Data-(Geo-loc)-Alt-RET-CV1-R"+(i+1)+"-PS(1500)-CRF-Cxt(PS2.3.4.5,P2.3.4)+Wrd.tagged";
		inputFilenameList[0] = dir + "Twitter-Ner-Data-(Geo-loc)-Alt-RET-CV1-VotingEvaluation-Cxt+Edg+Str+End.tagged";
		inputFilenameList[1] = dir + "Twitter-Ner-Data-(Geo-loc)-Alt-RET-CV1-VotingEvaluation-Cxt+Wrd2.tagged";
		inputFilenameList[2] = dir + "Twitter-Ner-Data-(Geo-loc)-Alt-RET-CV1-VotingEvaluation-Wrd+BHC+Sp.tagged";
		
		VotingEvaluation.evaluate(inputFilenameList, "");
	}
	
	public static void evaluate(String [] inputFilenameList, String evaluationFilenameList) {
		
		/*ArrayList<SequenceSet> sequenceSetList = new ArrayList<SequenceSet>();
		ArrayList<Sequence> sequenceList;
		ArrayList<String> labelList;
		
		try {
			
			Writer out = new OutputStreamWriter(new FileOutputStream("./samples/data/bcs2010/saves/Twitter-Ner-Data-(Geo-loc)-Alt-RET-CV1-VotingEvaluation.tagged"), "ISO-8859-1");
			System.out.println("-- Voting Evaluation");
			
			for(int i = 0; i < inputFilenameList.length; i++)
				sequenceSetList.add(HandlingSequenceSet.transformFileInSequenceSet(inputFilenameList[i],
					FileType.TEST, false));
			
			for(int i = 0; i < sequenceSetList.get(0).size(); i++) {
				
				sequenceList = new ArrayList<Sequence>();
				for(int j = 0; j < sequenceSetList.size(); j++)
					sequenceList.add(sequenceSetList.get(j).get(i));
				
				labelList = evaluateSequence(sequenceList, VotingType.OR);
				
				for(int k = 0; k < sequenceList.get(0).size(); k++) {
					out.write(sequenceList.get(0).x(k) + "|" + labelList.get(k) + "\n");
				}
				out.write("\n");
			}
			
			out.flush();
			out.close();
			
			getStatistics("./samples/data/bcs2010/", "saves/Twitter-Ner-Data-(Geo-loc)-Alt-RET-CV1-VotingEvaluation.tagged", "Twitter-Ner-Data-(Geo-loc)-Alt-RET-CV1.test");
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}*/	
		
		try {
			getStatistics("./samples/data/bcs2010/", "TEST-Result-DOCRFFormat.pipes", "Twitter-Ner-Data-(Geo-loc)-Alt-RET-CV1.test");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected static ArrayList<String> evaluateSequence (ArrayList<Sequence> sequenceList, VotingType votingType) {
		
		int [] stateFrequency;
		ArrayList<String> selectedSequenceLabel = new ArrayList<String>();
		
		for(int i = 0; i < sequenceList.get(0).size(); i++) {
			
			stateFrequency = new int [BILOU.values().length];
			
			for(int j = 0; j < sequenceList.size(); j++)
				stateFrequency[sequenceList.get(j).y(i)]++;
			
			selectedSequenceLabel.add(selectLabel(stateFrequency, votingType));
		}
		
		
		return(selectedSequenceLabel);
	}
	
	protected static String selectLabel(int [] stateFrequency, VotingType votingType) {
		
		String label = "";
		
		if(votingType == VotingType.AND) {
			label = getANDLabel(stateFrequency);
		} else if(votingType == VotingType.OR) {
			label = getORLabel(stateFrequency);
		} else if(votingType == VotingType.MAJORITY) {
			label = getMajorityLabel(stateFrequency);
		} else if(votingType == VotingType.MINORITY) {
			label = getMinorityLabel(stateFrequency);
		}
		
		return(label);
	}
	
	protected static String getMajorityLabel(int [] stateFrequency) {
		
		String label = "";
		int maxFrequency = 0;
		
		for(int i = 0; i < stateFrequency.length; i++) {
			if(stateFrequency[i] > maxFrequency) {
				label = BILOU.values()[i].name();
				maxFrequency = stateFrequency[i];
			}
		}
		
		return(label);
	}
	
	protected static String getMinorityLabel(int [] stateFrequency) {
		
		String label = "";
		int minFrequency = BILOU.values().length;
		
		for(int i = 0; i < stateFrequency.length; i++) {
			if(stateFrequency[i] < minFrequency) {
				label = BILOU.values()[i].name();
				minFrequency = stateFrequency[i];
			}
		}
		
		return(label);
	}
	
	protected static String getANDLabel(int [] stateFrequency) {
		
		String label = "";
		
		for(int i = 0; i < stateFrequency.length; i++) {
			if(stateFrequency[i] == 5) {
				label = BILOU.values()[i].name();
				break;
			}
		}
		
		if(label.isEmpty())
			label = BILOU.Outside.name();
		
		return(label);
	}
	
	protected static String getORLabel(int [] stateFrequency) {
		
		String label = "";
		
		for(int i = 0; i < stateFrequency.length; i++) {
			if(stateFrequency[i] > 0 && i != 3) {
				label = BILOU.values()[4].name();
				break;
			}
		}
		
		if(label.isEmpty())
			label = BILOU.Outside.name();
		
		return(label);
	}
	
	protected static void getStatistics(String dir, String taggedFile, String testFile) throws Exception {
		CRFExecutor crfExec = new CRFExecutor("./samples/bcs2010.conf");
		crfExec.getStatistics(dir, taggedFile, testFile);
	}

}
