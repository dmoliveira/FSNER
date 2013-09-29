package lbd.TSE;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbd.Utils.Connection.Connection;
import lbd.Utils.Connection.URLEncoding;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class TSEngine implements Runnable {

	//--
	protected final String SPACE = " ";
	protected final String COMMA = ",";
	protected final String NEW_LINE = "\n";

	public static final int THRESHOULD_LIMIT_TWEET_TO_WRITE = 100000;
	public static final int MAX_RESULT_ALLOWED = 1500;
	public static  final int MAX_RESULTS_PER_PAGE = 100;
	public static final int INITIAL_PAGE_NUMBER = 1;
	public static final boolean INITIAL_INCLUDE_ENTITIES = false;

	//--
	protected final String BASE_URL = "http://search.twitter.com/search.json?q=";
	protected final String RPP = "&rpp="; //-- number of results per page (max 100)
	protected final String PAGE = "&page="; //-- page number
	protected final String LANGUAGE = "&lang="; //-- default encoding (ISO 639-1) (en, pt, es)
	protected final String UNTIL = "&until="; //-- return pages before YYYY-MM-DD
	protected final String INCLUDE_ENTITIES = "&include_entities="; //-- some data as metagas, hashtag, etc. (true or false)
	protected final String RESULT_TYPE = "&result_type="; //-- Type of result (mixed, recent, popular)

	//--
	protected int rpp;
	protected int pageNumber;
	protected String untilValue;
	protected boolean includeEntities;
	protected String resultType;
	protected String lang;
	protected String query;

	protected int maxTweetNumber;

	protected final String TWEET_START_TEXT = "\"text\":\"";
	protected final String TWEET_END_TEXT = "\",\"";

	protected List<String> tweetList;
	protected Map<String, Object> tweetMap;
	protected List<List<String>> streamList;
	protected Writer out;

	public static enum State {Executing, Waiting};
	protected State state;
	protected String message;

	protected Thread runner;
	protected final int MAX_TRIAL = 3;

	public TSEngine() {
		this.state = State.Executing;
	}

	public void executeQuery(Writer out, String query, int maxTweetNumber, String language, String resultType,
			List<List<String>> streamList, String message) {

		this.query = query;
		this.lang = language;
		this.resultType = resultType;
		this.pageNumber = INITIAL_PAGE_NUMBER;
		this.includeEntities = INITIAL_INCLUDE_ENTITIES;
		this.streamList = streamList;
		this.message = message;
		this.out = out;

		this.rpp = Math.min(maxTweetNumber, MAX_RESULTS_PER_PAGE);
		this.maxTweetNumber = Math.min(maxTweetNumber, MAX_RESULT_ALLOWED);

		runner = new Thread(this, "TSEngine"); // (1) Create a new thread.
		//System.out.println(runner.getName());
		runner.start(); // (2) Start the thread.
	}

	protected void executeQuery() {

		int lastTweetListSize = 0;
		int maxPageNumber = maxTweetNumber/rpp;

		String fullQuery;
		BufferedReader connection;

		JSONObject jsonObject = null;

		tweetList = new ArrayList<String>();
		tweetMap = new HashMap<String, Object>();

		try {
			while(tweetList.size() < maxTweetNumber && pageNumber <= maxPageNumber &&
					(pageNumber == INITIAL_PAGE_NUMBER || tweetList.size() != lastTweetListSize)) {

				lastTweetListSize = tweetList.size();

				fullQuery = generateQuery(query, rpp, pageNumber, resultType, includeEntities);
				connection = Connection.createConnection(fullQuery, "UTF-8", false);

				//-- Try and wait until the connection get ready or die
				waitConnectionGetReady(connection);

				if(connection != null) {

					jsonObject = new JSONObject(new JSONTokener(connection));

					getTweetText(jsonObject.toString());

					pageNumber++;
					connection.close();
				} else {
					break;
				}

				TSEngineControl.wait(1000);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		synchronized (streamList) {
			this.streamList.addAll(getTweetArrayList());
			this.state = State.Waiting;
			System.out.println(message + " (" + getTweetArrayList().size()  + ")");

			writeStream(out, streamList);

			//-- Only to save memory
			if(streamList.size() > THRESHOULD_LIMIT_TWEET_TO_WRITE && out != null) {
				streamList.clear();
			}
		}
	}

	protected void waitConnectionGetReady(BufferedReader connection) {

		int trialTimes = 0;

		while(connection == null && trialTimes < MAX_TRIAL) {

			trialTimes++;

			TSEngineControl.wait(500);
		}
	}

	protected String generateQuery(String query, int rpp, int pageNumber,
			String resultType, boolean includeEntities) {

		String fullQuery = BASE_URL + URLEncoding.encoding(query);
		fullQuery += RPP + rpp + PAGE + pageNumber + LANGUAGE + lang + RESULT_TYPE + resultType + INCLUDE_ENTITIES + includeEntities;

		return (fullQuery);
	}

	protected void getTweetText(String queryResponse) {

		int tweetStartPosition;
		int tweetEndPosition;

		String tweet;

		while((tweetStartPosition = queryResponse.indexOf(TWEET_START_TEXT)) != -1 && tweetList.size() < maxTweetNumber) {

			queryResponse = queryResponse.substring(tweetStartPosition + TWEET_START_TEXT.length());
			tweetEndPosition = queryResponse.indexOf(TWEET_END_TEXT);
			tweet = queryResponse.substring(0, tweetEndPosition);

			if(!tweetMap.containsKey(tweet)) {
				tweetList.add(tweet);
				tweetMap.put(tweet, null);
				//System.out.println(tweet);
			}

			queryResponse = queryResponse.substring(tweetEndPosition + TWEET_END_TEXT.length());
		}
	}

	public List<String> getTweetList() {
		return(tweetList);
	}

	public List<List<String>> getTweetArrayList() {

		List<List<String>> tweetArrayList = new ArrayList<List<String>>();
		List<String> tweetList;
		String [] tweetArray;

		for(String tweet : this.tweetList) {

			tweetArray = tweet.split(SPACE);
			tweetList = new ArrayList<String>();

			for(int i = 0; i < tweetArray.length; i++) {
				tweetList.add(tweetArray[i]);
			}

			tweetArrayList.add(tweetList);
		}


		return (tweetArrayList);
	}

	public State getState() {
		return(state);
	}

	@Override
	public void run() {
		executeQuery();
	}

	public static void writeStream(Writer out, List<List<String>> streamList) {

		try {

			for(List<String> stream : streamList) {
				for(String term : stream) {
					out.write(term.replace("\\|", "").replace("|", "") + "|Outside\n");
				}
				out.write("\n");
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
