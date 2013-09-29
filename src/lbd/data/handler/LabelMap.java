package lbd.data.handler;


public class LabelMap {

	/** **/
	public static enum BILOUPlayerTeamPOSTag{PlayerBeginning, PlayerInside, PlayerLast, Outside, PlayerUnitToken,
		TeamBeginning, TeamInside, TeamLast, TeamUnitToken,
		Article, Preposition, Conjunction, Pronoun, Adverb, Numeral};

		/** Part of Speech PTBR **/
		public static enum POSTagPTBR{Beginning, Inside, Last, Outside, UnitToken,
			Article, Preposition, Conjunction, Interjection, Pronoun, Adverb, Numeral};

			/** Testing Context **/
			public static enum ContextE{Beginning, Inside, Last, Outside, UnitToken, Context};
			public static enum Context{Beginning, Inside, Last, Outside, UnitToken,
				InputContext, SharedContext, OutputContext};

				/** Orthographical States **/
				//public static enum OrthographicState {Beginning, Inside, Last, Outside, UnitToken, P, SC, SS, SA, SN, OC, OS, OA, ON};
				public static enum OrthographicState {Beginning, Inside, Last, Outside, UnitToken, C, S, A, N};

				/** Normal Tags **/
				public static enum BILOU {Beginning, Inside, Last, Outside, UnitToken};//"Other", "Team", "Player"};
				public static enum BIO {Beginning, Inside, Outside};
				public static enum IO {Inside, Outside};
				public static enum PreInSuFix {Prefix, Infix, Sufix, Inside, Outside};

				public static int getLabelIndexBILOUPlayerTeamPOSTag(String label) {
					return (BILOUPlayerTeamPOSTag.valueOf(label).ordinal());
				}

				public static String getLabelNameBILOUPlayerTeamPOSTag(int label) {
					return (BILOUPlayerTeamPOSTag.values()[label].toString());
				}


				public static int getLabelIndexPOSTagPTBR(String label) {
					return (POSTagPTBR.valueOf(label).ordinal());
				}

				public static String getLabelNamePOSTagPTBR(int label) {
					return (POSTagPTBR.values()[label].toString());
				}

				public static int getLabelIndexBILOU(String label) {
					return (Context.valueOf(label).ordinal());
				}

				public static String getLabelNameBILOU(int label) {
					return (Context.values()[label].toString());
				}

				public static int getLabelIndexBIO(String label) {
					return (BIO.valueOf(label).ordinal());
				}

				public static String getLabelNameBIO(int label) {
					return (BIO.values()[label].toString());
				}

				public static int getLabelIndexOI(String label) {
					return (IO.valueOf(label).ordinal());
				}

				public static String getLabelNameOI(int label) {
					return (IO.values()[label].toString());
				}
}
