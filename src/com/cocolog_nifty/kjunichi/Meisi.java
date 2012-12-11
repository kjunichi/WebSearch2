package com.cocolog_nifty.kjunichi;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Meisi {
	private String meisi = "";

	public String getMeisi() {
		return meisi;
	}

	public void setMeisi(String meisi) {
		try {
			this.meisi = new String(meisi.getBytes("iso-8859-1"), "Windows-31J");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getUrl() {
		if ("".equals(getMeisi())) {
			// キーワードがない場合、何もしない。
			return "";
		}
		String[] keywords = getMeisi().split(" ");

		String dbUrl = "jdbc:postgresql://192.168.0.198/websearch";
		String dbUser = "";
		String dbPassWord = "";

		Connection conn = null;
		PreparedStatement stmt = null;

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
		StringBuffer sb = new StringBuffer();

		try {

			// SQL文の作成
			String sql = "select url from url u";

			// 検索条件の編集
			for (int i = 0; i < keywords.length; i++) {
				sql = sql + " ,meisi m" + i;

			}
			sql = sql + " where ";

			for (int i = 0; i < keywords.length; i++) {
				if (i > 0) {
					sql = sql + " and u.urlid=m" + i + ".urlid and m" + i
							+ ".meisi='" + keywords[i] + "'";
				} else {
					sql = sql + " m0.urlid=u.urlid and m0.meisi='"
							+ keywords[i] + "'";

				}
			}

			sb.append(sql);
			stmt = conn.prepareStatement(sql);
			// 検索実行
			// stmt.setString(1, getMeisi());
			ResultSet rs = stmt.executeQuery();

			if (rs != null) {
				while (rs.next()) {
					if (rs.getObject(1) != null) {
						// タイトルを表示
						sb.append("<a href=\"" + rs.getObject(1).toString()
								+ "\">" + rs.getObject(1).toString()
								+ "</a><br>\n");
					}
				}
			}
			stmt.close();
			rs.close();

		} catch (Exception ex) {
			sb.append(ex);
			System.out.println(ex);

		}
		return sb.toString();

	}
}
