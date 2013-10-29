package lbd.FSNER.Utils;

import java.util.ArrayList;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Filter.Component.Entity;
import lbd.data.handler.ISequence;
import lbd.fsner.label.encoding.Label;

public class EntityUtils {

	public static int getEntityIndex(ISequence pSequence, int pStartPosition) {

		int vEntityIndex = -1;

		for(int i = pStartPosition; i < pSequence.length(); i++){
			if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(pSequence.getLabel(i)))) {
				vEntityIndex = i;
				break;
			}
		}

		return(vEntityIndex);
	}

	//TODO: Used only for BILOU label. Make it more generic.
	public static int getEntityEndIndex(ISequence pSequence, int pEntityStartIndex) {
		int vEntityEndIndex = -1;

		if(!Parameters.DataHandler.mLabelEncoding.isOutside(Label.getCanonicalLabel(pSequence.getLabel(pEntityStartIndex)))) {
			if(Label.getCanonicalLabel(pSequence.getLabel(pEntityStartIndex)) == Label.UnitToken) {
				vEntityEndIndex = pEntityStartIndex;
			} else {
				for(int pEntityIndex = pEntityStartIndex + 1; pEntityIndex < pSequence.length(); pEntityIndex++) {
					if(Label.getCanonicalLabel(pSequence.getLabel(pEntityIndex)) == Label.Last) {
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
