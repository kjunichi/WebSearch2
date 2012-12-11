package com.cocolog_nifty.kjunichi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.xml.sax.SAXException;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebResponse;

public class CheckRelatedImageFromRelatedUrl {

	private List<String> getUrlListByKeyword(String keyword) {
		String dbUrl = "jdbc:postgresql://192.168.0.178/websearch";
		// String dbUrl = "jdbc:postgresql://192.168.0.192/websearch";
		String dbUser = "";
		String dbPassWord = "";
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

	private void insertUrl(PreparedStatement pstmt, String keyword, String url) {
		// System.out.println(url);
		// related_urlテーブルに登録
		try {
			pstmt.setString(1, (String) keyword);
			pstmt.setString(2, (String) url);
			int rs = pstmt.executeUpdate();
			// System.out.println(url);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

	}

	public void execute() {
		String dbUrl = "jdbc:postgresql://192.168.0.192/smartmemo3";
		String dbUser = "";
		String dbPassWord = "";

		String dbUrl2 = "jdbc:postgresql://192.168.0.178/websearch";
		String dbUser2 = "";
		String dbPassWord2 = "";

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
			System.exit(-1);
		}
		Connection connWebsearch = null;
		try {
			connWebsearch = DriverManager.getConnection(dbUrl2, dbUser2,
					dbPassWord2);
		} catch (Exception ex) {
			System.out.println(ex);
			System.exit(-1);
		}

		try {
			stmt = conn.createStatement();
			// SQL文の作成
			String sql = "select meisi,url from related_url where meisi > '車' order by meisi";

			// 検索実行
			ResultSet rs = stmt.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					if (rs.getObject(1) != null) {
						String urls[] = getImageUrls(rs.getString(2));
						if (urls != null) {
							System.out.println(rs.getString(1));
							for (int i = 0; i < urls.length; i++) {
								System.out.println(urls[i]);
								insertData(rs.getString(1),urls[i]);
							}
						}
					}
				}

			}
			rs.close();
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new CheckRelatedImageFromRelatedUrl().execute();
	}

	private String[] getImageUrls(String url) {
		WebConversation wc = new WebConversation();
		//wc.setHeaderField("Referer", "");

		// ページを表示する
		try {
			WebResponse homePage = wc.getResponse(url);
			WebImage webImages[] = homePage.getImages();
			String urls[] = new String[webImages.length];
			for (int i = 0; i < webImages.length; i++) {
				urls[i] = webImages[i].getSource();
			}
			return urls;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Throwable e){
			e.printStackTrace();
		}
		return null;
	}

	private void insertData(String keyword, String url) {
		RelatedImageDao dao = RelatedImageDao.getInstance();
		dao.insertData(keyword,url);
	}
}
