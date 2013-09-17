package lbd.Model;

import lbd.CRF.LabelMap;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class GrammaticalClassFeatureAdverb extends GrammaticalClassFeature {
	
	private static final long serialVersionUID = 1L;

	protected static final String [] ADVERB = {"sim", "certamente", "deveras", "incontestavelmente", "realmente",
		"efetivamente", "talvez", "qui��", "quic�", "qui�a", "acaso", "porventura", "certamente", "provavelmente",
		"decerto", "certo", "muito", "mui", "pouco", "assaz", "bastante", "mais", "menos", "t�o", "tao",
		"demasiado", "meio", "todo", "completamente", "profundamente", "demasiadamente", "excessivamente",
		"demais", "nada", "ligeiramente", "levemente", "que", "qu�o", "quanto", "bem", "mal", "quase", 
		"apenas", "como", "abaixo", "acima", "acol�", "acola", "c�", "ca", "l�", "la", "aqui",
		"ali", "a�", "ai", "al�m", "alem", "aqu�m", "aquem", "algures", "alhures", "nenhures",
		"atr�s", "atras", "fora", "afora", "dentro", "perto", "longe", "adiante", "diante", 
		"onde", "avante", "atrav�s", "atraves", "defronte", "aonde", "donde", "detr�s", "detras",
		"bem", "mal", "assim", "depressa", "devagar", "como", "adrede", "debalde", "alerta",
		"melhor", "ali�s", "calmamente", "livremente", "propositadamente", "selvagemente", "n�o",
		"tampouco", "agora", "hoje", "amanh�", "amanha", "depois", "ontem", "anteontem", "j�", "ja",
		"sempre", "ami�de", "amiude", "nunca", "jamais", "ainda", "logo", "antes", "cedo", "tarde",
		"ora", "afinal", "outrora", "ent�o", "entao", "breve", "nisto", "a�", "entrementes", "brevemente",
		"imediatamente", "raramente", "finalmente", "comumente", "presentemente", "diariamente", 
		"concomitantemente", "simultaneamente"};
	
	public GrammaticalClassFeatureAdverb(FeatureGenImpl fgen, float weight) {
		super(fgen, weight, ADVERB);
		
		featureName = "Adverb";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Adverb");
	}
	
	public GrammaticalClassFeatureAdverb(FeatureGenImpl fgen) {
		super(fgen, ADVERB);
		
		featureName = "Adverb";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Adverb");
	}

	@Override
	protected void additionalStartScanFeaturesAt(DataSequence data,
			int prevPos, int pos, boolean isBelongsToThisGrammaticalClass) {}
}
