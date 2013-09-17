package lbd.Model;

import lbd.CRF.LabelMap;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class GrammaticalClassFeatureAdverb extends GrammaticalClassFeature {
	
	private static final long serialVersionUID = 1L;

	protected static final String [] ADVERB = {"sim", "certamente", "deveras", "incontestavelmente", "realmente",
		"efetivamente", "talvez", "quiçá", "quicá", "quiça", "acaso", "porventura", "certamente", "provavelmente",
		"decerto", "certo", "muito", "mui", "pouco", "assaz", "bastante", "mais", "menos", "tão", "tao",
		"demasiado", "meio", "todo", "completamente", "profundamente", "demasiadamente", "excessivamente",
		"demais", "nada", "ligeiramente", "levemente", "que", "quão", "quanto", "bem", "mal", "quase", 
		"apenas", "como", "abaixo", "acima", "acolá", "acola", "cá", "ca", "lá", "la", "aqui",
		"ali", "aí", "ai", "além", "alem", "aquém", "aquem", "algures", "alhures", "nenhures",
		"atrás", "atras", "fora", "afora", "dentro", "perto", "longe", "adiante", "diante", 
		"onde", "avante", "através", "atraves", "defronte", "aonde", "donde", "detrás", "detras",
		"bem", "mal", "assim", "depressa", "devagar", "como", "adrede", "debalde", "alerta",
		"melhor", "aliás", "calmamente", "livremente", "propositadamente", "selvagemente", "não",
		"tampouco", "agora", "hoje", "amanhã", "amanha", "depois", "ontem", "anteontem", "já", "ja",
		"sempre", "amiúde", "amiude", "nunca", "jamais", "ainda", "logo", "antes", "cedo", "tarde",
		"ora", "afinal", "outrora", "então", "entao", "breve", "nisto", "aí", "entrementes", "brevemente",
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
