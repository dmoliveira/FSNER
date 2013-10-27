package lbd.data.handler.sequenceSetHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbd.FSNER.Configuration.Constants.FileType;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;
import lbd.data.handler.SequenceSegment;
import lbd.data.handler.SequenceSet;
import lbd.fsner.entity.Entity;
import lbd.fsner.entity.EntityType;
import lbd.fsner.label.encoding.Label;

public class SequenceSetHandlerLine extends AbstractSequenceSetHandler{

	protected Map<String, EntityType> mEntityMarkSet;

	public SequenceSetHandlerLine() {
		initialize();
	}

	protected void initialize() {
		mEntityMarkSet = new HashMap<String, EntityType>();
		for(EntityType cEntityType : EntityType.values()) {
			mEntityMarkSet.put(Symbol.SQUARE_BRACKET_LEFT + cEntityType.getValue(), cEntityType);
		}
	}

	@Override
	protected SequenceSet getSequenceFromFile(BufferedReader pReader, FileType pFileType) {

		SequenceSet vSequenceSet = new SequenceSet();

		String vLine = Symbol.EMPTY;
		int vLineNumber = 0;

		try {
			while ((vLine = pReader.readLine()) != null) {

				vLineNumber++;
				vLine = mTokenizer.tokenize(vLine);
				vLine = adjustEntityMarkers(vLine);

				if (!vLine.isEmpty()) {
					vSequenceSet.add(getSequence(vLine));
				}
			}
		} catch (ArrayIndexOutOfBoundsException pException) {
			System.err.println("Error when reading line(" + vLineNumber + "): \"" + vLine + "\"");
			pException.printStackTrace();
		} catch (IOException pException) {
			System.err.println("Error when reading line(" + vLineNumber + "): \"" + vLine + "\"");
			pException.printStackTrace();
		}

		return(vSequenceSet);
	}

	private String adjustEntityMarkers(String pMessage) {

		String vProcessedMessage = pMessage;

		for(EntityType cEntityType : EntityType.values()) {
			vProcessedMessage = vProcessedMessage.replace(Symbol.SQUARE_BRACKET_LEFT + Symbol.SPACE
					+ cEntityType.getValue(), Symbol.SQUARE_BRACKET_LEFT + cEntityType.getValue());
		}

		return vProcessedMessage;
	}

	private SequenceSegment getSequence(String pMessage) {

		SequenceSegment vSequence = new SequenceSegment();

		String [] vTokenList = pMessage.split(Symbol.SPACE);

		for(int cToken = 0; cToken < vTokenList.length; cToken++) {
			if(mEntityMarkSet.containsKey(vTokenList[cToken])) {
				int vEndEntityIndex = getEndEntityIndex(vTokenList, cToken + 1);
				if(vEndEntityIndex > -1) {

					List<String> vEntity = Arrays.asList(vTokenList).subList(cToken + 1, vEndEntityIndex + 1);
					EntityType vEntityType = mEntityMarkSet.get(vTokenList[cToken]);

					for(Label cLabel : Parameters.DataHandler.mLabelEncoding.getLabels(vEntity)) {
						vSequence.add(vTokenList[++cToken], (vEntityType.ordinal() * EntityType.values().length) + cLabel.ordinal());
					}
					cToken++;
				}

			} else {
				vSequence.add(vTokenList[cToken], Parameters.DataHandler.mLabelEncoding.getOutsideLabel().ordinal());
			}
		}

		return vSequence;
	}

	private int getEndEntityIndex(String [] pTokenList, int pStartEntityIndex) {
		int vEndEntityIndex = -1;

		for(int cToken = pStartEntityIndex; cToken < pTokenList.length; cToken++) {
			if(pTokenList[cToken].equals(Symbol.SQUARE_BRACKET_RIGHT)) {
				vEndEntityIndex = cToken - 1;
				break;
			}
		}

		return vEndEntityIndex;
	}

	@Override
	public void writeSequenceToFile(Writer pOutput, ISequence pSequence) throws IOException {

		List<Entity> vEntityList = Parameters.DataHandler.mLabelEncoding.getEntities(pSequence);

		for(int i = 0; i < pSequence.length(); i++) {
			if(vEntityList.size() > 0 && vEntityList.get(0).getIndex() == i) {

				Entity vEntity = vEntityList.get(0);

				pOutput.write(((i != 0)? Symbol.SPACE : Symbol.EMPTY) + Symbol.SQUARE_BRACKET_LEFT
						+ vEntity.getEntityType().getValue() + Symbol.SPACE
						+ vEntity.getValue() + Symbol.SQUARE_BRACKET_RIGHT);

				i += vEntity.getValue().split(Symbol.SPACE).length - 1;
				vEntityList.remove(vEntity);
			} else {
				pOutput.write(((i != 0)? Symbol.SPACE : Symbol.EMPTY) + pSequence.getToken(i));
			}
		}

		pOutput.write(Symbol.NEW_LINE);
	}

	@Override
	protected SequenceSet getSegmentSequenceFromFile(BufferedReader pReader,
			FileType pFileType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeSegmentSequenceToFile(Writer pWriter,
			SequenceSegment pSequence, boolean pIsSegment) {
		// TODO Auto-generated method stub

	}
}
