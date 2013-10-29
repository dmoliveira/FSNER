package lbd.FSNER.Filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;
import lbd.fsner.label.encoding.Label;

public class FtrEntityTerm extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	//Token: Revisit - Entity must be more than one token.
	protected Map<Integer, Set<String>> mEntityMap;
	protected Map<String, Set<Integer>> mInvertedIndex;

	//Memory
	protected String mEntity;
	protected int mStartEntityPosition;
	protected int mEndEntityPosition;

	public FtrEntityTerm(int pPreprocessingTypeNameIndex) {
		super(ClassName.getSingleName(FtrEntityTerm.class.getName()), pPreprocessingTypeNameIndex);
		mEntityMap = new HashMap<Integer, Set<String>>();
		mInvertedIndex = new HashMap<String, Set<Integer>>();
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(ISequence pPreprocessedSequence) {
		mEntity = Symbol.EMPTY;

	}

	@Override
	public void loadTermSequence(ISequence pPreprocessedSequence, int pIndex) {

		mEntity += ((mEntity.isEmpty())? Symbol.EMPTY : Symbol.SPACE) + pPreprocessedSequence.getToken(pIndex);

		//TODO: Put a more generic type. Using BILOU for now.
		if(pPreprocessedSequence.getLabel(pIndex) == Parameters.DataHandler.mLabelEncoding.getOutsideLabel().ordinal()
				|| Label.getCanonicalLabel(pPreprocessedSequence.getLabel(pIndex)) == Label.UnitToken) {

			int vEntitySize = mEntity.split(Symbol.SPACE).length;
			String vEntityBeginning = mEntity.split(Symbol.SPACE)[0];

			if(!mEntityMap.containsKey(vEntitySize)) {
				mEntityMap.put(vEntitySize, new HashSet<String>());
			}

			if(!mInvertedIndex.containsKey(vEntityBeginning)) {
				mInvertedIndex.put(vEntityBeginning, new TreeSet<Integer>(Collections.reverseOrder()));
			}

			mEntityMap.get(vEntitySize).add(mEntity);
			mInvertedIndex.get(vEntityBeginning).add(vEntitySize);
			mEntity = Symbol.EMPTY;
		}
	}

	@Override
	public void loadActionAfterSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		mEntity = Symbol.EMPTY;
	}

	@Override
	public void adjust(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSequenceInstanceIdSub(ISequence pSequence, ISequence pPreprocessedSequence, int pIndex) {

		String vId = Symbol.EMPTY;

		if(pIndex == 0) {
			resetFilterMemory();
		}

		if(mEntity.isEmpty() && hasEntity(pPreprocessedSequence, pIndex,
				mInvertedIndex.get(pPreprocessedSequence.getToken(pIndex)))) {

			vId = "id:" + mId + Symbol.HYPHEN + mEntity + ".Pos:" + (pIndex - mStartEntityPosition)
					+ ".Sz:" + (mEndEntityPosition - mStartEntityPosition + 1);

		} else if(!mEntity.isEmpty()) {

			vId = "id:" + mId + Symbol.HYPHEN + mEntity+ ".Pos:" + (pIndex - mStartEntityPosition)
					+ ".Sz:" + (mEndEntityPosition - mStartEntityPosition + 1);
		}

		if(pIndex == mEndEntityPosition) {
			resetFilterMemory();
		}

		return (vId);
	}

	private void resetFilterMemory() {
		mEntity = Symbol.EMPTY;
		mStartEntityPosition = -1;
		mEndEntityPosition = -1;
	}

	public boolean hasEntity(ISequence pPreprocessedSequence, int pIndex, Set<Integer> pEntitySizeList) {

		if(pEntitySizeList == null || pEntitySizeList.size() == 0) {
			return false;
		}

		boolean vHasEntity = false;

		for(int cEntitySize : pEntitySizeList) {
			String vCandidateEntity = getSegment(pPreprocessedSequence, pIndex, cEntitySize);

			if(!vCandidateEntity.isEmpty() && mEntityMap.get(cEntitySize).contains(vCandidateEntity)) {
				vHasEntity = true;
				mEntity = vCandidateEntity;
				mStartEntityPosition = pIndex;
				mEndEntityPosition = pIndex + cEntitySize - 1;
				break;
			}
		}

		return vHasEntity;
	}

	public String getSegment(ISequence pPreprocessedSequence, int pIndex, int pSize) {

		String vSegment = Symbol.EMPTY;

		if(pIndex + pSize - 1 < pPreprocessedSequence.length()) {
			for(int cIndex = pIndex; cIndex < pIndex + pSize; cIndex++) {
				vSegment += ((vSegment.isEmpty())? Symbol.EMPTY : Symbol.SPACE) + pPreprocessedSequence.getToken(cIndex);
			}
		}

		return vSegment;
	}

}
