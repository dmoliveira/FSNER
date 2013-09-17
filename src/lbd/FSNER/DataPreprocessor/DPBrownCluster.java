package lbd.FSNER.DataPreprocessor;

import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.DataProcessor.Component.BrownCluster;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.FSNER.Utils.LabelEncoding;

public class DPBrownCluster extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;
	
	protected BrownCluster brownCluster;
	protected int prefixSize;

	public DPBrownCluster(String initializeFile, int prefixSize) {
		
		super(ClassName.getSingleName(DPBrownCluster.class.getName())+
				"Pfx"+prefixSize, initializeFile);
		
		this.prefixSize = prefixSize;
	}
	
	@Override
	public SequenceLabelElement preprocessingTerm(String term, int label) {
		
		String proccessedTerm = brownCluster.getClusterValue(term);
		proccessedTerm = (proccessedTerm.length() > prefixSize)? proccessedTerm.substring(0, prefixSize) : null;
		
		SequenceLabelElement sequenceLabelElement = (proccessedTerm != null)? 
				new SequenceLabelElement(proccessedTerm, label) :
					new SequenceLabelElement(Symbol.EMPTY, LabelEncoding.BILOU.Outside.ordinal());
		
		return (sequenceLabelElement);
	}

	@Override
	public void initialize() {
		brownCluster = new BrownCluster();
		brownCluster.loadBrownHierarquicalCluster(mInitializeFile);
	}

}
