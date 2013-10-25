package com.zunnit.recognition;

import java.io.IOException;
import java.util.List;

import lbd.FSNER.Factory.FSNERFactory;
import lbd.FSNER.Utils.Symbol;
import lbd.Utils.Utils;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class FSNERRecognizerEntity implements EntityRecognizer{

	private FSNERFactory fsnerFactory;
	private static FSNERRecognizerEntity fsner;
	private static ObjectMapper jsonMapper = new ObjectMapper();

	private FSNERRecognizerEntity() {
		fsnerFactory = new FSNERFactory();
	}

	public static FSNERRecognizerEntity getFSNERRecognizer() {
		if(fsner == null) {
			fsner = new FSNERRecognizerEntity();
		}

		return fsner;
	}

	@Override
	public String recognize(String message) {
		if(Utils.isEmptyOrNull(message)) {
			return Symbol.EMPTY;
		}

		List<Entity> entityList =  fsner.fsnerFactory.getEntities(message);
		String jsonEntities = null;

		try {
			jsonEntities = jsonMapper.writeValueAsString(entityList);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jsonEntities;
	}

}
