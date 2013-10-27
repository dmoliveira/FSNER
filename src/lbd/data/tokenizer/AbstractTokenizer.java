package lbd.data.tokenizer;

import lbd.FSNER.Utils.Symbol;

public abstract class AbstractTokenizer {

	public AbstractTokenizer(String pInitializeFilenameAddress) {
		initialize(pInitializeFilenameAddress);
	}

	protected abstract void initialize(String pInitializeFilenameAddress);

	public String tokenize(String pMessage) {
		return tokenizeSub(removeUnnecessarySpaces(pMessage));
	}

	public abstract String tokenizeSub(String pMessage);

	protected String removeUnnecessarySpaces(String pMessage) {
		String vProcessedMessage = pMessage.replace(Symbol.TAB, Symbol.SPACE);
		vProcessedMessage = vProcessedMessage.replace(Symbol.CARRIER_RETURN, Symbol.EMPTY);
		vProcessedMessage = vProcessedMessage.replace(Symbol.SPACE + Symbol.SPACE, Symbol.SPACE);
		vProcessedMessage = vProcessedMessage.replace(Symbol.SPACE + Symbol.SPACE, Symbol.SPACE);
		return vProcessedMessage.trim();
	}
}
