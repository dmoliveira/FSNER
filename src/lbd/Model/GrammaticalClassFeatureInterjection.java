package lbd.Model;

import lbd.CRF.LabelMap;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class GrammaticalClassFeatureInterjection extends GrammaticalClassFeature {

	private static final long serialVersionUID = 1L;

	protected static final String [] INTERJECTION = {"ai", "aai", "aii", "aaii",
		"ui", "uui", "uii", "uuii",	"ah", "aah", "ahh", "aahh", "kk",
		"hahaha", "hehehe", "rsrsrs", "oh", "ooh", "ohh", "oohh", "xi",
		"xii", "hum", "huum", "epa", "epaa", "ih", "iih", "ihh", "iihh", "puxa",
		"puxaa", "caramba", "carambaa", "qu�", "u�", "uee", "ue", "hem", "heem",
		"heim", "heeim", "heiim", "heeiim", "hein", "heein", "heiin", "heeiin", 
		"uai", "uuai", "uaai", "uaii", "uuaai", "uaaii", "uuaii", "uuaaii", "credo", "credoo",
		"opa", "oopa", "opaa", "oopaa", "irra", "iirra", "irraa", "iirraa", "apre",
		"aapre", "apree", "aapree", "arre", "aare", "arree", "aarree", "eia",
		"eeia", "eiia", "eiaa", "eeiia", "eiiaa", "eeiaa", "eeiiaa",
		"upa", "uupa", "upaa", "uupaa", "eta", "eeta", "etaa", "eetaa", "oba",
		"ooba", "obaa", "oobaa", "eh", "eeh", "ehh", "eehh", "uf", "uuf", "uff",
		"ufa", "uufa", "ufaa", "uffa", "uffaa", "uuffaa", "�", "�oo",  "psiu",
		"pssiu", "pssiuu", "al�", "�loo", "alo", "aloo", "chit", "arreda", 
		"n�o", "n�oo", "nao", "naoo", "naum", "naumm", "pudera", "sim", "simm",
		"t�", "t�aa", "ta", "taaa", "h�", "fiau", "fiauu", "ora", "oraa",
		"pst", "tchau", "tchauu", "uh", "uuh", "uhh", "uuhh", "cruzes", "pum",
		"puum", "pumm", "miau", "miauu", "plaft", "trac", "pof", "z�s", "zas",
		"aee", "��", "hm", "s2", "affz", "affs", "haha", "hehe", "hihi", "rsrs",
		"hahaha", "hehehe", "hihihi", "rsrsrs",	"yeah", "rss", "o_o", "o_O", "O_o",
		"O_O"};
	
	public GrammaticalClassFeatureInterjection(FeatureGenImpl fgen, float weight) {
		super(fgen, weight, INTERJECTION);
		
		featureName = "Interjection";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Interjection");
	}
	
	public GrammaticalClassFeatureInterjection(FeatureGenImpl fgen) {
		super(fgen, INTERJECTION);
		
		featureName = "Interjection";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Interjection");
	}

	@Override
	protected void additionalStartScanFeaturesAt(DataSequence data,
			int prevPos, int pos, boolean isBelongsToThisGrammaticalClass) {}
}
