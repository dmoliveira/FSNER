package lbd.DCluster;

public class RunDCluster {

	
	public static void main(String [] args) {
		
		String dir = "./samples/data/bcs2010/DCluster/";
		String wfreqInputFilenameAddress = "tweets2009-06-First348K.wfreq";
		
		DCluster dClusterInstance = new DCluster();
		dClusterInstance.generateDCluster(dir + wfreqInputFilenameAddress);
		
	}
}
