package com.cocolog_nifty.kjunichi;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Util {

	/**
	 * Wikiのタイトルを取得する
	 */
	public static List<String> getWikiTitles() {
		String dbUrl = "jdbc:postgresql://127.0.0.1/smartmemo3";
		String dbUser = "junichi";
		String dbPassWord = "";
		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception ex) {
			System.out.println(ex);
		}

		Connection conn = null;

		try {
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassWord);

		} catch (Exception ex) {
			System.out.println(ex);
			System.exit(-1);
		}
		List<String> list = new ArrayList<String>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			// SQL文の作成
			String sql = "select distinct filename from yuki_wiki where filename like '%/YukiWiki/wiki/%'";

			// 検索実行
			rs = stmt.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					if (rs.getObject(1) != null) {

						// 普通の文字列に変換して保持する。
						list.add(Util.convertTitleFromFileName(rs.getObject(1)
								.toString()));
						// System.out.println(Util.convertTitleFromFileName(rs.getObject(1)
						// .toString()));
					}
				}

			}
		} catch (Exception ex) {
			System.out.println("Util.getWikiTitle " + ex);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * YukiWiki形式のファイルパスからファイル名を取得する
	 * 
	 * @param filepath
	 * @return
	 */
	public static String convertTitleFromFileName(String filepath) {
		// ファイル名からパスを取り除く
		String[] items = filepath.split("/");
		String filename = items[items.length - 1];

		byte bt[] = new byte[filename.length()];
		int btCount = 0;
		for (int i = 0; i < filename.length() - 4; i += 2) {

			bt[btCount++] = (byte) Integer.valueOf(
					filename.substring(i, i + 2), 16).intValue();
			// System.out.print(filename.substring(i, i+2)+":");
		}

		byte nb[] = new byte[btCount];
		for (int i = 0; i < btCount; i++) {
			nb[i] = bt[i];
		}
		String tmp = null;
		try {
			tmp = new String(nb, "euc-jp");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp;

	}

	/**
	 * 画像のリンクかチェック
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isImageUrl(String url) {
		if (url.endsWith("jpg") || url.endsWith("JPG") || url.endsWith("jpeg")
				|| url.endsWith("JPEG") || url.endsWith("png")
				|| url.endsWith("PNG") || url.endsWith("gif")
				|| url.endsWith("GIF")) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param url
	 * @param keywords
	 * @return
	 */
	static boolean hasKeyWords(String url, List<String> keywords) {

		return false;
	}
}
