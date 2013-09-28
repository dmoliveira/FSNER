package lbd.NewModels.Affix;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.Affix.Affix.AffixType;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class TokenAffixFeature extends NewFeatureTypes{

	private static final long serialVersionUID = 1L;

	protected transient AffixManager affixManager;
	protected transient AffixType affixType;

	public TokenAffixFeature(FeatureGenImpl fgen, AffixManager affixManager, AffixType affixType) {
		super(fgen);

		this.affixManager = affixManager;
		this.affixType = affixType;

		featureName = "tokenAffix+"+affixType.name()+"(" + affixType.name() + ")";
		featureType = FeatureType.TokenAffix;

		proccessSequenceType = ProccessSequenceType.Plain;
		iterateOverPreviousState = false;
		skipOutsideState = false;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {

		int tokenAffixId = -1;
		String affixValue = affixManager.getAffix(proccessedSequence[pos], affixType);

		if(!affixValue.isEmpty()) {
			tokenAffixId = affixManager.getAffixId(affixValue);

			if(!isInTrain() && tokenAffixId > -1 && stateFrequencyMap.containsKey(tokenAffixId)) {

				Integer [] freq = stateFrequencyMap.get(tokenAffixId);
				double perc = ((double)freq[3])/(freq[0] + freq[1] + freq[2] + freq[3] + freq[4]);

				/*if(perc >= 0.4 &&  perc <= 0.7){
					tokenAffixId = -1;
					//System.out.println("Perc: " + perc);
				}*/

			}
		}

		/*if(!affixValue.isEmpty()) {
			tokenAffixId = affixManager.getAffixId(affixValue);
			//System.out.println(tokenAffixId + " " + affixValue);

			String clusterValue = brownCluster.getClusterValue(proccessedSequence[pos]);
			boolean isOk = true;

			for(int i = 8; i <= 18; i++)
				isOk &= (clusterValue.length() > i)? brownCluster.getClusterValuePrefixId(clusterValue.substring(0,i)) < 0 : false;

			//if(contextAnalysisList != null && summarizedPattern.getSummarizedPatternId(proccessedSequence[pos]) < 0 && isOk && dict.count((String)data.x(pos)) < 1 && tokenAffixId > -1) {
			if(contextAnalysisList != null && tokenAffixId > -1) {

				int contextId = -1;
				int index = 0;

				while(contextId == -1 && index < contextAnalysisList.size()) {

					contextId = contextAnalysisList.get(index).getContextId(data, pos, ContextType.Prefix, 1);
					if(contextId > -1) {
						tokenAffixId = tokenAffixId * contextAnalysisList.get(index).getMaxContextId() + contextId;
					} else {

						contextId = contextAnalysisList.get(index).getContextId(data, pos, ContextType.Suffix, 1);

						if(contextId > -1) {
							tokenAffixId = tokenAffixId * contextAnalysisList.get(index).getMaxContextId() + contextId;
						} else {
							tokenAffixId = -1;
						}
					}
					index++;
				}
			}
		}*/

		return tokenAffixId;
	}
}
