package kusoBotMaker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class Access_db {
	static String Db_host;
	static String Db_Name;
	static String User_Name;
	static String Password;
	// DBに接続

	public static void initAccess_db() {

		// 値の取得
		Properties properties = KbmUtil.properties;
		Db_Name = properties.getProperty("DBNAME");
		Db_host = "jdbc:mysql://" + properties.getProperty("DBHOST") + "/" + Db_Name + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=JST";
		User_Name = properties.getProperty("DBUSER");
		Password = properties.getProperty("DBPASS");

		System.out.println(Db_Name + "\r\n" + Db_host + "\r\n" + User_Name + "\r\n" + Password);
		createTable();// コレはここじゃダメだ
	}

	public static Connection Connect_db() {
		Connection con = null;
		if (Db_Name == null) {
			initAccess_db();
		}

		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			con = (DriverManager.getConnection(Db_host, User_Name, Password));

			// System.out.println("MySQLに接続できました。");
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// System.out.println("MySQLに接続できませんでした。");
			e.printStackTrace();
			// KbmUtil.openMessageBox("データベースに接続できませんでした、\n 設定を確認してください。",
			// "起動失敗", new Shell());
		}

		return con;
	}
	/*
	 * public static void Close_db() { if (con != null) { try { con.close();
	 * System.out.println("MySQLのクローズに成功しました。"); } catch (SQLException e) {
	 * System.out.println("MySQLのクローズに失敗しました。"); } } }
	 */

	public static Connection getprepareStatement() {
		// TODO 自動生成されたメソッド・スタブ
		// PreparedStatement preparedStatement = con.prepareStatement(string);
		return Connect_db();
	}

	private static Boolean isDbtable(String type, String tableNmae) throws SQLException {
		String sql = "SHOW TABLES FROM " + Db_Name + " LIKE ?;";

		Connection con = Connect_db();
		PreparedStatement pstat = con.prepareStatement(sql);
		// pstat.setString(1, type);
		pstat.setString(1, tableNmae);
		ResultSet rs = pstat.executeQuery();
		if (!rs.next()) {
			return false;
		}
		boolean rt = (rs.getString(1).equals(tableNmae));
		con.close();
		return rt;

	}

	private static boolean isColumn(String table, String column) throws SQLException {
		/// String sql = "SHOW COLUMNS FROM ?";
		Connection con = Connect_db();
		PreparedStatement pstat = con.prepareStatement(table);
		ResultSet rs = pstat.executeQuery();
		try {
			while (rs.next()) {
				if (rs.getString("Field").equals(column)) {
					return true;
				}
			}
			return false;
		} finally {
			// TODO: handle finally clause
			con.close();
		}
	}

	private static void AlterTable(String table, String column, String sql) throws SQLException {
		if (!isColumn(table, column)) {
			Connection con = Connect_db();
			PreparedStatement pstat = con.prepareStatement(sql);
			pstat.execute(sql);
			con.close();
		}
	}

	private static void createTable() {
		try {
			if (!isDbtable("table", "accesstokentable")) {
				/*
				 * getprepareStatement( "CREATE TABLE accesstokentable (" +
				 * "User_ID BIGINT NOT NULL," + "Consumer_Key text," +
				 * "Consumer_Secret text," + "Access_Token text," +
				 * "Access_Token_Secret text," +
				 * "bot_enable tinyint(1) DEFAULT NULL," +
				 * "mode_name char(20) NOT NULL DEFAULT '通常'," +
				 * "normal_post_interval int(11) DEFAULT '60'," +
				 * "pause_time int(11) DEFAULT '120'," +
				 * "replytoRT tinyint(1) DEFAULT '0'," +
				 * "PRIMARY KEY (User_ID)," + "UNIQUE (User_ID));")
				 */
				// + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;")
				// .execute();
				Connection con = Connect_db();
				String sql = "CREATE TABLE accesstokentable (" + "User_ID BIGINT NOT NULL," + "Consumer_Key text,"
						+ "Consumer_Secret text," + "Access_Token text," + "Access_Token_Secret text,"
						+ "bot_enable tinyint(1) DEFAULT NULL," + "mode_name char(20) NOT NULL DEFAULT '通常',"
						+ "normal_post_interval int(11) DEFAULT '60'," + "pause_time int(11) DEFAULT '120',"
						+ "replytoRT tinyint(1) DEFAULT '0'," + "PRIMARY KEY (User_ID)," + "UNIQUE (User_ID));";
				PreparedStatement pstat = con.prepareStatement(sql);
				pstat.execute();
				con.close();
			}

			if (!isDbtable("table", "accountmode")) {
				String sql = "CREATE TABLE accountmode (" + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
						+ "User_ID BIGINT DEFAULT NULL," + "mode_name char(20) DEFAULT NULL,"
						+ "mode_type char(20) DEFAULT NULL," + "user_name char(20) DEFAULT NULL,"
						+ "user_url char(100) DEFAULT NULL," + "user_location char(30) DEFAULT NULL,"
						+ "user_description char(160) DEFAULT NULL," + "profile_icon_path char(160) DEFAULT NULL,"
						+ "UNIQUE (id));";

				Connection con = Connect_db();
				PreparedStatement pstat = con.prepareStatement(sql);
				pstat.execute();
				con.close();
			}

			if (!isDbtable("table", "nickname")) {
				String sql = "CREATE TABLE nickname (" + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
						+ "User_ID BIGINT DEFAULT NULL," + "Friends_ID char(20) DEFAULT NULL," + "nickname text,"
						+ "UNIQUE (id))";
				Connection con = Connect_db();
				PreparedStatement pstat = con.prepareStatement(sql);
				pstat.execute();
				con.close();
			}
			if (!isDbtable("table", "posttable")) {
				String sql = "CREATE TABLE posttable (" + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
						+ "User_ID BIGINT DEFAULT NULL," + "Search_str text," + "Post_str text,"
						+ "RT tinyint(1) DEFAULT NULL," + "Fav tinyint(1) DEFAULT NULL,"
						+ "Air tinyint(1) DEFAULT NULL," + "Tw4Me tinyint(1) DEFAULT NULL,"
						+ "Tw4Tl tinyint(1) DEFAULT NULL," + "Normalpost tinyint(1) DEFAULT NULL,"
						+ "Follow tinyint(1) DEFAULT NULL," + "Last_use timestamp NOT NULL ,"
						+ "mode_name char(20) NOT NULL DEFAULT '通常'," + "Priority int(11) NOT NULL DEFAULT 10,"
						+ "LoopLimit int(11) NOT NULL DEFAULT 10," + "Delay int(11) NOT NULL DEFAULT 0,"
						+ "UNIQUE (id)) ;";
				Connection con = Connect_db();
				PreparedStatement pstat = con.prepareStatement(sql);
				pstat.execute();
				con.close();
			}
			AlterTable("SHOW COLUMNS FROM posttable", "Tw4Other",
					"ALTER TABLE `posttable` ADD COLUMN `Tw4Other` TINYINT(1) NOT NULL DEFAULT '1' AFTER `Delay`;");
			AlterTable("SHOW COLUMNS FROM posttable", "Song",
					"ALTER TABLE `posttable` ADD COLUMN `Song` TINYINT(1) NOT NULL DEFAULT '0' AFTER `Tw4Other`,ADD COLUMN `Song_ID` BIGINT(20) NULL AFTER `Song`;");

			if (isDbtable("table", "posttable_mode")) {
				String sql = "drop view posttable_mode";
				Connection con = Connect_db();
				PreparedStatement pstat = con.prepareStatement(sql);
				pstat.execute();
				con.close();
			}
			{
				String sql = "CREATE VIEW posttable_mode AS SELECT " + "posttable.*"
				/*
				 * + "posttable.id AS id, + "posttable.User_ID AS User_ID," +
				 * "posttable.Search_str AS Search_str," +
				 * "posttable.Post_str AS Post_str," + "posttable.RT AS RT," +
				 * "posttable.Fav AS Fav," + "posttable.Air AS Air," +
				 * "posttable.Tw4Me AS Tw4Me," + "posttable.Tw4Tl AS Tw4Tl," +
				 * "posttable.Normalpost AS Normalpost," +
				 * "posttable.Follow AS Follow," +
				 * "posttable.Last_use AS Last_use," +
				 * "posttable.mode_name AS mode_name," +
				 * "posttable.Priority AS Priority," +
				 * "posttable.LoopLimit AS LoopLimit," +
				 * "posttable.Delay AS Delay"
				 */
						+ " FROM (posttable JOIN accesstokentable)"
						+ " WHERE((posttable.mode_name = accesstokentable.mode_name)"
						+ " AND (posttable.User_ID = accesstokentable.User_ID))";
				Connection con = Connect_db();
				PreparedStatement pstat = con.prepareStatement(sql);
				pstat.execute();
				con.close();
			}

			if (!isDbtable("table", "log")) {
				String sql = "CREATE TABLE log (" + "id BIGINT(20) NOT NULL AUTO_INCREMENT,"
						+ "User_ID BIGINT(20) NOT NULL,"
						+ "TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
						+ "Code text," + "Data TEXT NOT NULL," + "PRIMARY KEY (id)," + "KEY TIME (User_ID,TIME),"
						+ "UNIQUE INDEX id_UNIQUE (id ASC));";
				Connection con = Connect_db();
				PreparedStatement pstat = con.prepareStatement(sql);
				pstat.execute();
				con.close();
			}
			if (!isDbtable("table", "accounticonimage")) {
				String sql = "CREATE TABLE accounticonimage (" + "id bigint(20) NOT NULL," + "Icon LONGBLOB,"
						+ "PRIMARY KEY (id)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				Connection con = Connect_db();
				PreparedStatement pstat = con.prepareStatement(sql);
				pstat.execute();
				con.close();

			}
			if (!isDbtable("table", "song_text")) {
				String sql = "CREATE TABLE song_text (" + " Song_id bigint(20) NOT NULL,"
						+ " song_Sequence bigint(20) NOT NULL AUTO_INCREMENT," + " User_ID bigint(20) DEFAULT NULL,"
						+ " Post_str text, Delay int(11) DEFAULT 10," + " PRIMARY KEY (Song_id,song_Sequence)"
						+ " ) ENGINE=MyISAM  DEFAULT CHARSET=utf8;";

				;
				Connection con = Connect_db();
				PreparedStatement pstat = con.prepareStatement(sql);
				pstat.execute();
				con.close();
			}
			if (!isDbtable("table", "song_list")) {
				String sql =

						"CREATE TABLE song_list (" + "Song_id bigint(20) NOT NULL AUTO_INCREMENT," + "Song_Title text,"
								+ "PRIMARY KEY (Song_id)" + ") ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8;";
				Connection con = Connect_db();
				PreparedStatement pstat = con.prepareStatement(sql);
				pstat.execute();
				con.close();
			}

		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

}
