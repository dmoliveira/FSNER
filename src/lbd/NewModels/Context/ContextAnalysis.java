package lbd.NewModels.Context;
import iitb.CRF.DataSequence;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.CRF.LabelMap.BILOU;
import lbd.FSNER.Component.SequenceLabel;
import lbd.NewModels.Affix.Affix.AffixType;
import lbd.NewModels.BrownHierarquicalCluster.BrownCluster;
import lbd.NewModels.Context.ContextManager.ContextType;
import lbd.SummirizedPattern.SummarizedPattern;
import lbd.Utils.Utils;

public class ContextAnalysis implements Serializable{

	private static final long serialVersionUID = 1L;

	protected transient final String ENCODE_USED = "ISO-8859-1";
	protected transient final String DELIMITER_SPLIT = "\\|";
	protected transient final String TAG_EMPTY = "";

	protected AffixType affixType;

	//-- Parameters
	protected int maxContextId;
	protected int [] windowSize; //-- 3
	protected double maximumDistance; //-- 0.25

	protected transient DataSequence sequence;
	protected String [] processedSequence;

	protected ArrayList<HashMap<String, ArrayList<ContextUnit>>> affixList;

	protected transient BrownCluster brownCluster;
	protected final int BIT_PREFIX_SIZE = 10;

	protected transient SummarizedPattern summarizedPattern;

	protected boolean addOutsideTerm = false;

	public ContextAnalysis(double maximumDistance, int maximumWindowSize, AffixType affixType, SummarizedPattern summarizedPattern) {

		this(maximumDistance, maximumWindowSize, affixType);
		this.summarizedPattern = summarizedPattern;
		maxContextId = 0;
	}

	public ContextAnalysis(double maximumDistance, int maximumWindowSize, AffixType affixType, BrownCluster brownCluster) {

		this(maximumDistance, maximumWindowSize, affixType);
		this.brownCluster = brownCluster;
		maxContextId = 0;
	}

	public ContextAnalysis(double maximumDistance, int maximumWindowSize, AffixType affixType) {

		affixList = new ArrayList<HashMap<String, ArrayList<ContextUnit>>>();
		for(int i = 0; i < ContextType.values().length; i++) {
			affixList.add(new HashMap<String, ArrayList<ContextUnit>>());
		}

		windowSize = new int [maximumWindowSize];
		for(int i = 0; i < maximumWindowSize; i++) {
			windowSize[i] = i+1;
		}

		this.maximumDistance = maximumDistance;
		this.affixType = affixType;
	}

	public void addContext(DataSequence data) {

		proccessSequence(data);

		for(int i = 0; i < data.length(); i++) {
			if(addOutsideTerm || data.y(i) != BILOU.Outside.ordinal()) {
				addSimilarContext(processedSequence, i);
			}
		}
	}

	public void addContext(SequenceLabel sequenceLabel) {

		for(int i = 0; i < sequenceLabel.size(); i++) {
			if(addOutsideTerm || sequenceLabel.getLabel(i) != BILOU.Outside.ordinal()) {
				addSimilarContext(sequenceLabel.toArraySequence(), i);
			}
		}
	}

	public void loadContext(String inputFilenameAddress) {

		String load = (inputFilenameAddress.isEmpty())?"./samples/data/bcs2010/ContextAnalysis/weps-3_task-2_training.tagged":inputFilenameAddress;

		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(load)));
			String line;
			ArrayList<String> sequence = new ArrayList<String>();

			while((line = in.readLine()) != null) {
				if(!line.isEmpty()) {
					sequence.add(line.split("\\|")[0]);
				} else {

					String [] proccessedSequence = sequence.toArray(new String[sequence.size()]);

					for(int i = 0; i < sequence.size(); i++) {
						addSimilarContext(proccessedSequence, i);
					}

					sequence = new ArrayList<String>();
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void addSimilarContext(String [] sequenceLowerCase, int indexTerm) {

		String encodedContext = ContextManager.getEncodedContext(sequenceLowerCase, indexTerm);

		String encodedContextWindow;
		String similarContext;

		Context context = new Context();
		context.addEncodedContext(encodedContext);

		for(int i = 0; i < windowSize.length; i++) {

			//-- Add for each affix (PreffixSuffix, Prefix, Suffix)
			for(int a = 0; a < ContextType.values().length; a++) {
				encodedContextWindow = context.getEncodedContext(ContextType.values()[a], windowSize[i]);

				if(encodedContextWindow.length() > 0) {

					similarContext = getSimilarContext(encodedContextWindow, ContextType.values()[a], windowSize[i]);

					if(similarContext.isEmpty()) {
						similarContext = encodedContextWindow;
						affixList.get(a).put(encodedContextWindow, new ArrayList<ContextUnit>());
					} {

					}

					affixList.get(a).get(similarContext).add(new ContextUnit(encodedContextWindow, sequenceLowerCase[indexTerm]));
					maxContextId++;
				}
			}
		}
	}

	public String getSimilarContext(String encodedContext, ContextType contextType, int windowSize) {

		HashMap<String, ArrayList<ContextUnit>> afixMap = affixList.get(contextType.ordinal());

		String key = "";
		String similarContext = "";

		String encondedContextWindow = ContextManager.trimEncodedContext(encodedContext, contextType, windowSize);

		if(afixMap.containsKey(encondedContextWindow)) {
			similarContext = encondedContextWindow;
		}

		/*Iterator<Entry<String, ArrayList<ContextUnit>>> ite = afixMap.entrySet().iterator();
		JaroWinklerDistance jWDistance = new JaroWinklerDistance(); // less better
		SoftTFIDF softTFIDF = new SoftTFIDF(new JaroWinkler(), maximumDistance); // more better

		while(ite.hasNext()) {
			key = ite.next().getKey();

			if(softTFIDF.score(key, encondedContextWindow) >= maximumDistance) {
			//if(jWDistance.distance(key, encondedContextWindow) <= maximumDistance) {
				similarContext = key;
				break;
			}
		}*/

		return(similarContext);
	}

	public ArrayList<ContextUnit> getSimilarContextList(String [] sequenceLowerCase, int indexTerm,
			ContextType contextType, int windowSize) {

		String encodedContext = ContextManager.getEncodedContext(sequenceLowerCase, indexTerm);
		String encondedContextWindow = ContextManager.trimEncodedContext(encodedContext, contextType, windowSize);

		ArrayList<ContextUnit> contextList = null;

		if(affixList.get(contextType.ordinal()).containsKey(encondedContextWindow)) {
			contextList = affixList.get(contextType.ordinal()).get(encondedContextWindow);
		}

		return(contextList);
	}

	/**********************************************************************************
	 * 
	 * Auxiliary Methods
	 *
	 **********************************************************************************/

	protected void proccessSequence(DataSequence data) {

		if(this.sequence != data) {
			this.sequence = data;

			if(brownCluster == null && summarizedPattern == null) { // -- Transform to Lower Case
				processedSequence = Utils.transformSequenceToArray(data, data.length(), affixType);//Utils.convertSequenceToLowerCase(data, data.length(), affixType);
			} else {

				processedSequence = new String[data.length()];

				for(int i = 0; i < data.length(); i++) {
					if(brownCluster != null) { //-- Brown Hierarquical Cluster Encoding
						processedSequence[i] = brownCluster.getClusterValue((String)data.x(i));
						if(processedSequence[i].length() > BIT_PREFIX_SIZE && !processedSequence[i].equals(BrownCluster.TAG_NOT_FOUND)) {
							processedSequence[i] = processedSequence[i].substring(0, BIT_PREFIX_SIZE);
						}
					} else if(summarizedPattern != null) {
						processedSequence[i] = summarizedPattern.getPattern((String)data.x(i));
					}
				}
			}
		}
	}

	public int getContextId(DataSequence data, int pos, ContextType contextType, int windowSize) {

		proccessSequence(data);
		int contextId = -1;

		if(data.length() > 1) {
			contextId = getContextId(pos, contextType, windowSize);
		}

		return(contextId);
	}

	public int getContextId(String [] processedSequence, int pos, ContextType contextType, int windowSize) {

		int contextId = -1;

		this.processedSequence = processedSequence;

		if(processedSequence.length > 1) {
			contextId = getContextId(pos, contextType, windowSize);
		}

		return(contextId);
	}

	protected int getContextId(int pos, ContextType contextType, int windowSize) {

		int contextId = -1;

		Context context = new Context();
		context.addEncodedContext(ContextManager.getEncodedContext(processedSequence, pos));

		String encodedContext = context.getEncodedContext(contextType, windowSize);

		ArrayList<ContextUnit> contextUnitList = affixList.get(contextType.ordinal()).get(encodedContext);
		ContextUnit contextUnit = (contextUnitList != null)? contextUnitList.get(0) : null;

		contextId = (contextUnit != null)? contextUnit.getContextNumberId() : -1;

		return(contextId);
	}

	public int getMaxContextId() {
		return(maxContextId);
	}

	public void readContextAnalysisObject(String filename, ContextAnalysis target) throws IOException, ClassNotFoundException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		ContextAnalysis contextAnalysis = (ContextAnalysis) in.readObject();
		cloneContextAnalysis(target, contextAnalysis);

		in.close();
	}

	private void cloneContextAnalysis(ContextAnalysis target, ContextAnalysis clone) {

		target.windowSize = clone.windowSize;
		target.maximumDistance = clone.maximumDistance;
		target.affixList = clone.affixList;
	}

	public void writeContextAnalysisObject(String filename) throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
	}

}
