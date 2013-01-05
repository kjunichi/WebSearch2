package com.cocolog_nifty.kjunichi;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class CheckKeywordsFromUrl {
	static int savedMaxUrlId = -1;

	/**
	 * execute
	 * 
	 */
	public void execute() {

		// HTTPのタイムアウトの設定
		System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		System.setProperty("sun.net.client.defaultReadTimeout", "10000");

		String dbUrl = "jdbc:postgresql://127.0.0.1/websearch";
		String dbUser = "junichi";
		String dbPassWord = "jktp4xe";

		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception ex) {
			System.out.println(ex);
		}

		Statement stmt = null;

		Connection conn2 = null;
		try {
			conn2 = Util.getWebSearchConnection();
		} catch (Exception ex) {
			System.out.println(ex);
			System.exit(-1);
		}

		// URL毎に取得したページタイトルがページ中に含まれるかをチェックする

		// int urlid = 1;
		int urlid = 1608642;

		Runtime rt = Runtime.getRuntime();
		long freeMemSize = rt.freeMemory();
		int savedFindUrlCount = -1;
		ResultSet rs = null;
		try {
			// ホスト名保存用リストの作成
			List<String> savedHostnames = new ArrayList<String>();

			while (urlid > 0) {
				int searchResultCount = 0;
				// wikiページのタイトルを取得する
				List<String> list = null;
				try {
					list = Util.getWikiTitles();
					if (savedHostnames.size() > 2000) {
						System.out.println("savedHostnames.clear");
						savedHostnames.clear();
					}
					System.out.println("Mem info = "
							+ (freeMemSize - rt.freeMemory()));

					// URLテーブルからurlid順に処理対象となるurlを取得する
					stmt = conn2.createStatement();

					// String sql = getDataBaseQuery(urlid);
					// String sql = getXXXQuery(urlid);
					// String sql = getHighRankQuery(urlid);
					String sql = getHighRankQuery2(urlid); 
					// String sql = getFxQuery(urlid);

					CheckKeywordsClient clients[] = new CheckKeywordsClient[100];

					// 検索実行
					System.out.println("sql" + sql);
					rs = stmt.executeQuery(sql);

					if (rs != null) {
						int i = 0;
						while (rs.next() && i < 100) {
							if (rs.getObject(1) != null) {
								String url = rs.getObject(1).toString();
								savedMaxUrlId = Integer.parseInt(rs.getObject(2).toString());
								// すでに取得予定のURLと同一ホストかチェックする。
								URL checkUrl = null;
								try {
									checkUrl = new URL(url);

									String hostname = checkUrl.getHost();
									boolean isSavedHostname = false;
									for (String savedHostname : savedHostnames) {
										if (hostname.equals(savedHostname)) {
											// すでに取得したドメインの場合、除外する。
											isSavedHostname = true;
										}
									}
									if (!isSavedHostname) {
										// 取得していないホストの場合、解析処理を行う。
										clients[i] = new CheckKeywordsClient(rs
												.getInt(2), url, list);
										// キーワードが含まれるかスレッドごとにチェックする。
										clients[i].start();
										savedMaxUrlId = clients[i].getUrlid();
										i++;
										savedHostnames.add(hostname);
									}
								} catch (MalformedURLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (OutOfMemoryError oome) {
									oome.printStackTrace();
								}
							}

						}

						searchResultCount = i;
					}
					rs.close();
					rs = null;
					stmt.close();
					stmt = null;
					//
					savedHostnames.clear();
					System.out.println("searchResultCount = "
							+ searchResultCount);
					if (searchResultCount == 0) {
						// urlid = -1;
						// break;
						urlid = urlid + 100;
						continue;
					}
					// スレッドの処理を待つ
					for (int i = 0; i < searchResultCount; i++) {
						int waitTime = 10 * 1000 - i * 2000;
						if (waitTime <= 0) {
							waitTime = 500;
						}
						clients[i].join(waitTime);
						System.out.println("clients.joined : " + i
								+ " urlid = " + clients[i].getUrlid()
								+ " status = " + clients[i].getStatus());
						System.out.println(" <-url : " + clients[i].getUrl());
					}
					// キーワードを順番にDBに登録していく。
					CheckKeywordsDao checkKeywordsDao = CheckKeywordsDao
							.getInstance();
					UrlDao urlDao = UrlDao.getInstance();

					for (int i = 0; i < searchResultCount; i++) {
						List<String> insertKeywords = clients[i]
								.getHasKeywords();
						String insertUrl = clients[i].getUrl();
						if (Util.isOkUrl(insertUrl)) {
							int insertUrlid = clients[i].getUrlid();
							urlDao.updateRank(insertUrlid, insertKeywords
									.size(), clients[i].getStatus());
							try {
								for (String keyword : insertKeywords) {
									checkKeywordsDao.insertData(keyword,
											insertUrlid, insertUrl);
								}
							} catch (ConcurrentModificationException cme) {
								cme.printStackTrace();
							}
						}
					}
					// 見つけたUrlも登録する。

					RelatedImageDao relatedImageDao = RelatedImageDao
							.getInstance();

					for (int i = 0; i < searchResultCount; i++) {
						List<String> keywords = clients[i].getHasKeywords();
						if (keywords.size() > 0) {
							// キーワードを含むページに含まれるリンクのみを登録する。
							List<String> insertLinks = clients[i].getLinks();
							try {
								for (String link : insertLinks) {
									if (Util.isOkUrl(link)) {
										// URLの登録
										urlDao.insertData(link, clients[i]
												.getUrlid());

									}
									if (Util.isImageUrl(link)) {
										// 画像に関連しそうなキーワードを登録
										for (String keyword : keywords) {
											// 画像として不要なキーワードを除外
											if (keyword.equals("リンク集")) {
												continue;
											}

											relatedImageDao.insertData(keyword,
													link);
										}
									}
								}// end for
							} catch (ConcurrentModificationException cme) {
								// エラーを無視して続行
								cme.printStackTrace();
							}
						} // end if
					} // end for
					// 処理済みのurlidの取得
					urlid = clients[searchResultCount - 1].getUrlid();
					System.out.println(new Date().toString() + " done urlid = "
							+ urlid + "\nsearchResultCount = "
							+ searchResultCount);
					System.out.println("savedFindUrlCount = "
							+ savedFindUrlCount);
					if (savedFindUrlCount == -1) {
						savedFindUrlCount = searchResultCount;
					}
					if (searchResultCount < 100 && savedFindUrlCount < 100) {
						// urlid = -1;
						// System.out.println("savedFindUrlCount = " +
						// savedFindUrlCount)
					}

				} catch (OutOfMemoryError ome) {
					// メモリー不足は無視
					System.out.println("savedHostnames.clear");
					savedHostnames.clear();
				} catch (org.postgresql.util.PSQLException psqle) {
					// PostgreSQLのメモリ不足も無視して続行する
					System.out.println("savedHostnames.clear");
					savedHostnames.clear();
				}
				savedFindUrlCount = searchResultCount;
				// urlidを進める。
				urlid = urlid + 1;
				if (list != null) {
					list.clear();
				}
			}// end while
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	

	private String getFxQuery(int urlid) {
		return "select u.url,u.urlid from url u,meisi m1 where u.urlid = m1.urlid "
				+ " and (m1.meisi='為替' or m1.meisi='政府系ファンド' or m1.meisi='FX') and u.urlid < "
				+ urlid + "  " + " order by urlid desc limit 100";

	}

	private String getXXXQuery(int urlid) {
		return "select u.url,u.urlid from url u,meisi m1 where u.urlid = m1.urlid "
				+ " and  u.status is null and m1.meisi='アナル' and u.urlid < "
				+ urlid + "  " + " order by urlid desc limit 100";

	}

	private String getMacQuery(int urlid) {
		return "select u.url,u.urlid from url u,meisi m1 where u.urlid = m1.urlid "
				+ " and m1.meisi='MacBook' and u.urlid < "
				+ urlid
				+ "  "
				+ " order by urlid desc limit 100";

	}

	private String getDataBaseQuery(int urlid) {
		return "select u.url,u.urlid from url u,meisi m1 where u.urlid = m1.urlid "
				+ " and (m1.meisi='主キー' or m1.meisi='関数従属' or m1.meisi='正規化') and u.urlid < "
				+ urlid + "  " + " order by urlid desc limit 100";

	}

	private String getHighRankQuery(int urlid) {
		return "select u.url,u.urlid from url u,link2 l where u.urlid < "
				+ urlid
				+ " and u.url like '%tdiary%html' and u.url=l.url and 2 < (select u2.rank from url u2 where u2.urlid=l.source_urlid)"
				+ " and (u.status is null)" + " order by urlid desc limit 100";

	}

	static int targetUrlid = 622510953;

	private String getHighRankQuery2(int urlid) {
		// return "select u.url,u.urlid from url u,link2 l where u.urlid < "
		// + urlid
		// +
		// " and u.url like '%html' and u.url=l.url and 2 < (select u2.rank from url u2 where u2.urlid=l.source_urlid)"
		// + " and (u.status is null)"
		// + " order by urlid desc limit 25000";
		if (savedMaxUrlId == -1) {
			targetUrlid = targetUrlid;
		} else {
			targetUrlid = savedMaxUrlId;
		}
		return "select u.url,u.urlid from url u where url like 'http://%tdiary/%' and (u.status is null and (rank >=0 or rank is null) ) and u.urlid > "
				+ targetUrlid + " order by urlid limit 1000";
	}

	private List<String> getKeywordsFromUrl(String url) {
		List<String> keywords = new ArrayList<String>();
		// CheckKeywordsClient client = new CheckKeywordsClient(-1,url);

		return keywords;
	}

	/**
	 * 登録する価値があるURLか判定する
	 * 
	 * @param url
	 * @return
	 */
	private boolean isWorthyUrl(String url) {
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new CheckKeywordsFromUrl().execute();
	}
}
