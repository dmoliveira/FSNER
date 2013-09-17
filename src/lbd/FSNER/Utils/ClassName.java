package lbd.FSNER.Utils;

public class ClassName {
	
	public static String getSingleName(String className) {
		return(className.substring(className.lastIndexOf(Symbol.DOT)+1));
	}

}
