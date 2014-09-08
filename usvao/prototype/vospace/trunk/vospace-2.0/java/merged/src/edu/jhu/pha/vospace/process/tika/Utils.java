package edu.jhu.pha.vospace.process.tika;

public class Utils {
	public static String trimSpaces(String s) {
		s = s.trim();
		s = s.replaceAll(" {2,}", " ");
		return s;
	}
}
