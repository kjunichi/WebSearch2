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

public class GetMeisi {
	
	public void execute() {
		String dbUrl = "jdbc:postgresql://192.168.0.198/websearch";
		String dbUser = "junichi";
		String dbPassWord = "jkap5855";

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
			String sql = "select distinct meisi from meisi";

			// 検索実行
			ResultSet rs = stmt.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					if (rs.getObject(1) != null) {
						// タイトルを表示
						System.out.println(rs.getObject(1).toString());

				
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
		new GetMeisi().execute();
	}

}
