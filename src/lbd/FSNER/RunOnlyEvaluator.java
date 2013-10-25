package lbd.FSNER;

import lbd.FSNER.Evaluation.SimpleBILOUEvaluator;
import lbd.FSNER.Model.AbstractEvaluator;
import lbd.FSNER.Model.AbstractEvaluator.OutputStyle;

public class RunOnlyEvaluator {

	public static void main(String [] args) {

		System.out.println("Running ONLY evaluation...\n");
		OutputStyle outputStyle = OutputStyle.Plain;

		String vLabeledFileDir = "/mnt/windows8/Users/Diego/Documents/Research/MyProgramming/NER-Project/FS-NER/Data/Saves/Real/";
		String vTestFileDir = "/mnt/windows8/Users/Diego/Documents/Research/MyProgramming/Repository/Collections/Traditional Data/Zunnit/Extra/Training Set/02. 5-Fold Cross Validation/All-NewCVs/";

		String [] labeledFileList = {"Zunnit-GloboExtraCollection-All-PER-CV1.tagged",
				"Zunnit-GloboExtraCollection-All-ORG-CV1.tagged",
		"Zunnit-GloboExtraCollection-All-LOC-CV1.tagged"};

		String [] testFileList = {"Zunnit-GloboExtraCollection-All-PER-CV1.test",
				"Zunnit-GloboExtraCollection-All-ORG-CV1.test",
		"Zunnit-GloboExtraCollection-All-LOC-CV1.test"};

		AbstractEvaluator simpleEvaluator = new SimpleBILOUEvaluator(null, outputStyle);
		for(int cFile = 0; cFile < labeledFileList.length; cFile++) {
			simpleEvaluator.evaluate(vLabeledFileDir + labeledFileList[cFile], vTestFileDir + testFileList[cFile], "Labeled x Real");
		}

		//AmbiguityEvaluator ambiguityEvaluator = new AmbiguityEvaluator(null, true, outputStyle);
		//GeneralizationEvaluator generalizationEvaluator = new GeneralizationEvaluator(null, true, outputStyle);
		//LearningEvaluator learningEvaluator = new LearningEvaluator(null, true, outputStyle);

	}

}
