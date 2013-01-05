package com.cocolog_nifty.kjunichi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CheckKeywordsDao {
	static private CheckKeywordsDao checkKeywordsDao = null;
	static private Connection connSmartMemo = null;
	static private Connection connWebSearch = null;

	private void init() {

		try {
			Class.forName("org.postgresql.Driver");
			connSmartMemo =Util.getSmartMemoConnection();
			connWebSearch = Util.getWebSearchConnection();
		} catch (Exception ex) {
			System.out.println(ex);
		}

	}
/**
 * 
 * @param keyword �L�[���[�h
 * @param urlid urlid
 * @param url URL
 */
	public synchronized void insertData(String keyword,int urlid, String url) {
	
		PreparedStatement pstmt = null;

		try {
			// meisi�e�[�u�����L�[���[�h���o�^
			pstmt = connWebSearch
					.prepareStatement("insert into meisi (meisi,urlid,itimestamp)values(?,?,now())");
			pstmt.setString(1, keyword);
			pstmt.setInt(2, urlid);

			pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			// ���AURL�e�[�u����URL���o�^
			String insertSql = "insert into related_url(meisi,url,lastupdate) values (?,?,now())";

			pstmt = connSmartMemo.prepareStatement(insertSql);
			pstmt.setString(1, keyword);
			pstmt.setString(2, url);
			int rs = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			System.out.println(keyword + " �V�K�o�^ : " + url);
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
