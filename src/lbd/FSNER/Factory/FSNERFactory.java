package lbd.FSNER.Factory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbd.FSNER.FSNER;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.Sequence;

import com.zunnit.recognition.Entity;

public class FSNERFactory {

	protected enum EntityType {
		PER, ORG, LOC, EVT, MISC
	}

	private static Map<EntityType, FSNER> fsnermap;
	private static final String FSNER_MODEL_PATTERN =
			"/var/lib/tomcat7/webapps/FS-NER-Models/FSNER-Model-Zunnit-GloboExtraCollection-All-{0}-CV1.fsbin";

	// To optimize use this as singleton instance.
	public FSNERFactory() {
		fsnermap = new HashMap<EntityType, FSNER>();
		for (EntityType cEntityType : EntityType.values()) {
			fsnermap.put(cEntityType, FSNER.loadObject(MessageFormat.format(
					FSNER_MODEL_PATTERN, cEntityType.name())));
		}
	}

	public List<Entity> getEntities(String message) {

		List<Entity> entityList = new ArrayList<Entity>();

		for (EntityType entityType : EntityType.values()) {

			FSNER fsner = fsnermap.get(entityType);

			for (String entity : fsner.labelSequenceToList(new Sequence(message
					.split(Symbol.SPACE)))) {
				entityList.add(new Entity(entity, entityType.name()));
			}
		}

		return entityList;
	}

}
