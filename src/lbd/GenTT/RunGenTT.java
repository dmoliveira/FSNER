package lbd.GenTT;

public class RunGenTT {
	
	public static void main(String [] args) {
		
		//int windowSize = 3;
		
		/** Select Sample By Context **/
		/*SelectSampleByContext ssBCxt = new SelectSampleByContext();
		ssBCxt.selectSamples("samples/data/bcs2010/GenTT/Input/Twitter-2000-Player-MANUAL+AUTO-LABELED-BILOU-CV5-CRF-RUS-POSTag-REWE-RRT-RRL-RRWS.tagged");*/
		
		/** Select Sample By Bag of Words **/
		/*String sourceFilename = "samples/data/bcs2010/GenTT/Input/Twitter-2000-Player-MANUAL-LABELED-BILOU-CV1-CRF-POSTag-REWE-RRT-RRL-RRWS.tagged";
		String inputFilename = "samples/data/bcs2010/GenTT/Input/Twitter-2000-Player-MANUAL+AUTO-LABELED-BILOU-CV1-CRF-RUS-POSTag-REWE-RRT-RRL-RRWS.tagged";
		int windowSize = 20;
		boolean considerOnlyEntities = true;
		double threshould = 0.6;
		
		for(int i = 1; i <= 5; i++) {
			sourceFilename = "samples/data/bcs2010/GenTT/Input/Twitter-2000-Player-MANUAL-LABELED-BILOU-CV"+i+"-POSTag--RUS-RRL-RRWS-REWE-RRT-CRF-REWE.train";
			inputFilename = "samples/data/bcs2010/GenTT/Input/Twitter-2000-Player-MANUAL+AUTO-LABELED-BILOU-CV"+i+"-CRF-RUS-POSTag-REWE-RRT-RRL-RRWS.tagged";
			SelectSampleByBagOfWordsContext ssbyBagOfWords = new SelectSampleByBagOfWordsContext(sourceFilename, windowSize, considerOnlyEntities, threshould);
			ssbyBagOfWords.executeSelection(inputFilename);
		}*/
		
		/*SelectSampleByBalancedContext ssBalByCxt = new SelectSampleByBalancedContext(windowSize);
		
		for(float i = 0f; i <= 1.04; i+=0.05f)	
			ssBalByCxt.selectSamples("./samples/data/bcs2010/GenTT/Input/Twitter-33850(34000)BILOU-NLN-LA(411.0)-PProc-R1.tagged", i);
		ssBalByCxt.test("./samples/data/bcs2010/GenTT/Input/Twitter-33850(34000)BILOU-NLN-LA(411.0)-PProc-R1.tagged");*/
		
		/** Merge Files **/
		/*for(int i = 1; i <= 5; i++) {
			String inputFile1 = "./samples/data/bcs2010/GenTT/Input/Twitter-2000-Player-MANUAL-LABELED-BILOU-CV"+i+"-POSTag--RUS-RRL-RRWS-REWE-RRT-CRF-REWE-CRF.tagged";
			String inputFile2 = "./samples/data/bcs2010/GenTT/Input/Twitter-33850-AutoTagger-(34000)-R1-RRT-RRL-RRWS-CapSel[p80-f>2].tagged";
			String output = "./samples/data/bcs2010/GenTT/Output/Twitter-2000-Player-MANUAL+AUTO-LABELED-BILOU-CV"+i+"-CRF-CapSel[p80-f>2].tagged";
			
			MergeFiles mF = new MergeFiles();
			mF.mergeFiles(inputFile1, inputFile2, output);
		}*/
		
		/** KGram Sequence Generation **/
		/*String inputFilenameAddress = "./samples/data/bcs2010/GenTT/Input/2K-Player-MANUAL-BILOU.tagged";
		
		KGramSequenceGeneration kGram = new KGramSequenceGeneration();
		kGram.executeKGramSequenceGeneration(inputFilenameAddress, 3);*/
		
		/** Sequence Split **/
		for(int i = 1; i <= 5; i++)
			SequenceSplit.generateSplitSequence("./samples/data/bcs2010/GenTT/Input/URLsFROMAllKeyWords-Venue-Web-JogoFutebol-rng(0-101)-maxRst(4)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef-R"+i+".lst", new int [] {205, 820});
		
		
	}

}
