package lbd.Model;

import lbd.CRF.LabelMap;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class GrammaticalClassFeatureNumeral extends GrammaticalClassFeature {

	private static final long serialVersionUID = 1L;

	protected static final String [] NUMERAL = {"um", "dois", "tr�s", "tres", "quatro", "cinco",
		"seis", "sete", "oito", "nove", "dez", "onze", "doze", "treze",
		"catorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove",
		"vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta", "oitenta",
		"noventa", "cem", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos",
		"seiscentos", "setecentos", "oitocentos", "novecentos", "mil", "milh�o", "bilh�o",
		"primeiro", "primeira", "segundo", "segunda", "terceiro", "terceira", "quarto", "quarta",
		"quinto", "quinta", "sexto", "sexta", "s�timo", "setimo", "s�tima", "setima", "oitavo",
		"oitava", "nono", "nona", "d�cimo", "decimo", "d�cima", "decimo", "vig�simo", "vigesimo",
		"vig�sima", "vigesima", "trig�simo", "trigesimo", "trig�sima", "trigesima", "quadrag�simo",
		"quadrag�sima", "quadragesima", "quinquag�simo", "quinquagesimo", "quinquag�sima", 
		"quinquagesima", "sexag�simo", "sexagesimo", "sexag�sima", "sexagesima", "septuag�simo",
		"septuagesimo", "septuag�sima", "septuagesima", "octog�simo", "octogesimo", "octog�sima",
		"octogesima", "nonag�simo", "nonagesimo", "nonag�sima", "nonagesima", "cent�simo", 
		"centesimo", "cent�sima", "centesima", "ducent�simo", "ducentesimo", "ducent�sima", 
		"ducentesima", "trecent�simo", "trecentesimo", "trecent�sima", "trecentesima",
		"quadringent�simo", "quadringentesimo", "quadringent�sima", "quadringentesima",
		"quingent�simo", "quingentesimo", "quingent�sima", "quingentesima", "sexcent�simo",
		"sexcentesimo", "sexcent�sima", "sexcentesima", "setingent�simo", "setingentesimo",
		"setingent�sima", "setingentesima", "octingent�simo", "octingentesimo", "octingent�sima",
		"octingentesima", "nongent�simo", "nongentesimo", "nongent�sima", "nongentesima",
		"mil�simo", "milesimo", "mil�sima", "milesima", "bilion�simo", "bilionesimo", "bilion�sima",
		"bilionesima", "dobro", "duplo", "triplo", "tr�plice", "triplice", "qu�druplo", "quadruplo",
		"qu�ntuplo", "quintuplo", "s�xtuplo", "sextuplo", "s�tuplo", "setuplo", "�ctuplo", "octuplo",
		"n�nuplo", "nonuplo", "d�cuplo", "decuplo", "c�ntuplo", "centuplo", "meio", "ter�o", "terco",
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
