package com.cocolog_nifty.kjunichi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class CheckKeywordsClientHttpUnit extends Thread {
	private int urlid = -1;
	private String url = null;
	private List<String> keywords = null;

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public int getUrlid() {
		return urlid;
	}

	public void setUrlid(int urlid) {
		this.urlid = urlid;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public CheckKeywordsClientHttpUnit(int urlid, String url, List<String> keywords) {
		setUrlid(urlid);
		setUrl(url);
		setKeywords(keywords);
	}

	@Override
	public void run() {
		

		try {
			WebConversation wc = new WebConversation();
			wc.setHeaderField("Referer", "http://kjunichi.cocolog-nifty.com/misc/");
			
			// ページを表示する
			WebResponse homePage = wc.getResponse(this.getUrl());
			if (homePage.getContentType().indexOf("text") < 0) {
				return;
			}
			
			String text = homePage.getText();

			// ページの解析
			// ページ中にwikiのタイトルが含まれるかをチェック
			for (String keyword : getKeywords()) {
				if (text.indexOf(keyword) > -1) {
					System.out.println(url + " : " + keyword);
					insertData(keyword);
				}
			}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("<- urlid = " + this.getUrlid());
			System.out.println("<- url = " + this.getUrl());
		}
	}

	private void insertData(String keyword) {
		CheckKeywordsDao checkKeywordsDao = CheckKeywordsDao.getInstance();
		checkKeywordsDao.insertData(keyword,this.getUrlid(),this.getUrl());
	}
}
