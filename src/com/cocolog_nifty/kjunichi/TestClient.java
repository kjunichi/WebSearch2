package com.cocolog_nifty.kjunichi;

import java.util.List;

import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

import sun.misc.Cleaner;

public class TestClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://www.hellohiro.com/regex.htm";

		try {
			WebConversation wc = new WebConversation();
			//wc.setHeaderField("Referer","");

			// ページを表示する
			WebResponse homePage = wc.getResponse(url);
			if (homePage.getContentType().indexOf("text") < 0) {
				// テキスト以外は対象外
				return;
			}

			// 対象のページをテキストで取り出す
			String text = homePage.getText();

			// ページの解析
			// ページ中にwikiのタイトルが含まれるかをチェック
			List<String> keywords = Util.getWikiTitles();
			for (String keyword : keywords) {
				if (text.indexOf(keyword) > -1) {
					// 一致したリストを一致キーワードリストの追加
					System.out.println(keyword + " : あり");
				} else {
					System.out.println(keyword + " : なし");
				}
			}
		} catch (HttpNotFoundException hnfe) {
			// 404は無視。
			hnfe.printStackTrace();
		} catch (Throwable e) {
			// 各クライアントで発生したエラーは呼び出し元に通知しない。
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

}
