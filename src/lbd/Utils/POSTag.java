package lbd.Utils;

import java.io.Serializable;

public class POSTag implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static int globalID;
	private int id;
	
	private String posTagKey;
	private String posTagValue;
	private int frequency;
	private int correctHits;
	private int wrongHits;
	
	double percentageCorrectHits;
	double percentageWrongHits;
	
	public POSTag(String posTagKey, String posTagValue, int frequency) {
		this(posTagValue, frequency);
		this.posTagKey = posTagKey;
		id = ++globalID;
	}
	
	public POSTag(String posTagValue, int frequency) {
		this(posTagValue);
		this.frequency = frequency;
	}
	
	public POSTag(String posTagValue) {
		this.posTagValue = posTagValue;
	}
	
	public void addFrequency() {
		frequency++;
	}
	
	public void addCorrectHit() {
		correctHits++;
	}
	
	public void addWrongHit() {
		wrongHits++;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public static int getGlobalID() {
		return globalID;
	}

	public String getPosTagKey() {
		return posTagKey;
	}

	public void setPosTagKey(String posTagKey) {
		this.posTagKey = posTagKey;
	}

	public String getPosTagValue() {
		return posTagValue;
	}

	public void setPosTagValue(String posTagValue) {
		this.posTagValue = posTagValue;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getCorrectHits() {
		return correctHits;
	}

	public void setCorrectHits(int correctHits) {
		this.correctHits = correctHits;
	}

	public int getWrongHits() {
		return wrongHits;
	}

	public void setWrongHits(int wrongHits) {
		this.wrongHits = wrongHits;
	}

	public double getPercentageCorrectHits() {
		return percentageCorrectHits;
	}

	public void setPercentageCorrectHits(double percentageCorrectHits) {
		this.percentageCorrectHits = percentageCorrectHits;
	}

	public double getPercentageWrongHits() {
		return percentageWrongHits;
	}

	public void setPercentageWrongHits(double percentageWrongHits) {
		this.percentageWrongHits = percentageWrongHits;
	}		
}
