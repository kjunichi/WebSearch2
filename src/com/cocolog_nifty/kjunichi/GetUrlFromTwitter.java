package com.cocolog_nifty.kjunichi;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class GetUrlFromTwitter {

	private String getTwitterPage() {
		String twitterPageUrl = "http://twitter.com/statuses/public_timeline.rss";
		String text = null;
		try {
			WebConversation wc = new WebConversation();
			//wc.setHeaderField("Referer","");

			// ページを表示する
			WebResponse homePage = wc.getResponse(twitterPageUrl);
			// if (homePage.getContentType().indexOf("text") < 0) {
			// // テキスト以外は対象外
			// return null;
			// }

			// 対象のページをテキストで取り出す
			text = homePage.getText();
			
			// 不要なテキストを削除する
			

		} catch (Throwable e) {
			// 各クライアントで発生したエラーは呼び出し元に通知しない。
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return text;
	}

	public void execute() {
		// TwitterのRSSページにアクセスする。
		String contents = getTwitterPage();
		System.out.println(contents);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new GetUrlFromTwitter().execute();
	}
}
