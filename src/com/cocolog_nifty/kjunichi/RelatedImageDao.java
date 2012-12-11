package com.cocolog_nifty.kjunichi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RelatedImageDao {
	static private RelatedImageDao checkKeywordsDao = null;
	static private Connection connSmartMemo = null;
	static private Connection connWebSearch = null;

	private void init() {

		String dbUrl = "jdbc:postgresql://127.0.0.1/smartmemo3";
		String dbUser = "";
		String dbPassWord = "";

		try {
			Class.forName("org.postgresql.Driver");
			connSmartMemo = DriverManager.getConnection(dbUrl, dbUser,
					dbPassWord);
		} catch (Exception ex) {
			System.out.println(ex);
		}

	}

	public synchronized void insertData(String keyword, String url) {

		PreparedStatement pstmt = null;

		try {

			// 関連イメージURLテーブルにURLを登録
			String insertSql = "insert into related_image(meisi,url,rank,lastupdate) values (?,?,0,now())";

			pstmt = connSmartMemo.prepareStatement(insertSql);
			pstmt.setString(1, keyword);
			pstmt.setString(2, url);
			int rs = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			// System.out.println(url);
		} catch (SQLException e) {
			if ("23505".equals(e.getSQLState())) {
				// 23505は一意制約違反なので、無視する。
			} else {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// System.out.println(e.getSQLState());
			}
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

	public synchronized static RelatedImageDao getInstance() {
		if (checkKeywordsDao == null) {
			checkKeywordsDao = new RelatedImageDao();
			checkKeywordsDao.init();
		}
		return checkKeywordsDao;
	}
}
