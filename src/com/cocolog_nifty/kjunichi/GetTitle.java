package com.cocolog_nifty.kjunichi;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

public class GetTitle {
	private String convertTitleFromFileName(String filepath) {
		// ファイル名からパスを取り除く
		String[] items = filepath.split("/");
		String filename = items[items.length - 1];
		System.out.println("filename = " + filename);
		byte bt[] = new byte[filename.length()];
		int btCount = 0;
		for(int i = 0; i < filename.length()-4; i+=2) {
			
			bt[btCount++] = (byte)Integer.valueOf(filename.substring(i, i+2), 16).intValue();
			//System.out.print(filename.substring(i, i+2)+":");
		}
		String tmp = "";
		byte nBt[] = new byte[btCount];
		for(int i =0;i<btCount;i++){
			nBt[i]=bt[i];
		}
		try {
			tmp = new String(nBt, "euc-jp");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp;

	}

	public void execute() {
		String dbUrl = "jdbc:postgresql://192.168.0.198/smartmemo3";
		String dbUser = "";
		String dbPassWord = "";

		Connection conn = null;
		Statement stmt = null;

		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception ex) {
			System.out.println(ex);
		}
		try {
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassWord);

		} catch (Exception ex) {
			System.out.println(ex);
		}
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
						//System.out.println(rs.getObject(1).toString());
						System.out.println(convertTitleFromFileName(rs
								.getObject(1).toString()).replaceAll("　", "")
								+ " : " + rs.getObject(1).toString());

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
		new GetTitle().execute();
	}

}
