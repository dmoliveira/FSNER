package lbd.DCluster;

import java.io.Serializable;
import java.util.ArrayList;

public class Cluster implements Serializable {

	private static final long serialVersionUID = 1L;

	private static int globalClusterId;
	protected int clusterId;

	protected String mainTermValue;
	ArrayList<Term> termList;

	public Cluster(String mainTermValue) {
		clusterId = ++globalClusterId;

		this.mainTermValue = mainTermValue;

		termList = new ArrayList<Term>();
	}

	public static int getGlobalClusterId() {
		return(globalClusterId);
	}

	public int getClusterId() {
		return clusterId;
	}

	public String getMainTermValue() {
		return mainTermValue;
	}

	public ArrayList<Term> getTermList() {
		return(termList);
	}

	public Term getTerm(String termValue) {

		Term termFound = null;

		for(Term term : termList) {
			if(term.getValue().equals(termValue)) {
				termFound = term;
				break;
			}
		}

		return(termFound);
	}

	public void addTerm(Term term) {
		termList.add(term);
	}

}
