package lbd.NewModels.BrownHierarquicalCluster;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class JClusterFeature extends NewFeatureTypes {

	private static final long serialVersionUID = 677788119244886216L;
	
	protected transient JCluster jCluster;
	protected int bitPrefixSize;

	public JClusterFeature(FeatureGenImpl fgen, JCluster jCluster, int bitPrefixSize) {
		
		super(fgen);
		
		featureName = "JCluster";
		featureType = FeatureType.JCluster;
		proccessSequenceType = ProccessSequenceType.Plain;
		skipOutsideState = true;
		
		this.jCluster = jCluster;
		this.bitPrefixSize = bitPrefixSize;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		return jCluster.getJClusterId(proccessedSequence[pos], bitPrefixSize);
	}

}
