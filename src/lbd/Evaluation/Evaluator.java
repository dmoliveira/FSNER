package lbd.Evaluation;

import lbd.CRF.CRFExecutor;

public class Evaluator {
	
	protected static final String CRF_CONFIG_FILE = "./samples/bcs2010.conf";
	
	public static void main(String [] args) {
		
		String dir = "./samples/data/bcs2010/";
		
		for(int i = 1; i <= 5; i++) {
			String taggedFile = "ETZCollection(All-10-EntitiesType)CV"+i+"-PlainFormat-ENTITY-DOCRF.tagged";
			String testFile = "ETZCollection(All-10-EntitiesType)CV" + i + ".test";
			
			evaluate("Result (" + i + "): ", dir, taggedFile, testFile);
		}
	}
	
	public static void evaluate(String message, String dir, String taggedFile, String testFile) {
		System.out.print(message);
		evaluate(dir, taggedFile, testFile);
	}

	public static void evaluate(String dir, String taggedFile, String testFile) {
		try {
			CRFExecutor crfExec = new CRFExecutor(CRF_CONFIG_FILE);
			crfExec.getStatistics(dir, taggedFile, testFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
