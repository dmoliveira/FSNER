package lbd.SummirizedPattern;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class SummarizedPatternFeature extends NewFeatureTypes{

	private static final long serialVersionUID = 1792008400816426047L;
	protected SummarizedPattern summarizedPattern;

	public SummarizedPatternFeature(FeatureGenImpl fgen, SummarizedPattern summarizedPattern) {
		super(fgen);
		
		featureName = "SummarizedPattern";
		proccessSequenceType = ProccessSequenceType.Plain;
		featureType = FeatureType.SummarizedPattern;
		
		this.summarizedPattern = summarizedPattern;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		
		return summarizedPattern.getSummarizedPatternId((String)data.x(pos));
	}

}
