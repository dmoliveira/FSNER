package lbd.FSNER;


import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;
import lbd.data.handler.Sequence;
import lbd.fsner.entity.Entity;

public class FSNERModusOperandi {

	private enum EntityType {PER};//, ORG, LOC, EVT, MISC};

	private FSNER mFSNER;
	private String mFSNERModelFile;

	public static void main(String [] args) {

		String vFSNERModelFilenamePattern = Parameters.Directory.mModels + "FSNER-Model-Zunnit-GloboExtraCollection-All-{0}-CV1.fsbin";
		String [] vSentences = {"Romário rebate Leandro Amaral e Renato Gaúcho e abre crise no Vasco",
				"Os deputados Wagner Montes e Luiz Paulo do PSDB também participaram da audiência",
				"Copacabana com 26% Laranjeiras com 24% e Flamengo com 19% todos na Zona Sul estão logo em seguida no ranking",
				"O cenário já foi visto em 2009 na Copa das Confederações",
		"Quem é cotista do FGTS tem redução automática de 0.5% na taxa de juros"};

		for(EntityType cEntityType : EntityType.values()) {

			String vFSNERModel = MessageFormat.format(vFSNERModelFilenamePattern,cEntityType.name());

			// Initialize the method
			FSNERModusOperandi vFSNERPER = new FSNERModusOperandi(vFSNERModel);
			List<String> vEntityList = vFSNERPER.recognize(vSentences[cEntityType.ordinal()]);

			// Print only the first entity for example.
			System.out.print(cEntityType.name() + ": ");
			if(vEntityList.size() > 0) {
				Iterator<String> vIter = vEntityList.iterator();
				while (vIter.hasNext()) {
					System.out.print(vIter.next() + ((vIter.hasNext()) ? ", " : ""));
				}
				System.out.println();
			} else {
				System.out.println("None.");
			}
		}
	}

	public FSNERModusOperandi(String pFSNERModelFile) {
		mFSNERModelFile = pFSNERModelFile;
		mFSNER = FSNER.loadObject(mFSNERModelFile);
	}

	public List<String> recognize(String pMessage) {
		String vEncodedMessage = pMessage;
		try {
			vEncodedMessage = new String(vEncodedMessage.getBytes(), Parameters.DataHandler.mDataEncoding);
		} catch (UnsupportedEncodingException pException) {
			pException.printStackTrace();
		}

		ISequence labeledSequence = mFSNER.labelSequence(new Sequence(vEncodedMessage.split(Symbol.SPACE)));
		return GetEntities(labeledSequence);
	}

	//Has same code in FS-NER WebService
	public static List<String> GetEntities(ISequence pLabeledSequence) {

		List<String> vEntityList = new ArrayList<String>();

		for(Entity cEntity : Parameters.DataHandler.mLabelEncoding.getEntities(pLabeledSequence)) {
			vEntityList.add(cEntity.getValue());
		}

		return vEntityList;
	}
}
