package lbd.FSNER.Collection;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.Annotations.Comment;

public class CollectionDefinition implements Serializable{

	private static final long serialVersionUID = 1L;

	public interface FileExtension {
		public final String Train = ".train";
		public final String Test = ".test";
		public final String Tagged = ".tagged";
	}

	public interface Directory {
		public final String Collection = Parameters.Directory.collection;
		public final String Dictionary = Parameters.Directory.dictionary;
		public final String BrownCluster = Parameters.Directory.cluster + "BrownCluster/";
	}

	public interface Dictionary {
		public final String EnglishStopWords = "EnglishStopWords-Tweet.dic";
		public final String PortugueseStopWords = "PortugueseStopWords-Tweet.dic";
	}

	public interface Cluster {
		public final String BrownCluster60k = "60K_clusters.txt";
		public final String BrownBllipClusters = "brownBllipClusters";
		public final String BrownClusterTwitter100KPtbrBHC = "Twitter-100K-Ptbr-BHC";
	}

	public enum CollectionName {PlayerCV, VenueCV, TeamCV, CompanyCV,
		GeolocCV, PersonCV, OrganizationCV,
		PER_MSM13CV, ORG_MSM13CV, LOC_MSM13CV, MISC_MSM13CV,
		PER_MSM13_ETZCV, ORG_MSM13_ETZCV, LOC_MSM13_ETZCV, MISC_MSM13_ETZCV,
		ORG_MSM13_ETZ_WTCV, PER_MSM13CV_PREPROCESSED, ORG_MSM13CV_PREPROCESSED,
		LOC_MSM13CV_PREPROCESSED, MISC_MSM13CV_PREPROCESSED, PER_MSM13_ETZ_PREPROCESSED,
		ORG_MSM13_ETZ_WT_PREPROCESSED, LOC_MSM13_ETZ_PREPROCESSED, MISC_MSM13_ETZ_PREPROCESSED,
		PER_MSM13_CONQUEST, ORG_MSM13_CONQUEST, LOC_MSM13_CONQUEST, MISC_MSM13_CONQUEST,
		PER_MSM13_V15_PREPROCESSED_CV, ORG_MSM13_V15_PREPROCESSED_CV, LOC_MSM13_V15_PREPROCESSED_CV, MISC_MSM13_V15_PREPROCESSED_CV, SAMPLE_ORG,
		Zunnit_Extra_Casa_PER, Zunnit_Extra_Casa_ORG, Zunnit_Extra_Casa_LOC, Zunnit_Extra_Casa_EVT, Zunnit_Extra_Casa_MISC,
		Zunnit_Extra_Casos_de_Policia_PER, Zunnit_Extra_Casos_de_Policia_ORG, Zunnit_Extra_Casos_de_Policia_LOC,
		Zunnit_Extra_Casos_de_Policia_EVT, Zunnit_Extra_Casos_de_Policia_MISC, Zunnit_Extra_Emprego_PER, Zunnit_Extra_Emprego_ORG,
		Zunnit_Extra_Emprego_LOC, Zunnit_Extra_Emprego_EVT, Zunnit_Extra_Emprego_MISC, Zunnit_Extra_Esportes_PER,
		Zunnit_Extra_Esportes_ORG, Zunnit_Extra_Esportes_LOC, Zunnit_Extra_Esportes_EVT, Zunnit_Extra_Esportes_MISC,
		Zunnit_Extra_Famosos_PER, Zunnit_Extra_Famosos_ORG, Zunnit_Extra_Famosos_LOC, Zunnit_Extra_Famosos_EVT,
		Zunnit_Extra_Famosos_MISC, Zunnit_Extra_Noticia_PER, Zunnit_Extra_Noticia_ORG, Zunnit_Extra_Noticia_LOC,
		Zunnit_Extra_Noticia_EVT, Zunnit_Extra_Noticia_MISC, Zunnit_Extra_TV_e_Lazer_PER, Zunnit_Extra_TV_e_Lazer_ORG,
		Zunnit_Extra_TV_e_Lazer_LOC, Zunnit_Extra_TV_e_Lazer_EVT, Zunnit_Extra_TV_e_Lazer_MISC};

		public HashMap<String, DataCollection> mDataCollectionMap;

		public CollectionDefinition() {
			mDataCollectionMap = new HashMap<String, DataCollection>();

			createCVCollection(CollectionName.PlayerCV.name(), "OW", "OWCollection(Player)CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.VenueCV.name(), "OW", "OWCollection(Venue)CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.TeamCV.name(), "OW", "OWCollection(Team)CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);

			createCVCollection(CollectionName.CompanyCV.name(), "ETZ/company", "ETZCollection(Company)CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.GeolocCV.name(), "ETZ/geo-loc", "ETZCollection(Geo-loc)CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.PersonCV.name(), "ETZ/person", "ETZCollection(Person)CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);

			createCVCollection(CollectionName.OrganizationCV.name(), "WT", "WTCollection(Organization)Train+Eval-CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);

			createCVCollection(CollectionName.PER_MSM13CV.name(), "MSM13/PER", "MSM13Collection(PER)CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.ORG_MSM13CV.name(), "MSM13/ORG", "MSM13Collection(ORG)CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.LOC_MSM13CV.name(), "MSM13/LOC", "MSM13Collection(LOC)CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.MISC_MSM13CV.name(), "MSM13/MISC", "MSM13Collection(MISC)CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);

			createCVCollection(CollectionName.PER_MSM13_ETZCV.name(), "MSM13/PER", "MSM13Collection(PER)+ETZCV{0}", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.ORG_MSM13_ETZCV.name(), "MSM13/ORG", "MSM13Collection(ORG)+ETZCV{0}", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.LOC_MSM13_ETZCV.name(), "MSM13/LOC", "MSM13Collection(LOC)+ETZCV{0}", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.MISC_MSM13_ETZCV.name(), "MSM13/MISC", "MSM13Collection+ETZCollection(MISC)CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);

			createCVCollection(CollectionName.PER_MSM13CV_PREPROCESSED.name(), "MSM13/PER", "MSM13Collection(PER)CV{0}-RUS-RET-RRT-RRL-RRWS-RSW", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.ORG_MSM13CV_PREPROCESSED.name(), "MSM13/ORG", "MSM13Collection(ORG)CV{0}-RUS-RET-RRT-RRL-RRWS-RSW", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.LOC_MSM13CV_PREPROCESSED.name(), "MSM13/LOC", "MSM13Collection(LOC)CV{0}-RUS-RET-RRT-RRL-RRWS-RSW", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.MISC_MSM13CV_PREPROCESSED.name(), "MSM13/MISC", "MSM13Collection(MISC)CV{0}-RUS-RET-RRT-RRL-RRWS-RSW", CollectionDefinition.Dictionary.EnglishStopWords);

			createCVCollection(CollectionName.ORG_MSM13_ETZ_WTCV.name(), "MSM13/ORG", "MSM13Collection(ORG)+ETZ+WTCV{0}", CollectionDefinition.Dictionary.EnglishStopWords);

			createCVCollection(CollectionName.PER_MSM13_ETZ_PREPROCESSED.name(), "MSM13/PER", "MSM13Collection+ETZ(PER)CV{0}-RUS-RET-RRT-RRL-RRWS-RSW", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.ORG_MSM13_ETZ_WT_PREPROCESSED.name(), "MSM13/ORG", "MSM13Collection+ETZ+WT(ORG)CV{0}-RUS-RET-RRT-RRL-RRWS-RSW", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.LOC_MSM13_ETZ_PREPROCESSED.name(), "MSM13/LOC", "MSM13Collection+ETZ(LOC)CV{0}-RUS-RET-RRT-RRL-RRWS-RSW", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.MISC_MSM13_ETZ_PREPROCESSED.name(), "MSM13/MISC", "MSM13Collection+ETZ(MISC)CV{0}-RUS-RET-RRT-RRL-RRWS-RSW", CollectionDefinition.Dictionary.EnglishStopWords);

			createCVCollection(CollectionName.PER_MSM13_CONQUEST.name(), "MSM13/PER", "MSM13Collection+ETZ(PER)v1.5-RUS-RET-RRT-RRL-RRWS-RSW", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.ORG_MSM13_CONQUEST.name(), "MSM13/ORG", "MSM13Collection(ORG)v1.5-RUS-RET-RRT-RRL-RRWS-RSW", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.LOC_MSM13_CONQUEST.name(), "MSM13/LOC", "MSM13Collection+ETZ(LOC)v1.5-RUS-RET-RRT-RRL-RRWS-RSW", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.MISC_MSM13_CONQUEST.name(), "MSM13/MISC", "MSM13Collection(MISC)v1.5-RUS-RET-RRT-RRL-RRWS-RSW", CollectionDefinition.Dictionary.EnglishStopWords);

			createCVCollection(CollectionName.PER_MSM13_V15_PREPROCESSED_CV.name(), "MSM13/PER", "MSM13Collection(PER)v1.5-RUS-RET-RRT-RRL-RRWS-RS-CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.ORG_MSM13_V15_PREPROCESSED_CV.name(), "MSM13/ORG", "MSM13Collection(ORG)v1.5-RUS-RET-RRT-RRL-RRWS-RS-CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.LOC_MSM13_V15_PREPROCESSED_CV.name(), "MSM13/LOC", "MSM13Collection(LOC)v1.5-RUS-RET-RRT-RRL-RRWS-RS-CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);
			createCVCollection(CollectionName.MISC_MSM13_V15_PREPROCESSED_CV.name(), "MSM13/MISC", "MSM13Collection(MISC)v1.5-RUS-RET-RRT-RRL-RRWS-RS-CV{0}", CollectionDefinition.Dictionary.EnglishStopWords);

			createCVCollection(CollectionName.SAMPLE_ORG.name(), "MSM13/ORG", "Sample-ORG", CollectionDefinition.Dictionary.EnglishStopWords);
			//createCVCollection(CollectionName.MISC_MSM13_ETZCV.name(), "MSM13/MISC", "MSM13Collection(MISC)+ETZCV{0}", CollectionDefinition.Dictionary.EnglishStopWords);

			createCVCollection(CollectionName.Zunnit_Extra_Casa_PER.name(), "MSM13/PER", "Zunnit-GloboExtraCollection-Casa-PER-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Casa_ORG.name(), "MSM13/ORG", "Zunnit-GloboExtraCollection-Casa-ORG-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Casa_LOC.name(), "MSM13/LOC", "Zunnit-GloboExtraCollection-Casa-LOC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Casa_EVT.name(), "none/", "Zunnit-GloboExtraCollection-Casa-EVT-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Casa_MISC.name(), "MSM13/MISC", "Zunnit-GloboExtraCollection-Casa-MISC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);

			createCVCollection(CollectionName.Zunnit_Extra_Casos_de_Policia_PER.name(), "MSM13/PER", "Zunnit-GloboExtraCollection-Casos_de_Policia-PER-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Casos_de_Policia_ORG.name(), "MSM13/ORG", "Zunnit-GloboExtraCollection-Casos_de_Policia-ORG-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Casos_de_Policia_LOC.name(), "MSM13/LOC", "Zunnit-GloboExtraCollection-Casos_de_Policia-LOC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Casos_de_Policia_EVT.name(), "none/", "Zunnit-GloboExtraCollection-Casos_de_Policia-EVT-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Casos_de_Policia_MISC.name(), "MSM13/MISC", "Zunnit-GloboExtraCollection-Casos_de_Policia-MISC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);

			createCVCollection(CollectionName.Zunnit_Extra_Emprego_PER.name(), "MSM13/PER", "Zunnit-GloboExtraCollection-Emprego-PER-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Emprego_ORG.name(), "MSM13/ORG", "Zunnit-GloboExtraCollection-Emprego-ORG-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Emprego_LOC.name(), "MSM13/LOC", "Zunnit-GloboExtraCollection-Emprego-LOC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Emprego_EVT.name(), "none/", "Zunnit-GloboExtraCollection-Emprego-EVT-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Emprego_MISC.name(), "MSM13/MISC", "Zunnit-GloboExtraCollection-Emprego-MISC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);

			createCVCollection(CollectionName.Zunnit_Extra_Esportes_PER.name(), "MSM13/PER", "Zunnit-GloboExtraCollection-Esportes-PER-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Esportes_ORG.name(), "MSM13/PER", "Zunnit-GloboExtraCollection-Esportes-ORG-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Esportes_LOC.name(), "MSM13/PER", "Zunnit-GloboExtraCollection-Esportes-LOC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Esportes_EVT.name(), "MSM13/PER", "Zunnit-GloboExtraCollection-Esportes-EVT-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Esportes_MISC.name(), "MSM13/PER", "Zunnit-GloboExtraCollection-Esportes-MISC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);

			createCVCollection(CollectionName.Zunnit_Extra_Famosos_PER.name(), "MSM13/PER", "Zunnit-GloboExtraCollection-Famosos-PER-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Famosos_ORG.name(), "MSM13/ORG", "Zunnit-GloboExtraCollection-Famosos-ORG-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Famosos_LOC.name(), "MSM13/LOC", "Zunnit-GloboExtraCollection-Famosos-LOC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Famosos_EVT.name(), "none/", "Zunnit-GloboExtraCollection-Famosos-EVT-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Famosos_MISC.name(), "MSM13/MISC", "Zunnit-GloboExtraCollection-Famosos-MISC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);

			createCVCollection(CollectionName.Zunnit_Extra_Noticia_PER.name(), "MSM13/PER", "Zunnit-GloboExtraCollection-Noticia-PER-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Noticia_ORG.name(), "MSM13/ORG", "Zunnit-GloboExtraCollection-Noticia-ORG-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Noticia_LOC.name(), "MSM13/LOC", "Zunnit-GloboExtraCollection-Noticia-LOC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Noticia_EVT.name(), "none/", "Zunnit-GloboExtraCollection-Noticia-EVT-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_Noticia_MISC.name(), "MSM13/MISC", "Zunnit-GloboExtraCollection-Noticia-MISC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);

			createCVCollection(CollectionName.Zunnit_Extra_TV_e_Lazer_PER.name(), "MSM13/PER", "Zunnit-GloboExtraCollection-TV_e_Lazer-PER-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_TV_e_Lazer_ORG.name(), "MSM13/ORG", "Zunnit-GloboExtraCollection-TV_e_Lazer-ORG-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_TV_e_Lazer_LOC.name(), "MSM13/LOC", "Zunnit-GloboExtraCollection-TV_e_Lazer-LOC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_TV_e_Lazer_EVT.name(), "none/", "Zunnit-GloboExtraCollection-TV_e_Lazer-EVT-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);
			createCVCollection(CollectionName.Zunnit_Extra_TV_e_Lazer_MISC.name(), "MSM13/MISC", "Zunnit-GloboExtraCollection-TV_e_Lazer-MISC-CV{0}", CollectionDefinition.Dictionary.PortugueseStopWords);

			/*createCVCollection(CollectionName.PER_MSM13CV.name(), "MSM13/PER", "MSM13Collection(PER)", CollectionDefinition.Dictionary.EnglishStopWords);
		createCVCollection(CollectionName.ORG_MSM13CV.name(), "MSM13/ORG", "MSM13Collection(ORG)", CollectionDefinition.Dictionary.EnglishStopWords);
		createCVCollection(CollectionName.LOC_MSM13CV.name(), "MSM13/LOC", "MSM13Collection(LOC)", CollectionDefinition.Dictionary.EnglishStopWords);
		createCVCollection(CollectionName.MISC_MSM13CV.name(), "MSM13/MISC", "MSM13Collection(MISC)", CollectionDefinition.Dictionary.EnglishStopWords);*/
		}

		@Comment(message="For pCVFile, it is need to pass the parameter in the MessageFormat pattern (e.g., {0}).")
		protected void createCVCollection(String pDataCollectionName, String pDictionaryName,
				String pCVFile, String pTermListRestrictionName) {
			DataCollection vDataCollection = createDataCollection(pDataCollectionName, pDictionaryName, pTermListRestrictionName);
			for(int cCVFile = 1; cCVFile <= 5; cCVFile++) {
				vDataCollection.mFilenameList.add(MessageFormat.format(pCVFile, cCVFile));
			}
			mDataCollectionMap.put(pDataCollectionName, vDataCollection);
		}

		public DataCollection createDataCollection(String pDataCollectionName, String pDictionaryName, String pTermListRestrictionName) {
			DataCollection vDataCollection = new DataCollection(pDataCollectionName);
			vDataCollection.mCollectionAddress = Directory.Collection;
			vDataCollection.mDictionaryAddress = Directory.Dictionary;
			vDataCollection.mDictionaryName = pDictionaryName;
			vDataCollection.mTermListRestrictionName = pTermListRestrictionName;

			return vDataCollection;
		}

		public DataCollection getDataCollection(CollectionName pCollectionName) {
			return mDataCollectionMap.get(pCollectionName.name());
		}



}