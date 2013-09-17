package lbd.Utils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;


public class POSTaggerPTBR implements Serializable {
	
	private static final long serialVersionUID = 1L;

	//-- The encode ISO-8859-1 accepts accent
	public static final String ENCODE_USED = "ISO-8859-1";
	
	private static int [] numberClassesFound;
	private static final String DELIMITER_LABEL = "|";
	private static final String DELIMITER_SPLIT = "\\|";
	
	private HashMap<String, Boolean> posTagsMap;
	
	private static final int INDEX_TERM = 0, INDEX_LABEL = 1;
	private static final String LABEL_OUTSIDE = "Outside";
	
	private static final int INDEX_ARTICLE = 0, INDEX_PREPOSITION = 1, INDEX_CONJUNCTION = 2,
					  INDEX_PRONOUN = 3, INDEX_ADVERB = 4, INDEX_NUMERAL = 5;
	private static String[] labelArray = {"Article", "Preposition", "Conjunction", "Pronoun", "Adverb", "Numeral"};
	private static String[][] termArray = {
			
			//-- Articles
			{"o", "a", "os", "as", "um", "uma", "uns", "umas",
				"ao", "aos", "do", "dos", "no", "nos", "pelo"
				, "pelos", "num", "nuns", "à", "às", "da", "das",
				"na", "nas", "pela", "pelas", "numa", "numas"},
				
			//-- Prepositions	
			{"ante", "após", "até", "com", "contra", "de", "desde",
					"dês", "em", "entre", "para", "perante", "por",
					"per", "sem", "sob", "sobre", "trás"}, //"a"
					
			//-- Conjunctions
			{"e", "nem", "mas", "também", //Algumas aditivas 
			 "porém", "todavia", "contudo", "entretanto", "senão", "antes", // Algumas adversativas
			 "ou", "ora", "já", "quer", // Algumas alternativas
			"logo", "portanto", "conseguinte", "pois", // Algumas conclusivas 
			 "que", "porque", "porquanto", //Algumas explicativas
			 "como", "qual", "tal", "quanto", //Algumas comparativas
			 "conquanto","embora", //Algumas concessivas
			 "se", "caso", //Algumas condicionais //Pronomes demostrativos
			 "conforme", "segundo", "consoante", //Algumas conformativas
			 "quando", "enquanto"},//Algumas temporais
			 
			 //-- Pronouns
			 {"eu", "tu", "ele", "ela", "nós", "vós", "eles", "elas", //Pronomes retos
			  "me", "mim", "comigo", "te", "ti", "contigo", "si", "consigo", "lhe",
			  "nos", "conosco", "vos", "convosco", "lhes",//Pronomes oblíquos
			  "meu", "minha", "meus", "minhas", "teu", "tua", "teus", "tuas", //Pronomes possessivos
			  "seu", "sua", "seus", "suas", "nosso", "nossa", "nossos", "nossas",
			  "vosso", "vossa", "vossos", "vossas", 
			  "este", "estes", "esse", "esses", "essa", "essas", "aquele", "aqueles", //Pronomes demostrativos
			  "aquela", "aquelas", "aqueloutro", "aqueloutros", "aqueloutra", "aqueloutras", "mesmo",
			  "mesmos", "mesma", "mesmas", "próprio", "próprios", "tal", "tais", "semelhante", "semelhantes",
			  "isso", "isto", "aquilo", 
			  "cujo", "cuja", "cujos", "cujas", "quantos", "onde", "quem", //Pronomes relativos 
			  "deste", "destes", "desta", "destas", "desse", "dessa", "desses", "dessas",
			  "algo", "alguém", "fulano", "sicrano", "beltrano", "nada", "ninguém", "outrem", "tudo", //Pronome Indefinos
			  "cada", "certo", "certos", "certa", "certas",
			  "quantos", "qual", "quantas"},
			  
			  //-- Adverbs
			  {"sim", "certamente", "deveras", "incontestavelmente", "realmente", "efetivamente", //Adv. Afirmação
			   "talvez", "quiça", "acaso", "porventura", "certamente", "provavelmente", "decerto", "certo", //Adv. dúvida
			   "muito", "mui", "pouco", "assaz", "bastante", "mais", "menos", "tão", //Adv. intensidade
			   "demasiado", "meio", "todo", "completamente", "profundamente", "demasiadamente",
			   "excessivamente", "demais", "nada", "ligeiramente", "levemente", "que", "quão",
			   "quanto", "bem", "mal", "quase", "apenas", "como",
			   "abaixo", "acima", "acolá", "cá", "lá", "aqui", "ali", "aí", "além", "aquém", //Adv. lugar
			   "algures", "alhures", "nenhures", "atrás", "fora", "afora", "dentro", "perto", "longe",
			   "adiante", "diante", "onde", "avante", "através", "defronte", "aonde", "onde", "detrás",
			   //Adverbio de modo (-mente)
			   "bem", "mal", "assim", "depressa", "devagar", "como", "adrede", "debalde", "alerta",
			   "melhor", "pior", "calmamente", "livremente", "propositalmente", "selvagemente",
			   //Adverbio de negação
			   "não", "tampouco",
			   //Adverbio de tempo
			   "agora", "hoje", "amanhã", "depois", "ontem", "anteontem", "já", "sempre",
			   "amiúde", "nunca", "jamais", "ainda", "logo", "antes", "cedo", "tarde", "ora",
			   "afinal", "outrora", "então", "breve", "nisto", "aí", "entrementes", "brevemente", 
			   "imediatamente", "raramente", "finalmente", "comumente", "presentemente",
			   "diariamente", "concomitantemente", "simultaneamente",
			   //Palavras e locuções denotativas
			   "eis", "menos", "senão", "sequer", "também", "ainda", "até", "só", "apenas",
			   ""},
			   
			   //-- Numeral
			   {
				   //Cardinais
				   "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove", "dez",
				   "onze", "doze", "treze", "catorze", "quinze", "dezesseis", "dezessete", "dezoito",
				   "dezenove", "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta",
				   "oitenta", "noventa", "cem", "cento", "duzentos", "trezentos", "quatrocentos",
				   "quinhentos", "seiscentos", "setecentos", "oitocentos", "novecentos", "mil",
				   "milhão", "bilhão",
				   //Ordinais
				   "primeiro", "segundo", "terceiro", "quarto", "quinto", "sexto", "sétimo", "oitavo",
				   "nono", "décimo", "vigésimo", "trigésimo", "quadragésimo", "quinquagésimo", "sexagésimo",
				   "septuagésimo", "octogésimo", "nonagésimo", "centésimo", "ducentésimo", "trecentésimo",
				   "quadringentésimo", "quingentésimo", "sexcentésimo", "setingentésimo", "octingentésimo",
				   "nongentésimo", "milésimo", "milionésimo", "bilionésimo",
				   "primeira", "segunda", "terceira", "quarta", "quinta", "sexta", "sétima", "oitava",
				   "nona", "décima", "vigésima", "trigésima", "quadragésima", "quinquagésima", "sexagésima",
				   "septuagésima", "octogésima", "nonagésima", "centésima", "ducentésima", "trecentésima",
				   "quadringentésima", "quingentésima", "sexcentésima", "setingentésima", "octingentésima",
				   "nongentésima", "milésima", "milionésima", "bilionésima",
				   //Multiplicativos
				   "dobro", "duplo", "dupla", "tripla", "triplo", "tríplice", "quádruplo", "quíntuplo",
				   "sêxtuplo", "óctuplo", "nônunplo", "décuplo", "cêntuplo",
				 }};

	public POSTaggerPTBR() {
		loadPOSTagMap();
	}
	
	public static void applyPOSTag(String inputFilenameAddress) {
		
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilenameAddress), ENCODE_USED));
			Writer out = new OutputStreamWriter(new FileOutputStream(generateOutputFilename(inputFilenameAddress)), ENCODE_USED);
			
			int posIndex = -1;
			
			String line;
			String [] lineComponents;
			numberClassesFound = new int [termArray.length];
			int totalPOSTag = 0;
			
			while((line = in.readLine()) != null) {
				
				if(!line.equals("")) {
					
					lineComponents = line.split(DELIMITER_SPLIT);
				
					if(lineComponents[INDEX_LABEL].equals(LABEL_OUTSIDE)) {
						
						posIndex = getPOSIndex(lineComponents[INDEX_TERM]);
						
						if(posIndex != -1)
							lineComponents[INDEX_LABEL] = labelArray[posIndex]; 
					}
					
					out.write(lineComponents[INDEX_TERM] + DELIMITER_LABEL + lineComponents[INDEX_LABEL] + "\n");
				} else
					out.write("\n");
					
			}
			
			//@DMZDebug
			for(int i = 0; i < numberClassesFound.length; i++) {
				totalPOSTag += numberClassesFound[i];
				System.out.println("Number of POS Tag type " + labelArray[i] + ": " + numberClassesFound[i]);
			}
			System.out.println("-----------------------------------");
			System.out.println("Total number of POS Tag: " + totalPOSTag);
				
			out.flush();
			out.close();
			in.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private static int getPOSIndex(String term) {
		
		int posIndex = -1;
		term = term.toLowerCase();
		
		for(int classIndex = 0; posIndex == -1 && classIndex < termArray.length; classIndex++) {
			for(int termIndex = 0; termIndex < termArray[classIndex].length; termIndex++) {
				if(isEquals(term, termArray[classIndex][termIndex])) {
					posIndex = classIndex;
					numberClassesFound[posIndex]++;
					break;
				}
			}
		}
		
		//-- Try to see if it's number, in here can try adverbs too
		if(posIndex == -1) {
			try { 
				Double.parseDouble(term);
				posIndex = INDEX_NUMERAL;
			} catch(NumberFormatException e1) {}
		}
			
		return(posIndex);
	}
	
	//-- Change the function to better performance like cosine or other more appropriated
	private static boolean isEquals(String termA, String termB) {
		return(termA.equals(termB));
	}
	
	private void loadPOSTagMap() {
		posTagsMap = new HashMap<String, Boolean>();
		
		for(int i = 0; i < termArray.length; i++)
			for(int j = 0; j < termArray[i].length; j++)
				posTagsMap.put(termArray[i][j], true);
	}
	
	public boolean isTermPOSTag(String term) {
		
		boolean isPOSTag = false;
		
		if(posTagsMap.containsKey(term))
			isPOSTag = true;
		
		if(!isPOSTag) {
			try { 
				Double.parseDouble(term);
				isPOSTag = true;
			} catch(NumberFormatException e1) {}
		}
		
		return(isPOSTag);
	}
	
	private static String generateOutputFilename(String inputFilenameAddress) {
		
		int endInputFilename = inputFilenameAddress.lastIndexOf(".");
		
		String outputFilenameAddress = inputFilenameAddress.substring(0, endInputFilename);
		outputFilenameAddress += "-POSTag" + inputFilenameAddress.substring(endInputFilename);
		outputFilenameAddress = outputFilenameAddress.replace("/Input/", "/Output/");
		
		return(outputFilenameAddress);
	}
			 

}


