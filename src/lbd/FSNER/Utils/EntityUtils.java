package lbd.FSNER.Utils;

import java.util.ArrayList;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Filter.Component.Entity;
import lbd.data.handler.DataSequence;

public class EntityUtils {

	public static int getEntityIndex(DataSequence pSequence, int pStartPosition) {

		int vEntityIndex = -1;

		for(int i = pStartPosition; i < pSequence.length(); i++){
			if(LabelEncoding.isEntity(pSequence.y(i))) {
				vEntityIndex = i;
				break;
			}
		}

		return(vEntityIndex);
	}

	//TODO: Used only for BILOU label. Make it more generic.
	public static int getEntityEndIndex(SequenceLabel pSequence, int pEntityStartIndex) {
		int vEntityEndIndex = -1;

		if(pSequence.getLabel(pEntityStartIndex) != LabelEncoding.BILOU.Outside.ordinal()) {
			if(pSequence.getLabel(pEntityStartIndex) == LabelEncoding.BILOU.UnitToken.ordinal()) {
				vEntityEndIndex = pEntityStartIndex;
			} else {
				for(int pEntityIndex = pEntityStartIndex + 1; pEntityIndex < pSequence.size(); pEntityIndex++) {
					if(pSequence.getLabel(pEntityIndex) == LabelEncoding.BILOU.Last.ordinal()) {
						vEntityEndIndex = pEntityIndex;
						break;
					}
				}
			}
		}

		return vEntityEndIndex;
	}

	public static Entity getEntity(String pEntityName, ArrayList<Entity> pEntityList) {

		Entity pEntity = null;

		for(Entity cCandidateEntity : pEntityList) {
			if(cCandidateEntity.isTermMatching(pEntityName)) {
				pEntity = cCandidateEntity;
				break;
			}
		}

		return(pEntity);
	}

}
