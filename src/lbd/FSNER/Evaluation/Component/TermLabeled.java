package lbd.FSNER.Evaluation.Component;
public class TermLabeled {
	
	protected String term;
	protected String label;
	
	public TermLabeled(String term, String label) {
		this.term = term;
		this.label = label;
	}
	
	public String getTerm() {
		return(term);
	}
	
	public String getLabel() {
		return(label);
	}
}