package lbd.NewModels.FeatureFilters;

import iitb.CRF.DataSequence;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Filter.Component.Context;
import lbd.FSNER.Filter.Component.Context.ContextType;
import lbd.FSNER.Utils.CommonEnum.Flexibility;

public class ContextComponent implements Serializable{

	private static final long serialVersionUID = 1L;
	protected static final int MAX_CONTEXT_WINDOW_SIDE_SIZE = 10;

	//-- It is common use for any Context Filter. Only to optimize memory stored.
	protected Context context;

	protected Flexibility contextFlexibility;
	protected ContextType contextType;
	protected int windowSize;

	protected String filename;

	public ContextComponent(ContextType contextType, int windowSize, Flexibility contextFlexibility) {

		this.contextType = contextType;
		this.windowSize = windowSize;
		this.contextFlexibility = contextFlexibility;

		this.filename = "./Data/Temp/CRFFeaturesAsFilters/Context-" + contextType.name() +
				"." + windowSize + "." + contextFlexibility;

		context = new Context(MAX_CONTEXT_WINDOW_SIDE_SIZE, contextFlexibility);
	}

	public void addAsContext(DataSequence sequence, int pos) {

		SequenceLabel sequenceLabeled = transformDataSequence2SequenceLabeled(sequence);

		context.addAsContext(sequenceLabeled, pos);
	}

	public int getSequenceInstanceIdSub(DataSequence sequence, int index) {

		int id = -1;
		SequenceLabel sequenceLabeled = transformDataSequence2SequenceLabeled(sequence);

		int contextId = context.getContextId(sequenceLabeled, index,
				contextType, windowSize, contextFlexibility);

		if(contextId > -1) {
			id = contextId;
		}

		return (id);
	}

	public SequenceLabel transformDataSequence2SequenceLabeled(DataSequence sequence) {

		SequenceLabel sequenceLabeled = new SequenceLabel();

		for(int i = 0 ; i < sequence.length(); i++) {
			sequenceLabeled.addTerm((String)sequence.x(i), sequence.y(i));
		}

		return(sequenceLabeled);
	}

	public void read(ContextComponent target) throws IOException, ClassNotFoundException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		ContextComponent clone = (ContextComponent) in.readObject();
		clone(target, clone);

		in.close();
	}

	private void clone(ContextComponent target, ContextComponent clone) {
		target.context = clone.context;
	}

	public void write() throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
	}

}
