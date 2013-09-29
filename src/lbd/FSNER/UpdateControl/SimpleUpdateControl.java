package lbd.FSNER.UpdateControl;

import lbd.FSNER.Model.AbstractUpdateControl;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.DataSequence;

public class SimpleUpdateControl extends AbstractUpdateControl{

	private static final long serialVersionUID = 1L;

	public SimpleUpdateControl(double threshouldConfidenceSequence) {
		super(threshouldConfidenceSequence);
	}

	@Override
	public boolean addSequence(DataSequence sequence) {

		String encodedSequence = encodeSequence(sequence);

		//-- It can use similarity measures to exclude some sequences
		boolean canAddSequence = !sequenceAdded.containsKey(encodedSequence);

		if(canAddSequence) {
			sequenceListToUpdate.add(sequence);
			sequenceAdded.put(encodedSequence, null);

			/** Debug **/
			/*for(int i = 0; i < sequence.length(); i++)
				System.out.println(sequence.x(i) + "|" + BILOU.values()[sequence.y(i)].name());
			System.out.println();*/
		}

		return (canAddSequence);
	}

	protected String encodeSequence(DataSequence sequence) {

		String encodedSequence = Symbol.EMPTY;

		for(int i = 0; i < sequence.length(); i++) {
			encodedSequence += sequence.x(i) + Symbol.DOT + sequence.y(i) + Symbol.SPACE;
		}

		return(encodedSequence);
	}

}
