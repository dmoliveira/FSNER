package lbd.Thesauru;

import java.io.Serializable;
import java.util.ArrayList;

public class Thesaurus implements Serializable{

	private static final long serialVersionUID = 1L;

	protected static int globalId;
	protected int id;

	protected String termId;
	protected transient ArrayList<ThesaurusElement> thesaurusList;
	protected ArrayList<String> synonymList;

	protected boolean hasThesaurus;

	public Thesaurus(String term) {
		thesaurusList = new ArrayList<ThesaurusElement>();
		synonymList = new ArrayList<String>();

		id = ++globalId;
		termId = term;
	}

	public void addThesaurusElement(ThesaurusElement thesaurusElement) {
		thesaurusList.add(thesaurusElement);
	}

	public ArrayList<ThesaurusElement> getThesaurusList() {
		return(thesaurusList);
	}

	public int getId() {
		return(id);
	}

	public String getTermId() {
		return(termId);
	}

	public void setThesaurus(boolean hasThesaurus) {
		this.hasThesaurus = hasThesaurus;
	}

	public void addSynonym(String term) {
		synonymList.add(term);
	}

	public ArrayList<String> getSynonymList() {
		return(synonymList);
	}

	public boolean hasThesaurus() {
		return(hasThesaurus);
	}
}
