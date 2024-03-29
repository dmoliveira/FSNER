package lbd.FSNER;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import lbd.FSNER.ActivityControl.ParallelActivityControl;
import lbd.FSNER.ActivityControl.SimpleActivityControl;
import lbd.FSNER.Collection.CollectionDefinition;
import lbd.FSNER.Collection.CollectionDefinition.CollectionName;
import lbd.FSNER.Collection.CollectionDefinition.FileExtension;
import lbd.FSNER.Collection.DataCollection;
import lbd.FSNER.Configuration.Constants;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Configuration.FilterParameters;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Evaluation.AmbiguityEvaluator;
import lbd.FSNER.Evaluation.GeneralizationEvaluator;
import lbd.FSNER.Evaluation.LearningEvaluator;
import lbd.FSNER.Evaluation.SimpleBILOUEvaluator;
import lbd.FSNER.Evaluation.SimpleEvaluator;
import lbd.FSNER.Filter.old.FtrSingleTermDictionary4;
import lbd.FSNER.LabelFile.SimpleLabelFile;
import lbd.FSNER.LabelFile.LabelCalculatorModel.LCMOrContinuosScore;
import lbd.FSNER.LabelFile.LabelCalculatorModel.LCMOrDiscreteScore;
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
import lbd.FSNER.Utils.SimpleStopWatch;
import lbd.FSNER.Utils.Symbol;
import lbd.Utils.SoundToClass;
import lbd.Utils.songStorms;
import lbd.data.handler.ISequence;
import lbd.fsner.entity.Entity;
import lbd.fsner.labelFile.level2.AbstractLabelFileLevel2;
import lbd.fsner.labelFile.level2.SimpleLabelFileLevel2;

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
				FileUtils.getListOfOnlyFiles(Parameters.Directory.mFilterConfiguration).get(0));

		for(FilterParameters cFilterParameters : vFilterParametersList) {
			System.out.println("-- FilterConfiguration: " + cFilterParameters);
			//Save.optionalDirectory = "FilterCombinationHitTraining/" + cFilterParameters.toString();'
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
			//CollectionName [] vSubcollection = {CollectionName.Zunnit_Shuf_PER, CollectionName.Zunnit_Shuf_LOC, CollectionName.Zunnit_Shuf_ORG, CollectionName.Zunnit_Shuf_MISC};
			CollectionName [] vSubcollection = {CollectionName.Zunnit_Shuf};
			/*CollectionName [] vSubcollection = {CollectionName.Zunnit_Extra_Casa, CollectionName.Zunnit_Extra_Casos,
					CollectionName.Zunnit_Extra_Emprego, CollectionName.Zunnit_Extra_Esportes,
					CollectionName.Zunnit_Extra_Famosos, CollectionName.Zunnit_Extra_Noticias};*/

			for(CollectionName cCollection : vSubcollection) {
				FSNER vFSNER = new FSNER();
				vFSNER.runFSNER(cCollection, cFilterParameters);
			}
		}
	}

	public void runFSNER(CollectionName pCollectionName, FilterParameters pFilterParameters) {
		mFilterParameters = pFilterParameters;

		initializeVariables(pCollectionName);
		runStandardFSNER(mCollectionDefinition.getDataCollection(pCollectionName));

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

	protected void runStandardFSNER(DataCollection pDataCollection) {

		for(int cIteration = 1; cIteration <= Parameters.FSNERExecution.mTrainFileIteration; cIteration++) {

			String vTrainFile = CollectionDefinition.getFilenameAddress(pDataCollection, cIteration, FileExtension.Train);
			String vTestFile = CollectionDefinition.getFilenameAddress(pDataCollection, cIteration, FileExtension.Test);

			if(cIteration == 1) {
				System.out.println(MessageFormat.format("Train:{0} - Test:{1}", vTrainFile, vTestFile));
			}

			runFSNERCore(vTrainFile, vTestFile, "", pDataCollection);

			if(Parameters.Save.mIsToSaveNERModel) {
				writeObject(Parameters.getOutputDirectory() + Constants.FSNERModel + Symbol.HYPHEN
						+ pDataCollection.getFilename(cIteration - 1) + Symbol.DOT + Constants.FileExtention.FSNERModel);
			}
		}
	}

	protected void runFSNERCore(String pTrainFile, String pTestFile, String pReferenceDataFile, DataCollection pDataCollection) {
		//-- Create filenames
		String vTermListRestrictionName = CollectionDefinition.Directory.Dictionary + pDataCollection.mTermListRestrictionName;

		mNERModel = new SimpleNERModel(pDataCollection);

		//-- Start count time
		SimpleStopWatch vStopWatch = new SimpleStopWatch();
		vStopWatch.start();

		//-- Create Components
		AbstractActivityControl vSimpleActivityControl = new SimpleActivityControl();
		AbstractActivityControl pParallelActivityControl = new ParallelActivityControl();

		AbstractLabelFile vSimpleLabelFile = new SimpleLabelFile();
		AbstractUpdateControl vSimpleUpdateControl =  new SimpleUpdateControl(0);
		AbstractLabelFileLevel2 vSimpleLabelFileLevel2 = new SimpleLabelFileLevel2();

		//-- Create Label File Score Models
		AbstractTermRestrictionChecker vTermRestrictionChecker = new SimpleTermRestrictionChecker(vTermListRestrictionName);
		AbstractLabelFileLabelCalculatorModel vOrDiscreteScoreModel = new LCMOrDiscreteScore(vTermRestrictionChecker); //Best for Recall
		AbstractLabelFileLabelCalculatorModel vOrContinuosScoreModel = new LCMOrContinuosScore(vTermRestrictionChecker);
		AbstractLabelFileLabelCalculatorModel vSumScoreModel = new LCMSumScore(vTermRestrictionChecker); //Default [!]: Best for Precision

		//-- Joint Components to NERModel
		mNERModel.addModelActivityControl(vSimpleActivityControl);
		mNERModel.addModelLabelFile(vSimpleLabelFile);
		mNERModel.addLabelFileLevel2(vSimpleLabelFileLevel2);
		mNERModel.addModelUpdateControl(vSimpleUpdateControl);
		mNERModel.addModelEvaluator(mEvaluator);
		mNERModel.addFilterParameters(mFilterParameters);

		//-- Joint Subcomponents
		vSimpleLabelFile.addSequenceScoreCalculatorModel(vSumScoreModel);

		//-- Execute FS-NER
		mNERModel.allocModel(new String [] {vTermListRestrictionName});
		mNERModel.load(pTrainFile);
		mNERModel.labelFile(pTestFile);
		//mNERModel.evaluate(mNERModel.getTaggedFilenameAddress(), testFile, "(Label)");
		new SimpleEvaluator().evaluate(mNERModel.getTaggedFilenameAddress(), pTestFile);

		//-- Other evaluation analisys
		//mFilterCombinationEvaluator.evaluate(mNERModel.getTaggedFilenameAddress(), testFile, "");
		//mGeneralizationEvaluator.evaluate(trainFile, mNERModel.getTaggedFilenameAddress(), testFile, "");
		//learningEvaluator.evaluate(trainFile, nerTagger.getTaggedFilenameAddress(), testFile, "");

		//-- Update the NER Model
		updateModel(mNERModel, "", pReferenceDataFile, pTestFile, UpdateType.None);

		if(mShowElapsedTime)
		{
			vStopWatch.show("Total Elipsed Time:");
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

	public ISequence labelSequence(ISequence pSequence) {
		return mNERModel.getLabelFile().labelSequence(pSequence);
	}

	public List<String> labelSequenceToList (ISequence pSequence) {

		List<String> vEntityList = new ArrayList<String>();

		for(Entity cEntity : Parameters.DataHandler.mLabelEncoding.getEntities(pSequence)) {
			vEntityList.add(cEntity.getValue());
		}
		return vEntityList;
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
