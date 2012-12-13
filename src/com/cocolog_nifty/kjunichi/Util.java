package com.cocolog_nifty.kjunichi;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
	public static Connection getSmartMemoConnection() {
		String dbUrl = Const.SM_DB_URL;
		String dbUser = Const.SM_DB_USER;
		String dbPassWord = "";
		Connection conn = null;
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassWord);

		} catch (Exception ex) {
			System.out.println(ex);
		}
		return conn;
	}

	public static Connection getWebSearchConnection() {
		String dbUrl = Const.WS_DB_URL;
		String dbUser = Const.WS_DB_USER;
		String dbPassWord = "jktp4xe";
		Connection conn = null;
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassWord);
		} catch (Exception ex) {
			System.out.println(ex);
			conn = null;
		}
		return conn;
	}

	/**
	 * Wikiのタイトルを取得する
	 */
	public static List<String> getWikiTitles() {
		Connection conn = Util.getSmartMemoConnection();
		
		List<String> list = new ArrayList<String>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			// SQL文の作成
			String sql = "select distinct filename from yuki_wiki where filename like '%/YukiWiki/wiki/%'";

			// 検索実行
			rs = stmt.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					if (rs.getObject(1) != null) {

						// 普通の文字列に変換して保持する。
						list.add(Util.convertTitleFromFileName(rs.getObject(1)
								.toString()));
						// System.out.println(Util.convertTitleFromFileName(rs.getObject(1)
						// .toString()));
					}
				}

			}
		} catch (Exception ex) {
			System.out.println("Util.getWikiTitle " + ex);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * YukiWiki形式のファイルパスからファイル名を取得する
	 * 
	 * @param filepath
	 * @return
	 */
	public static String convertTitleFromFileName(String filepath) {
		// ファイル名からパスを取り除く
		String[] items = filepath.split("/");
		String filename = items[items.length - 1];

		byte bt[] = new byte[filename.length()];
		int btCount = 0;
		for (int i = 0; i < filename.length() - 4; i += 2) {

			bt[btCount++] = (byte) Integer.valueOf(
					filename.substring(i, i + 2), 16).intValue();
			// System.out.print(filename.substring(i, i+2)+":");
		}

		byte nb[] = new byte[btCount];
		for (int i = 0; i < btCount; i++) {
			nb[i] = bt[i];
		}
		String tmp = null;
		try {
			tmp = new String(nb, "euc-jp");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp;

	}

	/**
	 * 画像のリンクかチェック
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isImageUrl(String url) {
		if (url.endsWith("jpg") || url.endsWith("JPG") || url.endsWith("jpeg")
				|| url.endsWith("JPEG") || url.endsWith("png")
				|| url.endsWith("PNG") || url.endsWith("gif")
				|| url.endsWith("GIF")) {
			return true;
		}
		return false;
	}

	/**
	 * 登録するべきURLか判定する
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isOkUrl(String url) {
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
	/**
	 * 
	 * @param url
	 * @param keywords
	 * @return
	 */
	static boolean hasKeyWords(String url, List<String> keywords) {

		return false;
	}
}
