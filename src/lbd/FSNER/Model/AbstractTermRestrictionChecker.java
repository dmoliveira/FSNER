package lbd.FSNER.Model;

import java.io.Serializable;

public abstract class AbstractTermRestrictionChecker implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract boolean isTermRestricted(String term);

}
