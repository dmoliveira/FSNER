package lbd.FSNER.DataPreprocessor;

import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.DataProcessor.Component.BrownClusterEtzioni;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.Symbol;

public class DPBrownClusterEtzioni extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;
	
	protected int bitPrefixSize;
	protected BrownClusterEtzioni brownClusterEtzioni;

	public DPBrownClusterEtzioni(String activityName, String initializeFile, int prefixSize) {
		super(activityName, initializeFile);
		
		this.bitPrefixSize = prefixSize;
	}

	@Override
	public SequenceLabelElement preprocessingTerm(String term, int label) {
		
		return (new SequenceLabelElement(Symbol.EMPTY + brownClusterEtzioni.getClusterId(term, bitPrefixSize), label));
	}

	@Override
	public void initialize() {
		brownClusterEtzioni = new BrownClusterEtzioni();
		brownClusterEtzioni.loadBrownCluster(mInitializeFile);
	}
	
}
