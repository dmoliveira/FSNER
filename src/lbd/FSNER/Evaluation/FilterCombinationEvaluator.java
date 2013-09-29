package lbd.FSNER.Evaluation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;

import lbd.FSNER.Configuration.Constants;
import lbd.FSNER.Evaluation.Component.TermLabeled;
import lbd.FSNER.Model.AbstractEvaluator;
import lbd.FSNER.Utils.FileUtils;
import lbd.FSNER.Utils.Symbol;
import lbd.FSNER.Utils.Annotations.Comment;
import lbd.FSNER.Utils.Collections.Pair;
import lbd.Utils.Utils;

public class FilterCombinationEvaluator extends AbstractEvaluator {

	private static final long serialVersionUID = 1L;
	protected ArrayList<ArrayList<Pair<String, Byte>>> mFileFilterHitList;
	protected ArrayList<Pair<String, Byte>> mMessageAndFilterHitList;

	public FilterCombinationEvaluator(ArrayList<String> pFilenameList, OutputStyle outputStyle) {
		super(pFilenameList, OutputStyle.FilterModelHit);
		mFileFilterHitList = new ArrayList<ArrayList<Pair<String,Byte>>>();
	}

	@Override
	protected void prepareEvaluationVariables(String pTaggedFilenameAddress,
			String pTestFilenameAddress) throws UnsupportedEncodingException,
			FileNotFoundException {
		super.prepareEvaluationVariables(pTaggedFilenameAddress, pTestFilenameAddress);

		mMessageAndFilterHitList = new ArrayList<Pair<String,Byte>>();
		mFileFilterHitList.add(mMessageAndFilterHitList);
	}

	@Override
	protected void evaluate(BufferedReader pTaggedInput, BufferedReader pTestInput) throws IOException {
		String vTaggedLine;
		String vTestLine;

		String vObservedMessage = Symbol.EMPTY;

		TermLabeled vTaggedTerm;
		TermLabeled vTestTerm;

		int cTermTaggedCorrectly = 0;
		int cTotalTermTagged = 0;

		while((vTaggedLine = pTaggedInput.readLine()) != null) {

			vTestLine = pTestInput.readLine();

			if(!vTaggedLine.isEmpty()) {

				vTaggedTerm = getTerm(vTaggedLine);
				vTestTerm = getTerm(vTestLine);
				vObservedMessage += vTaggedTerm.getTerm() + Symbol.SPACE;

				//TODO: Comentario - Mudei a estratégia. Ao invés de atribuir o filtro se for a entidade
				// correta, será analisado se o filtro acerta corretamente toda a mensagem.
				// Caso contrário, a precisão seria bem alta para todos os filtros e atrapalharia
				// o uso do LAC.
				//if(LabelEncoding.BILOU.isEntity(vTestTerm.getLabel())) {
				if(vTaggedTerm.getLabel().equalsIgnoreCase(vTestTerm.getLabel())) {
					cTermTaggedCorrectly++;
				}
				cTotalTermTagged++;
				//}
			} else {
				addMessageAndFilterHitValue(vObservedMessage.trim(), cTermTaggedCorrectly, cTotalTermTagged);

				cTermTaggedCorrectly = 0;
				cTotalTermTagged = 0;
				vObservedMessage = Symbol.EMPTY;
			}
		}
		mCurrentStatisticalFile.calculateStatistics();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void addMessageAndFilterHitValue(String pObservedMessage, int pTaggedCorrectly,
			int pTotalTagged) {
		if(pTotalTagged != 0) {
			if(pTaggedCorrectly == pTotalTagged) {
				mMessageAndFilterHitList.add(new Pair(pObservedMessage, 1));
			} else {
				mMessageAndFilterHitList.add(new Pair(pObservedMessage, 0));
			}
		}
	}

	@Override
	public void writeOverviewStatistics(String pObservation) {
		try {
			Writer vOutputFile = FileUtils.createOutputStreamWriter(
					FileUtils.createCommonFilename(mTestFileList),
					Constants.FileExtention.filterCombination);

			int cFile = 0;
			for(ArrayList<Pair<String, Byte>> cFilterHitList : mFileFilterHitList) {
				Utils.printlnLog(MessageFormat.format("\n-- File {0}:", mTestFileList.get(cFile++)), vOutputFile);

				for(Pair cFilterCombinationHit : cFilterHitList) {
					vOutputFile.write(MessageFormat.format("{0}{1}{2}\n",
							cFilterCombinationHit.getLeft(),
							Symbol.SPECIAL_DELIMITER,
							cFilterCombinationHit.getRight()));
				}
			}

			vOutputFile.flush();
			vOutputFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	@Deprecated
	@Comment(message="This method do nothing.")
	protected void evaluateTerm(TermLabeled pTaggedTerm, TermLabeled pTestTerm,
			int pLineNumber) {
		//Do nothing.
	}



}
