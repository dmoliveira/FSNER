package lbd.FSNER;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import lbd.FSNER.ActivityControl.ParallelActivityControl;
import lbd.FSNER.ActivityControl.SimpleActivityControl;
import lbd.FSNER.Collection.CollectionDefinition;
import lbd.FSNER.Collection.CollectionDefinition.CollectionName;
import lbd.FSNER.Collection.DataCollection;
import lbd.FSNER.Configuration.Constants;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Configuration.FilterParameters;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Evaluation.AmbiguityEvaluator;
import lbd.FSNER.Evaluation.GeneralizationEvaluator;
import lbd.FSNER.Evaluation.LearningEvaluator;
import lbd.FSNER.Evaluation.SimpleBILOUEvaluator;
import lbd.FSNER.FSF.LabelFile.SimpleLabelFile;
import lbd.FSNER.Filter.FtrSingleTermDictionary4;
import lbd.FSNER.LabelFile.LabelCalculatorModel.LCMAndScore;
import lbd.FSNER.LabelFile.LabelCalculatorModel.LCMNewOrScore;
import lbd.FSNER.LabelFile.LabelCalculatorModel.LCMOrScore;
import lbd.FSNER.LabelFile.LabelCalculatorModel.LCMSimpleAndScore;
import lbd.FSNER.LabelFile.LabelCalculatorModel.LCMSimpleSumScore;
import lbd.FSNER.LabelFile.LabelCalculatorModel.LCMSumScore;
import lbd.FSNER.Model.AbstractActivityControl;
import lbd.FSNER.Model.AbstractEvaluator;
import lbd.FSNER.Model.AbstractEvaluator.OutputStyle;
import lbd.FSNER.Model.AbstractLabelFile;
import lbd.FSNER.Model.AbstractLabelFileLabelCalculatorModel;
import lbd.FSNER.Model.AbstractNERModel;
import lbd.FSNER.Model.AbstractTermRestrictionChecker;
import lbd.FSNER.Model.AbstractUpdateControl;
import lbd.FSNER.NERModel.SimpleNERModel;
import lbd.FSNER.TermRestrictionChecker.SimpleTermRestrictionChecker;
import lbd.FSNER.UpdateControl.SimpleUpdateControl;
import lbd.FSNER.Utils.FileUtils;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.LabelEncoding.EncodingType;
import lbd.FSNER.Utils.SimpleStopWatch;
import lbd.FSNER.Utils.Symbol;
import lbd.Utils.SoundToClass;
import lbd.Utils.songStorms;
import lbd.data.handler.DataSequence;

public class FSNER implements Serializable {

	private static final long serialVersionUID = 1L;

	protected enum UpdateType {Output, ReferenceData, Stream, LabeledFile, None};
	protected boolean mShowElapsedTime = false;

	protected static OutputStyle sOutputStyle;

	protected CollectionDefinition mCollectionDefinition;
	protected FilterParameters mFilterParameters;

	protected AbstractNERModel mNERModel;

	protected AbstractEvaluator mEvaluator;
	protected AbstractEvaluator mFilterCombinationEvaluator;
	protected AmbiguityEvaluator mAmbiguityEvaluator;
	protected GeneralizationEvaluator mGeneralizationEvaluator;
	protected LearningEvaluator mLearningEvaluator;

	public static void main(String [] args) {

		ArrayList<FilterParameters> vFilterParametersList = FilterParameters.loadFilterConfiguration(
				FileUtils.getListOfOnlyFiles(Parameters.Directory.filterConfiguration).get(0));

		for(FilterParameters cFilterParameters : vFilterParametersList) {
			System.out.println("-- FilterConfiguration: " + cFilterParameters);
			//Save.optionalDirectory = "FilterCombinationHitTraining/" + cFilterParameters.toString();'
			/*CollectionName [] vSubcollection = {CollectionName.Zunnit_Extra_Casa_PER,
					CollectionName.Zunnit_Extra_Casa_ORG, CollectionName.Zunnit_Extra_Casa_LOC,
					CollectionName.Zunnit_Extra_Casa_EVT, CollectionName.Zunnit_Extra_Casa_MISC,
					CollectionName.Zunnit_Extra_Casos_de_Policia_PER,
					CollectionName.Zunnit_Extra_Casos_de_Policia_ORG, CollectionName.Zunnit_Extra_Casos_de_Policia_LOC,
					CollectionName.Zunnit_Extra_Casos_de_Policia_EVT, CollectionName.Zunnit_Extra_Casos_de_Policia_MISC,
					CollectionName.Zunnit_Extra_Emprego_PER,
					CollectionName.Zunnit_Extra_Emprego_ORG, CollectionName.Zunnit_Extra_Emprego_LOC,
					CollectionName.Zunnit_Extra_Emprego_EVT, CollectionName.Zunnit_Extra_Emprego_MISC,
					CollectionName.Zunnit_Extra_Esportes_PER,
					CollectionName.Zunnit_Extra_Esportes_ORG, CollectionName.Zunnit_Extra_Esportes_LOC,
					CollectionName.Zunnit_Extra_Esportes_EVT, CollectionName.Zunnit_Extra_Esportes_MISC,
					CollectionName.Zunnit_Extra_Famosos_PER,
					CollectionName.Zunnit_Extra_Famosos_ORG, CollectionName.Zunnit_Extra_Famosos_LOC,
					CollectionName.Zunnit_Extra_Famosos_EVT, CollectionName.Zunnit_Extra_Famosos_MISC,
					CollectionName.Zunnit_Extra_Noticia_PER,
					CollectionName.Zunnit_Extra_Noticia_ORG, CollectionName.Zunnit_Extra_Noticia_LOC,
					CollectionName.Zunnit_Extra_Noticia_EVT, CollectionName.Zunnit_Extra_Noticia_MISC,
					CollectionName.Zunnit_Extra_TV_e_Lazer_PER,
					CollectionName.Zunnit_Extra_TV_e_Lazer_ORG, CollectionName.Zunnit_Extra_TV_e_Lazer_LOC,
					CollectionName.Zunnit_Extra_TV_e_Lazer_EVT, CollectionName.Zunnit_Extra_TV_e_Lazer_MISC};*/

			/*CollectionName [] vSubcollection = {
					CollectionName.Zunnit_Extra_Noticia_PER,
					CollectionName.Zunnit_Extra_Noticia_ORG, CollectionName.Zunnit_Extra_Noticia_LOC,
					CollectionName.Zunnit_Extra_Noticia_EVT};*/

			/*CollectionName [] vSubcollection = {CollectionName.PER_MSM13_CONQUEST, CollectionName.ORG_MSM13_CONQUEST, CollectionName.LOC_MSM13_CONQUEST, CollectionName.MISC_MSM13_CONQUEST};*/
			/*CollectionName [] vSubcollection = {
					CollectionName.PlayerCV, CollectionName.VenueCV, CollectionName.TeamCV,
					CollectionName.CompanyCV, CollectionName.GeolocCV, CollectionName.PersonCV,
					CollectionName.OrganizationCV, CollectionName.PER_MSM13_V15_PREPROCESSED_CV,
					CollectionName.ORG_MSM13_V15_PREPROCESSED_CV, CollectionName.LOC_MSM13_V15_PREPROCESSED_CV,
					CollectionName.MISC_MSM13_V15_PREPROCESSED_CV,
					CollectionName.Zunnit_Extra_Casa_PER,
					CollectionName.Zunnit_Extra_Casa_ORG, CollectionName.Zunnit_Extra_Casa_LOC,
					CollectionName.Zunnit_Extra_Casa_EVT, CollectionName.Zunnit_Extra_Casa_MISC,
					CollectionName.Zunnit_Extra_Casos_de_Policia_PER,
					CollectionName.Zunnit_Extra_Casos_de_Policia_ORG, CollectionName.Zunnit_Extra_Casos_de_Policia_LOC,
					CollectionName.Zunnit_Extra_Casos_de_Policia_EVT, CollectionName.Zunnit_Extra_Casos_de_Policia_MISC,
					CollectionName.Zunnit_Extra_Emprego_PER,
					CollectionName.Zunnit_Extra_Emprego_ORG, CollectionName.Zunnit_Extra_Emprego_LOC,
					CollectionName.Zunnit_Extra_Emprego_EVT, CollectionName.Zunnit_Extra_Emprego_MISC,
					CollectionName.Zunnit_Extra_Esportes_PER,
					CollectionName.Zunnit_Extra_Esportes_ORG, CollectionName.Zunnit_Extra_Esportes_LOC,
					CollectionName.Zunnit_Extra_Esportes_EVT, CollectionName.Zunnit_Extra_Esportes_MISC,
					CollectionName.Zunnit_Extra_Famosos_PER,
					CollectionName.Zunnit_Extra_Famosos_ORG, CollectionName.Zunnit_Extra_Famosos_LOC,
					CollectionName.Zunnit_Extra_Famosos_EVT, CollectionName.Zunnit_Extra_Famosos_MISC,
					CollectionName.Zunnit_Extra_Noticias_PER,
					CollectionName.Zunnit_Extra_Noticias_ORG, CollectionName.Zunnit_Extra_Noticias_LOC,
					CollectionName.Zunnit_Extra_Noticias_EVT, CollectionName.Zunnit_Extra_Noticias_MISC,
					CollectionName.Zunnit_Extra_TV_e_Lazer_PER,
					CollectionName.Zunnit_Extra_TV_e_Lazer_ORG, CollectionName.Zunnit_Extra_TV_e_Lazer_LOC,
					CollectionName.Zunnit_Extra_TV_e_Lazer_EVT, CollectionName.Zunnit_Extra_TV_e_Lazer_MISC,
					CollectionName.Zunnit_Extra_All_PER, CollectionName.Zunnit_Extra_All_ORG,
					CollectionName.Zunnit_Extra_All_LOC, CollectionName.Zunnit_Extra_All_EVT,
					CollectionName.Zunnit_Extra_All_MISC};*/
			//CollectionName [] vSubcollection = {CollectionName.Zunnit_Extra_All_PER, CollectionName.Zunnit_Extra_All_ORG, CollectionName.Zunnit_Extra_All_LOC, CollectionName.Zunnit_Extra_All_EVT, CollectionName.Zunnit_Extra_All_MISC};
			CollectionName [] vSubcollection = {CollectionName.Zunnit_Extra_All_PER};

			for(CollectionName cCollection : vSubcollection) {
				FSNER vFSNER = new FSNER();
				vFSNER.runFSNER(cCollection, cFilterParameters);
			}
		}
	}

	public void runFSNER(CollectionName pCollectionName, FilterParameters pFilterParameters) {
		mFilterParameters = pFilterParameters;

		initializeVariables(pCollectionName);
		runStandardFSF(mCollectionDefinition.getDataCollection(pCollectionName));

		mNERModel.writeNERModelSpecification(mCollectionDefinition.getDataCollection(pCollectionName).mFilenameList, pFilterParameters);

		//-- To show overview statistics
		if(Debug.Evaluator.isToPrintStatistics) {
			mEvaluator.writeOverviewStatistics("");
			//mGeneralizationEvaluator.writeOverviewStatistics("");
			//mFilterCombinationEvaluator.writeOverviewStatistics("");
		}

		//playStormsSound();
	}

	protected void initializeVariables(CollectionName pCollectionName) {
		sOutputStyle = OutputStyle.Plain;

		mCollectionDefinition = new CollectionDefinition();

		DataCollection vDataCollection = mCollectionDefinition.getDataCollection(pCollectionName);
		ArrayList<String> vFilenameList = vDataCollection.mFilenameList;

		FtrSingleTermDictionary4.Clear();
		FtrSingleTermDictionary4.sDictionaryDirectory = vDataCollection.mDictionaryAddress +
				vDataCollection.mDictionaryName + "/";

		mEvaluator = new SimpleBILOUEvaluator(vFilenameList, sOutputStyle);
		//mFilterCombinationEvaluator = new FilterCombinationEvaluator(vFilenameList, sOutputStyle);
		//mAmbiguityEvaluator = new AmbiguityEvaluator(vFilenameList, false, outputStyle);
		//mGeneralizationEvaluator = new GeneralizationEvaluator(vFilenameList, false, outputStyle);
		//mLearningEvaluator = new LearningEvaluator(vFilenameList, false, outputStyle);
	}

	protected void playStormsSound() {
		SoundToClass.play(new String [] {"songStorms.wav"});
		songStorms song = new songStorms();
		song.play();
	}

	protected void runStandardFSF(DataCollection pDataCollection) {
		String vDir = CollectionDefinition.Directory.Collection;
		String vTrainFile;
		String vTestFile;
		String vFilename;
		String vTermListRestrictionName = CollectionDefinition.Directory
				.Dictionary + pDataCollection.mTermListRestrictionName;

		for(int cIteration = 1; cIteration <= Parameters.FSNERExecution.trainFileIteration; cIteration++) {
			vFilename = pDataCollection.getFilename(cIteration - 1);
			vTrainFile = vDir + vFilename + CollectionDefinition.FileExtension.Train;
			vTestFile = vDir + vFilename + CollectionDefinition.FileExtension.Test;

			if(cIteration == 1) {
				System.out.println("Train: " + vTrainFile.replace(vDir, "") +
						" Test: " + vTestFile.replace(vDir, "") + " -- CV" + cIteration);
			}

			runFSF(vTrainFile, vTestFile, "", vTermListRestrictionName);

			if(Parameters.Save.isToSaveNERModel) {
				writeObject(Parameters.getOutputDirectory() +
						Constants.FSNERModel + Symbol.HYPHEN +  vFilename + Symbol.DOT + Constants.FileExtention.FSNERModel);
			}
		}
	}

	protected void runFSF(String trainFile, String testFile, String referenceDataFile, String pTermListRestrictionFile) {
		//-- Set Label Encoding (Mandatory!)
		LabelEncoding.setEncodingType(EncodingType.BILOU);

		mNERModel = new SimpleNERModel();

		//-- Start count time
		SimpleStopWatch stopWatch = new SimpleStopWatch();
		stopWatch.start();

		//-- Create Components
		AbstractActivityControl simpleActivityControl = new SimpleActivityControl();
		AbstractActivityControl parallelActivityControl = new ParallelActivityControl();
		AbstractLabelFile simpleLabelFile = new SimpleLabelFile();
		AbstractUpdateControl simpleUpdateControl =  new SimpleUpdateControl(0);

		//-- Create Label File Score Models
		AbstractTermRestrictionChecker termRestrictionChecker = new SimpleTermRestrictionChecker(pTermListRestrictionFile);
		AbstractLabelFileLabelCalculatorModel orScoreModel = new LCMOrScore(termRestrictionChecker);
		AbstractLabelFileLabelCalculatorModel andScoreModel = new LCMAndScore(termRestrictionChecker);
		AbstractLabelFileLabelCalculatorModel simpleANDScoreModel = new LCMSimpleAndScore(termRestrictionChecker);
		AbstractLabelFileLabelCalculatorModel sumScoreModel = new LCMSumScore(termRestrictionChecker);
		AbstractLabelFileLabelCalculatorModel sumSimpleScoreModel = new LCMSimpleSumScore(termRestrictionChecker);
		AbstractLabelFileLabelCalculatorModel sumNewOrScoreModel = new LCMNewOrScore(termRestrictionChecker);

		//-- Joint Components to NERModel
		mNERModel.addModelActivityControl(simpleActivityControl);
		mNERModel.addModelLabelFile(simpleLabelFile);
		mNERModel.addModelUpdateControl(simpleUpdateControl);
		mNERModel.addModelEvaluator(mEvaluator);
		mNERModel.addFilterParameters(mFilterParameters);

		//-- Joint Subcomponents
		simpleLabelFile.addSequenceScoreCalculatorModel(sumScoreModel);
		((LCMSumScore)sumScoreModel).setFilterProbability(0);
		((LCMSumScore)sumScoreModel).setAlpha(0);

		//-- Execute FS-NER
		mNERModel.allocModel(new String [] {pTermListRestrictionFile});
		mNERModel.load(trainFile);
		mNERModel.labelFile(testFile);
		mNERModel.evaluate(mNERModel.getTaggedFilenameAddress(), testFile, "(Label)");

		//-- Other evaluation analisys
		//mFilterCombinationEvaluator.evaluate(mNERModel.getTaggedFilenameAddress(), testFile, "");
		//mGeneralizationEvaluator.evaluate(trainFile, mNERModel.getTaggedFilenameAddress(), testFile, "");
		//learningEvaluator.evaluate(trainFile, nerTagger.getTaggedFilenameAddress(), testFile, "");

		//-- Update the NER Model
		updateModel(mNERModel, "", referenceDataFile, testFile, UpdateType.None);

		if(mShowElapsedTime)
		{
			stopWatch.show("Total Elipsed Time:");
			//nerTagger.writeEvaluation("");
		}
	}

	protected void updateModel(AbstractNERModel pNERTagger, String pTopicFile,
			String pReferenceDataFile, String pTestFile, UpdateType pUpdateType) {
		//Auto-update model not implemented.
	}

	public void labelFile(String pFilenameAddressToLabel, boolean pIsToClearPreviousResults) {
		if(pIsToClearPreviousResults) {
			mFilterCombinationEvaluator.clear();
		}
		mNERModel.labelFile(pFilenameAddressToLabel);
	}

	public DataSequence labelSequence(DataSequence pSequence) {
		return mNERModel.getLabelFile().labelSequence(pSequence);
	}

	public void writeOverviewStatistics(String pOutputFilenameAddress) {
		mFilterCombinationEvaluator.writeOverviewStatistics(pOutputFilenameAddress, "");
	}

	public AbstractNERModel getNERModel() {
		return mNERModel;
	}

	public static FSNER loadObject(String pInputObjectFilenameAddress) {
		FSNER vFSNER = null;
		try {
			ObjectInputStream vReader = new ObjectInputStream(new FileInputStream(pInputObjectFilenameAddress));
			vFSNER = (FSNER) vReader.readObject();
			vReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return vFSNER;
	}

	public void writeObject(String pOutputObjectFilenameAddress) {
		try {
			ObjectOutputStream vWriter = new ObjectOutputStream(new FileOutputStream(pOutputObjectFilenameAddress));
			vWriter.writeObject(FSNER.this);

			vWriter.flush();
			vWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
