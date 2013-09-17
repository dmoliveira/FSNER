package lbd.Utils.Connection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

public class Connection {
	
	protected static final int CONNECTION_TIMEOUT = 60000;
	protected static final int READ_TIMEOUT = 50000;
	protected static final String TAG_META_CHARSET = "CHARSET";
	protected static final String [] CHARSET = {"UTF-8", "ISO-8859-1"};
	protected static final int CHARSET_SEARCH_DEPTH = 10; 
	
	public static boolean executeInSilenceMode = false;
	
	public static BufferedReader createConnectionBySocket(String urlString, String encodeUsed, boolean autoCharSet) {
		
		BufferedReader in = null;
		
		try {
			
			URL url = new URL(urlString);

			String host = url.getHost();
			
			InputStream response = new Socket(host, 80).getInputStream();
			
			in = new BufferedReader(new InputStreamReader(new BufferedInputStream(response)));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return(in);
	}
	
	public static BufferedReader createFastConnection(String urlString, String encodeUsed) {
		
		try {
			return(new BufferedReader (new InputStreamReader ((new URL (urlString)).openStream(), encodeUsed)));
		} catch (IOException e) {
			//e.printStackTrace();
			if(!executeInSilenceMode) System.err.print("\tError (Probably Access Forbidden) ");
		}
		
		return(null);
	}
	
	/**
	 * createConnection: Create a simple connection to a given urlString
	 *  to start to get the information about the page or service
	 * @param urlString The URL to connect
	 * @return The object that is enable to read the information
	 *   got be request
	 */	
	public static BufferedReader createConnection(String urlString, String encodeUsed, boolean autoCharSet) {
		
		URL url;
		BufferedReader in = null;
		
		try {
			
			encodeUsed = (autoCharSet)? findPageEncode(urlString, encodeUsed) : encodeUsed;
			
			url = new URL (urlString);
			
			URLConnection uConnection = url.openConnection();
			uConnection.setConnectTimeout(CONNECTION_TIMEOUT);
			uConnection.setReadTimeout(READ_TIMEOUT);
			uConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");

			in = new BufferedReader (new InputStreamReader (uConnection.getInputStream(), encodeUsed));
			
			waitStreamBeReady(in);
			
		} catch (MalformedURLException e) {
			if(!executeInSilenceMode) e.printStackTrace();
		} catch (SocketTimeoutException e)  {
			if(!executeInSilenceMode) System.err.println("\tError (Read Time Out) ");
		} catch (IOException e) {
			//e.printStackTrace();
			if(!executeInSilenceMode) System.err.println("\tError (Probably Access Forbidden) ");
		}
		
		return(in);
	}
	
	public static String findPageEncode(String urlString, String defaultEncodeUsed) {
		
		String line;
		String encode = defaultEncodeUsed;
		
		int metaCharsetPosition = -1;
		int lineNumber = 0;
		
		boolean foundEncode = false;
		
		try {
			
			URL url = new URL (urlString);
			
			URLConnection uConnection = url.openConnection();
			uConnection.setConnectTimeout(CONNECTION_TIMEOUT);
			uConnection.setReadTimeout(READ_TIMEOUT);
			uConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
			
			InputStream content = (InputStream)uConnection.getInputStream();
			BufferedReader in = new BufferedReader (new InputStreamReader (content, defaultEncodeUsed));
			
			while((line = in.readLine()) != null && !foundEncode && lineNumber < CHARSET_SEARCH_DEPTH) {
				
				lineNumber++;
				line = line.toUpperCase();
				
				metaCharsetPosition = line.indexOf(TAG_META_CHARSET);
				
				if(metaCharsetPosition != -1) {					
					for(int i = 0; i < CHARSET.length; i++) {
						if(line.indexOf(CHARSET[i]) != -1) {							
							encode = CHARSET[i];
							foundEncode = true;
							break;
						}
					}
				}
			}
			
			in.close();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e)  {
			System.err.println("\tError (Read Time Out) ");
		} catch (IOException e) {
			System.err.println("\tError (Probably Access Forbidden) ");
		}
		
		return(encode);
	}
	
	public static void waitStreamBeReady(BufferedReader in) {
		try {
			while (!in.ready())
			     Thread.sleep(100); // wait for stream to be ready.
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
