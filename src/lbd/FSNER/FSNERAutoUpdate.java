package lbd.FSNER;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractNERModel;
import lbd.FSNER.Utils.FormatUtils;
import lbd.FSNER.Utils.SimpleStopWatch;
import lbd.FSNER.Utils.Symbol;
import lbd.TSE.TSEngine;
import lbd.TSE.TSEngineControl;
import lbd.Utils.SoundToClass;

public class FSNERAutoUpdate extends FSNER {

	private static final long serialVersionUID = 1L;

	@Override
	protected void runFSF(String trainFile, String testFile,
			String referenceDataFile, String pTermListRestrictionFile) {
		super.runFSF(trainFile, testFile, referenceDataFile,
				pTermListRestrictionFile);
	}

	@Override
	protected void updateModel(AbstractNERModel pNERTagger, String pTopicFile,
			String pReferenceDataFile, String pTestFile, UpdateType pUpdateType) {
		// updateModel(nerTagger, topicFile, testFile, UpdateType.Stream);
		// updateModel(nerTagger, referenceDataFile, testFile,
		// UpdateType.ReferenceData);
		// updateModel(nerTagger, referenceDataFile, testFile,
		// UpdateType.Output);
	}

	public void updateModel(AbstractNERModel nerTagger, String updateFile,
			String testFile, UpdateType updateType) {

		// -- Use Reference Data Update
		if (updateType == UpdateType.ReferenceData) {
			updateWithReferenceData(nerTagger, updateFile, testFile);
		}

		// -- Use Stream Update
		if (updateType == UpdateType.Stream) {
			updateWithStream(nerTagger, updateFile, testFile);
		}

		if (updateType == UpdateType.LabeledFile) {
			updateWithLabeledFile(nerTagger, updateFile, testFile);
		}

		// -- Use Labeled Data as Update
		if (updateType == UpdateType.Output) {
			updateWithOutput(nerTagger, testFile);
		}

		// System.out.println();
	}

	public void updateWithReferenceData(AbstractNERModel nerTagger,
			String dataReferenceFile, String testFile) {

		int updateNumber = 1;

		// -- Do Update
		// do {
		nerTagger.labelFile(dataReferenceFile);
		System.out.print(nerTagger.getUpdateControl().getUpdateListSize()
				+ "\t");
		nerTagger.update("Reference Data(" + updateNumber++ + ")");
		nerTagger.labelFile(testFile);
		System.out.print(FormatUtils.toDecimal(0) + "\t");
		nerTagger.evaluate(nerTagger.getTaggedFilenameAddress(), testFile,
				"(Label)");
		// }while(nerTagger.hasSequenceToUpdate());
	}

	public void updateWithStream(AbstractNERModel nerTagger, String updateFile,
			String testFile) {

		SimpleStopWatch stopWatch = new SimpleStopWatch();
		stopWatch.start();

		// -- get Tweet stream
		List<List<String>> streamList = new ArrayList<List<String>>();

		try {

			Writer out = new OutputStreamWriter(new FileOutputStream(
					"./Team-StreamTweet.data"), Parameters.dataEncoding);

			// ArrayList<String> topicList = getTopicList(updateFile);
			streamList.addAll(getTweetStream(out, nerTagger,
					nerTagger.getEntityList(), 2, Symbol.EMPTY,
					TSEngine.MAX_RESULT_ALLOWED, "pt", "recent"));

			out.flush();
			out.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// -- Do Update
		nerTagger.labelStream(streamList, false);
		nerTagger.update("Tweet Stream");

		stopWatch.show("Stream Update Time:");
		System.out.println();

		nerTagger.labelFile(testFile);
		// Call proper evaluator Evaluator.evaluate("Final Results after Stream Update: ", "", nerTagger.getTaggedFilenameAddress(), testFile);
	}

	protected List<List<String>> getTweetStream(Writer out,
			AbstractNERModel nerTagger, List<String> termList,
			int maxThreadNumber, String topicTerm, int maxResults,
			String language, String resultType) {

		List<List<String>> streamList = new ArrayList<List<String>>();
		TSEngineControl tSEngineControl = new TSEngineControl(maxThreadNumber);

		int termNumber = 1;
		String updateMessage;

		System.out.println("Looking for a total of (" + termList.size()
				+ ") terms in tweets");

		for (String term : termList) {

			tSEngineControl.waitUntilCanAddMore();
			tSEngineControl.add(new TSEngine());

			updateMessage = "\t("
					+ new DecimalFormat("#.##")
			.format(100.0 * ((termNumber++) / (double) termList
					.size())) + "%)"
					+ "\t...looked for tweets of " + term;

			tSEngineControl.getLastTSEngine().executeQuery(
					out,
					term
					+ ((!topicTerm.isEmpty()) ? Symbol.SPACE
							+ topicTerm : Symbol.EMPTY), maxResults,
							language, resultType, streamList, updateMessage);
		}

		tSEngineControl.waitUntilAllFinish();

		return (streamList);
	}

	public void updateWithLabeledFile(AbstractNERModel nerTagger,
			String labeledFile, String testFile) {

		// -- Do Update
		nerTagger.updateWithLabeledFile(labeledFile);

		nerTagger.labelFile(testFile);
		// TODO: Call proper evaluator
		// Evaluator.evaluate("Final Results after LabeledFile Update: ", "",
		// nerTagger.getTaggedFilenameAddress(), testFile);
	}

	public void updateWithOutput(AbstractNERModel nerTagger, String testFile) {

		int updateNumber = 1;

		do {
			nerTagger.update("(Auto-Update(" + updateNumber++ + "))");
			nerTagger.labelFile(testFile);
			nerTagger.evaluate(nerTagger.getTaggedFilenameAddress(), testFile,
					"(Label)");
		} while (nerTagger.hasSequenceToUpdate());
	}

	public ArrayList<String> getTopicList(String topicFile) {

		ArrayList<String> topicList = new ArrayList<String>();

		String line;

		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(topicFile), Parameters.dataEncoding));

			while ((line = in.readLine()) != null) {
				topicList.add(line);
			}

			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return (topicList);
	}

	public static void playSound(String soundFile) {
		SoundToClass.play(new String[] { soundFile });
	}
}
