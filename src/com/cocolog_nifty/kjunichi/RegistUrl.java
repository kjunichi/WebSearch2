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

public class RegistUrl {
	private String url = "";
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		try {
			this.url = new String(url.getBytes("iso-8859-1"),"Windows-31J");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// URL‚ð“o˜^‚·‚é
		String dbUrl = "jdbc:postgresql://192.168.0.178/websearch";
		String dbUser = "";
		String dbPassWord = "";

	}

	public String getRegist() {
		if("".equals(getUrl())) {
			return "";
		}
		String dbUrl = "jdbc:postgresql://192.168.0.178/websearch";
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
		
		try {
			
		
		//	return sb.toString();
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return "";
	}
}
