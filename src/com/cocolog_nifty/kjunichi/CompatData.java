package com.cocolog_nifty.kjunichi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompatData {
	String dbUrl = "jdbc:postgresql://192.168.0.198/websearch";
	// String dbUrl = "jdbc:postgresql://192.168.0.192/websearch";
	String dbUser = "junichi";
	String dbPassWord = "jktp4xe";

	Connection conn = null;
	PreparedStatement pstmtSelectUrlid = null;
	PreparedStatement pstmtDeleteMeisi = null;
	PreparedStatement pstmtEndUrlid = null;
	

	public CompatData() {
		this.init();
	}

	private void init() {

		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception ex) {
			System.out.println(ex);
			System.exit(1);
		}

		// データベースへの接続
		try {
			// データベースに接続する
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
		// ステートメントの宣言

		try {
			String sql = "select u.url from url u where urlid=?";
			pstmtSelectUrlid = conn.prepareStatement(sql);

			String deleteSql = "delete from meisi where urlid>=? and urlid <=?";
			pstmtDeleteMeisi = conn.prepareStatement(deleteSql);
			
			String endSql = "select min(urlid) from url where urlid>?";
			pstmtEndUrlid = conn.prepareStatement(endSql);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

	}

	public void execute() {
		int currentUrlid = 347910681;
		// 削除されたurlidを探す
		int startUrlid = -1;
		boolean prevDelete = false;
		while (currentUrlid < 352969138) {
			try {

				// SQL文の作成
				// 検索実行

				pstmtSelectUrlid.setInt(1, currentUrlid);
				ResultSet rs = pstmtSelectUrlid.executeQuery();
				if (!rs.next()) {
					// 0件の場合
					pstmtEndUrlid.setInt(1, currentUrlid);
					ResultSet rsEnd = pstmtEndUrlid.executeQuery();
					if(rsEnd.next()) {
						int endUrlid = rsEnd.getInt(1);
						if(endUrlid>0){
							System.out.println("Delete startUrlid = "
									+ (currentUrlid));
							System.out.println("Delete endUrlid = "
									+ (endUrlid - 1));
							deleteMeisiByUrlid(startUrlid, endUrlid - 1);	
						}
						
					}
					
					prevDelete = true;
				} else {
					if (prevDelete && startUrlid != -1) {
						// urlidが存在しないのでmeisiテーブル等の該当レコードを削除
						
					}
					prevDelete = false;
				}
				rs.close();
			} catch (Exception ex) {
				System.out.println(ex);
			}
			// 対象urlidを進める
			currentUrlid++;
		}
	}

	private void deleteMeisiByUrlid(int startUrlid, int endUrlid) {
		try {
			pstmtDeleteMeisi.setInt(1, startUrlid);
			pstmtDeleteMeisi.setInt(2, endUrlid);
			int rc = pstmtDeleteMeisi.executeUpdate();
			System.out.println("rc = " + rc);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CompatData compatData = new CompatData();
		compatData.execute();
	}

}
