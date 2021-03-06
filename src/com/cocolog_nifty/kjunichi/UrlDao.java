package com.cocolog_nifty.kjunichi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UrlDao {
	static private UrlDao urlDao = null;
	static private Connection connSmartMemo = null;
	static private Connection connWebSearch = null;

	private void init() {
		connSmartMemo = Util.getSmartMemoConnection();
		connWebSearch = Util.getWebSearchConnection();
	}

	public synchronized void insertData(String url, int sourceUrlid) {

		PreparedStatement pstmt = null;

		try {
			// link2テーブルにリンク元urlを登録

			pstmt = connWebSearch
					.prepareStatement("insert into link2 (url,source_urlid,lastupdate)values(?,?,now())");
			pstmt.setString(1, url);
			pstmt.setInt(2, sourceUrlid);
			int recCount = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			if (recCount > 0) {
				// System.out.println("URL新規登録 : " + url);
			}
		} catch (SQLException e) {
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

		try {
			// urlテーブルにurlを登録
			pstmt = connWebSearch
					.prepareStatement("insert into url (url,isnew,timestamp,lastupdate)values(?,'1',now(),now())");
			pstmt.setString(1, url);
			int recCount = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			if (recCount > 0) {
				System.out.println("URL新規登録 : " + url);
			}

		} catch (SQLException e) {
			// 重複登録でエラーが発生することがある。
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

	public synchronized void updateRank(int urlid, int rank, String status) {

		PreparedStatement pstmt = null;

		try {
			// urlテーブルにurlを登録
			pstmt = connWebSearch
					.prepareStatement("update url set rank=?,status=?,lastupdate=now() where urlid = ?");
			pstmt.setInt(1, rank);
			pstmt.setString(2, status);
			pstmt.setInt(3, urlid);
			int recCount = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			if (recCount > 0) {
				System.out.println("URL rank : " + rank);
			}
		} catch (SQLException e) {
			// 重複登録でエラーが発生することがある。
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public synchronized static UrlDao getInstance() {
		if (urlDao == null) {
			urlDao = new UrlDao();
			urlDao.init();
		}
		return urlDao;
	}
}
