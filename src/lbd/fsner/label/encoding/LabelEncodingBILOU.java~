package lbd.fsner.label.encoding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lbd.FSNER.Model.AbstractLabelEncoding;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;
import lbd.fsner.entity.Entity;
import lbd.fsner.entity.EntityType;

public class LabelEncodingBILOU extends AbstractLabelEncoding{

	@Override
	protected void createLabelMap() {
		mLabelMap = new HashMap<Integer, Label>();
		mLabelMap.put(Label.Beginning.ordinal(), Label.Beginning);
		mLabelMap.put(Label.Inside.ordinal(), Label.Inside);
		mLabelMap.put(Label.Last.ordinal(), Label.Last);
		mLabelMap.put(Label.Outside.ordinal(), Label.Outside);
		mLabelMap.put(Label.UnitToken.ordinal(), Label.UnitToken);
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

			EntityType vEntityTypeIndex = EntityType.getEntityType(pSequence.getLabel(i));

			Label vPreviousLabel = (i == 0)? null : getLabel(pSequence.getLabel(i - 1), false);
			Label vCurrentLabel = getLabel(pSequence.getLabel(i), false);

			if(isEntity(vCurrentLabel)) {
				if(vCurrentLabel == Label.UnitToken) {
					vEntityList.add(new Entity(pSequence.getToken(i), vEntityTypeIndex, i));
				} else {
					if(vCurrentLabel == Label.Beginning) {
						vEntity = pSequence.getToken(i);
						vEntityList.add(new Entity(vEntity, vEntityTypeIndex, i));
					} else if(vCurrentLabel == Label.Inside || vCurrentLabel == Label.Last) {
						if(vPreviousLabel == Label.Beginning || vPreviousLabel == Label.Inside) {
							vEntity += Symbol.SPACE + pSequence.getToken(i);
							vEntityList.get(vEntityList.size() - 1).setValue(vEntity);
						} else {
							vEntity = pSequence.getToken(i);
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
		if(pEntityTokenList == null || pEntityTokenList.size() == 0) {
			throw new ArrayIndexOutOfBoundsException("Error: Entity token list must be larger than 0.");
		}

		List<Label> vLabelList = new ArrayList<Label>();

		if(pEntityTokenList.size() == 1) {
			vLabelList = createUnitTokenLabelList();
		} else {
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

		for(int i = 1; i < pEntityTokenList.size() - 1; i++) {
			vLabelList.add(Label.Inside);
		}

		vLabelList.add(Label.Last);

		return vLabelList;
	}

	@Override
	public ISequence setEntityLabel(ISequence pSequence, Entity pEntity, int pEntityStartIndex) {
		for(Label cLabel : pEntity.getLabels()) {
			pSequence.setLabel(pEntityStartIndex++, Label.getOrdinalLabel(pEntity.getEntityType(), cLabel));
		}

		return pSequence;
	}

	@Override
	public void joinEntities(ISequence pSequence, EntityType pEntityType, int pEndIndexFirstEntity) {
		Label vPreviousLabel = Label.getCanonicalLabel(pSequence.getLabel(pEndIndexFirstEntity));
		Label vCurrentLabel = Label.getCanonicalLabel(pSequence.getLabel(pEndIndexFirstEntity + 1));

		if(vPreviousLabel == Label.UnitToken) {
			pSequence.setLabel(pEndIndexFirstEntity, Label.getOrdinalLabel(pEntityType, Label.Beginning));
		} else if(vPreviousLabel == Label.Last) {
			pSequence.setLabel(pEndIndexFirstEntity, Label.getOrdinalLabel(pEntityType, Label.Inside));
		}

		if(vCurrentLabel == Label.UnitToken) {
			pSequence.setLabel(pEndIndexFirstEntity + 1, Label.getOrdinalLabel(pEntityType, Label.Last));
		} else if(vCurrentLabel == Label.Beginning) {
			pSequence.setLabel(pEndIndexFirstEntity + 1, Label.getOrdinalLabel(pEntityType, Label.Inside));
		}
	}

	@Override
	public void joinEntities(ISequence pSequence, EntityType pEntityType,
			int pEndIndexFirstEntity, int pStartIndexSecondEntity) {

		joinEntities(pSequence, pEntityType, pEndIndexFirstEntity);

		for(int i = pEndIndexFirstEntity + 1; i < pStartIndexSecondEntity; i++) {
			pSequence.setLabel(i, Label.getOrdinalLabel(pEntityType, Label.Inside));
		}

		joinEntities(pSequence, pEntityType, pStartIndexSecondEntity - 1);
	}

	@Override
	public boolean isStartEntity(ISequence pSequence, int pIndex) {
		Label vLabel = Label.getCanonicalLabel(pSequence.getLabel(pIndex));
		return vLabel == Label.UnitToken || vLabel == Label.Beginning;
	}

	@Override
	public boolean isEndEntity(ISequence pSequence, int pIndex) {
		Label vLabel = Label.getCanonicalLabel(pSequence.getLabel(pIndex));
		return vLabel == Label.UnitToken || vLabel == Label.Last;
	}
}
