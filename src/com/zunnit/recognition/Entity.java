package com.zunnit.recognition;


public class Entity {

	private String name;
	private String type;

	public Entity(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public Entity(String value) {
		this.name = value;
	}

	public String getValue() {
		return name;
	}

	public void setValue(String pValue) {
		name = pValue;
	}

	public String getType() {
		return type;
	}

	public void setType(String pType) {
		type = pType;
	}
}
