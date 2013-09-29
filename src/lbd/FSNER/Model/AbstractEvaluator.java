package lbd.FSNER.Model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

import lbd.FSNER.Configuration.Constants;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Evaluation.Component.Statistics;
import lbd.FSNER.Evaluation.Component.TermLabeled;
import lbd.FSNER.Utils.FileUtils;
import lbd.FSNER.Utils.FormatUtils;

public abstract class AbstractEvaluator implements Serializable {

	private static final long serialVersionUID = 1L;
	protected ArrayList<String> mTaggedFileList;
	protected ArrayList<String> mTestFileList;

	protected transient BufferedReader mCurrentTaggedInput;
	protected transient BufferedReader mCurrentTestInput;

	protected ArrayList<Statistics> mStatisticalFileList;
	protected Statistics mCurrentStatisticalFile;

	protected transient Writer mOutputFile;

	protected double mAVGPrecision;
	protected double mAVGRecall;
	protected double mAVGF1;

	protected double mSTDDevPrecision;
	protected double mSTDDevRecall;
	protected double mSTDDevF1;

	public enum OutputStyle {Plain, Latex, FilterModelHit}
	protected OutputStyle outputStyle;

	public AbstractEvaluator(ArrayList<String> pFilenameList, OutputStyle outputStyle) {
		mTaggedFileList = new ArrayList<String>();
		mTestFileList = new ArrayList<String>();

		mStatisticalFileList = new ArrayList<Statistics>();

		this.outputStyle = outputStyle;

		initializeOutputFile(pFilenameList);
		initializeAVGAndSTDDevVariables();
	}

	protected void initializeOutputFile(ArrayList<String> pFilenameList) {
		try {
			if(Debug.Evaluator.isToWriteStatistics) {
				mOutputFile = FileUtils.createOutputStreamWriter(
						FileUtils.createCommonFilename(pFilenameList),
						Constants.FileExtention.stats);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void evaluate(String pTaggedFilenameAddress, String pTestFilenameAddress, String pObservation) {

		try {
			prepareEvaluationVariables(pTaggedFilenameAddress,
					pTestFilenameAddress);

			beforeEvaluate(pTaggedFilenameAddress, pTestFilenameAddress);

			evaluate(mCurrentTaggedInput, mCurrentTestInput);

			afterEvaluate(pTaggedFilenameAddress, pTestFilenameAddress);

			mCurrentTaggedInput.close();
			mCurrentTestInput.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void prepareEvaluationVariables(String pTaggedFilenameAddress,
			String pTestFilenameAddress) throws UnsupportedEncodingException,
			FileNotFoundException {
		mCurrentTaggedInput = new BufferedReader(new InputStreamReader(
				new FileInputStream(pTaggedFilenameAddress), Parameters.dataEncoding));

		mCurrentTestInput = new BufferedReader(new InputStreamReader(
				new FileInputStream(pTestFilenameAddress), Parameters.dataEncoding));

		mTaggedFileList.add(pTaggedFilenameAddress);
		mTestFileList.add(pTestFilenameAddress);

		mStatisticalFileList.add(new Statistics());
		mCurrentStatisticalFile = mStatisticalFileList.get(mStatisticalFileList.size() - 1);
	}

	protected void beforeEvaluate(String pTaggedFilenameAddress, String pTestFilenameAddress) throws IOException {
		//Implement in sub-class when necessary.
	}

	protected void evaluate(BufferedReader pTaggedInput, BufferedReader pTestInput) throws IOException {
		String vTaggedLine;
		String vTestLine;

		TermLabeled vTaggedTerm;
		TermLabeled vTestTerm;

		int cLine = 0;

		while((vTaggedLine = pTaggedInput.readLine()) != null) {

			vTestLine = pTestInput.readLine();
			cLine++;

			if(!vTaggedLine.isEmpty()) {

				vTaggedTerm = getTerm(vTaggedLine);
				vTestTerm = getTerm(vTestLine);

				evaluateTerm(vTaggedTerm, vTestTerm, cLine);
			}
		}
		mCurrentStatisticalFile.calculateStatistics();
		writeAndPrintResults(mOutputFile, getCurrentPlainResults(),
				Debug.Evaluator.isToWriteParcialStatistics,
				Debug.Evaluator.isToPrintParcialStatistics);
	}

	protected void afterEvaluate(String pTaggedFilenameAddress, String pTestFilenameAddress)  throws IOException {
		//Implement in sub-class when necessary.
	}

	protected abstract void evaluateTerm(TermLabeled pTaggedTerm, TermLabeled pTestTerm, int pLineNumber);

	protected TermLabeled getTerm(String pLine) {
		TermLabeled vLabeledTerm = null;
		String [] pLineElement;

		pLineElement = pLine.split("\\|");
		vLabeledTerm = new TermLabeled(pLineElement[0], pLineElement[1]);

		return(vLabeledTerm);
	}

	protected void calculateOverallStatistics() {
		initializeAVGAndSTDDevVariables();
		int vNumberOfValidStatisticalFiles = 0;

		for(Statistics cStatistics : mStatisticalFileList) {
			mAVGPrecision += cStatistics.getPrecision();
			mAVGRecall += cStatistics.getRecall();
			mAVGF1 += cStatistics.getF1();

			if(!cStatistics.isInvalid()) {
				vNumberOfValidStatisticalFiles++;
			}
		}

		mAVGPrecision /= vNumberOfValidStatisticalFiles;
		mAVGRecall /= vNumberOfValidStatisticalFiles;
		mAVGF1 /= vNumberOfValidStatisticalFiles;

		for(Statistics cStatistics : mStatisticalFileList) {
			mSTDDevPrecision += Math.pow(cStatistics.getPrecision() - mAVGPrecision, 2);
			mSTDDevRecall += Math.pow(cStatistics.getRecall() - mAVGRecall, 2);
			mSTDDevF1 += Math.pow(cStatistics.getF1() - mAVGF1, 2);
		}

		mSTDDevPrecision = Math.sqrt(mSTDDevPrecision/vNumberOfValidStatisticalFiles);
		mSTDDevRecall = Math.sqrt(mSTDDevRecall/vNumberOfValidStatisticalFiles);
		mSTDDevF1 = Math.sqrt(mSTDDevF1/vNumberOfValidStatisticalFiles);
	}

	protected void initializeAVGAndSTDDevVariables() {
		mAVGPrecision = 0;
		mAVGRecall = 0;
		mAVGF1 = 0;

		mSTDDevPrecision = 0;
		mSTDDevRecall = 0;
		mSTDDevF1 = 0;
	}

	public void writeOverviewStatistics(String pOutputFilenameAddress, String pObservation) {
		if(mOutputFile != null) {
			try {
				mOutputFile.flush();
				mOutputFile.close();

				mOutputFile = FileUtils.createOutputStreamWriter(
						pOutputFilenameAddress, Constants.FileExtention.stats);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	public void writeOverviewStatistics(String pObservation) {

		if(mTestFileList.size() > 0) {
			if(mTestFileList.size() > 1) {
				calculateOverallStatistics();
			}

			try {
				if(mOutputFile == null && Debug.Evaluator.isToWriteStatistics) {
					mOutputFile = FileUtils.createOutputStreamWriter(
							FileUtils.createCommonFilename(mTestFileList),
							Constants.FileExtention.stats);
				}

				if(outputStyle == OutputStyle.Plain) {
					writeOverviewPlainStatistics(mOutputFile, pObservation);
				} else if(outputStyle == OutputStyle.Latex) {
					writeOverviewLatexStatistics(mOutputFile, pObservation);
				}

				if(mOutputFile != null && Debug.Evaluator.isToWriteStatistics) {
					mOutputFile.flush();
					mOutputFile.close();
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void writeOverviewPlainStatistics(Writer pOutputFile, String pObservation)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		if(mTestFileList.size() > 0) {
			String vResult = "-- Overview Results [P/R/F1]\n";
			vResult += "avg\t" + mAVGPrecision + "\t" + mAVGRecall + "\t" + mAVGF1 + "\n";
			vResult += "stdev\t" + mSTDDevPrecision + "\t" + mSTDDevRecall + "\t" + mSTDDevF1 + "\n";

			writeAndPrintResults(pOutputFile, vResult,
					Debug.Evaluator.isToWriteStatistics, Debug.Evaluator.isToPrintStatistics);
		}
	}

	protected void writeOverviewLatexStatistics(Writer pOutputFile, String pObservation)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		writeAndPrintResults(pOutputFile, getOverviewResultInLatexFormat(),
				Debug.Evaluator.isToWriteStatistics, Debug.Evaluator.isToPrintStatistics);
	}

	public String getCurrentPlainResults() {
		return mCurrentStatisticalFile.getPrecision() + "\t" +
				mCurrentStatisticalFile.getRecall() + "\t" + mCurrentStatisticalFile.getF1();
	}

	private String getOverviewResultInLatexFormat() {
		String vOutput = FormatUtils.toDecimal(mAVGPrecision, 4) + "\\pm" + FormatUtils.toDecimal(mSTDDevPrecision) + " & ";
		vOutput += FormatUtils.toDecimal(mAVGRecall, 4) + "\\pm" + FormatUtils.toDecimal(mSTDDevRecall) + " & ";
		vOutput += FormatUtils.toDecimal(mAVGF1, 4) + "\\pm" + FormatUtils.toDecimal(mSTDDevF1);

		return vOutput;
	}

	public void clear() {
		mTaggedFileList = new ArrayList<String>();
		mTestFileList = new ArrayList<String>();

		mStatisticalFileList = new ArrayList<Statistics>();

		mOutputFile = null;
		initializeAVGAndSTDDevVariables();
	}

	protected void writeAndPrintResults(Writer pOutputFile, String pMessage,
			boolean pIsToWrite, boolean pIsToPrint) throws IOException {
		if(pOutputFile != null && pIsToWrite) {
			pOutputFile.write(pMessage + "\n");
		}


		if(pIsToPrint) {
			System.out.println(pMessage);
		}
	}
}
