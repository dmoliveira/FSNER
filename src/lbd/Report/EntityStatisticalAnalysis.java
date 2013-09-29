package lbd.Report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


public class EntityStatisticalAnalysis {
	
	public static void writeEntityStatisticAnalysis(String inputFilenameAddress, HashMap<String, StatisticalEntry> statisticalEntryMap) {
		
		String outputFilenameAddress = "./-EntStatsAnalysis.esa";
		Writer out;		
		
		double totalEntityFrequency = 0;
		double totalEntityCorrectFrequency = 0;
		
		double averageCorrectLabel = 0;
		double stdevCorrectLabel = 0;
		
		Iterator<Entry<String, StatisticalEntry>> ite = statisticalEntryMap.entrySet().iterator();
		StatisticalEntry entry;
		
		try {
			
			out = new OutputStreamWriter(new FileOutputStream(outputFilenameAddress), "ISO-8859-1");
			out.write("-- Entity Statistic Analysis\n");
			
			while(ite.hasNext()) {
				
				entry = ite.next().getValue();
				entry.calculateStatisticalMeasures();
				
				totalEntityFrequency += entry.getTruePositive() + entry.getFalseNegative();
				totalEntityCorrectFrequency += entry.getTruePositive();
			}
			
			averageCorrectLabel = totalEntityCorrectFrequency/statisticalEntryMap.size();
			
			ite = statisticalEntryMap.entrySet().iterator();
			ArrayList<String> termAboveAverage = new ArrayList<String>();
			ArrayList<String> termBelowAverage = new ArrayList<String>();
		
			while(ite.hasNext()) {
				
				entry = ite.next().getValue();
				stdevCorrectLabel += Math.pow(entry.getTruePositive() - averageCorrectLabel, 2);
				
				if(entry.getTruePositive() < averageCorrectLabel) 
					termBelowAverage.add(entry.getId()); 
				else 
					termAboveAverage.add(entry.getId());
				
					out.write(entry.getId() + " P " + format(entry.getPrecision()) + 
							"%(" + (int)entry.getTruePositive() + "/" + (int)entry.getFalsePositive() + 
							") R " + format(entry.getRecall()) + 
							"%(" + (int)entry.getTruePositive() + "/" + (int)entry.getFalseNegative() + 
							") F1 " + format(entry.getfMeasure()) + "% [" + 
							((entry.getTruePositive() >= averageCorrectLabel)? "Above" : "Below") + "]" +
							" [Relevance asEnt: " + format((entry.getTruePositive())/totalEntityCorrectFrequency) +
							"% asTerm: " + format((entry.getTruePositive() + entry.getFalseNegative())/totalEntityFrequency) + "%]\n");
			}
			
			stdevCorrectLabel = Math.sqrt(stdevCorrectLabel/statisticalEntryMap.size());
			
			out.write("\nAvgCorrectLabel: " + format(averageCorrectLabel, 1) + " StdDev: " + format(stdevCorrectLabel, 1) + "\n\n");
			
			out.write("Term Below Average: ");
			for(String term : termBelowAverage)
				out.write(term + "(" + ((int)(statisticalEntryMap.get(term).getTruePositive() +
						statisticalEntryMap.get(term).getFalseNegative())) + "), ");
			
			out.write("\n\nTerm Above/Equal Average: ");
			for(String term : termAboveAverage)
				out.write(term + "(" + ((int)(statisticalEntryMap.get(term).getTruePositive() +
						statisticalEntryMap.get(term).getFalseNegative())) + "), ");
			
			out.flush();
			out.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected static String format(double number, double times) {
		return((new DecimalFormat("#.##").format(times * ((Double.isNaN(number))?0:number))));
	}
	
	protected static String format(double number) {
		return(format(number, 100));
	}
	
}
