package lbd.data.handler.sequenceSetHandler;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import lbd.FSNER.Configuration.Constants;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;
import lbd.data.handler.SequenceSegment;
import lbd.data.handler.SequenceSet;
import lbd.fsner.label.encoding.Label;

public class SequenceSetHandlerTokenPerLine extends AbstractSequenceSetHandler {

	@Override
	protected SequenceSet getSequenceFromFile(BufferedReader pReader, Constants.FileType pFileType) {

		SequenceSegment vSequence = new SequenceSegment();
		SequenceSet vSequenceSet = new SequenceSet();

		String vLine = Symbol.EMPTY;
		String vLineAttributes[];

		int vLineNumber = 0;

		try {
			while ((vLine = pReader.readLine()) != null) {

				vLineNumber++;

				if (!vLine.isEmpty()) {
					vLineAttributes = vLine.split(Parameters.DataHandler.mSplitTokenLabelDelimiter);

					// Token = 0, Label = 1
					vSequence.add(vLineAttributes[0], Label.getCanonicalLabel(vLineAttributes[1]).ordinal());
				} else {
					vSequenceSet.add(vSequence);
					vSequence = new SequenceSegment();
				}
			}
		} catch (IOException pException) {
			System.err.println("Error when reading line("+vLineNumber+"): \"" + vLine + "\"");
			pException.printStackTrace();
		}

		return(vSequenceSet);
	}

	@Override
	public void writeSequenceToFile(Writer pOutput, ISequence pSequence) throws IOException {

		for(int i = 0; i < pSequence.length(); i++) {
			pOutput.write(pSequence.getToken(i) + Symbol.DELIMITER_LABEL +
					Label.getCanonicalLabel(pSequence.getLabel(i)).name() + Symbol.NEW_LINE);
		}

		pOutput.write(Symbol.NEW_LINE);
	}

	@Override
	protected SequenceSet getSegmentSequenceFromFile(BufferedReader pReader, Constants.FileType pFileType) {

		SequenceSet vSequenceSet = new SequenceSet();

		try {

			Writer vWriter = new OutputStreamWriter(new FileOutputStream(Parameters.Save.mSaveOption + "SegmentSequence.log"),
					Parameters.DataHandler.mDataEncoding);

			SequenceSegment vSequence = new SequenceSegment();
			int vSegmentPosition = 0;
			int vSegmentStart = -1;
			int vSegmentEnd = -1;

			String vLine;
			String vLineAttributes[];

			while ((vLine = pReader.readLine()) != null) {
				if (!vLine.isEmpty()) {
					vLineAttributes = vLine.split(Parameters.DataHandler.mSplitTokenLabelDelimiter);

					String token = vLineAttributes[0];
					int label = Label.getCanonicalLabel(vLineAttributes[1]).ordinal();

					vSequence.add(token, label);

					if(pFileType == Constants.FileType.TRAIN || pFileType == Constants.FileType.TEST) {
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
					vSequenceSet.add(vSequence);
					vSequence = new SequenceSegment();

					vSegmentPosition = 0;
					vSegmentStart = vSegmentPosition;
					vSegmentEnd = vSegmentPosition;
				}
			}


			try {
				for(int i = 0; i < vSequenceSet.size(); i++) {
					for(int j = 0; j < vSequenceSet.get(i).length(); j++) {
						vWriter.write(vSequenceSet.get(i).getToken(j) + "|" + vSequenceSet.get(i).getLabel(j)+ "\n");
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

	@Override
	public void writeSegmentSequenceToFile(Writer pWriter, SequenceSegment pSequence, boolean pIsSegment) {

		String vLineOutput = Symbol.EMPTY;
		int vLabelIndex;

		try {

			for (int i = 0; i < pSequence.length(); i++) {
				vLineOutput = pSequence.getToken(i) + Parameters.DataHandler.mDelimiterTokenLabel;
				vLabelIndex = ((!pIsSegment)? pSequence.getLabel(i) : ((pSequence.getLabel(i) == 0)? 3 : 1));
				vLineOutput += Label.getCanonicalLabel(vLabelIndex).name() + Symbol.NEW_LINE;
				pWriter.write(vLineOutput);
			}

			pWriter.write(Symbol.NEW_LINE);
			pWriter.flush();
		} catch (IOException pException) {
			pException.printStackTrace();
		}
	}
}
