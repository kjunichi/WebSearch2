package com.cocolog_nifty.kjunichi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CheckRelatedKeywordsFromUrl {
	private Connection conWebSearch = null;
	private Connection conSmartMemo = null;

	public Connection getConSmartMemo() {
		return conSmartMemo;
	}

	public void setConSmartMemo(Connection conSmartMemo) {
		this.conSmartMemo = conSmartMemo;
	}

	public Connection getConWebSearch() {
		return conWebSearch;
	}

	public void setConWebSearch(Connection conWebSearch) {
		this.conWebSearch = conWebSearch;
	}

	private List<String> getMeisi() {
		List<String> meisis = new ArrayList<String>();
		try {
			Statement stmt = getConSmartMemo().createStatement();
			// SQL文の作成
			//String sql = "select distinct meisi from related_url where meisi > 'procmail' order by meisi";
			String sql = "select distinct meisi from related_url order by meisi";
			
			// 検索実行
			ResultSet rs = stmt.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					String meisi = rs.getString(1);
					meisis.add(meisi);

				}
			}
			rs.close();
			rs = null;
			stmt.close();
			stmt = null;
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return meisis;
	}

	private List<String> getUrlListByKeyword(String keyword) {
		//String dbUrl = "jdbc:postgresql://192.168.0.198/websearch";
		 String dbUrl = "jdbc:postgresql://192.168.0.178/websearch";
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
		String dbUrl = "jdbc:postgresql://192.168.0.198/smartmemo3";
		String dbUser = "";
		String dbPassWord = "";

		String dbUrl2 = "jdbc:postgresql://192.168.0.198/websearch";
		//String dbUrl2 = "jdbc:postgresql://192.168.0.178/websearch";
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
		setConSmartMemo(conn);
		Connection conn2 = null;
		try {
			conn2 = DriverManager.getConnection(dbUrl2, dbUser2, dbPassWord2);
		} catch (Exception ex) {
			System.out.println(ex);
			System.exit(-1);
		}
		setConWebSearch(conn2);

		// wikiページのタイトルを取得する
		List<String> list = new ArrayList<String>();
		List<String> meisis = this.getMeisi();
		for (String meisi : meisis) {
			System.out.println("meisi = " + meisi);
			List<String> urls = new ArrayList<String>();
			try {
				stmt = getConSmartMemo().createStatement();
				// SQL文の作成
				String sql = "select url from related_url where meisi='"
						+ meisi + "' order by url";

				// 検索実行
				ResultSet rs = stmt.executeQuery(sql);
				if (rs != null) {
					while (rs.next()) {
						String url = rs.getString(1);
						urls.add(url);
					}
				}

				rs.close();
			} catch (Exception ex) {
				System.out.println(ex);
			}
			List<String> words = getKeywords(urls, meisis);
			insertRelatedKeywords(meisi, words);

		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new CheckRelatedKeywordsFromUrl().execute();
	}

	private void insertRelatedKeywords(String meisi, List<String> keywords) {
		PreparedStatement pstmt = null;
		try {
			pstmt = getConSmartMemo()
					.prepareStatement(
							"insert into related_keyword (meisi,keyword,lastupdate)values(?,?,now())");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String keyword : keywords) {
			try {
				pstmt.setString(1, meisi);
				pstmt.setString(2, keyword);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				pstmt.executeUpdate();
				System.out.println(meisi + " : " + keyword);

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private List<String> getUrlByMeisi(String meisi) {
		System.out.println("getUrlByMeisi : " + meisi);
		List<String> urls = new ArrayList<String>();
		PreparedStatement stmt = null;
		try {

			// SQL文の作成
			String sql = "select u.url from url u,meisi m where u.urlid = m.urlid and m.meisi=? order by meisi";
			stmt = getConWebSearch().prepareStatement(sql);

			stmt.setString(1, meisi);
			// 検索実行
			ResultSet rs = stmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					if (rs.getObject(1) != null) {
						String url = rs.getString(1);

						urls.add(url);
					}
				}

			}
			rs.close();
		} catch (Exception ex) {
			System.out.println(ex);
		}

		return urls;
	}

	private List<String> getKeywords(List<String> urls, List<String> meisis) {
		System.out.println("getKeywords : begin");
		List<String> doneKeywords = new ArrayList<String>();
		List<String> keywords = new ArrayList<String>();

		PreparedStatement stmt = null;
		try {

			// SQL文の作成
			String sql = "select m.meisi from url u,meisi m where u.urlid = m.urlid and u.url=? order by m.meisi";
			stmt = getConWebSearch().prepareStatement(sql);

			for (String url : urls) {
				stmt.setString(1, url);
				// 検索実行
				ResultSet rs = stmt.executeQuery();
				if (rs != null) {
					while (rs.next()) {
						if (rs.getObject(1) != null) {
							String meisi = rs.getString(1);
							for (String keyword : meisis) {
								if (keyword.equals(meisi)) {
									if (!doneKeywords.contains(meisi)) {
											doneKeywords.add(meisi);
										List<String> targetUrls = getUrlByMeisi(meisi);
										int hitCount = 0;
										for (String targetUrl : targetUrls) {
											for (String srcUrl : urls) {
												if (targetUrl.equals(srcUrl)) {
													hitCount++;
												}
											}

										}
										if (hitCount > 2) {
											keywords.add(meisi);
										}
									}
								}
							}
						}
					}
				}

				rs.close();
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return keywords;
	}
}
