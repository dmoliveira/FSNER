package lbd.Utils;

import iitb.CRF.DataSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;

import lbd.NewModels.Affix.Affix.AffixType;

public class Utils implements Serializable {

	private static final long serialVersionUID = 5061717102238489019L;

	public enum TypeTestGenereted {
		CROSS_VALIDATION, EXAUSTIVE_TEST
	};

	/**
	 * getLineNumber():
	 * @param inputFile
	 * @return
	 */
	public static int getLineNumber(String inputFile) {
		int lineNumber = 0;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));

			while (in.readLine() != null) {
				lineNumber++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return (lineNumber);
	}

	/**
	 * deleteFile():
	 * @param fileName
	 */
	public static void deleteFile(String fileName) {

		try {
			// Construct a File object for the file to be deleted.
			File target = new File(fileName);

			if (!target.exists()) {
				System.err.println("File " + fileName
						+ " not present to begin with!");
				return;
			}

			// Quick, now, delete it immediately:
			if (target.delete()) {
				System.err.println("** Deleted " + fileName + " **");
			} else {
				System.err.println("Failed to delete " + fileName);
			}
		} catch (SecurityException e) {
			System.err.println("Unable to delete " + fileName + "("
					+ e.getMessage() + ")");
		}
	}

	/**
	 * coountSequenceInFile():
	 * @param inputFile
	 * @return
	 */
	public static int countSequenceInFile(String inputFile) {

		int numberSequences = 0;
		String line = "";

		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));

			while ((line = in.readLine()) != null) {
				if (line.equals("")) {
					numberSequences++;
				}
			}

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return (numberSequences);
	}

	/**
	 * generateOutputFilenameAddress():
	 * @param inputFilenameAddress
	 * @param appendInTheFinal
	 * @return
	 */
	public static String generateOutputFilenameAddress(String inputFilenameAddress, String appendInTheFinal) {
		return(generateOutputFilenameAddress(inputFilenameAddress, appendInTheFinal, ""));
	}

	/**
	 * generateOutputFilenameAddress():
	 * @param inputFilenameAddress
	 * @param appendInTheFinal
	 * @param extention
	 * @return
	 */
	public static String generateOutputFilenameAddress(String inputFilenameAddress, String appendInTheFinal, String extention) {

		int endPositionInputFileName = inputFilenameAddress.lastIndexOf(".");
		String outputFilenameAddress = inputFilenameAddress.substring(0, endPositionInputFileName);
		outputFilenameAddress += appendInTheFinal;
		outputFilenameAddress += (extention.length() > 0)? extention : inputFilenameAddress.substring(endPositionInputFileName);

		return(outputFilenameAddress);
	}

	/**
	 * convertSequenceToLowerCase(): Convert a sequence of
	 * DataSequence to lower case.
	 * @param data The data sequence to be converted
	 * @param size The size of the data sequence (optimization)
	 * @return A String array with the sequence in lower case
	 */
	public static String[] convertSequenceToLowerCase(DataSequence data, int size) {

		String [] seqList = new String[size];

		for(int i = 0; i < size; i++) {
			seqList[i] = ((String)data.x(i)).toLowerCase();
		}

		return(seqList);
	}

	public static String[] convertSequenceToLowerCase(DataSequence data, int size, AffixType affixType) {

		String [] seqList = new String[size];

		for(int i = 0; i < size; i++) {
			seqList[i] = getTermAffix(((String)data.x(i)).toLowerCase(), affixType);
		}

		return(seqList);
	}

	public static String[] convertSequenceToLowerCase(String [] sequence, int size) {

		String [] seqList = new String[size];

		for(int i = 0; i < sequence.length; i++) {
			seqList[i] = sequence[i].toLowerCase();
		}

		return(seqList);
	}

	public static String[] convertSequenceToLowerCase(String [] sequence, int size, AffixType affixType) {

		String [] seqList = new String[size];

		for(int i = 0; i < sequence.length; i++) {
			seqList[i] = getTermAffix(sequence[i].toLowerCase(), affixType);
		}

		return(seqList);
	}

	public static String[] transformSequenceToArray(DataSequence data, int size) {

		String [] seqList = new String[size];

		for(int i = 0; i < size; i++) {
			seqList[i] = (String)data.x(i);
		}

		return(seqList);
	}

	public static String[] transformSequenceToArray(DataSequence data, int size, AffixType affixType) {

		String [] seqList = new String[size];

		for(int i = 0; i < size; i++) {
			seqList[i] = getTermAffix((String)data.x(i), affixType);
		}

		return(seqList);
	}

	public static String getTermAffix (String term, AffixType affixType) {

		String termAffix = "*emptyAffix*";
		int tokenSize = term.length();

		if(affixType == AffixType.PrefixSize2 && tokenSize > 2) {
			termAffix = term.substring(0, 2);
		} else if(affixType == AffixType.PrefixSize3 && tokenSize > 3) {
			termAffix = term.substring(0, 3);
		} else if(affixType == AffixType.SuffixSize1 && tokenSize > 1) {
			termAffix = term.substring(tokenSize-1, tokenSize);
		} else if(affixType == AffixType.SuffixSize2 && tokenSize > 2) {
			termAffix = term.substring(tokenSize-2, tokenSize);
		} else if(affixType == AffixType.SuffixSize3 && tokenSize > 3) {
			termAffix = term.substring(tokenSize-3, tokenSize);
		} else if(affixType == AffixType.SuffixSize4 && tokenSize > 4) {
			termAffix = term.substring(tokenSize-4, tokenSize);
		} else if(affixType == AffixType.None) {
			termAffix = term;
		}

		return(termAffix);
	}

	public static void printLog(String toPrint, Writer out) {

		//System.out.print(toPrint);

		if(out != null) {
			try { out.write(toPrint); } catch (IOException e) { e.printStackTrace();}
		}
	}

	public static void printlnLog(String toPrint, Writer out) {

		System.out.println(toPrint);

		if(out != null) {
			try { out.write(toPrint + "\n"); } catch (IOException e) { e.printStackTrace();}
		}
	}

	public static ArrayList<String> getSequenceInFile(BufferedReader in, boolean transformToLowerCase) {

		final String DELIMITER_DOCRF = "\\|";
		final int INDEX_TOKEN = 0;

		String line = "";
		String [] lineElement;
		ArrayList<String> sequence = new ArrayList<String>();

		try {
			while((line = in.readLine()) != null && !line.equals("")) {

				lineElement = line.split(DELIMITER_DOCRF);

				sequence.add((!transformToLowerCase)? lineElement[INDEX_TOKEN] :
					lineElement[INDEX_TOKEN].toLowerCase());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sequence;

	}

	//-- to relax matching (2nd Step: LevenshteinDistance)
	//-- Param String id need to be in lowerCase
	public static boolean isTermMatching(String s, String t,
			int minimumRelaxMatchingLength, int editDistanceAcceptable) {

		boolean isMatching = false;

		if(s.length() >= minimumRelaxMatchingLength) {
			if(getLevenshteinDistance(s, t) <= editDistanceAcceptable) {
				isMatching = true;
			}
		} else if(s.equals(t)) {
			isMatching = true;
		}

		return(isMatching);
	}

	public static int getLevenshteinDistance (String s, String t) {

		if (s == null || t == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		/*
	    The difference between this impl. and the previous is that, rather
	     than creating and retaining a matrix of size s.length()+1 by t.length()+1,
	     we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
	     is the 'current working' distance array that maintains the newest distance cost
	     counts as we iterate through the characters of String s.  Each time we increment
	     the index of String t we are comparing, d is copied to p, the second int[].  Doing so
	     allows us to retain the previous cost counts as required by the algorithm (taking
	     the minimum of the cost count to the left, up one, and diagonally up and to the left
	     of the current cost count being calculated).  (Note that the arrays aren't really
	     copied anymore, just switched...this is clearly much better than cloning an array
	     or doing a System.arraycopy() each time  through the outer loop.)

	     Effectively, the difference between the two implementations is this one does not
	     cause an out of memory condition when calculating the LD over two very large strings.
		 */

		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		int p[] = new int[n+1]; //'previous' cost array, horizontally
		int d[] = new int[n+1]; // cost array, horizontally
		int _d[]; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i<=n; i++) {
			p[i] = i;
		}

		for (j = 1; j<=m; j++) {
			t_j = t.charAt(j-1);
			d[0] = j;

			for (i=1; i<=n; i++) {
				cost = s.charAt(i-1)==t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost
				d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		return p[n];
	}

	public static String formatDecimalNumber(double number) {
		return(formatDecimalNumber("#.##", number));
	}

	public static String formatDecimalNumber(String format, double number) {
		return((new DecimalFormat(format)).format(number));
	}


}
