package lbd.FSNER.Utils;

import java.util.ArrayList;

import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Utils.LabelEncoding.BILOU;
import iitb.CRF.DataSequence;

public class SupportEntity {
	
	public static int getEntityIndex(DataSequence sequence, int startPos) {
		
		int entityIndex = -1;
		
		for(int i = startPos; i < sequence.length(); i++){
			if(isEntity(sequence.y(i))) {
				entityIndex = i;
				break;
			}
		}
		
		return(entityIndex);
	}
	
	public static boolean isEntity(int label) {
		return(label >= BILOU.Beginning.ordinal() &&
				label <= BILOU.UnitToken.ordinal() &&
				label != BILOU.Outside.ordinal());
	}
	
	public static Entity getEntity(String entityName, ArrayList<Entity> entityList) {
		
		Entity entity = null;
		
		for(Entity candidateEntity : entityList) {
			if(candidateEntity.isTermMatching(entityName)) {
				entity = candidateEntity;
				break;
			}
		}
		
		return(entity);
	}

}
