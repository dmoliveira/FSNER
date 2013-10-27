package com.lbd.test.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Evaluation.SimpleEvaluator;
import lbd.FSNER.Utils.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleEvaluationTest {

	//-- Variables for the Test
	protected static SimpleEvaluator mSimpleEvaluator;
	protected static String mTaggedFile;
	protected static String mGoldFile;

	@BeforeClass
	public static void initVariables() {
		mSimpleEvaluator = new SimpleEvaluator();
		createFiles();
	}

	private static void createFiles() {
		mTaggedFile = Parameters.Directory.mSaveTemp + "TaggedFile.eval";
		mGoldFile = Parameters.Directory.mSaveTemp + "GoldFile.eval";
		createTaggedFile(mTaggedFile);
		createGoldFile(mGoldFile);
	}

	private static void createTaggedFile(String pTaggedFile) {
		try {

			BufferedWriter vTaggedWriter = FileUtils.createBufferedWriter(pTaggedFile, null);

			vTaggedWriter.write("O [ORG Cruzeiro Esporte] Clube é campeão Brasileiro de 2013.\n");
			vTaggedWriter.write("A presidenta [PER Dilma] Roussef não está satisfeita com as ações do Governo [LOC Americano].\n");
			vTaggedWriter.write("[PER Diego M.] [PER de Oliveira], que é Funcionário da [ORG Zunnit] [MISC Technologies], " +
					"localizada em [LOC Belo] [LOC Horizonte] - [LOC MG], [LOC Brasil], utiliza-se " +
					"de [MISC Aprendizado de Máquina] e [MISC Recuperação de Informação] para " +
					"criar novos produtos inovadores.\n");
			vTaggedWriter.write("A cidade de [LOC Seattle nos Estados Unidos] está próxima a [LOC Vancouver] " +
					" no [LOC Canadá] e contêm empresas como [ORG Microsoft] e [ORG Nintendo].\n");

			vTaggedWriter.flush();
			vTaggedWriter.close();

		} catch (UnsupportedEncodingException pException) {
			pException.printStackTrace();
		} catch (FileNotFoundException pException) {
			pException.printStackTrace();
		} catch (IOException pException) {
			pException.printStackTrace();
		}
	}

	private static void createGoldFile(String pTestFile) {
		try {

			BufferedWriter vTestWriter = FileUtils.createBufferedWriter(pTestFile, null);

			vTestWriter.write("O [ORG Cruzeiro Esporte Clube] é campeão Brasileiro de 2013.\n");
			vTestWriter.write("A presidenta [PER Dilma Roussef] não está satisfeita com as ações do [ORG Governo Americano].\n");
			vTestWriter.write("[PER Diego M. de Oliveira], que é Funcionário da [ORG Zunnit Technologies], " +
					"localizada em [LOC Belo Horizonte] - [LOC MG], [LOC Brasil], utiliza-se " +
					"de [MISC Aprendizado de Máquina] e [MISC Recuperação de Informação] para " +
					"criar novos produtos inovadores.\n");
			vTestWriter.write("A cidade de [LOC Seattle] nos [LOC Estados Unidos] está próxima a [LOC Vancouver] " +
					"no [LOC Canadá] e contêm empresas como [ORG Microsoft] e [ORG Nintendo].\n");

			vTestWriter.flush();
			vTestWriter.close();

		} catch (UnsupportedEncodingException pException) {
			pException.printStackTrace();
		} catch (FileNotFoundException pException) {
			pException.printStackTrace();
		} catch (IOException pException) {
			pException.printStackTrace();
		}
	}

	@AfterClass
	public static void destroyFiles() {
		File vTagged = new File(mTaggedFile);
		File vGold = new File(mGoldFile);

		if(vTagged.exists()) {
			vTagged.delete();
		}

		if(vGold.exists()) {
			vGold.delete();
		}
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void differentNumberOfLinesTest() {
		try {
			String vXFile = "x.test_";
			String vYFile = "y.test_";

			BufferedWriter vTaggedWriter = FileUtils.createBufferedWriter(vXFile, null);
			vTaggedWriter.write("x\n");
			vTaggedWriter.flush();
			vTaggedWriter.close();

			BufferedWriter vTestWriter = FileUtils.createBufferedWriter(vYFile, null);
			vTestWriter.write("x\n");
			vTestWriter.write("x\n");
			vTestWriter.flush();
			vTestWriter.close();

			mSimpleEvaluator.evaluate(vXFile, vYFile);

			new File(vXFile).delete();
			new File(vYFile).delete();

		} catch (UnsupportedEncodingException pException) {
			pException.printStackTrace();
		} catch (FileNotFoundException pException) {
			pException.printStackTrace();
		} catch (IOException pException) {
			pException.printStackTrace();
		}
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void differentNumberOfTokensInSequenceTest() {
		try {
			String vXFile = "x.test_";
			String vYFile = "y.test_";

			BufferedWriter vTaggedWriter = FileUtils.createBufferedWriter(vXFile, null);
			vTaggedWriter.write("x\n");
			vTaggedWriter.flush();
			vTaggedWriter.close();

			BufferedWriter vTestWriter = FileUtils.createBufferedWriter(vYFile, null);
			vTestWriter.write("x x\n");
			vTestWriter.flush();
			vTestWriter.close();

			mSimpleEvaluator.evaluate(vXFile, vYFile);

			new File(vXFile).delete();
			new File(vYFile).delete();

		} catch (UnsupportedEncodingException pException) {
			pException.printStackTrace();
		} catch (FileNotFoundException pException) {
			pException.printStackTrace();
		} catch (IOException pException) {
			pException.printStackTrace();
		}
	}

	@Test
	public void evaluationTest() {
		mSimpleEvaluator.evaluate(mTaggedFile, mGoldFile);
	}
}
