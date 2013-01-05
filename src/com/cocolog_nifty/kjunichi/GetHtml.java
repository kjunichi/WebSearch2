package com.cocolog_nifty.kjunichi;

import java.io.IOException;
import java.net.MalformedURLException;

import org.xml.sax.SAXException;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class GetHtml {

	/**
	 * @param args
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, IOException, SAXException {
		String url = "http://kjunichi.cocolog-nifty.com/misc/";
		WebConversation wc = new WebConversation();
		WebResponse homePage = wc.getResponse(url);
		System.out.println(homePage.getResponseCode());

		// 対象のページをテキストで取り出す
		String text = homePage.getText();
		System.out.println(text);
	}
}
