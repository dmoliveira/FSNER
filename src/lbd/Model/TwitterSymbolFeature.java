package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class TwitterSymbolFeature extends NewFeatureTypes{

	private static final long serialVersionUID = 1L;

	protected TwitterSymbol twitterSymbol;

	public TwitterSymbolFeature(FeatureGenImpl fgen, TwitterSymbol twitterSymbol) {
		super(fgen);

		this.twitterSymbol = twitterSymbol;

		featureName = "TwitterSymbol";
		featureType = FeatureType.TwitterSymbol;

		proccessSequenceType = ProccessSequenceType.Plain;
		skipOutsideState = false;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {

		String term = proccessedSequence[pos];
		int symbolPosition = -1;

		int symbolId = (CapitalizationFeature.isCapitalized(term) &&
				(symbolPosition = getTwitterSymbolPositionInSequence(pos)) != -1)?
						twitterSymbol.getTwitterSymbol(proccessedSequence[symbolPosition]) : -1;

						return symbolId;
	}

	protected int getTwitterSymbolPositionInSequence(int pos) {

		int twitterSymbolPos = -1;

		for(int i = 0; i < proccessedSequence.length; i++) {
			if(i != pos && twitterSymbol.getTwitterSymbol(proccessedSequence[i]) != -1) {
				twitterSymbolPos = i;
				break;
			}
		}

		return(twitterSymbolPos);
	}

}
