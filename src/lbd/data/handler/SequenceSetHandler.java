package lbd.data.handler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;

public class SequenceSetHandler {

	public static enum FileType {TRAINING, TEST, VALIDATION, TOLABEL};

	public static SequenceSet getSequenceSetFromStream(List<List<String>> pStreamList, int pDefaultLabel) {

		SequenceSet vSequenceSet = new SequenceSet();
		SegmentSequence vSequence = new SegmentSequence();

		for(List<String> cStreamList : pStreamList) {

			vSequenceSet.addSequence(new SegmentSequence());
			vSequence = vSequenceSet.get(vSequenceSet.size() - 1);

			for(String term : cStreamList) {
				vSequence.addElement(term, pDefaultLabel);
			}
		}

		return (vSequenceSet);
	}

	public static SequenceSet getSequenceSetFromFile(String pInputFile, FileType pFileType, boolean pIsSegment) {

		SequenceSet vSequenceSet = new SequenceSet();

		try {
			BufferedReader vReader = new BufferedReader(new InputStreamReader(new FileInputStream(pInputFile), Parameters.dataEncoding));

			if(!pIsSegment) {
				vSequenceSet = getSequenceFromFile(vReader, pFileType);
			} else {
				vSequenceSet = getSegmentSequenceFromFile(vReader, pFileType);
			}

			vReader.close();
		} catch (FileNotFoundException pException) {
			pException.printStackTrace();
		} catch (IOException pException) {
			pException.printStackTrace();
		}

		return (vSequenceSet);
	}

	@SuppressWarnings("static-access")
	private static SequenceSet getSequenceFromFile(BufferedReader pReader, FileType pFileType) {

		SegmentSequence vSequence = new SegmentSequence();
		SequenceSet vSequenceSet = new SequenceSet();

		String vLine = Symbol.EMPTY;
		String vLineAttributes[];

		int vLineNumber = 0;

		try {
			while ((vLine = pReader.readLine()) != null) {

				vLineNumber++;

				if (!vLine.isEmpty()) {
					vLineAttributes = vLine.split(Parameters.HandleFile.splitTokenLabelDelimiter);

					// Token = 0, Label = 1
					vSequence.addElement(vLineAttributes[0], LabelEncoding.BILOU.valueOf(vLineAttributes[1]).ordinal());
				} else {
					vSequenceSet.addSequence(vSequence);
					vSequence = new SegmentSequence();
				}
			}
		} catch (IOException pException) {
			System.err.println("Error when reading line("+vLineNumber+"): \"" + vLine + "\"");
			pException.printStackTrace();
		}

		return(vSequenceSet);
	}

	@SuppressWarnings("static-access")
	private static SequenceSet getSegmentSequenceFromFile(BufferedReader pReader, FileType pFileType) {

		SequenceSet vSequenceSet = new SequenceSet();

		try {

			Writer vWriter = new OutputStreamWriter(new FileOutputStream(Parameters.Save.saveOption + "SegmentSequence.log"), Parameters.dataEncoding);

			SegmentSequence vSequence = new SegmentSequence();
			int vSegmentPosition = 0;
			int vSegmentStart = -1;
			int vSegmentEnd = -1;

			String vLine;
			String vLineAttributes[];

			while ((vLine = pReader.readLine()) != null) {
				if (!vLine.isEmpty()) {
					vLineAttributes = vLine.split(Parameters.HandleFile.splitTokenLabelDelimiter);

					String token = vLineAttributes[0];
					int label = LabelEncoding.BILOU.valueOf(vLineAttributes[1]).ordinal();

					vSequence.addElement(token, label);

					if(pFileType == FileType.TRAINING || pFileType == FileType.TEST) {
						if(label == 0) { // Beginning
							vSegmentStart = vSegmentPosition;
						} else if(label == 2) { // Last Token
							vSegmentEnd = vSegmentPosition;
							vSequence.setSegment(vSegmentStart, vSegmentEnd, 1);
						} else if(label == 3 || label == 4) { //Outside or UnitToken
							vSegmentStart = vSegmentPosition;
							vSegmentEnd = vSegmentPosition;

							//0 - Outside, 1 - Inside
							vSequence.setSegment(vSegmentStart, vSegmentEnd, (label != 3) ? 1 : 0);
						}
					}

					vSegmentPosition++;
				} else {
					vSequenceSet.addSequence(vSequence);
					vSequence = new SegmentSequence();

					vSegmentPosition = 0;
					vSegmentStart = vSegmentPosition;
					vSegmentEnd = vSegmentPosition;
				}
			}


			try {
				for(int i = 0; i < vSequenceSet.size(); i++) {
					for(int j = 0; j < vSequenceSet.get(i).length(); j++) {
						vWriter.write(vSequenceSet.get(i).x(j) + "|" + vSequenceSet.get(i).y(j)+ "\n");
					}
					vWriter.write("\n");
				}
				vWriter.flush();
				vWriter.close();
			} catch (IOException pException) {
				pException.printStackTrace();
			}

		} catch (IOException pException) {
			pException.printStackTrace();
		}

		return(vSequenceSet);
	}

	@SuppressWarnings("static-access")
	public static void writeSequenceToFile(Writer pOutput, DataSequence pSequence) throws IOException {

		for(int i = 0; i < pSequence.length(); i++) {
			pOutput.write(pSequence.x(i) + Symbol.DELIMITER_LABEL +
					LabelEncoding.BILOU.values()[(pSequence.y(i))].name() + Symbol.NEW_LINE);
		}

		pOutput.write(Symbol.NEW_LINE);
	}

	@SuppressWarnings("static-access")
	public static void writeSegmentSequenceToFile(Writer pWriter, SegmentSequence pSequence, boolean pIsSegment) {

		String vLineOutput = Symbol.EMPTY;
		int vLabelIndex;

		try {

			for (int i = 0; i < pSequence.size(); i++) {
				vLineOutput = pSequence.x(i) + Parameters.HandleFile.delimiterTokenLabel;
				vLabelIndex = ((!pIsSegment)? pSequence.y(i) : ((pSequence.y(i) == 0)? 3 : 1));
				vLineOutput += LabelEncoding.BILOU.values()[vLabelIndex].name() + Symbol.NEW_LINE;
				pWriter.write(vLineOutput);
			}

			pWriter.write(Symbol.NEW_LINE);
			pWriter.flush();
		} catch (IOException pException) {
			pException.printStackTrace();
		}
	}
}
