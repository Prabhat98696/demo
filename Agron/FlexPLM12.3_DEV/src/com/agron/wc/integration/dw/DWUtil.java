package com.agron.wc.integration.dw;

import java.util.regex.Pattern;

import com.lcs.wc.util.LCSProperties;

public class DWUtil {

	
	private static  String skipChars = LCSProperties.get("com.agron.wc.integration.dw.skipChars", "*|~");
	private static  Pattern pattern = Pattern.compile("["+skipChars+"]");

	public static String  skipSpecialChars(String s){
		String replacedString = "";
		if(s != null) {
	        replacedString = pattern.matcher(s).replaceAll(" ");	       
	    }
		 return replacedString;
	}
}
