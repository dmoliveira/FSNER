package lbd.NewModels.BrownHierarquicalCluster;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class BrownClusterFeature extends NewFeatureTypes {

	private static final long serialVersionUID = 1L;

	protected BrownCluster brownCluster;

	protected int bitPrefixSize;

	public BrownClusterFeature(FeatureGenImpl fgen, BrownCluster brownCluster, int prefixSize) {

		super(fgen);

		this.brownCluster = brownCluster;
		this.bitPrefixSize = prefixSize;

		featureName = "brownClusterBitPfxSz[" + bitPrefixSize + "]";
		featureType = FeatureType.BrownHierarquicalCluster;
		proccessSequenceType = ProccessSequenceType.Plain;

		skipOutsideState = true;

	}

	@Override
	protected int startFeature(DataSequence data, int pos) {

		int brownClusterId = -1;
		String clusterValue = brownCluster.getClusterValue(proccessedSequence[pos]);

		if(!clusterValue.equals(BrownCluster.TAG_NOT_FOUND) && clusterValue.length() > bitPrefixSize) {
			brownClusterId = brownCluster.getClusterValuePrefixId(clusterValue.substring(0, bitPrefixSize));
			//System.out.println(clusterValue + " -("+bitPrefixSize+")-> " + clusterValue.substring(0, bitPrefixSize) + " bhcId: " + brownClusterId);
		}

		return brownClusterId;
	}

}
