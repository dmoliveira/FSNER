package iitb.CRF;


import iitb.Model.FeatureGenImpl;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.CRF.LabelMap;
import lbd.Model.ContextToken;
import lbd.Utils.ProbabilityInformationSet;
import lbd.Utils.Utils;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.Matrix2DMatrix2DFunction;
/**
 *
 * Viterbi search
 *
 * @author Sunita Sarawagi
 *
 */ 



public class Viterbi implements Serializable {
	
    private static final long serialVersionUID = 8122L;
    
    /** DMOliveira Log **/
    protected double [][][] stateTransitionMatrix;
    protected double [][] totalStateTransitionScore;
    protected double [] maxStateTransitionScoreInSequence;
    protected double [] relativeTransitionScorePercentageDivision;
    protected double totalStateTransitionSequence;
    protected double totalScoreSequencePercentage;
    protected double totalScoreOnlyEntityInSequencePercentage;
    protected ArrayList<Double> totalScoreOnlyEntityInSequenceListPercentage;
    protected int totalEntitiesInSequence;
    protected int currentSequenceNumber;
    protected int currentSequenceWithEntityNumber;
    protected double stateTransitionSequence;
    
    protected CRF model;
    protected int beamsize;
    Viterbi(CRF model, int bs) {
	this.model = model;
	beamsize = bs;
	if (model.params.miscOptions.getProperty("beamSize") != null)
	    beamsize = Integer.parseInt(model.params.miscOptions.getProperty("beamSize"));

    }
    protected class Entry {
        public Soln solns[]; // TODO.
        boolean valid=true;
        protected Entry() {}
        protected Entry(int beamsize, int id, int pos) {
            solns = new Soln[beamsize];
            for (int i = 0; i < solns.length; i++)
                solns[i] = newSoln(id, pos);
        }
        protected Soln newSoln(int label, int pos) {
            return new Soln(label,pos);
        }
        protected void clear() {
            valid = false;
            for (int i = 0; i < solns.length; i++)
                solns[i].clear();
        }
        protected int size() {return solns.length;}
        protected Soln get(int i) {return solns[i];}
        protected void insert(int i, float score, Soln prev) {
            for (int k = size()-1; k > i; k--) {
                solns[k].copy(solns[k-1]);
            }
            solns[i].setPrevSoln(prev,score);
        }
        protected void add(Entry e, float thisScore) {
            assert(valid);
            if (e == null) {
                add(thisScore);
                return;
            }
            
            int insertPos = 0;
            for (int i = 0; (i < e.size()) && (insertPos < size()); i++) {
                float score = e.get(i).score + thisScore;
                insertPos = findInsert(insertPos, score, e.get(i));
            }
            
            //@DMZDebug
            //print();
        }
        protected int findInsert(int insertPos, float score, Soln prev) {
            for (; insertPos < size(); insertPos++) {
                if (score >= get(insertPos).score) {
                    insert(insertPos, score, prev);
                    insertPos++;
                    break;
                }
            }
            return insertPos;
        }
        protected void add(float thisScore) {
            findInsert(0, thisScore, null);
        }
        protected int numSolns() {
            for (int i = 0; i < solns.length; i++)
                if (solns[i].isClear())
                    return i;
            return size();
        }
        public void setValid() {valid=true;}
        void print() {
            String str = "";
            for (int i = 0; i < size(); i++)
                str += ("[bs"+i + " sc:" + solns[i].score + " p:" + solns[i].pos + " y:" + solns[i].label+"]");
            System.out.println(str);
        }
    };

    Entry winningLabel[][];
    protected Entry finalSoln;
    protected DoubleMatrix2D Mi;
    protected DoubleMatrix1D Ri;

    void allocateScratch(int numY) {
	Mi = new DenseDoubleMatrix2D(numY,numY);
	Ri = new DenseDoubleMatrix1D(numY);
	winningLabel = new Entry[numY][];
	finalSoln = new Entry(beamsize,0,0);
    }
    double fillArray(DataSequence dataSeq, double lambda[], boolean calcScore) {
	double corrScore = 0;
	int numY = model.numY;
	
	//@DMZ
	startStateTransitionMatrix(dataSeq.length(), numY+1, numY);
	
	for (int i = 0; i < dataSeq.length(); i++) {
	    // compute Mi.
	    Trainer.computeLogMi(model.featureGenerator,lambda,dataSeq,i,Mi,Ri,false);
	    for (int yi = 0; yi < numY; yi++) {
		winningLabel[yi][i].clear();
		winningLabel[yi][i].valid = true;
	    }
	    for (int yi = model.edgeGen.firstY(i); yi < numY; yi = model.edgeGen.nextY(yi,i)) {
		if (i > 0) {
		    for (int yp = model.edgeGen.first(yi); yp < numY; yp = model.edgeGen.next(yi,yp)) {
			double val = Mi.get(yp,yi)+Ri.get(yi);
			winningLabel[yi][i].add(winningLabel[yp][i-1], (float)val);
			
			//-- previous state to current state @DMZ
			assert(stateTransitionMatrix[i][yp][yi] == 0);
			stateTransitionMatrix[i][yp][yi] = val;
			totalStateTransitionScore[i][yi] += val;
			maxStateTransitionScoreInSequence[i] = (maxStateTransitionScoreInSequence[i] < val)? val : maxStateTransitionScoreInSequence[i];
		    }
		} else {
		    winningLabel[yi][i].add((float)Ri.get(yi));
		    
		    //-- -1 to any state @DMZ
		    assert(stateTransitionMatrix[i][numY][yi] == 0);
		    stateTransitionMatrix[i][numY][yi] = Ri.get(yi);
		    totalStateTransitionScore[i][yi] += Ri.get(yi);
		    maxStateTransitionScoreInSequence[i] = (maxStateTransitionScoreInSequence[i] < Ri.get(yi))? Ri.get(yi) : maxStateTransitionScoreInSequence[i];
		}
	    }
	    if (calcScore)
		corrScore += (Ri.get(dataSeq.y(i)) + ((i > 0)?Mi.get(dataSeq.y(i-1),dataSeq.y(i)):0));
	}
	return corrScore;
    }
    
    protected void setSegment(DataSequence dataSeq, int prevPos, int pos, int label) {
        dataSeq.set_y(pos, label);
    }
    public void bestLabelSequence(DataSequence dataSeq, double lambda[]) {
    	
    	//-- Original Viterbi (false)
        //double corrScore = viterbiSearch(dataSeq, lambda,false);
    	if(relativeTransitionScorePercentageDivision == null)
    		relativeTransitionScorePercentageDivision = new double [21];//21
    	
    	double corrScore = viterbiSearch(dataSeq, lambda,false);
        assignLabels(dataSeq);
        
        //@DMZDebug
        currentSequenceNumber++;
        if(totalEntitiesInSequence > 0) {
	        /*System.out.print("#"+ currentSequenceNumber + "(" + (++currentSequenceWithEntityNumber) + ")");
	        System.out.print("-- totalScore: " + (new DecimalFormat("#.##")).format(corrScore));
	        System.out.print(" totSeq[" + (new DecimalFormat("#.##")).format(totalScoreSequencePercentage) +  "%] ");
	        System.out.print("onlyEnt[t(" + (new DecimalFormat("#.##")).format(totalScoreOnlyEntityInSequencePercentage) + "%),");	        
	        System.out.print(getAllEntitySequenceProbability() + totalEntitiesInSequence + "]");
	        System.out.print(" totalStateTransition: " + (new DecimalFormat("#.##")).format(100.0 * stateTransitionSequence));
	        System.out.println("[" + (new DecimalFormat("#.##")).format(100.0 * totalStateTransitionSequence) +  "%]\n");*/
	        //model.probabilityInformationSet.add(currentSequenceNumber, totalScoreOnlyEntityInSequencePercentage/100);
        }
    }
    void assignLabels(DataSequence dataSeq) {
        Soln ybest = finalSoln.get(0);
        ybest = ybest.prevSoln;
        int pos=-1;
        
        //@DMZDebug
        boolean alreadyAdded = false;
        String assignLabelsMsg = "";
        double stateTransitionValue;
        double stateTransitionNonEntityToEntityPer = 0;
        totalScoreSequencePercentage = 100;
        totalScoreOnlyEntityInSequencePercentage = 100;
        totalScoreOnlyEntityInSequenceListPercentage = new ArrayList<Double>();
        totalEntitiesInSequence = 0;        
        //System.out.println("Seq:");
        totalStateTransitionSequence = 0;
        stateTransitionSequence = 0;
        
      //@DMZDebug
        /*for(int i = 0; i < dataSeq.length(); i++)
        	printStateTransitionMatrix(i, ((String)dataSeq.x(i)));*/
        
        while (ybest != null) {
        	
        	//@DMZDebug
        	stateTransitionValue = ((ybest.prevSoln != null)?
        			stateTransitionMatrix[ybest.pos][ybest.prevSoln.label][ybest.label]:
        				stateTransitionMatrix[ybest.pos][model.numY][ybest.label]);
        	
        	if(ybest.prevSoln != null) {
        		if(ybest.label > -1 && ybest.label < 5 && ybest.label != 3)
        			stateTransitionNonEntityToEntityPer = stateTransitionMatrix[ybest.pos][ybest.prevSoln.label][3];
        		else {
        			stateTransitionNonEntityToEntityPer = stateTransitionMatrix[ybest.pos][ybest.prevSoln.label][4];
        		}
        		stateTransitionNonEntityToEntityPer  += stateTransitionMatrix[ybest.pos][ybest.prevSoln.label][ybest.label];
        	} else {
        		if(ybest.label > -1 && ybest.label < 5 && ybest.label != 3)
        			stateTransitionNonEntityToEntityPer = stateTransitionMatrix[ybest.pos][model.numY][3];
        		else {
        			stateTransitionNonEntityToEntityPer = stateTransitionMatrix[ybest.pos][model.numY][4];
        		}
        		stateTransitionNonEntityToEntityPer  += stateTransitionMatrix[ybest.pos][model.numY][ybest.label];
        	}
        	
        	if(ybest.label > -1 && ybest.label < 5 && ybest.label != 3) {
        	assignLabelsMsg += "   " + dataSeq.x(ybest.pos);
        	assignLabelsMsg += " (y[" + ((ybest.prevSoln != null)? ybest.prevSoln.label : -1) + "," +  ybest.label;
        	assignLabelsMsg += "], as:" + (new DecimalFormat("#.##")).format(ybest.score) + ", ";
        	assignLabelsMsg += "stv:" + (new DecimalFormat("#.##")).format(stateTransitionValue);
        	assignLabelsMsg += "[rel:" + (new DecimalFormat("#.##")).format(100 * stateTransitionValue/maxStateTransitionScoreInSequence[ybest.pos]) + "%";
        	assignLabelsMsg += ", rel-2:" + (new DecimalFormat("#.##")).format(100 * stateTransitionValue/stateTransitionNonEntityToEntityPer) + "%";
        	assignLabelsMsg += ", abs:" + (new DecimalFormat("#.##")).format(100 * stateTransitionValue/totalStateTransitionScore[ybest.pos][ybest.label]) + "%, ";
        	assignLabelsMsg += ", comb:" + (new DecimalFormat("#.##")).format(100 *
        			(2 * stateTransitionValue/maxStateTransitionScoreInSequence[ybest.pos] * stateTransitionValue/totalStateTransitionScore[ybest.pos][ybest.label])/
        			(stateTransitionValue/maxStateTransitionScoreInSequence[ybest.pos] + stateTransitionValue/totalStateTransitionScore[ybest.pos][ybest.label])) + "%]";
        	assignLabelsMsg += ", Ri:" + (new DecimalFormat("#.##")).format(Ri.get(ybest.label)) + ")\n";
        	
        		int index = (int)(Math.floor(stateTransitionValue/stateTransitionNonEntityToEntityPer* 100))/5;
        		//relativeTransitionScorePercentageDivision[index]++;
        	}
            
            stateTransitionSequence += stateTransitionValue;
            totalScoreSequencePercentage *= stateTransitionValue/maxStateTransitionScoreInSequence[ybest.pos];
            totalStateTransitionSequence *= stateTransitionValue/totalStateTransitionScore[ybest.pos][ybest.label];
            
            /* Fixed */
            /*if(!(ybest.label > -1 && ybest.label < 5 && ybest.label != 3)) {
            	setSegment(dataSeq,ybest.prevPos(),ybest.pos, ybest.label);
            } else {
            	
            	totalScoreOnlyEntityInSequencePercentage *= stateTransitionValue/maxStateTransitionScoreInSequence[ybest.pos];
            	totalScoreOnlyEntityInSequenceListPercentage.add(stateTransitionValue/maxStateTransitionScoreInSequence[ybest.pos]);
            	totalEntitiesInSequence++;
            	
            	if(stateTransitionValue/maxStateTransitionScoreInSequence[ybest.pos] >= model.probabilityInformationSet.getThreshould())
	            	setSegment(dataSeq,ybest.prevPos(),ybest.pos, ybest.label);
	            else
	            	setSegment(dataSeq,ybest.prevPos(),ybest.pos, LabelMap.getLabelIndexPOSTagPTBR("Outside"));            	
            }*/
            
            /* Not Fixed */
            if(ybest.label > -1 && ybest.label < 5 && ybest.label != 3) {
            	totalScoreOnlyEntityInSequencePercentage *= stateTransitionValue/maxStateTransitionScoreInSequence[ybest.pos];
            	totalScoreOnlyEntityInSequenceListPercentage.add(stateTransitionValue/maxStateTransitionScoreInSequence[ybest.pos]);
            	totalEntitiesInSequence++;
            	
            	//if(stateTransitionValue/maxStateTransitionScoreInSequence[ybest.pos] >= model.probabilityInformationSet.getThreshould())
            	if(stateTransitionValue/stateTransitionNonEntityToEntityPer >= model.probabilityInformationSet.getThreshould()) {	
	            	setSegment(dataSeq,ybest.prevPos(),ybest.pos, ybest.label);
            	}
            	else {
            		setSegment(dataSeq,ybest.prevPos(),ybest.pos, LabelMap.getLabelIndexPOSTagPTBR("Outside"));
            		if(!alreadyAdded) {
            			alreadyAdded = true;
            			model.probabilityInformationSet.addRealibility(false);
            		}
            	}
            	
            } else {
            	setSegment(dataSeq,ybest.prevPos(),ybest.pos, LabelMap.getLabelIndexPOSTagPTBR("Outside"));
            }
            
            pos = ybest.pos;
            ybest = ybest.prevSoln;
        }
        assert(pos>=0);
        
        if(!alreadyAdded) {
			alreadyAdded = true;
			model.probabilityInformationSet.addRealibility(true);
		}
        
        /*if(totalEntitiesInSequence > 0 && assignLabelsMsg.length() > 0)
        	System.out.print(assignLabelsMsg);*/
    }
    public double viterbiSearch(DataSequence dataSeq, double lambda[], boolean calcCorrectScore) {
	if (Mi == null) {
	    allocateScratch(model.numY);
	}
	if ((winningLabel[0] == null) || (winningLabel[0].length < dataSeq.length())) {
	    for (int yi = 0; yi < winningLabel.length; yi++) {
		winningLabel[yi] = new Entry[dataSeq.length()];
		for (int l = 0; l < dataSeq.length(); l++)
		    winningLabel[yi][l] = new Entry((l==0)?1:beamsize, yi, l);
	    }
	}
	
	double corrScore = fillArray(dataSeq, lambda,calcCorrectScore);

	finalSoln.clear();
	finalSoln.valid = true;
	for (int yi = 0; yi < model.numY; yi++) {
	    finalSoln.add(winningLabel[yi][dataSeq.length()-1], 0);
	}
	return corrScore;
    }
    int numSolutions() {return finalSoln.numSolns();}
    Soln getBestSoln(int k) {
	return finalSoln.get(k).prevSoln;
    }
    
    //@DMZ
    protected void printStateTransitionMatrix(int i, String token) {
    	
    	String printMsg;
    	String resultMsg;
    	
    	double totalPercent = 0;
    	double stateTransition = 0;
    	double totalStateTransition = 0;
    	
    	normalizeStateTransitionMatrix(i);
    	printMsg = "\n-- State Transition Matrix ("+token+")\n" + offsetMatrix("");
    	
    	for(int yp = 0; yp < stateTransitionMatrix[i].length; yp++) {
    		printMsg += offsetMatrix("yp(" 
    				+ ((yp < stateTransitionMatrix[i].length-1)?LabelMap.getLabelNamePOSTagPTBR(yp).substring(0, 4):-1) 
    				+ ")") + "\t";
    	}
    	
    	//System.out.println(printMsg);
    	
    	for(int yi = 0; yi < model.numY; yi++) {
    		
    		totalPercent = 0;
    		totalStateTransition = totalStateTransitionScore[i][yi];
    		
    		printMsg = offsetMatrix("yi(" + LabelMap.getLabelNamePOSTagPTBR(yi).substring(0, 4) + ")");
    		
    		for(int yp = 0; yp < stateTransitionMatrix[i].length; yp++) {
    			
    			stateTransition = stateTransitionMatrix[i][yp][yi];
    			resultMsg = (new DecimalFormat("#.##")).format(stateTransition) + "[";
    			resultMsg += (new DecimalFormat("#.##")).format((totalStateTransition != 0)? 100 * stateTransition/totalStateTransition : 0);
    			resultMsg += "%]";
    			printMsg += offsetMatrix(resultMsg) + "\t";
    			
    			totalPercent += (totalStateTransition != 0)? 100 * stateTransition/totalStateTransition : 0;
    		}
    		
    		printMsg += "TOTAL%: " + (new DecimalFormat("#.##")).format(totalPercent) + "%";
    		//System.out.println(printMsg);
    	}
    	
    	//System.out.println();
    }
    
    private String offsetMatrix(String term) {
    	String result = term;
    	
    	for(int i = term.length(); i < 10; i++)
    		result += " ";
    	
    	return(result);
    }
    
    private void startStateTransitionMatrix(int sequenceLength, int previousStateLength, int currentStateLength) {
    	
    	stateTransitionMatrix = new double[sequenceLength][previousStateLength][currentStateLength];
    	totalStateTransitionScore = new double[sequenceLength][currentStateLength];
    	maxStateTransitionScoreInSequence = new double[sequenceLength];
    	
    	for(int i = 0 ; i < sequenceLength; i++) {
    		
    		maxStateTransitionScoreInSequence[i] = Double.MIN_VALUE;
    		
    		for(int yp = 0; yp < previousStateLength; yp++) {
    			for(int yi = 0; yi < currentStateLength; yi++) {
    			stateTransitionMatrix[i][yp][yi] = 0;
    			}
    		}
    		
    		for(int yi = 0; yi < currentStateLength; yi++) {
    			totalStateTransitionScore[i][yi] = 0;
    		}
    	}
    }
    
    private void normalizeStateTransitionMatrix(int i) {
    	
    	double [] minStateTransitionValue = new double [stateTransitionMatrix[i][0].length];
    	
    	for(int yi = 0; yi < stateTransitionMatrix[i][0].length; yi++) {
    		
    		minStateTransitionValue[yi] = Double.MAX_VALUE;
    		
    		for(int yp = 0; yp < stateTransitionMatrix[i].length; yp++) {
    			if(minStateTransitionValue[yi]  > stateTransitionMatrix[i][yp][yi])
    				minStateTransitionValue[yi] = stateTransitionMatrix[i][yp][yi];
    		}
    	}
    	
    	for(int yi = 0; yi < minStateTransitionValue.length; yi++) {
    		if(minStateTransitionValue[yi] < 0) {
    			for(int yp = 0; yp < stateTransitionMatrix[i].length; yp++) {
    				stateTransitionMatrix[i][yp][yi] += -1 * minStateTransitionValue[yi];
    				totalStateTransitionScore[i][yi] += -1 * minStateTransitionValue[yi];
    			}    			
    		}
    	}
    }
    
    protected String getAllEntitySequenceProbability() {
    	String allEntitySequenceProbability = " ";
    	
    	for(int i = 0; i < totalScoreOnlyEntityInSequenceListPercentage.size(); i++)
    		allEntitySequenceProbability += "e" + (i+1) + "(" + 
    		(new DecimalFormat("#.##")).format(100 * totalScoreOnlyEntityInSequenceListPercentage.get(i)) + "%), ";
    	
    	return(allEntitySequenceProbability);
    }
    
    public void printPorcentageDivision() {
    	
    	int totalNumber = 0;
    	double accumulative = 0;
    	String msg = "";
    	
    	for(int i = 0; i < relativeTransitionScorePercentageDivision.length; i++)
    		totalNumber += relativeTransitionScorePercentageDivision[i];
    	
    	System.out.println("-- Relative Transition Score Percentage Division");
    	
    	for(int i = 0; i < relativeTransitionScorePercentageDivision.length; i++) {
    		
    		accumulative += relativeTransitionScorePercentageDivision[i];
    		
    		msg = "(" + (i * 5) + "--" + (i+1) * 5 + ")% ";
    		msg += "numRel: " + ((i > 0)?relativeTransitionScorePercentageDivision[i] - relativeTransitionScorePercentageDivision[i-1]:0);
    		msg += "["+ ((i > 0)?(new DecimalFormat("#.##")).format(100 * (relativeTransitionScorePercentageDivision[i]- relativeTransitionScorePercentageDivision[i-1])/totalNumber):0) +"%] ";
    		msg += "numAbs:" + relativeTransitionScorePercentageDivision[i];
    		msg += "[" + (new DecimalFormat("#.##")).format(100 * relativeTransitionScorePercentageDivision[i]/totalNumber) + "%] ";
    		msg += "acc: " + accumulative;
    		msg += "[" + (new DecimalFormat("#.##")).format(100 * accumulative/totalNumber) + "%]";
    		System.out.println(msg);
    	}
    	
    	relativeTransitionScorePercentageDivision = null;
    }
    
    protected boolean hasContext(DataSequence sequence, int pos) {
    	boolean hasContext = false;
    	
    	String [] sequenceList = Utils.convertSequenceToLowerCase(sequence, sequence.length());
    	ContextToken context = ((FeatureGenImpl)model.featureGenerator).getSupportContext().existContextInSequenceContextHashMap(sequenceList, pos);
    	
    	if(context == null)
    		context = ((FeatureGenImpl)model.featureGenerator).getSupportContext().existContextInSequencePrefixExtendedHashMap(sequenceList, pos);
    	
    	if(context == null)
    		context = ((FeatureGenImpl)model.featureGenerator).getSupportContext().existSuffixInSequenceRestrictedSuffixContextHashMap(sequenceList, pos);
    	
    	hasContext = context != null;
    	
    	return(hasContext);
    }
    
    public ProbabilityInformationSet getProbabilityInformation() {
    	return(model.probabilityInformationSet);
    }
};
