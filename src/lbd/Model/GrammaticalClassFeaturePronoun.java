package lbd.Model;

import lbd.CRF.LabelMap;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;


public class GrammaticalClassFeaturePronoun extends GrammaticalClassFeature{

	private static final long serialVersionUID = 1L;

	protected static final String [] PRONOUN = {"eu", "tu", "ele", "ela", "nós", "nos", 
		"vós", "vos", "eles", "elas", "me", "mim", "comigo", "te", "ti", "contigo",
		"se", "si", "consigo", "lhe", "conosco", "convosco", "lhes",
		"meu", "minha", "meus", "minhas", "teu", "tua", "teus", "tuas", "seu", "sua",
		"seus", "suas", "nosso", "nossa", "nossos", "nossas", "vosso",
		"vossa", "vossos", "vossas", "seu", "sua", "seus", "suas",
		"este", "estes", "esta", "estas", "esse", "esses", "essa", "essas",
		"aquele", "aqueles", "aquela", "aquelas", "aqueloutro", "aqueloutros",
		"aqueloutra", "aqueloutras", "mesmo", "mesmos", "mesma", "mesmas", 
		"próprio", "próprios", "própria", "próprias", "tal", "tais",
		"semelhante", "semelhantes", "isto", "isso", "aquilo",
		"cujo", "cujos", "quanto", "quantos", "qual", "quais", "cuja", 
		"cujas", "quanta", "quantas", "quem", "que", "onde",
		"algo", "alguém", "alguem", "fulano", "sicrano", "beltrano", "nada", "ninguém",
		"ninguem", "outrem", "quem", "tudo", "cada", "certo", "certos", "certa", "certas",
		"algum", "alguns", "alguma", "algumas", "bastante", "bastantes", "demais", "mais",
		"menos", "muito", "muitos", "muita", "muitas", "nenhum", "nenhuns", "nenhuma", "nenhumas",
		"outro", "outros", "pouco", "poucos", "qualquer", "quaisquer", "tanto", "tantos",
		"tanta", "tantas", "todo", "todos", "toda", "todas", "vários", "várias"};
	
	public GrammaticalClassFeaturePronoun(FeatureGenImpl fgen, float weight) {
		super(fgen, weight, PRONOUN);
		
		featureName = "Pronoun";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Pronoun");
	}
	
	public GrammaticalClassFeaturePronoun(FeatureGenImpl fgen) {
		super(fgen, PRONOUN);
		
		featureName = "Pronoun";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Pronoun");
	}

	@Override
	protected void additionalStartScanFeaturesAt(DataSequence data,
			int prevPos, int pos, boolean isBelongsToThisGrammaticalClass) {}	
}
