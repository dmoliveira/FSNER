package lbd.FSNER.Utils;

import iitb.CRF.DataSequence;

import java.io.IOException;
import java.io.Writer;

public class WriterOutput {
	
	public static void writeSequence(Writer out, DataSequence sequence) throws IOException {
		
		for(int i = 0; i < sequence.length(); i++) {
			out.write(sequence.x(i) + Symbol.DELIMITER_LABEL + 
					LabelEncoding.BILOU.values()[(sequence.y(i))].name() + Symbol.NEW_LINE);
		}
		
		out.write(Symbol.NEW_LINE);
	}

}
