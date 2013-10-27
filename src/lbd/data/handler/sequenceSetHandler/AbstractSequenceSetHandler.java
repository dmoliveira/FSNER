package lbd.data.handler.sequenceSetHandler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.List;

import lbd.FSNER.Configuration.Constants;
import lbd.FSNER.Configuration.Parameters;
import lbd.data.handler.ISequence;
import lbd.data.handler.SequenceSegment;
import lbd.data.handler.SequenceSet;
import lbd.data.tokenizer.AbstractTokenizer;

public abstract class AbstractSequenceSetHandler {

	protected AbstractTokenizer mTokenizer = Parameters.DataHandler.mTokenizer;

	public SequenceSet getSequenceSetFromStream(List<List<String>> pStreamList, int pDefaultLabel) {

		SequenceSet vSequenceSet = new SequenceSet();
		SequenceSegment vSequence = new SequenceSegment();

		for(List<String> cStreamList : pStreamList) {

			vSequenceSet.add(new SequenceSegment());
			vSequence = vSequenceSet.get(vSequenceSet.size() - 1);

			for(String term : cStreamList) {
				vSequence.add(term, pDefaultLabel);
			}
		}

		return (vSequenceSet);
	}

	public SequenceSet getSequenceSetFromFile(String pInputFile, Constants.FileType pFileType, boolean pIsSegment) {

		SequenceSet vSequenceSet = new SequenceSet();

		try {
			BufferedReader vReader = new BufferedReader(new InputStreamReader(new FileInputStream(pInputFile), Parameters.DataHandler.mDataEncoding));

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

	protected abstract SequenceSet getSequenceFromFile(BufferedReader pReader, Constants.FileType pFileType);

	public abstract void writeSequenceToFile(Writer pOutput, ISequence pSequence) throws IOException;

	protected abstract SequenceSet getSegmentSequenceFromFile(BufferedReader pReader, Constants.FileType pFileType);

	public abstract void writeSegmentSequenceToFile(Writer pWriter, SequenceSegment pSequence, boolean pIsSegment);
}
