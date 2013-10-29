package lbd.FSNER.Filter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.FileUtils;
import lbd.FSNER.Utils.Symbol;
import lbd.Utils.StringUtils;
import lbd.data.handler.ISequence;

public class FtrGazetteer extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	//Map token count to dictionary entry.
	protected Map<Integer, Set<String>> mDictionaryMap;
	protected Map<String, Set<Integer>> mInvertedIndex;
	protected String mDictionaryFilenameAddress;

	// Memory for the last entity found
	protected int mEntityStartPosition;
	protected int mEntityEndPosition;

	public FtrGazetteer(String pDictionaryFilenameAddress, int pPreprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel pScoreCalculator) {

		super(ClassName.getSingleName(FtrGazetteer.class.getName()) + "(" + pDictionaryFilenameAddress + ")",
				pPreprocessingTypeNameIndex, pScoreCalculator);

		mDictionaryFilenameAddress = pDictionaryFilenameAddress;
	}

	@Override
	public void initialize() {

		if(StringUtils.isNullOrEmpty(mDictionaryFilenameAddress)) {
			try {
				throw new IOException("[Error] Need to initilize variable of dictionary directory.");
			} catch (IOException pException) {
				pException.printStackTrace();
			}
		}

		mDictionaryMap = new TreeMap<Integer, Set<String>>();
		mInvertedIndex = new HashMap<String, Set<Integer>>();

		loadDictionary(mDictionaryFilenameAddress);
	}

	protected void loadDictionary(String pDictionaryFilenameAddress) {
		try {

			BufferedReader vInputReader = FileUtils.createBufferedReader(pDictionaryFilenameAddress);
			String vEntry;

			while((vEntry = vInputReader.readLine()) != null) {

				vEntry = vEntry.trim();

				if(!vEntry.isEmpty() && Parameters.Filter.Gazetter.mMinimumAcceptedDictionaryTermEntry <= vEntry.length()) {

					String [] vEntrySplitted = vEntry.split(Symbol.SPACE);
					int vEntrySize = vEntrySplitted.length;

					if(!mDictionaryMap.containsKey(vEntrySize)) {
						mDictionaryMap.put(vEntrySize, new HashSet<String>());
					}

					mDictionaryMap.get(vEntrySize).add(vEntry);

					// Add element to inverted index
					if(!mInvertedIndex.containsKey(vEntrySplitted[0].trim())) {
						mInvertedIndex.put(vEntrySplitted[0].trim(), new TreeSet<Integer>(Collections.reverseOrder()));
					}

					mInvertedIndex.get(vEntrySplitted[0].trim()).add(vEntrySize);
				}
			}

			vInputReader.close();

		} catch (FileNotFoundException pException) {
			pException.printStackTrace();
		} catch (UnsupportedEncodingException pException) {
			pException.printStackTrace();
		} catch (IOException pException) {
			pException.printStackTrace();
		}
	}

	@Override
	public void loadTermSequence(ISequence pPreprocessedSequence, int pIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(ISequence pSequence, ISequence pPreprocessedSequence, int pIndex) {

		String vId = Symbol.EMPTY;
		String vIdModel = "id:{0}.dic:{1}.entPos:{2}.entSz:{3}";

		if(pIndex == 0) {
			resetEntityPosition();
		}

		if(mEntityStartPosition == -1) {

			String vTerm = pSequence.getToken(pIndex);

			if(mInvertedIndex.containsKey(vTerm)) {

				Set<Integer> vEntitySizeList = mInvertedIndex.get(vTerm);

				for(Integer cEntitySize : vEntitySizeList) {

					if(pIndex + cEntitySize -1 < pSequence.length()) {

						String vCandidateEntity = getCandidateEntity(pSequence, pIndex, cEntitySize);

						if(mDictionaryMap.get(cEntitySize).contains(vCandidateEntity)) {

							mEntityStartPosition = pIndex;
							mEntityEndPosition = pIndex + cEntitySize - 1;

							vId = MessageFormat.format(vIdModel,mId , mDictionaryFilenameAddress,
									0, (mEntityEndPosition - mEntityStartPosition));

							break;
						}
					}
				}
			}
		} else {
			vId = MessageFormat.format(vIdModel,mId , mDictionaryFilenameAddress,
					(pIndex - mEntityStartPosition), (mEntityEndPosition - mEntityStartPosition));
		}

		if(pIndex == mEntityEndPosition) {
			resetEntityPosition();
		}

		return vId;
	}

	public String getCandidateEntity(ISequence pSequence, int pIndex, int pEntitySize) {

		String vCandidateEntity = Symbol.EMPTY;

		for(int i = pIndex; i < pIndex + pEntitySize && i < pSequence.length(); i++) {
			vCandidateEntity += pSequence.getToken(i) + Symbol.SPACE;
		}

		return vCandidateEntity.trim();
	}

	private void resetEntityPosition() {
		mEntityStartPosition = -1;
		mEntityEndPosition = -1;
	}
}
