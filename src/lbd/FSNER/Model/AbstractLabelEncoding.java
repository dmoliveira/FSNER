package lbd.FSNER.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;
import lbd.fsner.entity.Entity;
import lbd.fsner.entity.EntityType;
import lbd.fsner.label.encoding.Label;

public abstract class AbstractLabelEncoding {

	protected Map<Integer, Label> mLabelMap;

	public AbstractLabelEncoding() {
		createLabelMap();
	}

	protected abstract void createLabelMap();

	public abstract boolean isEntity(Label pLabel);

	public abstract boolean isOutside(Label pLabel);

	public abstract Label getOutsideLabel();

	public Label getLabel(String pLabelName) {
		return Label.valueOf(pLabelName);
	}

	public Label getLabel(int pLabelIndex, boolean pIsCanonicalLabelIndex) {
		return (pIsCanonicalLabelIndex)? mLabelMap.get(pLabelIndex) : mLabelMap.get(pLabelIndex % getLabels().size());
	}

	public abstract List<Entity> getEntities(ISequence pSequence);

	public List<Label> getLabels(String pEntity) {
		List<String> vEntityTokenList = new ArrayList<String>();
		for(String cEntityToken : pEntity.split(Symbol.SPACE)) {
			vEntityTokenList.add(cEntityToken);
		}

		return getLabels(vEntityTokenList);
	}

	public abstract List<Label> getLabels(List<String> pEntityTokenList);

	public List<Label> getLabels() {
		return new ArrayList<Label>(mLabelMap.values());
	}

	public abstract ISequence setEntityLabel(ISequence pSequence, Entity pEntity, int pEntityStartIndex);

	public abstract void joinEntities(ISequence pSequence, EntityType pEntityType, int pEndIndexFirstEntity);

	public abstract void joinEntities(ISequence pSequence, EntityType pEntityType, int pEndIndexFirstEntity, int pStartIndexSecondEntity);

	public int getAlphabetSize() {
		return EntityType.values().length *  Parameters.DataHandler.mLabelEncoding.getLabels().size();
	}

	public abstract boolean isStartEntity(ISequence pSequence, int pIndex);

	public abstract boolean isEndEntity(ISequence pSequence, int pIndex);

}
