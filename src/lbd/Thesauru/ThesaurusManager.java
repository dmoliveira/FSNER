package lbd.Thesauru;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import lbd.Utils.Connection.Connection;

public class ThesaurusManager implements Serializable{
	
	private static final long serialVersionUID = -1078725096668491213L;
	
	/** Model **/
	/*<table cellspacing="5" class="the_content">
	<tr>
	<td valign="top" nowrap >Main Entry:</td>
	<td>
	<a class="nud" href="http://dictionary.reference.com/browse/classy">classy</a>
	</td></tr>
	<tr>
	<td valign="top" nowrap >Part of Speech:</td>
	<td><i>adjective</i></td></tr>
	<tr>
	<td valign="top">Definition:</td>

	<td>stylish, having panache</td></tr>
	<tr>
	<td valign="top">Synonyms:</td>
	<td><span>
	chic, dashing,
	<a class="theColor" rel="nofollow" onmousedown="return hotwordOneClick(this);" href="http://thesaurus.com/browse/elegant">elegant</a>,
	<a class="theColor" rel="nofollow" onmousedown="return hotwordOneClick(this);" href="http://thesaurus.com/browse/exclusive">exclusive</a>,
	<a class="theColor" rel="nofollow" onmousedown="return hotwordOneClick(this);" href="http://thesaurus.com/browse/fashionable">fashionable</a>, high-class, <b>in</b>, in vogue, mod, modish, posh,
	<a class="theColor" rel="nofollow" onmousedown="return hotwordOneClick(this);" href="http://thesaurus.com/browse/select">select</a>,

	<a class="theColor" rel="nofollow" onmousedown="return hotwordOneClick(this);" href="http://thesaurus.com/browse/sharp">sharp</a>,
	<a class="theColor" rel="nofollow" onmousedown="return hotwordOneClick(this);" href="http://thesaurus.com/browse/superior">superior</a>, swank, swanky, tony, uptown
	</span></td>
	</tr>
	<tr>
	<td valign="top">Antonyms:</td>
	<td><span>
	inelegant, <a class="theColor" rel="nofollow" onmousedown="return hotwordOneClick(this);" href="http://thesaurus.com/browse/inferior">inferior</a>, <a class="theColor" rel="nofollow" onmousedown="return hotwordOneClick(this);" href="http://thesaurus.com/browse/plain">plain</a>, unstylish
	</span></td>
	</tr>

	</table>*/
	
	protected HashMap<String, Thesaurus> thesaurusMap;
	protected final String ENCODE_USED_CONNECTION = "UTF-8";
	
	protected final String BASE_URL = "http://www.thesaurus.com/browse/";
	
	protected final String TAG_THESAURUS_START = "<table cellspacing=\"5\" class=\"the_content\">";
	protected final String TAG_MAIN_ENTRY = "<td valign=\"top\" nowrap >Main Entry:</td>"; //2l
	protected final String TAG_POS_TAG = "<td valign=\"top\" nowrap >Part of Speech:</td>";//1l
	protected final String TAG_DEFINITION = "<td valign=\"top\">Definition:</td>";//1l
	protected final String TAG_SYNONYM = "<td valign=\"top\">Synonyms:</td>";//2l
	protected final String TAG_ANTONYM = "<td valign=\"top\">Antonyms:</td>";//2l
	protected final String TAG_THESAURUS_ELEMENT_ANALYSIS = "</table>";
	protected final String TAG_ANALYSIS_END = "<!-- identify the current view -->";
	
	protected final String TAG_START_HTML_CODE = "<";
	protected final String TAG_END_HTML_CODE = ">";
	
	protected final String TAG_SPACE = " ";
	protected final String TAG_EMPTY = "";
	protected final String TAG_COMMA = ",";
	protected final String DELIMITER_ELEMENTS = ",";
	
	protected boolean autoCharset = false;
	protected boolean isDebugActive = false;
	protected boolean getThesaurusLite = true;
	
	public ThesaurusManager() {
		thesaurusMap = new HashMap<String, Thesaurus>();
		
		/*try {
			if((new File("ThesaurusManager.bin")).exists())readContextAnalysisObject("ThesaurusManager.bin", this);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}*/
	}
	
	public static void main(String [] args) {
		ThesaurusManager tM = new ThesaurusManager();
		//tM.getThesaurusElement("Bike");
		tM.getThesaurusLite("whereas");
	}
	
	public Thesaurus getThesaurusLite(String term) {
		
		boolean hasThesaurus = false;
		
		final int MAX_SYNONYM = 1000;
		int synonymNumber = 0;
		
		String line;
		String [] element;
		
		term = term.toLowerCase();
		
		Thesaurus thesaurus;
		
		Date startTime = new Date();
		Date internDate;
		
		if(!thesaurusMap.containsKey(term)) {
			
			thesaurusMap.put(term, new Thesaurus(term));
			thesaurus = thesaurusMap.get(term);
		
			Connection.executeInSilenceMode = true;
			BufferedReader queryConnection = Connection.createFastConnection((BASE_URL + term), ENCODE_USED_CONNECTION);
			
			try {
				
				internDate = new Date();
				while((line = queryConnection.readLine()) != null && !line.equals(TAG_THESAURUS_START));
				
				while((line = queryConnection.readLine()) != null) {
					
					if(line.equals(TAG_SYNONYM)) {
						
						hasThesaurus = true;
						
						do { line = removeHTMLTags(queryConnection.readLine()); } while(line.isEmpty());
						if(isDebugActive) System.out.print("Synonyns: ");
						
						do {
							element = line.split(DELIMITER_ELEMENTS);
							for(int i = 0; i < element.length && synonymNumber < MAX_SYNONYM; i++) {
								thesaurus.addSynonym(element[i].toLowerCase());
								++synonymNumber;
							}
							
							if(isDebugActive)
								for(int i = thesaurus.getSynonymList().size()-element.length; i < thesaurus.getSynonymList().size(); i++)
									System.out.print(thesaurus.getSynonymList().get(i) + TAG_COMMA + TAG_SPACE);
							
						}while((line = removeHTMLTags(queryConnection.readLine())) != null && !line.isEmpty() && synonymNumber < MAX_SYNONYM);
						
						if(isDebugActive) System.out.println();
					}
					//System.out.println("-- HTMLLine: " + line + "\n\t*" + removeHTMLTags(line) + "*");
				}
			
				thesaurus.setThesaurus(hasThesaurus);
				
				queryConnection.close();
				
				if(isDebugActive) System.out.println("-- Elipsed Time: " + ((new Date()).getTime() - startTime.getTime())/1000.0 + "s intern(" + ((new Date()).getTime() - internDate.getTime())/1000.0  + "s)");
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch(NullPointerException e) {
			}
		} else {
			thesaurus = thesaurusMap.get(term);
		}
		
		return(thesaurus);
	}
	
	public Thesaurus getThesaurus(String term) {
		
		String line;
		String posTag = "";
		String [] element;
		
		boolean startThesaurusElementAnalysis = false;
		boolean hasThesaurus = false;
		
		term = term.toLowerCase();
		
		ThesaurusElement tElement;
		Thesaurus thesaurus;
		
		Date startTime = new Date();
		Date internDate;
		
		if(!thesaurusMap.containsKey(term)) {
			
			thesaurusMap.put(term, new Thesaurus(term));
			thesaurus = thesaurusMap.get(term);
			 
			thesaurus.addThesaurusElement(new ThesaurusElement());
			tElement = thesaurus.getThesaurusList().get(thesaurus.getThesaurusList().size()-1);
			
			String query = BASE_URL + term;
		
			Connection.executeInSilenceMode = true;
			BufferedReader queryConnection = Connection.createConnection(query, ENCODE_USED_CONNECTION, autoCharset);
			
			try {
				
				internDate = new Date();
				
				while((line = queryConnection.readLine()) != null && 
						!line.equals(TAG_THESAURUS_START) && (!hasThesaurus || !line.equals(TAG_ANALYSIS_END)));
				
				while((line = queryConnection.readLine()) != null && (!hasThesaurus || !line.equals(TAG_ANALYSIS_END))) {
					
					if(line.equals(TAG_MAIN_ENTRY)) {
						
						startThesaurusElementAnalysis = true;
						hasThesaurus = true;
						
						if(!getThesaurusLite) {
							while(removeHTMLTags(line = queryConnection.readLine()).isEmpty());
							
							tElement.setMainEntry(removeHTMLTags(line));
							printMainEntry(tElement);
						}
						
					} else if(line.equals(TAG_POS_TAG)) {
						
						while(removeHTMLTags(line = queryConnection.readLine()).isEmpty());
						
						posTag = removeHTMLTags(line);
						tElement.addPosTag(posTag.split(DELIMITER_ELEMENTS));
						
						printPosTags(tElement);
						
					} else if(!getThesaurusLite && line.equals(TAG_DEFINITION)) {
						
						while(removeHTMLTags(line = queryConnection.readLine()).isEmpty());
						
						tElement.setDefinition(removeHTMLTags(line));
						printDefinition(tElement);
						
					} else if(line.equals(TAG_SYNONYM)) {
						
						do { line = removeHTMLTags(queryConnection.readLine()); } while(line.isEmpty());
						if(isDebugActive) System.out.print("Synonyns("+posTag+"):");
						
						do {
							element = line.split(DELIMITER_ELEMENTS);
							for(int i = 0; i < element.length; i++) tElement.addSynonym(posTag, element[i].replace(TAG_COMMA, TAG_EMPTY));
							
							if(isDebugActive)
								for(int i = tElement.getSynonymList(posTag).size()-element.length; i < tElement.getSynonymList(posTag).size(); i++)
									System.out.print(tElement.getSynonymList(posTag).get(i) + ", ");
							
						}while((line = removeHTMLTags(queryConnection.readLine())) != null && !line.isEmpty());
						
					} else if(!getThesaurusLite && line.equals(TAG_ANTONYM)) {
						
						do { line = removeHTMLTags(queryConnection.readLine()); } while(line.isEmpty());
						if(isDebugActive) System.out.print("Antonyns("+posTag+"):");
						
						do {
							element = line.split(DELIMITER_ELEMENTS);
							for(int i = 0; i < element.length; i++) tElement.addAntonym(posTag, element[i].replace(TAG_COMMA, TAG_EMPTY));
							
							if(isDebugActive)
								for(int i = tElement.getAntonymList(posTag).size()-element.length; i < tElement.getAntonymList(posTag).size(); i++)
									System.out.print(tElement.getAntonymList(posTag).get(i) + ", ");
							
						}while((line = removeHTMLTags(queryConnection.readLine())) != null && !line.isEmpty());
						
					} else if(startThesaurusElementAnalysis && line.equals(TAG_THESAURUS_ELEMENT_ANALYSIS)) {
						
						thesaurus.addThesaurusElement(new ThesaurusElement());
						tElement = thesaurus.getThesaurusList().get(thesaurus.getThesaurusList().size()-1);
						
						if(isDebugActive) System.out.println();
						
						startThesaurusElementAnalysis = false;
					}
					
					//System.out.println("-- HTMLLine: " + line + "\n\t*" + removeHTMLTags(line) + "*");
				}
			
				thesaurus.setThesaurus(hasThesaurus);
				
				queryConnection.close();
				
				if(isDebugActive) System.out.println("-- Elipsed Time: " + ((new Date()).getTime() - startTime.getTime())/1000.0 + "s intern(" + ((new Date()).getTime() - internDate.getTime())/1000.0  + "s)");
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch(NullPointerException e) {
			}
		} else {
			thesaurus = thesaurusMap.get(term);
		}
		
		return(thesaurus);
	}
	
	protected String removeHTMLTags(String line) {
		
		String proccessedLine = line;
		
		String lineBeforeHTMLCode = "";
		String lineAfterHTMLCode = "";
		
		int startPosHTMLCode = -1;
		int endPosHTMLCode = -1;
		
		while((startPosHTMLCode = proccessedLine.indexOf(TAG_START_HTML_CODE)) != -1) {
			
			endPosHTMLCode = proccessedLine.indexOf(TAG_END_HTML_CODE) + 1;
			
			if(endPosHTMLCode > 0 && startPosHTMLCode < endPosHTMLCode) {
			
				lineBeforeHTMLCode = proccessedLine.substring(0, startPosHTMLCode);
				lineAfterHTMLCode = (endPosHTMLCode < proccessedLine.length())? proccessedLine.substring(endPosHTMLCode) : "";
				
				//System.out.println("*" + lineBeforeHTMLCode + "* *" + lineAfterHTMLCode + "*");
				
				proccessedLine = lineBeforeHTMLCode;
				//proccessedLine += (!lineBeforeHTMLCode.endsWith(TAG_SPACE) && !lineAfterHTMLCode.startsWith((TAG_SPACE))? TAG_SPACE : TAG_EMPTY);
				proccessedLine += lineAfterHTMLCode;
			} else {
				break;
			}
		}
		
		return(proccessedLine);
	}
	
	public static boolean isValidTerm(String term) {
		
		boolean isValidTerm = true;
		
		for(int i = 0; i < term.length(); i++) {
			if(!Character.isLetter(term.charAt(i))) {
				isValidTerm = false;
				break;
			}
		}
		
		return(isValidTerm);
	}
	
	protected void printMainEntry(ThesaurusElement thesaurusElement) {
		if(isDebugActive) 
			System.out.println("\nMain Entry: " + thesaurusElement.getMainEntry());
	}
	
	protected void printPosTags(ThesaurusElement thesaurusElement) {
		if(isDebugActive) {
			
			System.out.print("PosTags:");
			
			for(int i = 0; i < thesaurusElement.getPosTagList().size();i++)
				System.out.print(" " + thesaurusElement.getPosTagList().get(i));
			
			System.out.println();
		}
	}
	
	protected void printDefinition(ThesaurusElement thesaurusElement) {
		if(isDebugActive) 
			System.out.println("Definition: " + thesaurusElement.getDefinition());
	}
	
	public void readContextAnalysisObject(String filename, ThesaurusManager target) throws IOException, ClassNotFoundException {
    	
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		ThesaurusManager thesaurusManager = (ThesaurusManager) in.readObject();
		cloneThesaurusManager(target, thesaurusManager);
		
		in.close();
    }
	
	private void cloneThesaurusManager(ThesaurusManager target, ThesaurusManager clone) {
		
		target.thesaurusMap = clone.thesaurusMap;
	}
	
    public void writeThesaurusManagerObject(String filename) throws IOException {
    	
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
    }

}
