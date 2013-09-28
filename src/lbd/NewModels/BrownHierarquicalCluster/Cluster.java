package lbd.NewModels.BrownHierarquicalCluster;

import java.io.Serializable;
import java.util.HashMap;

public class Cluster implements Serializable {

	private static final long serialVersionUID = 1L;

	protected static int globalId;

	protected int id;
	protected String clusterValue;

	protected HashMap<String, Object> termMap;

	public Cluster(String clusterValue) {

		id = ++globalId;
		this.clusterValue = clusterValue;

		termMap = new HashMap<String, Object>();
	}

	public void addTerm(String term) {
		termMap.put(term, null);
	}

	public boolean hasTerm(String term) {
		return(termMap.containsKey(term));
	}

	public int getId() {
		return id;
	}

	public String getValue() {
		return(clusterValue);
	}

}
