package com.cocolog_nifty.kjunichi;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class GetRelatedUrl {
	private String convertTitleFromFileName(String filepath) {
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
		String tmp = "";
		try {
			byte nb[] = new byte[btCount];
			for (int i = 0; i < btCount; i++) {
				nb[i] = bt[i];
			}
			tmp = new String(nb, "euc-jp");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp;

	}

	private List<String> getUrlListByKeyword(String keyword) {
		String dbUrl = "jdbc:postgresql://192.168.0.178/websearch";
		// String dbUrl = "jdbc:postgresql://192.168.0.192/websearch";
		String dbUser = "junichi";
		String dbPassWord = "jktp4xe";
		System.out.println(keyword);
		List urls = new ArrayList<String>();

		Connection conn = null;

		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception ex) {
			System.out.println(ex);
		}
		try {
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassWord);
			/*
			 * { Statement stmt; try { stmt = conn.createStatement();
			 * stmt.executeUpdate("set client_encoding to 'EUC-JP'"); } catch
			 * (SQLException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } }
			 */
		} catch (Exception ex) {
			System.out.println(ex);
		}
		PreparedStatement stmt = null;
		try {

			// SQL文の作成
			// String sql = "select url from url u,meisi m where u.urlid=m.urlid
			// and m.meisi like ?";
			String sql = "select url from url u,meisi m where u.urlid = m.urlid and m.meisi like '"
					+ keyword + "%'";
			stmt = conn.prepareStatement(sql);
			// 検索実行
			// stmt.setString(1, "'"+new
			// String(keyword.getBytes("euc-jp"),"iso-8859-1") + "%'");
			// stmt.setString(1, "'"+ keyword + "'");
			ResultSet rs = stmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					if (rs.getObject(1) != null) {

						urls.add(rs.getObject(1).toString());
					}
				}

			}
			rs.close();
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return urls;
	}

	private void insertUrl(PreparedStatement pstmt, String keyword,
			List<String> urlList) {
		for (Object url : urlList) {
			// System.out.println(url);
			// related_urlテーブルに登録
			try {
				pstmt.setString(1, (String) keyword);
				pstmt.setString(2, (String) url);
				int rs = pstmt.executeUpdate();
				System.out.println(url);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
	}

	public void execute() {
		String dbUrl = "jdbc:postgresql://192.168.0.192/smartmemo3";
		String dbUser = "junichi";
		String dbPassWord = "jkap5855";

		String dbUrl2 = "jdbc:postgresql://192.168.0.178/websearch";
		String dbUser2 = "junichi";
		String dbPassWord2 = "jkap5855";

		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception ex) {
			System.out.println(ex);
		}

		Connection conn = null;
		Statement stmt = null;

		try {
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassWord);

		} catch (Exception ex) {
			System.out.println(ex);
		}
		Connection conn2 = null;
		try {
			conn2 = DriverManager.getConnection(dbUrl2, dbUser2, dbPassWord2);
		} catch (Exception ex) {
			System.out.println(ex);
		}

		List<String> list = new ArrayList<String>();
		try {
			stmt = conn.createStatement();
			// SQL文の作成
			String sql = "select distinct filename from yuki_wiki where filename like '%/YukiWiki/wiki/%'";

			// 検索実行
			ResultSet rs = stmt.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					if (rs.getObject(1) != null) {
						// タイトルを表示
						// System.out.println(rs.getObject(1).toString());

						list.add(convertTitleFromFileName(rs.getObject(1)
								.toString()));
					}
				}

			}
			rs.close();
		} catch (Exception ex) {
			System.out.println(ex);
		}

		// SQL文の作成
		String sql = "insert into related_url(meisi,url,lastupdate) values (?,?,now())";
		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 取得したページタイトルごとに関連URLを取得する
		for (Object keyword : list) {
			System.out.println(keyword);
			List<String> urlList = getUrlListByKeyword((String) keyword);

			insertUrl(pstmt, (String) keyword, urlList);

		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new GetRelatedUrl().execute();
	}

}
