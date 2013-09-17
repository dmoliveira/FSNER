package lbd.NewModels.Affix;

import java.io.Serializable;

public class Affix implements Serializable {

	private static final long serialVersionUID = 5452643743755673145L;
	
	public static enum AffixType {PrefixSize2, PrefixSize3, SuffixSize1, SuffixSize2, SuffixSize3, SuffixSize4, None};
	
	protected static int globalId;
	protected int id;
	protected int frequency;
	
	protected String value;
	protected AffixType type;
	
	public Affix(String affix, AffixType type) {
		id = ++globalId;
		value = affix;
		this.type = type;
		frequency = 1;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public AffixType getType() {
		return type;
	}

	public void setType(AffixType type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}
	
	public void addFrequency() {
		frequency++;
	}
	
	public int getFrequency() {
		return(frequency);
	}

}
