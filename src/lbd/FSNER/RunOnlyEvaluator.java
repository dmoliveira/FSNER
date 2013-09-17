package lbd.FSNER;

import lbd.FSNER.Evaluation.AmbiguityEvaluator;
import lbd.FSNER.Evaluation.GeneralizationEvaluator;
import lbd.FSNER.Evaluation.LearningEvaluator;
import lbd.FSNER.Model.AbstractEvaluator.OutputStyle;

public class RunOnlyEvaluator {

	protected static String dir = "./samples/data/bcs2010/";

	public static void main(String [] args) {

		System.out.println("Running ONLY evaluator...\n");
		OutputStyle outputStyle = OutputStyle.Latex;

		AmbiguityEvaluator ambiguityEvaluator = new AmbiguityEvaluator(null, true, outputStyle);
		GeneralizationEvaluator generalizationEvaluator = new GeneralizationEvaluator(null, true, outputStyle);
		LearningEvaluator learningEvaluator = new LearningEvaluator(null, true, outputStyle);

		String [] neutralFileNameList = {"OWCollection(Player)", "OWCollection(Venue)", "OWCollection(Team)",
				"ETZCollection(Company)", "ETZCollection(Geo-loc)", "ETZCollection(Person)", "WTCollection(Organization)Train+Eval-"};

		for(int t = 0; t < neutralFileNameList.length; t++) {
			String neutralFilename = neutralFileNameList[t];

			System.out.println("\n\n" + neutralFilename + "\n");

			for(int j = 0; j < 3; j++) {
				for(int i = 1; i <= 5; i++) {

					String trainingFilenameAddress = dir +  neutralFilename + "CV"+i+".train";
					String testFilenameAddress = dir + neutralFilename + "CV"+i+".test";
					String taggedFilenameAddress = dir + neutralFilename + "CV"+i+".tagged";

					if(j == 0) {
						ambiguityEvaluator.evaluate(taggedFilenameAddress, testFilenameAddress, "");
					} else if(j == 1) {
						generalizationEvaluator.evaluate(trainingFilenameAddress, taggedFilenameAddress, testFilenameAddress, "");
					} else if(j == 2) {
						learningEvaluator.evaluate(trainingFilenameAddress, taggedFilenameAddress, testFilenameAddress, "");
					}
				}

				System.out.println("\n");
			}
		}
	}

}
