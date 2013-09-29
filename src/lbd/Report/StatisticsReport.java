package lbd.Report;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/************************************************
 * NEVER use CTRL + SHIFT + F, this will unformat the code
 */

public class StatisticsReport {

	private final String ENCODE_USED = "ISO-8859-1";

	private String reportFileName;
	private transient Writer out;

	public StatisticsReport(String reportFileName, boolean isGlobal) {

		this.reportFileName = reportFileName + ((!isGlobal)? "" : "-GLOBAL") + ".html";

		try {
			out = new OutputStreamWriter(new FileOutputStream(this.reportFileName), ENCODE_USED);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public String createHeaderPartI(boolean isGlobal) {

		String header;

		header =  "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
		header += "<html xmlns=\"http://www.w3.org/1999/xhtml\">";
		header += "	 <head>";
		header += "		<title>" + ((isGlobal)?"Global":"") + " Statistical Report - CRF v.0.5 BETA LBD Edition</title>";
		header += "		<LINK REL=StyleSheet HREF=\"./style/ReportModel01.css\" TYPE=\"text/css\" MEDIA=screen>";
		header += "	 </head>";
		header += "  <body>";
		header += "    	<h3>" + ((isGlobal)?"Global":"") + " Statistical Report - CRF v.0.5 BETA <a href=\"http://www.lbd.dcc.ufmg.br/\" title=\"Laborat�rio de Banco de Dados\">LBD Edition</a></h3>";
		header += "		<div id=\"Header\">";

		return(header);
	}

	public String createHeaderPartII(int numLabel, String modelGraph) {

		String header = "";

		header += "           <strong>Label Numbers:</strong> <span class=\"headerResult\">"+ numLabel+"</span><br/>";
		header += "           <strong>Model Graph Type:</strong> <span class=\"headerResult\">"+ modelGraph +"</span><br/>";
		header += " 		  <hr noshade class=\"headerHR\"/>";
		header += " 		  <strong>Observation:</strong> These results were produced through an <span>experimental research</span>. If you find any error or some way to improve even more the quality of results send an e-mail to <a href=\"mailto:dmztheone@gmail.com\">dmztheone@gmail.com</a>.";
		header += "		</div>";
		header += "		<div class=\"Table\">";
		header += "			<div class=\"headerTable\">";
		header += "				<div class=\"columnTableFirstColumn\">";
		header += "                	Sequence";
		header += "				</div>";
		header += "                <div class=\"columnTableConfusionMatrixElements\">";
		header += "                	TP";
		header += "				</div>";
		header += "                <div class=\"columnTableConfusionMatrixElements\">";
		header += "                	FP";
		header += "				</div>";
		header += "                <div class=\"columnTableConfusionMatrixElements\">";
		header += "                	FN";
		header += "				</div>";
		header += "                <div class=\"columnTableConfusionMatrixElements\">";
		header += "                	TN";
		header += "				</div>";
		header += "                <div class=\"columnTableStatisticalMeasures\">";
		header += "                	Sp";
		header += "				</div>";
		header += "                <div class=\"columnTableStatisticalMeasures\">";
		header += "                	Acc";
		header += "				</div>";
		header += "                <div class=\"columnTableStatisticalMeasures\">";
		header += "                	&alpha;";
		header += "				</div>";
		header += "                <div class=\"columnTableStatisticalMeasures\">";
		header += "                	&beta;";
		header += "				</div>";
		header += "                <div class=\"columnTableStatisticalMeasures\">";
		header += "                	LRP";
		header += "				</div>";
		header += "                <div class=\"columnTableStatisticalMeasures\">";
		header += "                	LRN";
		header += "				</div>";
		header += "                <div class=\"columnTableStatisticalMeasures\">";
		header += "                	P";
		header += "				</div>";
		header += "                <div class=\"columnTableStatisticalMeasures\">";
		header += "                	R";
		header += "				</div>";
		header += "                <div class=\"columnTableStatisticalMeasures\">";
		header += "                	F";
		header += "				</div>";
		header += "			</div>";

		return(header);

	}

	//-- Only for a global overview of all test file verified in CRF
	public void createGlobalHeader(ArrayList<String> trainingFileName, ArrayList<String> testFileName, int numLabel, String modelGraph, Date startDate, Date finalDate, long runTime) {

		String header = createHeaderPartI(true);

		for(int i = 0; i < trainingFileName.size(); i++) {
			header += "           <strong>Training & Test (TT" + (i + 1) + ") File:</strong> <span class=\"headerResult\">"+trainingFileName.get(i)+"</span> & <span class=\"headerResult\">"+testFileName.get(i)+"</span><br/>";
		}

		header += createHeaderPartII(numLabel, modelGraph);

		try {
			out.write(header);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createHeader(String trainingFile, String testFile, int numLabel, String modelGraph, Date startDate, Date finalDate, long runTime) {

		String header = createHeaderPartI(false);

		SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat timeOnlyFormat = new SimpleDateFormat("HH:mm:ss");

		header += "           <strong>Training:</strong> <span class=\"headerResult\">" + trainingFile + "</span><br/>";
		header += "           <strong>Test File:</strong> <span class=\"headerResult\">" + testFile + "</span><br/>";

		header += "           <strong>Executed in:</strong> <span class=\"headerResult\">" + dateOnlyFormat.format(startDate) + "</span>, <strong>Started at</strong> <span class=\"headerResult\">" + timeOnlyFormat.format(startDate) + "</span>, <strong>Finished at</strong> <span class=\"headerResult\">" + timeOnlyFormat.format(finalDate) + "</span><br/>";
		header += "           <strong>Runtime:</strong> <span class=\"headerResult\">"+runTime+"s</span><br/>";

		header += createHeaderPartII(numLabel, modelGraph);

		try {
			out.write(header);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//-- AddLine to Test Result Table
	public void addLine(int seq, double truePositive, double falsePositive, double falseNegative,
			double trueNegative, double specificity, double accuracy, double alpha, double beta,
			double likelihoodRatioPositive, double likelihoodRatioNegative, double precision,
			double recall, double fMeasure) {

		addLine(false, seq, truePositive, falsePositive, falseNegative, trueNegative,
				specificity, accuracy, alpha, beta, likelihoodRatioPositive,
				likelihoodRatioNegative, precision, recall, fMeasure);
	}

	public void addLine(boolean isGlobal, int seq, double truePositive, double falsePositive, double falseNegative,
			double trueNegative, double specificity, double accuracy, double alpha, double beta,
			double likelihoodRatioPositive, double likelihoodRatioNegative, double precision,
			double recall, double fMeasure) {
		try {
			String tableLine;

			tableLine  = "<div class=\"rowTable"+(seq%2+1)+"\">";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += (!isGlobal)? seq : ("TT" + seq);
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += (int)truePositive;
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += (int)falsePositive;
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += (int)falseNegative;
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += (int)trueNegative;
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += writeStatisticalMeasureValue(specificity);
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += writeStatisticalMeasureValue(accuracy);
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += writeStatisticalMeasureValue(alpha);
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += writeStatisticalMeasureValue(beta);
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += writeStatisticalMeasureValue(likelihoodRatioPositive);
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += writeStatisticalMeasureValue(likelihoodRatioNegative);
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += writeStatisticalMeasureValue(precision);
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += writeStatisticalMeasureValue(recall);
			tableLine += "   </div>";
			tableLine += "   <div class=\"columnTable\">";
			tableLine += writeStatisticalMeasureValue(fMeasure);
			tableLine += "   </div>";
			tableLine += "</div>";

			out.write(tableLine);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//-- Create Test Overview Header HTML
	public void createOverviewHeader() {

		String overview;

		overview  = "</div>";
		overview += "<div id=\"StatisticalOverview\">";
		overview += "	<div class=\"Table\">";
		overview += "			<div class=\"headerTable\">";
		overview += "				<div class=\"columnTableFirstColumn\">";
		overview += "                	Description";
		overview += "				</div>";
		overview += "                <div class=\"columnTableConfusionMatrixElements\">";
		overview += "                	TP";
		overview += "				</div>";
		overview += "                <div class=\"columnTableConfusionMatrixElements\">";
		overview += "                	FP";
		overview += "				</div>";
		overview += "                <div class=\"columnTableConfusionMatrixElements\">";
		overview += "                	FN";
		overview += "				</div>";
		overview += "                <div class=\"columnTableConfusionMatrixElements\">";
		overview += "                	TN";
		overview += "				</div>";
		overview += "                <div class=\"columnTableStatisticalMeasures\">";
		overview += "                	Sp";
		overview += "				</div>";
		overview += "                <div class=\"columnTableStatisticalMeasures\">";
		overview += "                	Acc";
		overview += "				</div>";
		overview += "                <div class=\"columnTableStatisticalMeasures\">";
		overview += "                	&alpha;";
		overview += "				</div>";
		overview += "                <div class=\"columnTableStatisticalMeasures\">";
		overview += "                	&beta;";
		overview += "				</div>";
		overview += "                <div class=\"columnTableStatisticalMeasures\">";
		overview += "                	LRP";
		overview += "				</div>";
		overview += "                <div class=\"columnTableStatisticalMeasures\">";
		overview += "                	LRN";
		overview += "				</div>";
		overview += "                <div class=\"columnTableStatisticalMeasures\">";
		overview += "                	P";
		overview += "				</div>";
		overview += "                <div class=\"columnTableStatisticalMeasures\">";
		overview += "                	R";
		overview += "				</div>";
		overview += "                <div class=\"columnTableStatisticalMeasures\">";
		overview += "                	F";
		overview += "				</div>";
		overview += "			</div>";

		try {
			out.write(overview);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//-- Create Test Overview Line HTML (Item)
	public void createOverviewLine(String description, double truePositive, double falsePositive,
			double falseNegative, double trueNegative, double specificity, double accuracy,
			double alpha, double beta, double likelihoodRatioPositive,
			double likelihoodRatioNegative, double precision, double recall, double fMeasure) {

		String overview;

		overview  = "        <div class=\"rowTotalTable\">";
		overview += "            <div class=\"columnTable\">";
		overview += "                <strong>" + description + "</strong>";
		overview += "            </div>";
		overview += "            <div class=\"columnTable\">";
		overview += (int)truePositive;
		overview += "            </div>";
		overview += "           <div class=\"columnTable\">";
		overview += (int)falsePositive;
		overview += "            </div>";
		overview += "            <div class=\"columnTable\">";
		overview += (int)falseNegative;
		overview += "            </div>";
		overview += "            <div class=\"columnTable\">";
		overview += (int)trueNegative;
		overview += "            </div>";
		overview += "            <div class=\"columnTable\">";
		overview += writeStatisticalMeasureValue(specificity);
		overview += "            </div>";
		overview += "            <div class=\"columnTable\">";
		overview += writeStatisticalMeasureValue(accuracy);
		overview += "            </div>";
		overview += "            <div class=\"columnTable\">";
		overview += writeStatisticalMeasureValue(alpha);
		overview += "            </div>";
		overview += "            <div class=\"columnTable\">";
		overview += writeStatisticalMeasureValue(beta);
		overview += "            </div>";
		overview += "            <div class=\"columnTable\">";
		overview += writeStatisticalMeasureValue(likelihoodRatioPositive);
		overview += "            </div>";
		overview += "            <div class=\"columnTable\">";
		overview += writeStatisticalMeasureValue(likelihoodRatioNegative);
		overview += "            </div>";
		overview += "            <div class=\"columnTable\">";
		overview += writeStatisticalMeasureValue(precision);
		overview += "            </div>";
		overview += "            <div class=\"columnTable\">";
		overview += writeStatisticalMeasureValue(recall);
		overview += "            </div>";
		overview += "            <div class=\"columnTable\">";
		overview += writeStatisticalMeasureValue(fMeasure);
		overview += "            </div>";
		overview += "        </div>";

		try {
			out.write(overview);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//-- Create Test Overview Footer HTML
	public void createOverviewFooter() {

		String overview;

		overview  = "	</div>";
		overview += "</div>";

		try {
			out.write(overview);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String writeStatisticalMeasureValue(double value) {
		DecimalFormat numberFormat = new DecimalFormat("#0.0000");

		String resultValue;

		if(Double.isNaN(value)) {
			resultValue = "<span class=\"NaNType\">NaN</span>";
		} else if(Double.isInfinite(value)) {
			resultValue = "<span class=\"infType\">Inf.</span>";
		} else {
			resultValue = numberFormat.format(value);
		}

		return (resultValue);
	}

	//-- Create a footer of the document
	public void createFooter() {
		try {
			String footer;
			footer  = "<div id=\"Footer\">";
			footer += "<span class=\"FooterHeader\">Description</span>";
			footer += "<hr noshade class=\"footerHR\"/>";
			footer += "<strong>TP:</strong> The CRF give the correct label. E.g. label as <span style=\"color:red\">player</span>.<br/>";
			footer += "<strong>TN:</strong> The CRF don't label as the main labels. E.g. label as <span style=\"color:red\">other</span> instead of <span style=\"color:red\">time</span>.<br/>";
			footer += "<strong>FP:</strong> The CRF put wrong positive label. E.g. label as <span style=\"color:red\">player</span> instead of <span style=\"color:red\">other</span> or <span style=\"color:red\">unknown</span>.<br/>";
			footer += "<strong>FN:</strong> The CRF put wrong negative label. E.g. label as <span style=\"color:red\">other</span> instead of <span style=\"color:red\">player</span> or <span style=\"color:red\">time</span>.<br/>";
			footer += "<strong>Sensibility:</strong> See R.<br/>";
			footer += "<strong>Sp:</strong> Specificity. <strong style=\"color:red\">TN / (TN+FP)</strong>.<br/>";
			footer += "<strong>&alpha;:</strong> False Positive Rate. <strong style=\"color:red\">1 - specificity</strong>.<br/>";
			footer += "<strong>&beta;:</strong> False Negative Rate. <strong style=\"color:red\">1 - sensibility</strong>.<br/>";
			footer += "<strong>Acc:</strong> Accuracy. <strong style=\"color:red\">(TP + TN) / (TP + FP + FN + TN)</strong>.<br/>";
			footer += "<strong>LRP:</strong> Likelihood Ratio Positive. <strong style=\"color:red\">Sensitivity / (1 - specificity)</strong>.<br/>";
			footer += "<strong>LRN:</strong> Likelihood Ratio Negative. <strong style=\"color:red\">(1 - sensitivity) / specificity</strong>.<br/>";
			footer += "<strong>P:</strong> Precision. <strong style=\"color:red\">TP / (TP + FP)</strong>.<br/>";
			footer += "<strong>R:</strong> Recall or Sensibility. <strong style=\"color:red\">TP / (TP + FN)</strong>.<br/>";
			footer += "<strong>F:</strong> F-Measure. <strong style=\"color:red\">2 (R x P) / (R + P)</strong>.<br/>";
			footer += "</div>";
			footer += "</body>";
			footer += "</html>";

			out.write(footer);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
