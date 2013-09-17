package lbd.Thesauru;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ThesaurusElement implements Serializable{

	private static final long serialVersionUID = 9019590641870737797L;
	
	protected static int globalId;
	protected int id;
	
	protected String mainEntry;
	protected ArrayList<String> posTagList;
	protected String definition;
	protected HashMap<String, ArrayList<String>>  synonymsMap;
	protected HashMap<String, ArrayList<String>> antonymsMap;
	
	public ThesaurusElement() {
		
		id = ++globalId;
		
		posTagList = new ArrayList<String>();
		synonymsMap = new HashMap<String, ArrayList<String>>();
		antonymsMap = new HashMap<String, ArrayList<String>>();
	}
	
	public void setMainEntry(String mainEntry){
		this.mainEntry = mainEntry;
	}
	
	public String getMainEntry() {
		return(mainEntry);
	}
	
	public void setDefinition(String definition) {
		this.definition = definition;
	}
	
	public void addPosTag(String [] posTagList) {
		for(int i = 0; i < posTagList.length; i++)
			this.posTagList.add(posTagList[i]);
	}
	
	public String getPosTag(int index) {
		return(posTagList.get(index));
	}
	
	public ArrayList<String> getPosTagList() {
		return(posTagList);
	}
	
	public String getDefinition() {
		return(definition);
	}
	
	public void addSynonym(String posTag, String synonym) {
		if(!synonymsMap.containsKey(posTag))
			synonymsMap.put(posTag, new ArrayList<String>());
		synonymsMap.get(posTag).add(synonym);
	}
	
	public void addAntonym(String posTag, String antonym) {
		if(!antonymsMap.containsKey(posTag))
			antonymsMap.put(posTag, new ArrayList<String>());
		antonymsMap.get(posTag).add(antonym);
	}
	
	public ArrayList<String> getSynonymList(String posTag) {
		return(synonymsMap.get(posTag));
	}
	
	public ArrayList<String> getAntonymList(String posTag) {
		return(antonymsMap.get(posTag));
	}
	
	public HashMap<String, ArrayList<String>> getSynonymMap() {
		return(synonymsMap);
	}
	
	public HashMap<String, ArrayList<String>> getAntonymMap() {
		return(antonymsMap);
	}
	
	/*Main Entry: 	between
	Part of Speech: 	adverb, preposition
	Definition: 	middle from two points
	Synonyms: 	'tween, amid, amidst, among, at intervals, betwixt, bounded by, centrally located, enclosed by, halfway, in , in the middle, in the midst of, in the seam, in the thick of, inserted, interpolated, intervening, medially, mid, midway, separating, surrounded by, within
	Notes: 	use between  when referring to two, use among  for three or more entities, and use amid  for a quantity that is not made up of separate items; between applies to reciprocal arrangements and among to collective arrangements
	Antonyms: 	around, away, away from, outside, separate*/ 
	
}
