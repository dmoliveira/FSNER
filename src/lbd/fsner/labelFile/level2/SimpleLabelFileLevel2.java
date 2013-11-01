package lbd.fsner.labelFile.level2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Evaluation.Component.Statistics;
import lbd.FSNER.Filter.FtrGazetteer;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractLabelFile;
import lbd.FSNER.Utils.Symbol;
import lbd.Utils.FrequencyTwoStepMap;
import lbd.Utils.StringUtils;
import lbd.data.handler.ISequence;
import lbd.fsner.entity.Entity;
import lbd.fsner.entity.EntityType;
import lbd.fsner.label.encoding.Label;

public class SimpleLabelFileLevel2 extends AbstractLabelFileLevel2 {

	protected Map<EntityType, Map<String, Statistics>> mGapBetweenEntitiesMap;
	protected FrequencyTwoStepMap<String> mTokenEntityTypeFrequencyMap;
	protected FrequencyTwoStepMap<String> mEntityTypePrevalenceMap;
	protected Set<AbstractFilter> mGazetteerFilters;

	@Override
	public void trainLabelSequenceLevel2(List<ISequence> pGoldSequenceList,
			Map<String, Set<AbstractFilter>> pClassNameSingleFilterMap, AbstractLabelFile pLabelFile) {

		mGapBetweenEntitiesMap = new HashMap<EntityType, Map<String, Statistics>>();
		mTokenEntityTypeFrequencyMap = new FrequencyTwoStepMap<String>();
		mEntityTypePrevalenceMap = new FrequencyTwoStepMap<String>();
		mGazetteerFilters = pClassNameSingleFilterMap.get(FtrGazetteer.class.getName());

		//-- Collect data
		for(ISequence cGoldSequence : pGoldSequenceList) {

			ISequence vTaggedSequence = pLabelFile.labelSequence(cGoldSequence.clone());

			for(int i = 0; i < cGoldSequence.length(); i++) {
				if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(cGoldSequence.getLabel(i)))) {
					addToGapMap(vTaggedSequence, cGoldSequence, i, 1);
					addToGapMap(vTaggedSequence, cGoldSequence, i, 2);
					addToTokenEntityTypeFrequencyMap(vTaggedSequence, cGoldSequence, i);
					addToEntityTypePrevalenceMap(cGoldSequence, vTaggedSequence, i);
				}
			}
		}
	}

	private void addToGapMap(ISequence pTaggedSequence, ISequence pGoldSequence, int pIndex, int pGapSize) {

		EntityType vEntityType = EntityType.getEntityType(pTaggedSequence.getLabel(pIndex));

		if(hasGapBetweenEntities(pTaggedSequence, pIndex, pGapSize)) {
			if(!mGapBetweenEntitiesMap.containsKey(vEntityType)) {
				mGapBetweenEntitiesMap.put(vEntityType, new HashMap<String, Statistics>());
			}

			String vGap = getGap(pTaggedSequence, pIndex, pGapSize);

			if(!mGapBetweenEntitiesMap.get(vEntityType).containsKey(vGap)) {
				mGapBetweenEntitiesMap.get(vEntityType).put(vGap, new Statistics());
			}

			for(int cGap = pIndex + 1; cGap <= pIndex + pGapSize; cGap++) {
				if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(pGoldSequence.getLabel(cGap)))) {
					mGapBetweenEntitiesMap.get(vEntityType).get(vGap).addTP();
				} else {
					mGapBetweenEntitiesMap.get(vEntityType).get(vGap).addFP();
				}
			}
		}
	}

	private void addToTokenEntityTypeFrequencyMap(ISequence pTaggedSequence, ISequence pGoldSequence, int pIndex) {
		mTokenEntityTypeFrequencyMap.add(pTaggedSequence.getToken(pIndex), "" + pGoldSequence.getLabel(pIndex));
	}

	private void addToEntityTypePrevalenceMap(ISequence cGoldSequence,
			ISequence vTaggedSequence, int i) {
		if(i + 1 < cGoldSequence.length()
				&& Parameters.DataHandler.mLabelEncoding.isEndEntity(vTaggedSequence, i)
				&& Parameters.DataHandler.mLabelEncoding.isStartEntity(vTaggedSequence, i + 1)) {

			mEntityTypePrevalenceMap.add(EntityType.getEntityType(vTaggedSequence.getLabel(i)).getValue()
					+ "-" + EntityType.getEntityType(vTaggedSequence.getLabel(i + 1)).getValue(),
					"" + cGoldSequence.getLabel(i));

			mEntityTypePrevalenceMap.add(EntityType.getEntityType(vTaggedSequence.getLabel(i)).getValue()
					+ "-" + EntityType.getEntityType(vTaggedSequence.getLabel(i + 1)).getValue(),
					"" + cGoldSequence.getLabel(i + 1));
		}
	}

	@Override
	public ISequence labelSequenceLevel2(ISequence pSequence) {

		List<Entity> vEntityList = Parameters.DataHandler.mLabelEncoding.getEntities(pSequence);

		applyRuleFixBrokedLabel(pSequence, vEntityList);

		applyRuleRecoverTokenAroundEntity(pSequence, vEntityList, 1);

		applyRuleFixBrokedLabel(pSequence, vEntityList);

		applyRuleGlueEntityTogether(pSequence);

		applyRuleGlueEntitiesByGap(pSequence, 2);

		applyRuleGlueEntitiesByGap(pSequence, 1);

		applyRuleFixBrokedLabel(pSequence, vEntityList);

		applyRuleLabelingGatezetteerEntry(pSequence, vEntityList);

		applyRuleFixBrokedLabel(pSequence, vEntityList);

		applyRuleGlueEntityTogether(pSequence);

		applyRuleFixBrokedLabel(pSequence, vEntityList);

		applyRuleGlueEntitiesByGap(pSequence, 1);

		applyRuleFixBrokedLabel(pSequence, vEntityList);

		applyRuleGlueEntityTogether(pSequence);

		applyRuleFixBrokedLabel(pSequence, vEntityList);

		applyRuleGlueEntityTogether(pSequence);

		return pSequence;
	}

	protected void applyRuleFixBrokedLabel(ISequence pSequence, List<Entity> pEntityList) {
		if(Parameters.LabelFileLevel2.mIsToUseRuleFixBrokedLabel) {
			for(Entity cEntity : pEntityList) {
				int vIndex = 0;
				for(Label cLabel : cEntity.getLabels()) {
					pSequence.setLabel(cEntity.getIndex() + vIndex++, Label.getOrdinalLabel(cEntity.getEntityType(), cLabel));
				}
			}
		}
	}

	protected void applyRuleRecoverTokenAroundEntity(ISequence pSequence, List<Entity> pEntityList, int pWindowSize) {
		if(Parameters.LabelFileLevel2.mIsToUseRuleRecoverTokenAroundEntity) {
			for(Entity cEntity : pEntityList) {
				setEntityLabelInterval(pSequence, cEntity, cEntity.getIndex() - pWindowSize, cEntity.getIndex() - 1);
				setEntityLabelInterval(pSequence, cEntity, cEntity.getIndex() + cEntity.size(), cEntity.getIndex() + (cEntity.size() - 1) + pWindowSize);
			}
		}
	}

	private void setEntityLabelInterval(ISequence pSequence, Entity pEntity, int pStartIndex, int pEndIndex) {
		for(int i = pStartIndex; i >= 0 && i <= pEndIndex && i < pSequence.length(); i++) {
			String vLabelStr = mTokenEntityTypeFrequencyMap.getMax(pSequence.getToken(i));
			if(!StringUtils.isNullOrEmpty(vLabelStr)) {
				Integer vLabel = Integer.parseInt(vLabelStr);
				pSequence.setLabel(i, vLabel);
			}
		}
	}

	protected void applyRuleGlueEntityTogether(ISequence pSequence) {
		if(Parameters.LabelFileLevel2.mIsToUseRuleGlueEntityTogether) {
			for(int i = 1; i < pSequence.length(); i++) {

				int vPreviousLabel = pSequence.getLabel(i - 1);
				int vCurrentLabel = pSequence.getLabel(i);

				if((Label.getCanonicalLabel(vPreviousLabel) == Label.Last || Label.getCanonicalLabel(vPreviousLabel) == Label.UnitToken)
						&& Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(vCurrentLabel))) {

					EntityType vPreviousEntityType = EntityType.getEntityType(vPreviousLabel);
					EntityType vCurrentEntityType = EntityType.getEntityType(vCurrentLabel);
					EntityType vMostProbablyEntityType = getMostProbablyEntityLinkedTogether(vPreviousEntityType, vCurrentEntityType);

					if(vCurrentEntityType == vPreviousEntityType) {
						Parameters.DataHandler.mLabelEncoding.joinEntities(pSequence, vPreviousEntityType, i - 1);
					} else if(vMostProbablyEntityType != null) {
						Parameters.DataHandler.mLabelEncoding.joinEntities(pSequence, vMostProbablyEntityType, i - 1);
					}
				}
			}
		}
	}

	private EntityType getMostProbablyEntityLinkedTogether(
			EntityType vPreviousEntityType, EntityType vCurrentEntityType) {
		String vLabelStr = mEntityTypePrevalenceMap.getMax(vPreviousEntityType.getValue() + "-" + vCurrentEntityType.getValue());
		EntityType vEntityType = (StringUtils.isNullOrEmpty(vLabelStr)? null : EntityType.getEntityType(Integer.parseInt(vLabelStr)));
		return vEntityType;
	}

	protected void applyRuleGlueEntitiesByGap(ISequence pSequence, int pGapSize) {
		if(Parameters.LabelFileLevel2.mIsToUseRuleGlueEntitiesByGap) {
			for(int i = 1; i < pSequence.length(); i++) {
				if(hasGapBetweenEntities(pSequence, i, pGapSize)) {
					EntityType vCurrentEntityType = EntityType.getEntityType(pSequence.getLabel(i));
					String vGap = getGap(pSequence, i, pGapSize);
					if(mGapBetweenEntitiesMap.containsKey(vCurrentEntityType)
							&& mGapBetweenEntitiesMap.get(vCurrentEntityType).containsKey(vGap)
							&& mGapBetweenEntitiesMap.get(vCurrentEntityType).get(vGap)
							.getPrecision() > Parameters.LabelFileLevel2.mThreshouldToUseGapContext) {

						EntityType vNext2EntityType = EntityType.getEntityType(pSequence.getLabel(i + pGapSize + 1));
						EntityType vMostProbablyEntityType = vCurrentEntityType;

						if(vCurrentEntityType != vNext2EntityType) {
							vMostProbablyEntityType = getMostProbablyEntityLinkedTogether(vCurrentEntityType, vNext2EntityType);
						}

						Parameters.DataHandler.mLabelEncoding.joinEntities(pSequence, vMostProbablyEntityType, i, i + pGapSize + 1);
					}
				}
			}
		}
	}

	protected boolean hasGapBetweenEntities(ISequence pSequence, int pIndex, int pGapSize) {
		if(pIndex + pGapSize + 1 >= pSequence.length()) {
			return false;
		}

		return (pIndex < pSequence.length() - (pGapSize + 1) && Parameters.DataHandler.mLabelEncoding.isEndEntity(pSequence, pIndex)
				&& Parameters.DataHandler.mLabelEncoding.isStartEntity(pSequence, pIndex + pGapSize + 1));
	}

	protected String getGap(ISequence pSequence, int pIndex, int pGapSize) {
		String vGap = pSequence.getToken(pIndex + 1);

		for(int i = pIndex + 2; i <= pIndex + pGapSize; i++) {
			vGap += Symbol.SPACE + pSequence.getToken(i);
		}

		return vGap;
	}

	protected void applyRuleLabelingGatezetteerEntry(ISequence pSequence, List<Entity> pEntityList) {
		if(Parameters.LabelFileLevel2.mIsToUseRuleLabelingGatezetteerEntry) {
			if(pEntityList != null && pEntityList.size() > 0) {
				for(int i = 1; i < pSequence.length(); i++) {
					if(pSequence.getToken(i-1).length() > 1 && pSequence.getToken(i).length() > 3) {

					}

					for(AbstractFilter cFilter : mGazetteerFilters) {

						String vEntityValue = ((FtrGazetteer)cFilter).getEntryInGazetteer(pSequence, i);
						EntityType vFilterEntityTypeTendency = cFilter.getFilterProbability().getFilterEntityTypeTendency();

						if(vFilterEntityTypeTendency != null && !vEntityValue.isEmpty()
								&& hasAnyEntityAround(pEntityList, i, 1, pSequence.length(), vFilterEntityTypeTendency)) {

							Entity vEntity = new Entity(vEntityValue, vFilterEntityTypeTendency);
							Parameters.DataHandler.mLabelEncoding.setEntityLabel(pSequence, vEntity, i);
						}
					}
				}
			}
		}
	}

	protected boolean hasAnyEntityAround(List<Entity> pEntityList, int pIndex, int pWindowSize, int pSequenceLength,
			EntityType pFilterEntityTypeTendency) {

		boolean vHasAnyEntityAround = false;

		int vLowerBound = Math.max(0, pIndex - pWindowSize);
		int vUpperBound = Math.min(pIndex + pWindowSize, pSequenceLength);

		for(Entity cEntity : pEntityList) {
			if (vLowerBound <= cEntity.getIndex()
					&& cEntity.getIndex() <= vUpperBound
					&& pFilterEntityTypeTendency == cEntity.getEntityType()) {
				vHasAnyEntityAround = true;
				break;
			}
		}

		return vHasAnyEntityAround;
	}
}
