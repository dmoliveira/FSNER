package lbd.NewModels.BrownHierarquicalCluster;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class BrownClusterEtzioniFeature extends NewFeatureTypes{

	private static final long serialVersionUID = 6899071648149695506L;
	
	protected transient BrownClusterEtzioni brownClusterEtzioni;
	protected int bitPrefixSize;

	public BrownClusterEtzioniFeature(FeatureGenImpl fgen, BrownClusterEtzioni brownClusterEtzioni, int bitPrefixSize) {
		super(fgen);
		
		featureName = "BrownClusterEtzioni";
		featureType = FeatureType.BrownHierarquicalCluster;
		proccessSequenceType = ProccessSequenceType.AllLowerCase;
		skipOutsideState = true;
		
		this.brownClusterEtzioni = brownClusterEtzioni;
		this.bitPrefixSize = bitPrefixSize;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		return brownClusterEtzioni.getClusterId(proccessedSequence[pos], bitPrefixSize);
	}
	
	

}
