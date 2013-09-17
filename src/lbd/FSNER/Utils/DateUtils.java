package lbd.FSNER.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	public static String getSimplyfiedTimeStamp() {
		return  (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date()) ;
	}

	public static String getNow() {
		return  (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date()) ;
	}

}
