package com.cocolog_nifty.kjunichi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CheckKeywordsDao {
	static private CheckKeywordsDao checkKeywordsDao = null;
	static private Connection connSmartMemo = null;
	static private Connection connWebSearch = null;

	private void init() {

		String dbUrl = "jdbc:postgresql://127.0.0.1/smartmemo3";
		String dbUser = "";
		String dbPassWord = "";

		String dbUrl2 = "jdbc:postgresql://127.0.0.1/websearch";
		String dbUser2 = "";
		String dbPassWord2 = "";

		try {
			Class.forName("org.postgresql.Driver");
			connSmartMemo = DriverManager.getConnection(dbUrl, dbUser,
					dbPassWord);
			connWebSearch = DriverManager.getConnection(dbUrl2, dbUser2,
					dbPassWord2);
		} catch (Exception ex) {
			System.out.println(ex);
		}

	}
/**
 * 
 * @param keyword キーワード
 * @param urlid urlid
 * @param url URL
 */
	public synchronized void insertData(String keyword,int urlid, String url) {
	
		PreparedStatement pstmt = null;

		try {
			// meisiテーブルにキーワードを登録
			pstmt = connWebSearch
					.prepareStatement("insert into meisi (meisi,urlid,itimestamp)values(?,?,now())");
			pstmt.setString(1, keyword);
			pstmt.setInt(2, urlid);

			pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			// 関連URLテーブルにURLを登録
			String insertSql = "insert into related_url(meisi,url,lastupdate) values (?,?,now())";

			pstmt = connSmartMemo.prepareStatement(insertSql);
			pstmt.setString(1, keyword);
			pstmt.setString(2, url);
			int rs = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			System.out.println(keyword + " 新規登録 : " + url);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized static CheckKeywordsDao getInstance() {
		if(checkKeywordsDao == null) {
			checkKeywordsDao = new CheckKeywordsDao();
			checkKeywordsDao.init();
		}
		return checkKeywordsDao;
	}
}