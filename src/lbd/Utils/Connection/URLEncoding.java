package lbd.Utils.Connection;

import java.io.UnsupportedEncodingException;

public class URLEncoding {

	protected static final String ENCODE_USED = "ISO-8859-1";
	
	public static String encoding(String url) {
		
		String urlEncoded = "";
		
		try {
			urlEncoded = java.net.URLEncoder.encode(url, ENCODE_USED);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return(urlEncoded);
	}
	
	public static String bingEncoding(String url) {
		String urlEncoded = url.replace(" ", "+");
		
		return(urlEncoded);
	}
	
}
