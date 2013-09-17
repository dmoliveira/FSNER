package lbd.Utils;

public class Frequency {
	
	private String id;
	private int frequency;
	
	public Frequency(String id, int frequency) {
		this.id = id;
		this.frequency = frequency;
	}
	
	public Frequency(String id) {
		this.id = id;
		frequency = 0;
	}
	
	public void addFrequency() {
		frequency++;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

}
