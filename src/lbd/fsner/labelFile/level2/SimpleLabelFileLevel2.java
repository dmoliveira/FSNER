package lbd.fsner.labelFile.level2;

import java.util.List;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Filter.FtrGazetteer;
import lbd.FSNER.Model.AbstractActivityControl;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Utils.Symbol;
import lbd.Utils.StringUtils;
import lbd.data.handler.ISequence;
import lbd.fsner.entity.EntityType;
import lbd.fsner.label.encoding.Label;

public class SimpleLabelFileLevel2 extends AbstractLabelFileLevel2 {


	public SimpleLabelFileLevel2(AbstractActivityControl pActivityControl) {
		super(pActivityControl);
	}

	@Override
	public ISequence labelSequenceLevel2(ISequence pSequence) {

		ISequence vSequenceLabeledLevel2 = pSequence;

		for(int i = 0; i < vSequenceLabeledLevel2.length(); i++) {
			if(Label.getCanonicalLabel(vSequenceLabeledLevel2.getLabel(i)) == Label.UnitToken) {
				String vCandidateEntity = getCandidateEntityFromGazetteerFilterSet(pSequence, i);
				if(!StringUtils.isNullOrEmpty(vCandidateEntity)) {
					List<Label> vEntityLabels = Parameters.DataHandler.mLabelEncoding.getLabels(vCandidateEntity);
					EntityType vEntityType = EntityType.getEntityType(vSequenceLabeledLevel2.getLabel(i));
					for(Label cLabel : vEntityLabels) {
						vSequenceLabeledLevel2.setLabel(i++, Label.getOrdinalLabel(vEntityType, cLabel));
					}
					i--;
				}
			}
		}

		return vSequenceLabeledLevel2;
	}

	private String getCandidateEntityFromGazetteerFilterSet(ISequence pSequence, int pIndex) {
		String vCandidateEntity = Symbol.EMPTY;

		for(AbstractFilter cFilter : mActivityControl.getFiltersByClassName(FtrGazetteer.class.getName())) {
			int vEntitySize = 4;

			while(vEntitySize > 0 && StringUtils.isNullOrEmpty(vCandidateEntity = ((FtrGazetteer)cFilter).getCandidateEntity(pSequence, pIndex, vEntitySize--))) {
				;
			}

			if(cFilter.getFilterProbability().getProbability(cFilter.getSequenceInstanceId(pSequence,
					mActivityControl.getDataPreprocessorList().get(cFilter.getFilterPreprocessingTypeIndex()).preprocessingSequence(pSequence), pIndex)) < 0.7) {
				vCandidateEntity = Symbol.EMPTY;
			}

			if(!StringUtils.isNullOrEmpty(vCandidateEntity)) {
				break;
			}
		}

		return vCandidateEntity;
	}
}
