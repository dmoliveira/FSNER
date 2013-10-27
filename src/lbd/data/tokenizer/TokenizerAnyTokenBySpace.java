package lbd.data.tokenizer;

import lbd.FSNER.Utils.Symbol;
import lbd.Utils.StringUtils;

public class TokenizerAnyTokenBySpace extends AbstractTokenizer{

	public TokenizerAnyTokenBySpace(String pInitializeFilenameAddress) {
		super(pInitializeFilenameAddress);
	}

	@Override
	protected void initialize(String pInitializeFilenameAddress) {
		//-- Nothing to implement.
	}

	@Override
	public String tokenizeSub(String pMessage) {
		return separeNonAlphaNumbericBySpace(pMessage);
	}

	private String separeNonAlphaNumbericBySpace(String pMessage) {

		String vProcessedMessage = Symbol.EMPTY;
		int vMessageSize = pMessage.length();

		for(int i = 0; i < pMessage.length(); i++) {
			if(StringUtils.isNonAlphaNumericCharacter(pMessage.charAt(i))) {
				if(i > 0 && pMessage.charAt(i-1) != ' ') {
					vProcessedMessage += Symbol.SPACE;
				}

				vProcessedMessage += pMessage.charAt(i);

				if(i < vMessageSize - 1 && pMessage.charAt(i+1) != ' ') {
					vProcessedMessage += Symbol.SPACE;
				}

			} else {
				vProcessedMessage += pMessage.charAt(i);
			}
		}

		return removeUnnecessarySpaces(vProcessedMessage);
	}

}
