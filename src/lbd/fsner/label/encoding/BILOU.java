package lbd.fsner.label.encoding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lbd.FSNER.Model.AbstractLabelEncoding;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;
import lbd.fsner.entity.Entity;
import lbd.fsner.entity.EntityType;

public class BILOU extends AbstractLabelEncoding{

	@Override
	protected void createLabelMap() {
		mLabelStrMap = new HashMap<String, Label>();
		mLabelStrMap.put(Label.Beginning.getValue(), Label.Beginning);
		mLabelStrMap.put(Label.Inside.getValue(), Label.Inside);
		mLabelStrMap.put(Label.Last.getValue(), Label.Last);
		mLabelStrMap.put(Label.Outside.getValue(), Label.Outside);
		mLabelStrMap.put(Label.UnitToken.getValue(), Label.UnitToken);

		mLabelIntMap = new HashMap<Integer, Label>();
		mLabelIntMap.put(Label.Beginning.ordinal(), Label.Beginning);
		mLabelIntMap.put(Label.Inside.ordinal(), Label.Inside);
		mLabelIntMap.put(Label.Last.ordinal(), Label.Last);
		mLabelIntMap.put(Label.Outside.ordinal(), Label.Outside);
		mLabelIntMap.put(Label.UnitToken.ordinal(), Label.UnitToken);
	}

	@Override
	public boolean isEntity(Label pLabel) {
		return !isOutside(pLabel);
	}

	@Override
	public boolean isOutside(Label pLabel) {
		return pLabel == Label.Outside;
	}

	@Override
	public Label getOutsideLabel() {
		return Label.Outside;
	}

	@Override
	public List<Entity> getEntities(ISequence pSequence) {

		List<Entity> vEntityList = new ArrayList<Entity>();
		String vEntity = Symbol.EMPTY;

		for(int i = 0; i < pSequence.length(); i++) {

			EntityType vEntityTypeIndex = EntityType.values()[pSequence.getLabel(i) / EntityType.values().length];

			Label vPreviousLabel = (i == 0)? null : getLabel(pSequence.getLabel(i - 1) % getLabels().size());
			Label vCurrentLabel = getLabel(pSequence.getLabel(i) % getLabels().size());

			if(isEntity(vCurrentLabel)) {
				if(vCurrentLabel == Label.UnitToken) {
					vEntityList.add(new Entity((String) pSequence.getToken(i), vEntityTypeIndex, i));
				} else {
					if(vCurrentLabel == Label.Beginning) {
						vEntity = (String) pSequence.getToken(i);
						vEntityList.add(new Entity(vEntity, vEntityTypeIndex, i));
					} else if(vCurrentLabel == Label.Inside || vCurrentLabel == Label.Last) {
						if(vPreviousLabel == Label.Beginning || vPreviousLabel == Label.Inside) {
							vEntity += Symbol.SPACE + pSequence.getToken(i);
							vEntityList.get(vEntityList.size() - 1).setValue(vEntity);
						} else {
							vEntity = (String) pSequence.getToken(i);
							vEntityList.add(new Entity(vEntity, vEntityTypeIndex, i));
						}
					}
				}
			}
		}

		return vEntityList;
	}

	@Override
	public List<Label> getLabels(List<String> pEntityTokenList) {

		List<Label> vLabelList = new ArrayList<Label>();

		if(pEntityTokenList.size() == 1) {
			vLabelList = createUnitTokenLabelList();
		} else if(pEntityTokenList.size() >= 1) {
			vLabelList = createComposeLabelList(pEntityTokenList);
		}

		return vLabelList;
	}

	private List<Label> createUnitTokenLabelList() {
		List<Label> vLabelList = new ArrayList<Label>();
		vLabelList.add(Label.UnitToken);
		return vLabelList;
	}

	private List<Label> createComposeLabelList(List<String> pEntityTokenList) {

		List<Label> vLabelList = new ArrayList<Label>();

		vLabelList.add(Label.Beginning);

		for(int i = 1; i < pEntityTokenList.size()-1; i++) {
			vLabelList.add(Label.Inside);
		}

		vLabelList.add(Label.Last);

		return vLabelList;
	}

}
