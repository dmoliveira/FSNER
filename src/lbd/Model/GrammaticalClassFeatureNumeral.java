package lbd.Model;

import lbd.CRF.LabelMap;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class GrammaticalClassFeatureNumeral extends GrammaticalClassFeature {

	private static final long serialVersionUID = 1L;

	protected static final String [] NUMERAL = {"um", "dois", "três", "tres", "quatro", "cinco",
		"seis", "sete", "oito", "nove", "dez", "onze", "doze", "treze",
		"catorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove",
		"vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta", "oitenta",
		"noventa", "cem", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos",
		"seiscentos", "setecentos", "oitocentos", "novecentos", "mil", "milhão", "bilhão",
		"primeiro", "primeira", "segundo", "segunda", "terceiro", "terceira", "quarto", "quarta",
		"quinto", "quinta", "sexto", "sexta", "sétimo", "setimo", "sétima", "setima", "oitavo",
		"oitava", "nono", "nona", "décimo", "decimo", "décima", "decimo", "vigésimo", "vigesimo",
		"vigésima", "vigesima", "trigésimo", "trigesimo", "trigésima", "trigesima", "quadragésimo",
		"quadragésima", "quadragesima", "quinquagésimo", "quinquagesimo", "quinquagésima", 
		"quinquagesima", "sexagésimo", "sexagesimo", "sexagésima", "sexagesima", "septuagésimo",
		"septuagesimo", "septuagésima", "septuagesima", "octogésimo", "octogesimo", "octogésima",
		"octogesima", "nonagésimo", "nonagesimo", "nonagésima", "nonagesima", "centésimo", 
		"centesimo", "centésima", "centesima", "ducentésimo", "ducentesimo", "ducentésima", 
		"ducentesima", "trecentésimo", "trecentesimo", "trecentésima", "trecentesima",
		"quadringentésimo", "quadringentesimo", "quadringentésima", "quadringentesima",
		"quingentésimo", "quingentesimo", "quingentésima", "quingentesima", "sexcentésimo",
		"sexcentesimo", "sexcentésima", "sexcentesima", "setingentésimo", "setingentesimo",
		"setingentésima", "setingentesima", "octingentésimo", "octingentesimo", "octingentésima",
		"octingentesima", "nongentésimo", "nongentesimo", "nongentésima", "nongentesima",
		"milésimo", "milesimo", "milésima", "milesima", "bilionésimo", "bilionesimo", "bilionésima",
		"bilionesima", "dobro", "duplo", "triplo", "tríplice", "triplice", "quádruplo", "quadruplo",
		"quíntuplo", "quintuplo", "sêxtuplo", "sextuplo", "sétuplo", "setuplo", "óctuplo", "octuplo",
		"nônuplo", "nonuplo", "décuplo", "decuplo", "cêntuplo", "centuplo", "meio", "terço", "terco",
		"avos"};
	
	public GrammaticalClassFeatureNumeral(FeatureGenImpl fgen, float weight) {
		super(fgen, weight, NUMERAL);
		
		featureName = "Numeral";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Numeral");
	}
	
	public GrammaticalClassFeatureNumeral(FeatureGenImpl fgen) {
		super(fgen, NUMERAL);
		
		featureName = "Numeral";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Numeral");
	}

	@Override
	protected void additionalStartScanFeaturesAt(DataSequence data,
			int prevPos, int pos, boolean isBelongsToThisGrammaticalClass) {
		
		if(!isBelongsToThisGrammaticalClass) {
			try {
				Double.parseDouble(((String)data.x(pos)));
				
				isBelongsToThisGrammaticalClass = true;
				termIndex = NUMERAL.length;
			} catch(NumberFormatException e){}
		}
	}
}
