package kusoBotMaker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.IDs;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * @author paputema
 *
 */
public class PostTweet {
	//ツイート一覧用絞込
	static public List<PostTweet> GetPost(long User_ID, String Searchstr) throws SQLException {


		Connection con = Access_db.Connect_db();

		PreparedStatement pstat = con.prepareStatement("SELECT * from posttable WHERE User_ID = ? AND ? REGEXP  Search_str ORDER BY Priority DESC ,Last_use ASC");
		pstat.setLong(1, User_ID);
		pstat.setString(2, Searchstr);
		pstat.execute();
		List<PostTweet> postTweets = GetPostList(pstat.executeQuery());
		pstat.close();
		con.close();

		return postTweets;
	}
	// ノーマルポストを取得する
	static public List<PostTweet> GetPost(long User_ID) throws SQLException {


		Connection con = Access_db.Connect_db();

		PreparedStatement pstat = con.prepareStatement("SELECT * from posttable WHERE User_ID = ?");
		pstat.setLong(1, User_ID);
		pstat.execute();
		List<PostTweet> postTweets = GetPostList(pstat.executeQuery());
		con.close();

		return postTweets;
	}

	public boolean deletePost() throws SQLException {
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement("DELETE from posttable WHERE ID = ?");
		pstat.setLong(1, Id);

		boolean rt = pstat.execute();
		con.close();
		return rt;
	}

	static private List<PostTweet> GetPostList(ResultSet rs) throws SQLException {
		List<PostTweet> rt = new ArrayList<PostTweet>();
		while (rs.next()) {
			rt.add(new PostTweet(rs));
		}
		return rt;
	}

	// ノーマルポストを取得する
	static public PostTweet GetNomalPost(long User_ID) throws SQLException {
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(
				"SELECT * from posttable_mode WHERE User_ID = ?  AND Normalpost = 1 ORDER BY Last_use ASC");
		pstat.setLong(1, User_ID);
		PostTweet postTweet = new PostTweet(pstat.executeQuery());
		con.close();

		return postTweet;
	}

	// 自分へのメンションへのリプライを取得する
	static private PostTweet GetPostMe(long User_ID, String status_tw) throws SQLException {
		// メンションのループによる暴走を防ぐため、30秒以内に使ったものは対象外にする
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(
				"SELECT * from posttable_mode WHERE User_ID = ? AND Tw4Me = 1  AND ? REGEXP  Search_str AND Last_use < current_timestamp() - INTERVAL 300 SECOND  ORDER BY Priority DESC ,Last_use ASC;");
		pstat.setLong(1, User_ID);
		pstat.setString(2, status_tw);
		PostTweet postTweet = new PostTweet(pstat.executeQuery());
		con.close();

		return postTweet;
	}

	// 他の人向けのツイートへのリプライを取得する
	static private PostTweet GetPostOther(long User_ID, String status_tw) throws SQLException {
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(
				"SELECT * from posttable_mode WHERE User_ID = ? AND Tw4Other = 1  AND ? REGEXP  Search_str AND Last_use < current_timestamp() - INTERVAL 300 SECOND  ORDER BY Priority DESC ,Last_use ASC;");
		pstat.setLong(1, User_ID);
		pstat.setString(2, status_tw);
		PostTweet postTweet = new PostTweet(pstat.executeQuery());
		con.close();

		return postTweet;
	}

	// TL上のツイートへのリプライを取得する
	static private PostTweet GetPostTl(long User_ID, String status_tw) throws SQLException {
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(
				"SELECT * from posttable_mode WHERE User_ID = ? AND Tw4Tl = 1  AND ? REGEXP  Search_str AND Last_use < current_timestamp() - INTERVAL 300 SECOND  ORDER BY Priority DESC ,Last_use ASC;");
		pstat.setLong(1, User_ID);
		pstat.setString(2, status_tw);
		PostTweet postTweet = new PostTweet(pstat.executeQuery());
		con.close();

		return postTweet;
	}

	//歌じゃないか確認
	static boolean statusIsSong(Status status)
	{

		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat;
		try {
			pstat = con.prepareStatement(
					"SELECT count(*) FROM kbm_db.song_text where  ? REGEXP CONCAT('^([@0-9A-Za-z_ ]*)*',Post_str,'$');");
			String status_tw = status.getText();
			pstat.setString(1, status_tw );
			ResultSet resultSet = pstat.executeQuery();
			if(!resultSet.next())
			{
				return false;
			}
			int count = resultSet.getInt(1);
			resultSet.close();
			pstat.close();
			con.close();
			return (count > 0) ? true : false ;
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return false;


	}

	// リプライの取得
	public static PostTweet getReplyPostTweet(Twitter twitter, Status status)
			throws SQLException, TwitterException, InterruptedException {

		if (!status.isRetweet())// メンションか確認
		{
			if (IsMentionMe(status, twitter))// 自分宛てのメンション
			{
				return GetPostMe(twitter.getId(), status.getText());
			}
			else if (IsMentionOther(status, twitter))// フォロワー向けのメンション
			{
				return GetPostOther(twitter.getId(), status.getText());
			}
			else if(IsMention(status))// それ以外のメンションは対象外
			{
				return null;
			}
			else
			{
				return GetPostTl(twitter.getId(), status.getText());
			}
		} else {
			// メンションじゃない
			return GetPostTl(twitter.getId(), status.getText());
		}
	}

	private static Boolean IsMention(Status status) {
		Pattern pattern = Pattern.compile("@[0-9a-zA-Z_]");
		Matcher Matcher = pattern.matcher(status.getText());

		return Matcher.find();
	}

	private static Boolean IsMentionMe(Status status, Twitter twitter) throws IllegalStateException, TwitterException {
		return (status.getInReplyToUserId() == twitter.getId());
	}

	private static Boolean IsMentionOther(Status status, Twitter twitter) {

		User user = KbmUtil.getUser(status.getInReplyToUserId(), twitter);
		try {
			return (user != null && status.getUser().getId() != user.getId()) ? twitter.showFriendship(user.getId(), twitter.getId()).isSourceFollowingTarget()
					: false;
		} catch (IllegalStateException | TwitterException e) {
			// TODO 自動生成された catch ブロック
		}
		return false;
	}

	private long Delay;
	private boolean Air;// エアリプにする（@をつけない）

	private boolean Fav;// いいねする

	private boolean Follow;// フォローする

	long Id; // ID

	String mode_name;// モード

	private boolean Normalpost; // リプライじゃない通常のツイート

	private String post_str;// 投稿する文字列

	private boolean RT; // RTする

	private String Search_str;// 検索条件

	// String hit_str;//検索にヒットしたツイート
	private boolean Tw4Me; // 自分宛てのツイートに反応する

	private boolean Tw4Tl; // TL上すべてのツイートに反応する

	private boolean Tw4Other; // 他の人へのリプライに反応する

	public boolean isTw4Other() {
		return Tw4Other;
	}

	public void setTw4Other(boolean tw4Other) {
		Tw4Other = tw4Other;
	}
	private boolean Song; // 歌/掛け合い
	private long Song_ID; //歌/掛け合い用のID

	public long User_ID;

	long Priority;

	public long getPriority() {
		return Priority;
	}

	public void setPriority(long priority) {
		Priority = priority;
	}

	public long getLoopLimit() {
		return LoopLimit;
	}

	public void setLoopLimit(long loopLimit) {
		LoopLimit = loopLimit;
	}

	long LoopLimit;

	public PostTweet(long User_ID, String hit_str) {
		this.User_ID = User_ID;
		// this.hit_str = hit_str;
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public PostTweet(long User_ID) {
		this();
		this.User_ID = User_ID;

		// this.hit_str = hit_str;
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public Timestamp lastuse;
	public PostTweet(long is_Id, String Is_Search_str, String Is_post_str, boolean Is_RT, Boolean Is_Fav,
			boolean Is_Air, boolean Is_Follow,
			boolean Is_Song, long Is_songID) {
		Id = is_Id;
		Search_str = Is_Search_str;
		post_str = Is_post_str;
		RT = Is_RT;
		Fav = Is_Fav;
		Air = Is_Air;
		Follow = Is_Follow;
		Song = Is_Song;
		Song_ID = Is_songID;
	}

	private PostTweet(ResultSet rs) throws SQLException {
		this();
		if (rs.isBeforeFirst()) {
			if (!rs.next()) {
				return;
			}
		}
		try {
			this.User_ID = rs.getLong("User_ID");
			this.Search_str = rs.getString("Search_str");
			this.post_str = rs.getString("post_str");
			this.RT = rs.getBoolean("RT");
			this.Fav = rs.getBoolean("Fav");
			this.Air = rs.getBoolean("Air");
			this.Tw4Me = rs.getBoolean("Tw4Me");
			this.Tw4Tl = rs.getBoolean("Tw4Tl");
			this.Normalpost = rs.getBoolean("Normalpost");
			this.Follow = rs.getBoolean("Follow");
			this.mode_name = rs.getString("mode_name");
			this.Id = rs.getLong("Id");
			this.Priority = rs.getLong("Priority");
			this.LoopLimit = rs.getLong("LoopLimit");
			this.Delay = rs.getLong("Delay");
			this.lastuse = rs.getTimestamp("Last_use");
			this.Tw4Other = rs.getBoolean("Tw4Other");
			this.Song = rs.getBoolean("Song");
			this.Song_ID = rs.getLong("Song_ID");
		} catch (SQLException e) {
			// System.out.println("返信ツイートなし:" + e.getMessage());
		}

		// TODO 自動生成されたコンストラクター・スタブ
	}

	public long getDelay() {
		return Delay;
	}

	public void setDelay(long delay) {
		Delay = delay;
	}

	public PostTweet() {
		// TODO 自動生成されたコンストラクター・スタブ
		this.User_ID = -1;
		this.Search_str = "";
		this.post_str = "";
		this.RT = false;
		this.Fav = false;
		this.Air = false;
		this.Tw4Me = false;
		this.Tw4Tl = false;
		this.Normalpost = false;
		this.Follow = false;
		this.mode_name = "通常";
		this.Id = -1;
		this.Priority = 10;
		this.LoopLimit = 10;
		this.Delay = 0;
		this.Tw4Other = true;
		this.Song = false;
		this.Song_ID = -1;
	}

	public String b2i(Boolean bool) {
		if (bool == true) {
			return "1";
		}
		return "0";
	}

	public long getId() {
		return Id;
	}

	public String getMode_name() {
		return mode_name;
	}

	private String getNickname(User user) {

		String Nickname = "";
		try {

			Nickname = KbmUtil.getNickname(User_ID, user.getId());
		} catch (IllegalStateException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		if (Nickname.equals("")) {
			// Nickname = twitter.showUser(status.getId()).getScreenName();
			Nickname = user.getName();
		}
		// 嫌がらせ防止の為名前と愛称の「＠」を置き換える
		return Nickname.replace("@", "あっと");
	}

	public String getNomalTweet(Twitter twitter) {

		if (post_str == null || post_str.equals("") || post_str.contains("#stop#")) {
			return "";
		}
		String rtPoststr = post_str;
		// 正規表現の置き換え
		rtPoststr = rtPoststr.replaceAll("#group_[0-9]+#", "");
		// リプライ先のユーザー名を置き換え
		if (rtPoststr.contains("#user_name#")) {
			User user = getRandomFollow(twitter);
			rtPoststr = rtPoststr.replaceAll("#user_name#", getNickname(user));
			if (!isAir()) {
				rtPoststr = "@" + user.getScreenName() + " " + rtPoststr;
			}
			rtPoststr = rtPoststr.replaceAll("#@#", " @" + user.getScreenName() + " ");
		}
		return rtPoststr;
	}

	public String getPost_str() {

		return (post_str != null) ? post_str : "";
	}

	private User getRandomFollow(Twitter twitter) {

		User user;
		Random ran = new Random();
		try {
			IDs ids = twitter.getFollowersIDs(-1L);
			int index = ran.nextInt(ids.getIDs().length);
			long[] l_ids = ids.getIDs();
			user = twitter.showUser(l_ids[index]);
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return null;
		}
		return user;

	}

	public ArrayList<String> getReplyTweet(Status status) {
		ArrayList<String> rt = new ArrayList<String>();
		if (post_str == null || post_str.equals("") || post_str.contains("#stop#")) {
			rt.add("");
			return rt;
		}
		String statusText = status.getText();
		if (statusText == null) {
			for (String string : post_str.split("#next#")) {
				rt.add(string);
			}
			return rt;
		}
		String rtPoststr = post_str;
		// 正規表現の置き換え
		Pattern pattern = Pattern.compile(Search_str);
		Matcher Matcher = pattern.matcher(statusText);
		if (Matcher.find()) {
			int g = Matcher.groupCount();
			for (int i = 0; i <= g; i++) {
				if (Matcher.group(i) != null) {
					rtPoststr = rtPoststr.replaceAll("#group_" + i + "#", Matcher.group(i));
				}
			}
		}
		rtPoststr = rtPoststr.replaceAll("#group_[0-9]+#", "");
		// リプライ先のユーザー名を置き換え
		if (rtPoststr.contains("#user_name#")) {
			rtPoststr = rtPoststr.replaceAll("#user_name#", getNickname(status.getUser()));
		}
		if (rtPoststr.contains("#reply_name#")) {
			rtPoststr = rtPoststr.replaceAll("#reply_name#", getReplyNickname(status));
		}
		if (rtPoststr.contains("#reply_at#")) {
			rtPoststr = rtPoststr.replaceAll("#reply_at#", getReplyAt(status));
		}
		for (String string : rtPoststr.split("#next#")) {
			// リプライかエアリプにするか
			if (!Air) {
				string = "@" + status.getUser().getScreenName() + " " + string;
			}
			rt.add(string);
		}
		return rt;
	}

	private String getReplyAt(Status status) {
		String ReplyUserName = "@" + status.getInReplyToScreenName();
			if (ReplyUserName == null || ReplyUserName.equals("")|| ReplyUserName.equals("@")) {
				long ReplyStatusId = status.getInReplyToUserId();
				BotAccount botAccount = KbmUtil.BotAccount(User_ID);
				User user = KbmUtil.getUser(ReplyStatusId,botAccount.twitter);
				if(user != null)
				{
					ReplyUserName = "@" + user.getScreenName();
				}
			}
		return  " " + ReplyUserName + " ";
	}
	public String getReplyNickname(Status status)
	{
		String ReplyUserName = "";
		long ReplyStatusId = status.getInReplyToUserId();
			ReplyUserName = KbmUtil.getNickname(User_ID, ReplyStatusId);
			if (ReplyUserName.equals("")) {
				BotAccount botAccount = KbmUtil.BotAccount(User_ID);


				User user = KbmUtil.getUser(ReplyStatusId,botAccount.twitter);
				if(user != null)
				{
					ReplyUserName = user.getName();
				}
			}

		return ReplyUserName.replaceAll("@", "あっと");
	}

	public String getSearch_str() {
		return (Search_str != null) ? Search_str : "";
	}

	public long getUser_ID() {
		return User_ID;
	}

	public boolean isAir() {
		return Air;
	}

	public boolean isFav() {
		return Fav;
	}

	public boolean isFollow() {
		return Follow;
	}

	public boolean isNormalpost() {
		return Normalpost;
	}

	public boolean isRT() {
		return RT;
	}

	public boolean isTw4Me() {
		return Tw4Me;
	}

	public boolean isTw4Tl() {
		return Tw4Tl;
	}

	// 前後に「'」をつける
	public String ktt(String str) {
		return "\'" + str.replace("\'", "\'\'") + "\'";
	}

	public void setAir(boolean air) {
		Air = air;
	}

	public void setFav(boolean fav) {
		Fav = fav;
	}

	public void setFollow(boolean follow) {
		Follow = follow;
	}

	public void setMode_name(String mode_name) {
		this.mode_name = mode_name;
	}

	public void setNormalpost(boolean normalpost) {
		Normalpost = normalpost;
	}

	public void setPost_str(String post_str) {
		this.post_str = post_str;
	}

	public void setRT(boolean rT) {
		RT = rT;
	}

	public void setSearch_str(String search_str) {
		Search_str = search_str;
	}

	public void setTw4Me(boolean tw4Me) {
		Tw4Me = tw4Me;
	}

	public void setTw4Tl(boolean tw4Tl) {
		Tw4Tl = tw4Tl;
	}

	public void updateDb() throws SQLException {
		String sql = "UPDATE posttable SET" + " Search_str = ?," + " Post_str = ?," + " RT = ?," + " Fav = ?,"
				+ " Air = ?," + " Tw4Me = ?," + " Tw4Tl = ?," + " Normalpost = ?," + " Follow = ?," + " mode_name = ?,"
				+ " Priority = ?," + " LoopLimit = ?," + " Delay = ?," + " Tw4Other = ?," + " Song = ?," + " Song_ID = ?"
				+ " WHERE id = ?;"
				;
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(sql);
		// pstat.setLong(1, User_ID);
		pstat.setString(1, Search_str);
		pstat.setString(2, post_str);
		pstat.setBoolean(3, RT);
		pstat.setBoolean(4, Fav);
		pstat.setBoolean(5, Air);
		pstat.setBoolean(6, Tw4Me);
		pstat.setBoolean(7, Tw4Tl);
		pstat.setBoolean(8, Normalpost);
		pstat.setBoolean(9, Follow);
		pstat.setString(10, mode_name);
		pstat.setLong(11, Priority);
		pstat.setLong(12, LoopLimit);
		pstat.setLong(13, Delay);
		pstat.setBoolean(14, Tw4Other);
		pstat.setBoolean(15, Song);
		pstat.setLong(16, Song_ID);
		pstat.setLong(17, Id);

		pstat.executeUpdate();
		con.close();
	}

	public void insertDb() throws SQLException {
		String sql = "insert into posttable (" + "User_ID ," + "Search_str ," + "Post_str ," + " RT , " + "Fav ,"
				+ "Air ," + "Tw4Me ," + "Tw4Tl ," + "Normalpost," + "Follow," + "mode_name," + "Priority,"
				+ "LoopLimit," + "Delay," + "Tw4Other," + " Song," + " Song_ID" +") " + "value (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(sql);
		pstat.setLong(1, User_ID);
		pstat.setString(2, Search_str);
		pstat.setString(3, post_str);
		pstat.setBoolean(4, RT);
		pstat.setBoolean(5, Fav);
		pstat.setBoolean(6, Air);
		pstat.setBoolean(7, Tw4Me);
		pstat.setBoolean(8, Tw4Tl);
		pstat.setBoolean(9, Normalpost);
		pstat.setBoolean(10, Follow);
		pstat.setString(11, mode_name);
		pstat.setLong(12, Priority);
		pstat.setLong(13, LoopLimit);
		pstat.setLong(14, Delay);
		pstat.setBoolean(15, Tw4Other);
		pstat.setBoolean(16, Song);
		pstat.setLong(17, Song_ID);
		pstat.executeUpdate();
		con.close();
	}

	public void Last_use_update() throws SQLException {
		if(this == null || Id == -1)
		{
			return;
		}
		String sql = "update posttable SET Last_use = current_timestamp WhERE id = ?";
		if(Song)
		{
			sql = sql + " OR Song_ID = ?";
		}
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(sql);
		try {
			pstat.setLong(1, Id);
			if(Song)
			{
				pstat.setLong(2, Song_ID);
			}
			pstat.executeUpdate();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} finally {
			pstat.close();
			con.close();
		}
	}

	public boolean isSong() {
		return Song;
	}

	public void setSong(boolean song) {
		Song = song;
	}

	public long getSong_ID() {
		return Song_ID;
	}

	public void setSong_ID(long song_ID) {
		Song_ID = song_ID;
	}

}
