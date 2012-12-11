package com.cocolog_nifty.kjunichi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegEx {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String url="http://gsdv-avgqw.com/shop3/shop10/shop9/";
		String url="http://gsdv-avgqw.com/shop/shop10/shop9/";
		Pattern pattern = Pattern.compile(".*shop[0-9]/shop[0-9].*");
		Matcher matcher = pattern.matcher(url);
		if (matcher.matches()) {
			System.out.println("NG");
		}

	}

}
