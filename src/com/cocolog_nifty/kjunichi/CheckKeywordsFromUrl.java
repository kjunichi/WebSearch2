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

		String dbUrl2 = "jdbc:postgresql://127.0.0.1/websearch";
		String dbUser2 = "";
		String dbPassWord2 = "";

		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception ex) {
			System.out.println(ex);
		}

		Statement stmt = null;

		Connection conn2 = null;
		try {
			conn2 = DriverManager.getConnection(dbUrl2, dbUser2, dbPassWord2);
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
						if (this.isOkUrl(insertUrl)) {
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
									if (this.isOkUrl(link)) {
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

	/**
	 * 登録するべきURLか判定する
	 * 
	 * @param url
	 * @return
	 */
	private boolean isOkUrl(String url) {
		try {
			URL checkUrl = new URL(url);
			String checkHost = checkUrl.getHost();
			//
			if (checkHost.indexOf("d-064.com") > 0) {
				return false;
			}
			if (checkHost.indexOf("d-064.com") > 0) {
				return false;
			}
			
			if (checkHost.indexOf("apple.com") > 0) {
				return false;
			}
			if (checkHost.indexOf("microsoft.com") > 0) {
				return false;
			}
			if (checkHost.indexOf("zdnet.com") > 0) {
				return false;
			}
			if (checkHost.indexOf("nttdocomo.co.jp") > 0) {
				return false;
			}
			if (checkHost.indexOf("itmedia.co.jp") > 0) {
				return false;
			}
			if (checkHost.indexOf("hmv.co.jp") > 0) {
				return false;
			}
			if (checkHost.indexOf("wikipedia.org") > 0) {
				return false;
			}
			if (checkHost.indexOf("goodbox-pc.com") > 0) {
				return false;
			}
			if (checkHost.indexOf("www.9819.jp") > 0) {
				return false;
			}
			
			if (checkHost.indexOf("atmarkit.co.jp") > 0) {
				return false;
			}
			if (checkHost.indexOf("nikkeibp.co.jp") > 0) {
				return false;
			}
			if (checkHost.indexOf("ec-shopping.net") > 0) {
				return false;
			}
			if (checkHost.indexOf("tsutaya.co.jp") > 0) {
				return false;
			}
			if (checkHost.indexOf("impress.co.jp") > 0) {
				return false;
			}
			if (checkHost.indexOf("msn.com") > 0) {
				return false;
			}
			if (checkHost.indexOf("msn.co.jp") > 0) {
				return false;
			}
			if (checkHost.indexOf("google.co.jp") > 0) {
				return false;
			}
			if (checkHost.indexOf("rakuten.co.jp") > 0) {
				return false;
			}
			if (checkHost.indexOf("aol.com") > 0) {
				return false;
			}
			if (checkHost.indexOf("dell.com") > 0) {
				return false;
			}
			if (checkHost.indexOf("google.co.jp") > 0) {
				return false;
			}
			if (checkHost.indexOf("msdn.microsoft.com") > 0) {
				return false;
			}
			if (checkHost.indexOf("search.cpan.org") > 0) {
				return false;
			}
			if (checkHost.indexOf("wikipedia.org") > 0) {
				return false;
			}
			if (checkHost.indexOf("a9.com") > 0) {
				return false;
			}
			if (checkHost.indexOf("altavista.com") > 0) {
				return false;
			}
			if (checkHost.indexOf("google.com") > 0) {
				return false;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		if (url.startsWith("http://www.google.com/")) {
			return false;
		}
		if (url.indexOf(".wikimedia.org/") > 0) {
			return false;
		}
		if (url.indexOf("ads/adclick.php/") > 0) {
			return false;
		}
		if (url.indexOf(".fc2.com/?mode=edit") > 0) {
			return false;
		}
		if (url.indexOf(".fc2.com/?admin&vcr=") > 0) {
			return false;
		}
		if (url.indexOf(".auctions.yahoo.co.jp") > 0) {
			return false;
		}
		if (url.indexOf("/update.rb?edit") > 0) {
			return false;
		}
		if (url.indexOf("search.yahoo.com") > 0) {
			return false;
		}

		if (url.indexOf("search?q=cache") > 0) {
			return false;
		}

		if (url.indexOf("excite.co.jp") > 0) {
			return false;
		}
		if (url.indexOf("ezweb.ne.jp") > 0) {
			return false;
		}
		if (url.indexOf("search.biglobe.ne.jp") > 0) {
			return false;
		}
		// shopN/shopNを除外
		Pattern pattern = Pattern.compile(".*shop[0-9]/shop.*");
		Matcher matcher = pattern.matcher(url);
		if (matcher.matches()) {
			return false;
		}
		pattern = Pattern.compile(".*shop/shop.*");
		matcher = pattern.matcher(url);
		if (matcher.matches()) {
			return false;
		}
		pattern = Pattern.compile(".*shop[1-9]0/shop.*");
		matcher = pattern.matcher(url);
		if (matcher.matches()) {
			return false;
		}
		// ドイツ語読めないから
		pattern = Pattern.compile("http://(.*).de/(.*)");
		matcher = pattern.matcher(url);
		if (matcher.matches()) {
			return false;
		}
		if (url.indexOf("/keyword/") > 0) {
			return false;
		}

		if (url.startsWith("http://www2.atwiki.jp/hiroshi/")) {
			return false;
		}
		if (url.startsWith("http://designcheck.seesaa.net/")) {
			return false;
		}
		if (url.startsWith("http://drops07.blog105.fc2.com/")) {
			return false;
		}
		if (url.startsWith("http://www.yamaguchi.net/")) {
			return false;
		}
		if (url.startsWith("http://itosikic.blogspot.com")) {
			return false;
		}
		if (url.startsWith("http://susicyan.blog71.fc2.com/")) {
			return false;
		}
		if (url.startsWith("http://auth.livedoor.com/")) {
			return false;
		}
		if (url.startsWith("https://api.login.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www.gizmodo.jp")) {
			return false;
		}
		if (url.startsWith("http://www.youtube.com")) {
			return false;
		}
		if (url.startsWith("http://ad.doubleclick.net")) {
			return false;
		}

		if (url.startsWith("http://www.am-shop.jp")) {
			return false;
		}
		if (url.startsWith("http://twitter.com")) {
			return false;
		}

		if (url.startsWith("http://www.guardian.co.uk")) {
			return false;
		}
		if (url.startsWith("http://mac.ascii24.com")) {
			return false;
		}
		if (url.startsWith("http://www.zakzak.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www.galfhwl-afca.com")) {
			return false;
		}
		if (url.startsWith("http://key.infopingsv.net")) {
			return false;
		}
		if (url.startsWith("http://www.dmm.co.jp")) {
			return false;
		}
		if (url.startsWith("http://technorati.com")) {
			return false;
		}
		if (url.startsWith("http://www.feelg.net")) {
			return false;
		}
		if (url.startsWith("http://www.mailpia.jp")) {
			return false;
		}
		if (url.startsWith("http://movie.goo.ne.jp")) {
			return false;
		}
		if (url.startsWith("http://profile.mail.goo.ne.jp")) {
			return false;
		}
		if (url.startsWith("http://lolipuni.com/")) {
			return false;
		}
		if (url.startsWith("http://moba7.com")) {
			return false;
		}
		if (url.startsWith("http://www.buy.com")) {
			return false;
		}
		if (url.startsWith("http://slashdot.org")) {
			return false;
		}
		if (url.startsWith("http://www.macromedia.com")) {
			return false;
		}
		if (url.startsWith("http://gsdv-avgqw.com")) {
			return false;
		}
		if (url.startsWith("http://ad.de.doubleclick.net")) {
			return false;
		}
		if (url.startsWith("http://www.google.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www.cnet.com")) {
			return false;
		}
		if (url.startsWith("http://www.cnn.co.jp")) {
			return false;
		}
		if (url.startsWith("http://news.cnet.com")) {
			return false;
		}
		if (url.startsWith("http://keyword.de-search.com")) {
			return false;
		}
		if (url.startsWith("http://topic.sns-shop.net")) {
			return false;
		}
		if (url.startsWith("http://www.kaunet.biz")) {
			return false;
		}
		if (url.startsWith("http://websearch.yahoo.co.jp/")) {
			return false;
		}
		if (url.startsWith("http://www.bnmskj-fdas.com")) {
			return false;
		}
		if (url.startsWith("http://www.bwgw-sdfa.com")) {
			return false;
		}
		if (url.startsWith("http://xn--t8j8a3czftyz68nbvl.jp")) {
			return false;
		}
		if (url.startsWith("http://www.bdjss-skfd.com/")) {
			return false;
		}
		if (url.startsWith("http://clik.macnews.de")) {
			return false;
		}
		if (url.startsWith("http://store.apple.com")) {
			return false;
		}
		if (url.startsWith("http://www.bcfdl-jgjxd.com")) {
			return false;
		}
		if (url.startsWith("http://www.bdjss-skfd.com/")) {
			return false;
		}
		if (url.startsWith("http://eroeroyoutebe.blog119.fc2.com/")) {
			return false;
		}
		if (url.startsWith("http://click.dtiserv2.com/Direct")) {
			return false;
		}
		if (url.startsWith("http://news.navitem.com/")) {
			return false;
		}
		if (url.startsWith("http://erogupetika.blog47.fc2.com/")) {
			return false;
		}
		if (url.startsWith("http://mvnavi.blog114.fc2.com")) {
			return false;
		}
		if (url.startsWith("http://free-movnavi.ewinds.net/SiteJump.aspx")) {
			return false;
		}
		if (url.startsWith("http://hip17.blog10.fc2.com")) {
			return false;
		}
		if (url.startsWith("http://dapinfo.seesaa.net")) {
			return false;
		}
		if (url.startsWith("http://zebrafeeling.blog116.fc2.com")) {
			return false;
		}
		if (url.startsWith("http://www.nikkansports.com")) {
			return false;
		}
		if (url.startsWith("http://www.vector.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www.yomiuri.co.jp/")) {
			return false;
		}
		if (url.startsWith("http://biggner-fx-win.seesaa.net/")) {
			return false;
		}
		if (url.startsWith("http: // www.forest.impress.co.jp/")) {
			return false;
		}
		if (url.startsWith("http://fxmoneytuukadaller.blog118.fc2.com/")) {
			return false;
		}
		if (url.startsWith("http://piripijin.blog77.fc2.com/")) {
			return false;
		}
		if (url.startsWith("http://nikumantosan.seesaa.net/")) {
			return false;
		}
		if (url.startsWith("http://www.infocart.jp/")) {
			return false;
		}
		if (url.startsWith("http://yhagk413voygx.blog112.fc2.com/")) {
			return false;
		}
		if (url.startsWith("http://info-will.com")) {
			return false;
		}
		if (url.startsWith("http://kiseki7.seesaa.net/")) {
			return false;
		}
		if (url.startsWith("http://ayumifx.blog104.fc2.com/")) {
			return false;
		}

		if (url.startsWith("http://www.seesaa.jp/adredirect.pl")) {
			return false;
		}
		if (url.startsWith("http://www.asahi.com/")) {
			return false;
		}
		if (url.startsWith("http://thefreeencyclopedia8.seesaa.net/")) {
			return false;
		}
		if (url.startsWith("http://exploreconcepts9.seesaa.net")) {
			return false;
		}
		if (url.startsWith("http://starmagicsing7.seesaa.net/")) {
			return false;
		}
		if (url.startsWith("http://webresultshowto.seesaa.net")) {
			return false;
		}
		if (url.startsWith("http://flash-search.net/")) {
			return false;
		}
		if (url.startsWith("http://webtuhan.net/")) {
			return false;
		}
		if (url.startsWith("http://thefreeencyclopedia4.seesaa.net")) {
			return false;
		}
		if (url.startsWith("http://auc.a-inventiva.com/")) {
			return false;
		}
		if (url.startsWith("http://cacchao.net")) {
			return false;
		}
		if (url.startsWith("http://pt.afl.rakuten.co.jp")) {
			return false;
		}
		if (url.startsWith("http://thefreeencyclopedia6.seesaa.net")) {
			return false;
		}
		if (url.startsWith("http://sagechoiceshop8.seesaa.net")) {
			return false;
		}
		if (url.startsWith("http://link.blogmura.com")) {
			return false;
		}
		if (url.startsWith("http://hibi.hamazo.tv")) {
			return false;
		}
		if (url.startsWith("http://mvnavidr.blog116.fc2.com")) {
			return false;
		}
		if (url.startsWith("http://search.live.com")) {
			return false;
		}
		if (url.startsWith("http://www.su-jine.com/")) {
			return false;
		}

		if (url.startsWith("http://xn--t8jg5b8bxkj4534dbjn.jp/")) {
			return false;
		}
		if (url.startsWith("http://www-jp.mysql.com/")) {
			return false;
		}
		if (url.startsWith("http://www-it.mysql.com/")) {
			return false;
		}
		if (url.startsWith("http://www.mysql.com")) {
			return false;
		}
		if (url.startsWith("http://www.mysql.de")) {
			return false;
		}
		if (url.startsWith("http://www.mysql.fr")) {
			return false;
		}
		if (url.startsWith("http://miyazooo.blog114.fc2.com")) {
			return false;
		}
		if (url.startsWith("http://www.pagesupli.com")) {
			return false;
		}
		if (url.startsWith("http://crooz.jp")) {
			return false;
		}
		if (url.startsWith("http://www.google.co.jp/imode")) {
			return false;
		}
		if (url.startsWith("http://www.google.com/imode")) {
			return false;
		}
		if (url.startsWith("http://www.google.ru/search")) {
			return false;
		}
		if (url.startsWith("http://del.icio.us")) {
			return false;
		}
		if (url.startsWith("http://money.cnn.com")) {
			return false;
		}
		if (url.startsWith("http://ascii24.com")) {
			return false;
		}

		if (url.startsWith("http://http://www.google.de")) {
			return false;
		}
		if (url.startsWith("http://t-shopping.sakura.ne.jp")) {
			return false;
		}
		if (url.startsWith("http://www.microsoft.com")) {
			return false;
		}
		if (url.startsWith("http://ws.mobile.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://registration.myway.com")) {
			return false;
		}
		if (url.startsWith("http://www.FreeBSD.org/")) {
			return false;
		}
		if (url.startsWith("http://www.freebsd.org/")) {
			return false;
		}

		if (url.startsWith("http://b.hatena.ne.jp/append")) {
			return false;
		}
		if (url.startsWith("http://japan.zdnet.com")) {
			return false;
		}
		if (url.startsWith("http://search.cpan.org")) {
			return false;
		}
		if (url.startsWith("http://www.nifty.com/cgi-bin/cl")) {
			return false;
		}
		if (url.startsWith("http://www.jp.freebsd.org")) {
			return false;
		}
		if (url.startsWith("http://app.blog.livedoor.jp/itigochan1")) {
			return false;
		}
		if (url.startsWith("http://fu-zokumania.com")) {
			return false;
		}
		if (url.startsWith("http://www.ahohayashi.com")) {
			return false;
		}
		if (url.startsWith("http://ganbare.kaidasen.com")) {
			return false;
		}
		if (url.startsWith("http://www.ganbaronet.com")) {
			return false;
		}
		if (url.startsWith("http://hp.vector.co.jp")) {
			return false;
		}

		if (url.startsWith("http://www.dendoshi.com")) {
			return false;
		}
		if (url.startsWith("http://www.gorogoropee.com")) {
			return false;
		}
		if (url.startsWith("http://www.iimonosagashi.com")) {
			return false;
		}
		if (url.startsWith("http://www.item-word.com")) {
			return false;
		}
		if (url.startsWith("http://www.kaimonohiroba.com")) {
			return false;
		}
		if (url.startsWith("http://news.goo.ne.jp")) {
			return false;
		}
		if (url.startsWith("http://www.kensakugogo.com")) {
			return false;
		}
		if (url.startsWith("http://weather.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://japan.cnet.com")) {
			return false;
		}
		if (url.startsWith("http://www.mapion.co.jp")) {
			return false;
		}
		if (url.startsWith("http://rd.search.goo.ne.jp")) {
			return false;
		}
		if (url.startsWith("http://mail.google.com")) {
			return false;
		}
		if (url.startsWith("http://www.itmedia.co.jp")) {
			return false;
		}
		if (url.startsWith("http://isap.izm.lv")) {
			return false;
		}
		if (url.startsWith("http://static.freebsd.org")) {
			return false;
		}
		if (url.startsWith("http://blog.livedoor.com/cms/article/edit")) {
			return false;
		}
		if (url.startsWith("http://b.hatena.ne.jp/entry")) {
			return false;
		}
		if (url.startsWith("http://japan.internet.com")) {
			return false;
		}
		if (url.startsWith("http://clip.livedoor.com")) {
			return false;
		}
		if (url.startsWith("http://journal.mycom.co.jp")) {
			return false;
		}

		if (url.startsWith("http://www.sony.jp")) {
			return false;
		}
		if (url.startsWith("http://www.apple.com")) {
			return false;
		}
		if (url.startsWith("http://kakaku.com")) {
			return false;
		}
		if (url.startsWith("http://click.linksynergy.com")) {
			return false;
		}
		if (url.startsWith("http://hb.afl.rakuten.co.jp")) {
			return false;
		}
		if (url.startsWith("http://hb.afl.rakuten.co.jp")) {
			return false;
		}
		if (url.startsWith("http://developer.apple.com")) {
			return false;
		}
		if (url.startsWith("https://bpcgi.nikkeibp.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www.freebsd.org")) {
			return false;
		}
		if (url.startsWith("http://depart.livedoor.com")) {
			return false;
		}
		if (url.startsWith("http://tenant.depart.livedoor.com")) {
			return false;
		}
		if (url.startsWith("http://search.store.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://hana.rakuuten.com")) {
			return false;
		}
		if (url.startsWith("http://info-month.com/")) {
			return false;
		}
		if (url.startsWith("http://www.ceek.jp")) {
			return false;
		}
		if (url.startsWith("http://auctions.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www-06.ibm.com")) {
			return false;
		}
		if (url.startsWith("http://search.yahoo.com")) {
			return false;
		}
		if (url.startsWith("http://search.nifty.com")) {
			return false;
		}
		if (url.startsWith("http://translation.infoseek.co.jp")) {
			return false;
		}
		if (url.startsWith("http://search.iadb.org/search.asp")) {
			return false;
		}
		if (url.startsWith("http://gogloom.com/shop")) {
			return false;
		}
		if (url.startsWith("http://www.goo.ne.jp/click.php")) {
			return false;
		}
		if (url.startsWith("http://search.cn.yahoo.com")) {
			return false;
		}
		if (url.startsWith("http://rd.xlisting.jp")) {
			return false;
		}
		if (url.startsWith("http://www.seagate.com")) {
			return false;
		}
		if (url.startsWith("http://sredirect.www.infoseek.co.jp")) {
			return false;
		}
		if (url.startsWith("http://blogsearch.google.co.jp")) {
			return false;
		}
		if (url.startsWith("http://picasaweb.google.co.jp")) {
			return false;
		}
		if (url.startsWith("http://books.google.co.jp")) {
			return false;
		}
		if (url.startsWith("http://groups.google.co.jp")) {
			return false;
		}
		if (url.startsWith("http://yoshizawa-hitomi.info/tdiary/update.rb")) {
			return false;
		}
		if (url.startsWith("http://map.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("https://www.google.com")) {
			return false;
		}
		if (url.startsWith("http://news.google.co.jp")) {
			return false;
		}
		if (url.startsWith("http://images.google.co.jp")) {
			return false;
		}
		if (url.startsWith("http://help.yahoo.com")) {
			return false;
		}
		if (url.startsWith("http://us.rd.yahoo.com")) {
			return false;
		}
		if (url.startsWith("http://www.tkensaku.com")) {
			return false;
		}
		if (url.startsWith("http://jmp.search.biglobe.ne.jp")) {
			return false;
		}
		if (url.startsWith("http://jword.search.biglobe.ne.jp")) {
			return false;
		}
		if (url.startsWith("http://image-search.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://ksrd.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://cc.msnscache.com")) {
			return false;
		}
		if (url.startsWith("http://cgi.search.biglobe.ne.jp")) {
			return false;
		}
		if (url.startsWith("http://zone-mizuho.net/diary/update.rb")) {
			return false;
		}
		if (url.startsWith("http://search.map.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www.e-koi-dekita.biz")) {
			return false;
		}
		if (url.startsWith("http://vision.ameba.jp")) {
			return false;
		}
		if (url.startsWith("http://psrd.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www.sake-like.com")) {
			return false;
		}
		if (url.startsWith("http://rd.rakuten.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www.infoseek.co.jp/redirect")) {
			return false;
		}
		if (url.startsWith("http://esearch.rakuten.co.jp")) {
			return false;
		}
		if (url.startsWith("http://item.rakuten.co.jp")) {
			return false;
		}
		if (url.startsWith("http://search.www.infoseek.co.jp")) {
			return false;
		}
		if (url.startsWith("http://news.www.infoseek.co.jp")) {
			return false;
		}
		if (url.startsWith("http://click.pacrimlink.net")) {
			return false;
		}
		if (url.startsWith("http://adult.master-tv.net")) {
			return false;
		}
		if (url.startsWith("http://www.deaiha-yappari.info")) {
			return false;
		}
		if (url.startsWith("http://rdp.rakuten.co.jp")) {
			return false;
		}
		if (url.startsWith("https://susumeru.rakuten.co.jp")) {
			return false;
		}
		if (url.startsWith("http://review.rakuten.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www.chura2.com/")) {
			return false;
		}
		if (url.startsWith("http://dougamuryou.adultadultadultdouga.info")) {
			return false;
		}
		if (url.startsWith("http://www.google.co.jp/language_tools")) {
			return false;
		}
		if (url.startsWith("http://api.adult-tube.info")) {
			return false;
		}
		if (url.startsWith("http://ja.wikipedia.org")) {
			return false;
		}
		if (url.startsWith("http://login.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://edit.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://bookmarks.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www.intel.com")) {
			return false;
		}
		if (url.startsWith("http://www.intel.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www.watch.impress.co.jp")) {
			return false;
		}
		if (url.startsWith("http://www2.toshiba.co.jp")) {
			return false;
		}

		if (url.startsWith("https://edit.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://music.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://video-search.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://blog-search.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://search.chiebukuro.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://psrd.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://srd.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://search.msn.co.jp")) {
			return false;
		}
		if (url.startsWith("https://www.google.com/accounts")) {
			return false;
		}
		if (url.startsWith("http://search.goo.ne.jp")) {
			return false;
		}
		if (url.startsWith("http://www.orz-web.net")) {
			return false;
		}
		if (url.startsWith("http://jjjjjj.ddo.jp")) {
			return false;
		}
		if (url.startsWith("http://jjjjjj.dip.jp/")) {
			return false;
		}
		if (url.startsWith("http://www.store-mix.com")) {
			return false;
		}
		if (url.startsWith("http://auction.item.rakuten.co.jp")) {
			return false;
		}
		if (url.startsWith("http://store.shopping.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://sa.item.rakuten.co.jp")) {
			return false;
		}
		if (url.startsWith("http://ck.jp.ap.valuecommerce.com")) {
			return false;
		}
		if (url.startsWith("http://www.mix21.net")) {
			return false;
		}
		if (url.startsWith("https://login.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://ocntownpage.goo.ne.jp")) {
			return false;
		}
		if (url.startsWith("http://ocnsearch.goo.ne.jp")) {
			return false;
		}
		if (url.startsWith("http://booth.search.auctions.yahoo.co.jp")) {
			return false;
		}
		if (url.startsWith("http://dir.livedoor.com")) {
			return false;
		}
		if (url.startsWith("http://dic.livedoor.com")) {
			return false;
		}
		if (url.startsWith("http://asia.google.com")) {
			return false;
		}
		if (url.startsWith("http://search.livedoor.com/")) {
			return false;
		}
		if (url.startsWith("http://click.adv.livedoor.com")) {
			return false;
		}
		if (url.startsWith("http://shopping.msn.com")) {
			return false;
		}
		if (url.startsWith("http://login.live.com/")) {
			return false;
		}
		if (url.startsWith("http://hdtvsg.blogspot.com/search/label")) {
			return false;
		}
		if (url.startsWith("http://www.amazon.com/")) {
			return false;
		}
		if (url.startsWith("http://www.amazon.co.jp/")) {
			return false;
		}
		if (url.startsWith("http://search.yahoo.co.jp/")) {
			return false;
		}
		if (url.startsWith("http://blogsearch.google.com/blogsearch")) {
			return false;
		}
		if (url.startsWith("http://www.google.com/search")) {
			return false;
		}
		if (url.startsWith("http://www.google.co.jp/reader/")) {
			return false;
		}

		if (url.startsWith("http://wrs.search.yahoo.co.jp/")) {
			return false;
		}
		if (url.startsWith("http://www.adulttube.info/")) {
			return false;
		}

		if (url.startsWith("http://jump.zmapple.com/")) {
			return false;
		}
		if (url.startsWith("http://rd.yahoo.co.jp/")) {
			return false;
		}

		if (url.startsWith("http://www.adultsexual.info/")) {
			return false;
		}

		if (url.startsWith("http://www.google.co.jp/search")) {
			return false;
		}

		if (url.startsWith("http://search.fresheye.com")) {
			return false;
		}
		if (url.startsWith("http://www.tv-asahi.co.jp")) {
			return false;
		}
		if (url.startsWith("http://b.hatena.ne.jp/keyword")) {
			return false;
		}
		if (url.startsWith("http://a.hatena.ne.jp/")) {
			return false;
		}
		if (url.startsWith("https://www.hatena.ne.jp/login")) {
			return false;
		}
		if (url.startsWith("http://d.hatena.ne.jp/keyword")) {
			return false;
		}
		if (url.startsWith("http://www.084shop.com/key/")) {
			return false;
		}

		return true;
	}

	private String getFxQuery(int urlid) {
		return "select u.url,u.urlid from url u,meisi m1 where u.urlid = m1.urlid "
				+ " and (m1.meisi='為替' or m1.meisi='政府系ファンド' or m1.meisi='FX') and u.urlid < "
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
				+ " and u.url like '%html' and u.url=l.url and 2 < (select u2.rank from url u2 where u2.urlid=l.source_urlid)"
				+ " and (u.status is null)" + " order by urlid desc limit 100";

	}

	static int targetUrlid = 806562316;

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
		return "select u.url,u.urlid from url u where (u.status is null and (rank >=0 or rank is null) ) and u.urlid > "
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
