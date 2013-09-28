package lbd.CRF;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

public class HandlingSequenceSet {

	private static final String ENCODE_USED = "ISO-8859-1";
	public static enum FileType {TRAINING, TEST, VALIDATION, LABEL};
	private static final String DELIMITER_SPLIT = "\\|";
	private static final String DELIMITER = "|";
	/** private Writer out; (Disable) **/

	public static SequenceSet transformStreamInSequenceSet(ArrayList<ArrayList<String>> streamList, int defaultLabel) {

		SequenceSet sequenceSet = new SequenceSet();
		SegmentSequence sequence = new SegmentSequence();

		for(ArrayList<String> stream : streamList) {

			sequenceSet.addSequence(new SegmentSequence());
			sequence = sequenceSet.get(sequenceSet.size() - 1);

			for(String term : stream) {
				sequence.addElement(term, defaultLabel);
			}
		}

		return (sequenceSet);
	}

	public static SequenceSet transformFileInSequenceSet(String inputFile, FileType fileType, boolean isSegment) {

		SequenceSet sequenceSet = new SequenceSet();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), ENCODE_USED));

			if(!isSegment) {
				sequenceSet = getSequenceInFile(in, fileType);
			} else {
				sequenceSet = getSegmentSequenceInFile(in, fileType);
			}

			in.close();
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException: " + e);
		} catch (IOException e) {
			System.err.println("IOException: " + e);
		}

		return (sequenceSet);
	}

	private static SequenceSet getSequenceInFile(BufferedReader in, FileType fileType) {

		SegmentSequence sequence = new SegmentSequence();
		SequenceSet sequenceSet = new SequenceSet();

		String line = "";
		String elementList[];

		int lineNumber = 0;

		try {
			while ((line = in.readLine()) != null) {

				lineNumber++;

				if (!line.equals("")) {
					//System.err.println("Reading line("+lineNumber+"): \"" + line + "\"");
					elementList = line.split(DELIMITER_SPLIT);

					String token = elementList[0];
					int label = LabelMap.getLabelIndexBILOU(elementList[1]);

					sequence.addElement(token, label);
				} else {
					sequenceSet.addSequence(sequence);
					sequence = new SegmentSequence();
				}
			}
		} catch (IOException e) {
			System.err.println("Error when reading line("+lineNumber+"): \"" + line + "\"");
			e.printStackTrace();
		}

		return(sequenceSet);
	}

	private static SequenceSet getSegmentSequenceInFile(BufferedReader in, FileType fileType) {

		Writer out = null;

		try {
			out = new OutputStreamWriter(new FileOutputStream("./samples/data/bcs2010/log.txt"), ENCODE_USED);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		SegmentSequence sequence = new SegmentSequence();
		SequenceSet sequenceSet = new SequenceSet();
		int segmentPosition = 0;
		int segmentStart = -1;
		int segmentEnd = -1;

		String line;
		String elementList[];

		try {
			while ((line = in.readLine()) != null) {
				if (!line.equals("")) {
					elementList = line.split(DELIMITER_SPLIT);

					String token = elementList[0];
					int label = -1;

					if(fileType != FileType.VALIDATION) {
						label = LabelMap.getLabelIndexBILOU(elementList[1]);
					} else {
						label = LabelMap.getLabelIndexOI(elementList[1]);
					}

					sequence.addElement(token, label);

					if(fileType == FileType.TRAINING || fileType == FileType.TEST) {
						if(label == 0) { // Beginning
							segmentStart = segmentPosition;
						} else if(label == 2) { // Last Token
							segmentEnd = segmentPosition;
							sequence.setSegment(segmentStart, segmentEnd, 1);
						} else if(label == 3 || label == 4) { //Outside or UnitToken
							segmentStart = segmentPosition;
							segmentEnd = segmentPosition;

							//0 - Outside, 1 - Inside
							sequence.setSegment(segmentStart, segmentEnd, (label != 3) ? 1 : 0);
						}
					}

					segmentPosition++;
				} else {
					sequenceSet.addSequence(sequence);
					sequence = new SegmentSequence();

					segmentPosition = 0;
					segmentStart = segmentPosition;
					segmentEnd = segmentPosition;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			for(int i = 0; i < sequenceSet.size(); i++) {
				for(int j = 0; j < sequenceSet.get(i).length(); j++) {
					out.write(sequenceSet.get(i).x(j) + "|" + sequenceSet.get(i).y(j)+ "\n");
				}
				out.write("\n");
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return(sequenceSet);
	}

	public static void transformSequenceToFile(Writer out, SegmentSequence sequence, boolean isSegment) {

		String output = "";
		int labelIndex;

		for (int i = 0; i < sequence.size(); i++) {
			output += sequence.x(i);
			output += DELIMITER;

			labelIndex = ((!isSegment)? sequence.y(i) : ((sequence.y(i) == 0)? 3 : 1));

			output += LabelMap.getLabelNameBILOU(labelIndex) + "\n";
		}

		output += "\n";

		try {
			out.write(output);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
