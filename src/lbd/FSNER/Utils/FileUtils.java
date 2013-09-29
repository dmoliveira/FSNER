package lbd.FSNER.Utils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.LabelEncoding.BILOU;
import lbd.data.handler.DataSequence;

public class FileUtils {

	public interface CommonDirectory {
		public String Input = "Input";
		public String Output = "Output";
	}

	public static String getOnlyFilename(String pFilenameAddress) {
		int vStartInputFilenameAddress = pFilenameAddress.lastIndexOf(Symbol.SLASH) + Symbol.SLASH.length();
		int vEndInputFilenameAddress = pFilenameAddress.lastIndexOf(Symbol.DOT);

		if(vStartInputFilenameAddress == -1) {
			vStartInputFilenameAddress = 0;
		}

		if(vEndInputFilenameAddress == -1) {
			vEndInputFilenameAddress = pFilenameAddress.length();
		}

		String vOutputFilenameAddress = pFilenameAddress.substring(vStartInputFilenameAddress, vEndInputFilenameAddress);
		return vOutputFilenameAddress;
	}

	public static String getFilenameDirectory(String pFilenameAddress) {
		String vFilenameDirectory = Symbol.EMPTY;

		int vEndPositionFilenameDirectory = pFilenameAddress.lastIndexOf(Symbol.SLASH) + Symbol.SLASH.length();

		if(pFilenameAddress.isEmpty() || vEndPositionFilenameDirectory == 0) {
			return vFilenameDirectory;
		}

		return pFilenameAddress.substring(0, vEndPositionFilenameDirectory);
	}

	public static String createCommonFilename(ArrayList<String> pFilenameList) {
		if(pFilenameList == null || pFilenameList.size() == 0) {
			return Symbol.EMPTY;
		} else if (pFilenameList.size() == 1) {
			return pFilenameList.get(0);
		}

		String vFilenameBase = pFilenameList.get(0);
		String vFilenameExample = pFilenameList.get(1);
		String vFilenameOutput = Symbol.EMPTY;

		for(int cLetter = 0; cLetter < vFilenameBase.length(); cLetter++) {
			if(vFilenameExample.length() > cLetter && vFilenameBase.charAt(cLetter) == vFilenameExample.charAt(cLetter)) {
				vFilenameOutput += vFilenameBase.charAt(cLetter);
			}
		}

		return vFilenameOutput;
	}

	public static BufferedReader createBufferedReader(String pInputFilename)
			throws FileNotFoundException, UnsupportedEncodingException {
		return new BufferedReader(new InputStreamReader(
				new FileInputStream(pInputFilename), Parameters.dataEncoding));
	}

	public static BufferedWriter createBufferedWriter(String pOutputFilename, String pFileExtension)
			throws UnsupportedEncodingException, FileNotFoundException {

		String vFilename = Parameters.generateOutputFilenameAddress(pOutputFilename, pFileExtension);
		(new File(getFilenameDirectory(vFilename))).mkdirs();

		return new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(vFilename), Parameters.dataEncoding));
	}

	public static InputStreamReader createInputStreamReader(String pInputFilename)
			throws FileNotFoundException, UnsupportedEncodingException {
		return new InputStreamReader(new FileInputStream(pInputFilename), Parameters.dataEncoding);
	}

	public static OutputStreamWriter createOutputStreamWriter(String pOutputFilename, String pFileExtension)
			throws FileNotFoundException, UnsupportedEncodingException {

		String vFilename = Parameters.generateOutputFilenameAddress(pOutputFilename, pFileExtension);
		(new File(getFilenameDirectory(vFilename))).mkdirs();

		return new OutputStreamWriter(new FileOutputStream(vFilename), Parameters.dataEncoding);
	}

	public static ArrayList<String> getListOfAllFiles(String pDirectory) {
		ArrayList<String> vFilenameAddressList = new ArrayList<String>();

		for(File cFile : (new File(pDirectory)).listFiles()) {
			vFilenameAddressList.add(pDirectory + cFile.getName());
		}

		return vFilenameAddressList;
	}

	public static ArrayList<String> getListOfOnlyFiles(String pDirectory) {
		ArrayList<String> vFilenameAddressList = new ArrayList<String>();

		for(File cFile : (new File(pDirectory)).listFiles()) {
			if(cFile.isFile()) {
				vFilenameAddressList.add(pDirectory + Symbol.SLASH + cFile.getName());
			}
		}

		return vFilenameAddressList;
	}

	public static ArrayList<String> getListOfOnlyDirectory(String pDirectory) {
		ArrayList<String> vFilenameAddressList = new ArrayList<String>();

		for(File cFile : (new File(pDirectory)).listFiles()) {
			if(cFile.isDirectory()) {
				vFilenameAddressList.add(pDirectory + cFile.getName());
			}
		}

		return vFilenameAddressList;
	}

	public static String getLastDirectory(String pFilenameAddress) {
		int vLastDirectoryEndDelimiter = pFilenameAddress.lastIndexOf(Symbol.SLASH);
		int vLastDirectoryStartDelimiter;
		String vLastDirectory = "";

		if(vLastDirectoryEndDelimiter != -1) {
			vLastDirectory = pFilenameAddress.substring(0, vLastDirectoryEndDelimiter);
			vLastDirectoryStartDelimiter = vLastDirectory.lastIndexOf(Symbol.SLASH) + Symbol.SLASH.length();
			vLastDirectory = vLastDirectory.substring(vLastDirectoryStartDelimiter);
		}

		return vLastDirectory;
	}

	public static String changeExtension(String pFilenameAddress, String pExtension, boolean pIsToForceAddExtension) {
		String vFilenameAddress = pFilenameAddress;

		int vStartExtensionPosition = vFilenameAddress.lastIndexOf(Symbol.DOT);

		if(vStartExtensionPosition != -1) {
			vFilenameAddress = vFilenameAddress.substring(0,
					vStartExtensionPosition) + Symbol.DOT + pExtension;
		} else if(pIsToForceAddExtension) {
			vFilenameAddress += Symbol.DOT + pExtension;
		}

		return vFilenameAddress;
	}

	public static boolean isExtension(String pFilenameAddress, String pExtension) {
		return pFilenameAddress.endsWith(pExtension);
	}

	public static String setToInputDirectory(String pFilenameAddress) {
		return pFilenameAddress.replace(CommonDirectory.Output, CommonDirectory.Input);
	}

	public static String setToOutputDirectory(String pFilenameAddress) {
		return pFilenameAddress.replace(CommonDirectory.Input, CommonDirectory.Output);
	}

	public static void writeSequenceListInFile(ArrayList<DataSequence> pSequenceList,
			String pFilenameAddress, String pFilenameExtension) {
		try {
			Writer vWriter = FileUtils.createOutputStreamWriter(pFilenameAddress, pFilenameExtension);

			for(DataSequence cSequence : pSequenceList) {
				for(int i = 0; i < cSequence.length(); i++) {
					vWriter.write(cSequence.x(i) + Symbol.PIPE + BILOU.values()[cSequence.y(i)].name() + Symbol.NEW_LINE);
				}
				vWriter.write(Symbol.NEW_LINE);
			}

			vWriter.flush();
			vWriter.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
