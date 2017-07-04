package kusoBotMaker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserStreamAdapter;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class BotAccount {
	// ノーマルポスト定期投稿クラス
	class NormalPostTimer extends Thread {
		/**
		 *
		 */
		private final BotAccount botAccount;
		String last_tweet = "";
		PostTweet pt;
		Twitter twitter;

		/**
		 * @param botAccount
		 */
		NormalPostTimer(BotAccount botAccount) {
			this.botAccount = botAccount;
			twitter = this.botAccount.twitter;
		}

		@Override
		public void run() {
			try {
				pt = PostTweet.GetNomalPost(this.botAccount.User_ID);
				String tweet = pt.getNomalTweet(twitter);
				pt.Last_use_update();
				if (pt.isSong()) {
					HashSet<BotAccount> botAccounts = new HashSet<>();
					for (songData song : Songs.GetSong(pt.getSong_ID())) {
						botAccounts.add(KbmUtil.BotAccount(song.User_ID));
					}
					for (songData song : Songs.GetSong(pt.getSong_ID())) {
						sleep(song.Delay * 1000);
						String messages = song.Post_str;
						StatusUpdate su = new StatusUpdate(messages);
						Status updstatus = KbmUtil.BotAccount(song.User_ID).twitter.updateStatus(su);
						for (BotAccount botAccount : botAccounts) {
							if (song.User_ID != botAccount.User_ID) {
								botAccount.twitter.retweetStatus(updstatus.getId());
							}
						}
					}
				} else if (tweet != null && !tweet.equals("") && !tweet.equals(last_tweet)) {
					twitter.updateStatus(tweet);
					System.out.println("[ノーマルポスト：" + toString() + "]" + tweet);
				}
				last_tweet = tweet;
			} catch (TwitterException e) {
				System.out.println("[ノーマルポスト失敗：" + toString() + "]:" + e.getErrorMessage());
				onTwitterException(e);
			} catch (SQLException e) {
				System.out.println("[ノーマルポストの取得に失敗]:" + e.getMessage());
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}

	class ModeChangeDayThread extends Thread {
		@Override
		public void run() {
			super.run();
			AccountMode.execModeChangeDay(BotAccount.this);
		}
	}

	public long getNextDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 30);

		Date date = new Date();

		return calendar.getTimeInMillis() - date.getTime();
	}

	private ScheduledFuture<?> modeChangeFuture;

	/*
	 * public class StopBotThread extends Thread { public void run() {
	 * if(botAcountStatus == BotAcountStatus.BOTSTOP) { return; } if
	 * (normalPostFuture != null) { normalPostFuture.cancel(true); } if
	 * (modeChangeFuture != null) { modeChangeFuture.cancel(true); }
	 * twitterStream.cleanUp(); Date date = new Date(); System.out.println("停止["
	 * + date.toString() + "]：" + User_ID); botAcountStatus =
	 * BotAcountStatus.BOTSTOP; AccountMode.execModeChangeStop(BotAccount.this);
	 * } }
	 */

	class BotRestartTimer extends Thread {
		@Override
		public void run() {
			super.run();
			// TODO 自動生成されたメソッド・スタブ
			if (getEnumBotAcountStatus() == enumBotAcountStatus.BOTPAUSE || getEnumBotAcountStatus() == enumBotAcountStatus.BOTSTOP) {
				startBot();
			}
		}
	}

	public void deleteBot()
	{
		twitterStream.cleanUp();
		twitterStream.shutdown();
		stopBot();
		KbmUtil.delbotAccounts(this);
	}

	public Boolean startBot() {
		/* new StartBotThread().start(); */
		if (user == null) {
			try {
				user = twitter.verifyCredentials();
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
				return Enable;
			}
		}
		setBotAcountStatus(enumBotAcountStatus.BOTRUN);
		if (normalPostFuture == null || normalPostFuture.isDone()) {
			NormalPostTimer npt = new NormalPostTimer(BotAccount.this);
			npt.setName("[定期投稿スレッド:" + toString() + "]");
			normalPostFuture = scheduledexec.scheduleAtFixedRate(npt, 0, normalPostInterval, TimeUnit.MINUTES);
		}
		if (modeChangeFuture == null || modeChangeFuture.isDone()) {
			{
				modeChangeFuture = scheduledexec.scheduleAtFixedRate(new ModeChangeDayThread(), getNextDay(), 86400000 / 24,
						TimeUnit.MILLISECONDS);
			}
		}
		if (pauseFuture != null) {
			pauseFuture.cancel(true);
		}
		//startBotTwitterStream();
		try {
			if(botAcountStatus == enumBotAcountStatus.BOTRUN)
			{
				AccountMode.execModeChangeRun(BotAccount.this);
			}
			new ModeChangeDayThread().start();
		} catch (IllegalStateException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return Enable;
		}


		Date date = new Date();
		System.out.println("開始[" + date.toString() + "]：" + toString() + twitterStream.toString());
		return Enable;
	}

	protected void startBotTwitterStream() {
		if(twitterStream != null)
		{
			twitterStream.user();
		}
	}

	protected void stoptBotTwitterStream() {

			twitterStream.shutdown();
	}
	public Boolean stopBot() {
		/* new StopBotThread().start(); */
		if (getEnumBotAcountStatus() == enumBotAcountStatus.BOTPAUSE) {
			return Enable;
		}
		setBotAcountStatus(enumBotAcountStatus.BOTSTOP);
		if (normalPostFuture != null) {
			normalPostFuture.cancel(true);
		}
		if (modeChangeFuture != null) {
			modeChangeFuture.cancel(true);
		}
		//stopBotTwitterStream();
		AccountMode.execModeChangeStop(BotAccount.this);
		Date date = new Date();
		System.out.println("停止[" + date.toString() + "]：" + toString());
		return Enable;
	}



	public class BotAccountUserStreamAdapter extends UserStreamAdapter {
		// リプライ投稿クラス
		class ReplylPostTimer extends Thread {
			ArrayList<String> messages;
			Status status;

			ReplylPostTimer(ArrayList<String> messages, Status status) {
				super("[リプライタイマー]" + user.getName() + ":" + messages);
				this.messages = messages;
				this.status = status;
			}

			@Override
			public void run() {
				super.run();
				try {
					for (String message : messages) {
						if (!message.equals("") && !message.equals("#stop#"))
							ReplyTweet(message, status);
					}
				} catch (TwitterException e) {
					// TODO 自動生成された catch ブロック
					onTwitterException(e);
				} finally {
					this.messages = null;
					this.status = null;
				}
			}
		}

		class ReplylSongPostTimer extends Thread {
			public ReplylSongPostTimer(Status status, PostTweet pt) {
				super("[歌/掛け合いタイマー]" + user.getName() + ":" + pt.getSong_ID());
				this.status = status;
				this.pt = pt;
			}

			Status status;
			PostTweet pt;
			Status updstatus;

			@Override
			public void run() {
				super.run();

				try {
					HashSet<BotAccount> botAccounts = getSongUsers(pt);
					for (songData song : Songs.GetSong(pt.getSong_ID())) {
						sleep(song.Delay * 1000);
						String messages = song.Post_str;
						if (pt.isAir()) {
							messages = (is(song)  ? ("@" + status.getUser().getScreenName() + " ") : "")
									+ messages;
						} else {
							messages = "@" + status.getUser().getScreenName() + " " +
									(is(song) ? ("@" + updstatus.getUser().getScreenName() + " ") : "")
									+ messages;
						}
						StatusUpdate su = new StatusUpdate(messages);
						su.setInReplyToStatusId(is(song)  ? updstatus.getId() : status.getId());
						updstatus = KbmUtil.BotAccount(song.User_ID).twitter.updateStatus(su);
						//System.out.println("[" + new Date().toString() + "]" + "リプライ：" + updstatus.getUser().getName() + ":" + updstatus.getText());
						for (BotAccount botAccount : botAccounts) {
							if (song.User_ID != botAccount.User_ID) {
								//botAccount.twitter.retweetStatus(updstatus.getId());

							}
						}
					}
				} catch (TwitterException e) {
					// TODO 自動生成された catch ブロック
					onTwitterException(e);
				} catch (SQLException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} finally {
					this.pt = null;
					this.status = null;
				}
			}
			boolean is(songData song)
			{
				return (updstatus != null && updstatus.getUser().getId() != song.User_ID);
			}

		}

		// Twitterのコンフィグ、認証情報等を保存するのに使用、もっと良い使い方あるかも
		Configuration conf;
		// データベース
		// Access_db db = new Access_db();
		// 最後にしたツイート
		String last_tweet;

		// 投稿ツイート情報
		PostTweet pt;

		protected BotAccountUserStreamAdapter(Configuration conf) {
			this.conf = conf;
			last_tweet = "";
		}

		private void Favorite(Status status) throws TwitterException {
			// TODO 自動生成されたメソッド・スタブ
			Status i_Status = getRetweet(status);
			if (!i_Status.isFavorited()) {
				twitter.createFavorite(i_Status.getId());
			}
		}

		private void Follow(long UserId) throws TwitterException {
			twitter.createFriendship(UserId);
		}

		private Status getRetweet(Status status) throws TwitterException {

			if (status.isRetweet()) {
				return status.getRetweetedStatus();
			}

			return status;
		}

		// 自分のつぶやき？
		private Boolean isMyTweet(Status status) {
			if (User_ID == status.getUser().getId()) {
				return true;
			} else if (status.isRetweet() && User_ID == status.getRetweetedStatus().getUser().getId()) {
				return true;
			}
			return false;

		}

		@Override
		public void onException(Exception ex) {
			super.onException(ex);
			System.out.println("Exception [" + ex.toString() + "].");
			ex.printStackTrace();
		}

		// フォローされた、source：した人、followedUser：された人
		@Override
		public void onFollow(User source, User followedUser) {
			if (user == null || getEnumBotAcountStatus() != enumBotAcountStatus.BOTRUN) {
				return;
			}
			super.onFollow(source, followedUser);
			try {
				// フォローされたのは自分か？
				if (followedUser.getId() == twitter.getId()) {
					// フォローした相手の使用言語は？
					String lang = source.getLang();
					if (lang.contentEquals("ja")) {
						//自動フォロー無効
						//twitter.createFriendship(source.getId());
					} else if (lang.contentEquals("ku") || lang.contentEquals("ms") || lang.contentEquals("sd")
							|| lang.contentEquals("sa")) {
						System.out.println(lang.toString());

					}
				}

			} catch (IllegalStateException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				onTwitterException(e);
			}
		}

		// ステータス更新のハンドラ
		@Override
		public void onStatus(Status status) {
			super.onStatus(status);
			if (user == null || getEnumBotAcountStatus() != enumBotAcountStatus.BOTRUN) {
				return;
			}
			// ステータスを受け取って何かをする
			KbmUtil.setReplyHistory(status);
			// 自分のつぶやきは無視して即終了
			if (isMyTweet(status)) {
				return;
			}
			if(getEnumBotAcountStatus() != kusoBotMaker.enumBotAcountStatus.BOTRUN)
			{
				return;
			}
			// RTは無視する設定かつ対象がRTだったら無視
			if (!replyRt && status.isRetweet()) {
				return;
			}

			// データベースに問い合わせて当たればリプライを送る
			try {
				// ニックネームのリクエストだったら登録する
				setNickname(status);
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				onTwitterException(e);
			}

			try {
				// フォロー解除のリクエストならフォロー解除
				anFollow(status);
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				onTwitterException(e);
			}
			try {
				pt = PostTweet.getReplyPostTweet(twitter, status);
				if (pt != null) {
					pt.Last_use_update();
					ReplyTweetExe(status, pt);
				}
			} catch (SQLException | InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
				return;
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				onTwitterException(e);
				return;
			}
			status = null;
		}

		@Override
		public void onUserProfileUpdate(User updatedUser) {
			// TODO 自動生成されたメソッド・スタブ
			if (user == null || getEnumBotAcountStatus() != enumBotAcountStatus.BOTRUN) {
				return;
			}
			super.onUserProfileUpdate(updatedUser);
		}

		/*
		 * void onTwitterException(TwitterException e) { onTwitterException(e);
		 * }
		 */

		/**
		 * 特定のツイートに向けたリプライを送信するメソッド
		 *
		 * @param message
		 *            送信するメッセージ
		 * @param status
		 *            送信先のツイート
		 * @throws TwitterException
		 * @throws InterruptedException
		 */
		public void ReplyTweet(String message, Status status) throws TwitterException {

			if (message.equals(last_tweet)) {
				return;
			}

			last_tweet = message;
			StatusUpdate su = new StatusUpdate(message);

			su.setInReplyToStatusId(status.getId());

			//Status updstatus =
			BotAccount.this.twitter.updateStatus(su);
			//System.out.println("[" + new Date().toString() + "]" + "リプライ：" + updstatus.getUser().getName() + ":" + updstatus.getText());
			// System.out.println("[リプライ" + twitter.getScreenName() + "→" +
			// status.getUser().getName() + ":" + status.getText() + "]" +
			// su.getStatus());
		}

		private void ReplyTweetExe(Status status, PostTweet pt)
				throws TwitterException, InterruptedException, SQLException {
			if (pt == null) {
				// System.out.println("[リプライ情報なし]");
				return;
			}

			try {
				if (pt.isFollow()) {
					Follow(status.getUser().getId());
				}
				if (pt.isRT()) {
					reTweet(status);
				}
				if (pt.isFav()) {
					Favorite(status);
				}
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				onTwitterException(e);
			}

			if (KbmUtil.isLessReplyLooplimit(status.getId(), pt.getLoopLimit())) {
				if (pt.isSong()) {
					if (PostTweet.statusIsSong(status) || isSongUser(pt,status))//取得したステータスが他のbotの歌や掛け合いならば無視
					{
						return;
					}
					ReplyPostFuture.add(scheduledexec.schedule(new ReplylSongPostTimer(status, pt), pt.getDelay(),
							TimeUnit.SECONDS));
				} else {
					ArrayList<String> replyList = pt.getReplyTweet(status);
					ReplyPostFuture.add(scheduledexec.schedule(new ReplylPostTimer(replyList, status), pt.getDelay(),
							TimeUnit.SECONDS));
				}
			}

		}
		private boolean isSongUser(PostTweet pt, Status status)
		{
			 try {
				for(BotAccount account : getSongUsers(pt))
				 {
					 if(account.User_ID == status.getUser().getId())
					 {
						 return true;
					 }
				 }
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
				return true;
			}

			return false;

		}

		private void reTweet(Status status) throws TwitterException {
			// TODO 自動生成されたメソッド・スタブ
			Status i_Status = getRetweet(status);
			if (!i_Status.isRetweeted() && !status.isRetweeted()) {
				twitter.retweetStatus(i_Status.getId());
			}
		}

		private void setNickname(Status status) throws TwitterException {

			Pattern pattern;

			pattern = Pattern.compile("@" + twitter.getScreenName() + " 「(.*)」って呼んで");
			Matcher matcher = pattern.matcher(status.getText());
			if (!matcher.matches()) {
				return;
			}
			KbmUtil.updateNickname(twitter.getId(), status.getId(), matcher.group(1));

		}

		public void anFollow(Status status) throws IllegalStateException, TwitterException {
			Pattern pattern;

			pattern = Pattern.compile("@" + twitter.getScreenName() + " フォロー解除して");
			Matcher matcher = pattern.matcher(status.getText());
			if (!matcher.matches()) {
				return;
			}
			twitter.destroyFriendship(status.getUser().getId());

		}

		public void onUnfollow(User source, User unfollowedUser) {
			// TODO 自動生成されたメソッド・スタブ
			super.onUnfollow(source, unfollowedUser);
			return;
		}
	}

	// 実行状態
	private enumBotAcountStatus botAcountStatus;
	public boolean Enable;

	ScheduledFuture<?> normalPostFuture;

	// ポーズのスケジューラー
	ScheduledFuture<?> pauseFuture;

	List<ScheduledFuture<?>> ReplyPostFuture = new ArrayList<>();
	// ノーマルポスト用スケジューラー
	static ScheduledExecutorService scheduledexec = Executors.newSingleThreadScheduledExecutor();

	// NormalPostTimer timer;
	// ツイッターのやつ、ここから投稿などの操作をする
	public Twitter twitter;

	// ツイッターのユーザーストリームのやつ
	public TwitterStream twitterStream;

	public long User_ID;
	public User user;
	public Long normalPostInterval;
	public Long pauseTime;
	public boolean replyRt;

	public BotAccount(long User_ID) {
		ResultSet rs = GetAccessToken(User_ID);
		try {
			while (rs.next()) {

				set(rs.getLong("User_ID"), rs.getString("Consumer_Key"), rs.getString("Consumer_Secret"),
						rs.getString("Access_Token"), rs.getString("Access_Token_Secret"), rs.getBoolean("bot_enable"),
						rs.getLong("normal_post_interval"), rs.getLong("pause_time"), rs.getBoolean("replytoRT"));

			}
			rs.close();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

	// アクセストークンをＤＢから取得する
	static public ResultSet GetAccessToken(long User_ID) {
		try {
			String sql = "SELECT * FROM accesstokentable  where User_ID = ?;";
			Connection con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setLong(1, User_ID);
			return pstat.executeQuery();

		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
	}
	private void set(long User_ID, String consumerKey, String consumerSecret, String Access_Token,
			String Access_Token_Secret, boolean Enable, long normalPostInterval, long pauseTime, boolean replyRt)
	{
		this.User_ID = User_ID;
		this.Enable = Enable;
		this.setBotAcountStatus(enumBotAcountStatus.BOTSTOP);
		this.normalPostInterval = normalPostInterval;
		this.pauseTime = pauseTime;
		this.replyRt = replyRt;
		if(Enable)
		{
			exec_twitter(get_conf(consumerKey, consumerSecret, Access_Token, Access_Token_Secret));
			exec_twitterstream(get_conf(consumerKey, consumerSecret, Access_Token, Access_Token_Secret));

			KbmUtil.addbotAccounts(this);
			try {
				this.user = twitter.verifyCredentials();
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				// e.printStackTrace();
				onTwitterException(e);
			}
		}
		//
		this.startBotTwitterStream();
	}
	public BotAccount(long User_ID, String consumerKey, String consumerSecret, String Access_Token,
			String Access_Token_Secret, boolean Enable, long normalPostInterval, long pauseTime, boolean replyRt) {
		set( User_ID,  consumerKey,  consumerSecret,  Access_Token,
				 Access_Token_Secret,  Enable,  normalPostInterval,  pauseTime,  replyRt) ;
	}

	public Boolean closeBot() {
		return closeBot(false);
	}

	public Boolean closeBot(boolean clientOnry) {
		closeBotThread cbt = new closeBotThread(clientOnry);
		cbt.setName("ボット完了スレッド :" + toString());
		cbt.start();

		return Enable;
	}

	class closeBotThread extends Thread {
		boolean clientOnry;

		public closeBotThread(boolean clientOnry) {
			// TODO 自動生成されたコンストラクター・スタブ
			this.clientOnry = clientOnry;
		}

		@Override
		public void run() {
			super.run();
			if (normalPostFuture != null) {
				normalPostFuture.cancel(true);
			}
			if (pauseFuture != null) {
				pauseFuture.cancel(true);
			}
			if (!clientOnry) {
				AccountMode.execModeChangeStop(BotAccount.this);
			}
			scheduledexec.shutdown();
			scheduledexec.shutdownNow();
			twitterStream.cleanUp();
			// twitterStream.shutdown();
			System.out.println("停止：" + toString());
			setBotAcountStatus(enumBotAcountStatus.BOTSTOP);
		}
	}

	// ツイッターを開始
	void exec_twitter(Configuration conf) {
		if(Enable)
		{
			TwitterFactory twitterfactory = new TwitterFactory(conf);
			twitter = twitterfactory.getInstance();
		}
	}

	// トリームの取得を開始
	public void exec_twitterstream(Configuration conf) {
		// TwitterStreamを生成
		TwitterStreamFactory factory = new TwitterStreamFactory(conf);
		twitterStream = factory.getInstance();
		// イベントを受け取るリスナーオブジェクトを設定
		twitterStream.addListener(new BotAccountUserStreamAdapter(conf));
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO 自動生成されたメソッド・スタブ
		super.finalize();
		pauseFuture.cancel(true);
		for (ScheduledFuture<?> scheduledFuture : ReplyPostFuture) {
			if (!scheduledFuture.isCancelled() && !scheduledFuture.isDone()) {
				scheduledFuture.cancel(true);
			}
		}
	}

	// アクセストークンをからコンフィグを取得
	private Configuration get_conf(String consumerKey, String consumerSecret, String Access_Token,
			String Access_Token_Secret) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(consumerKey);
		builder.setOAuthConsumerSecret(consumerSecret);
		builder.setOAuthAccessToken(Access_Token);
		builder.setOAuthAccessTokenSecret(Access_Token_Secret);

		// Configurationを生成
		return builder.build();
	}

	public String getbotAcountStatus() {
		switch (getEnumBotAcountStatus()) {
		case BOTRUN:
			return "実行中";
		case BOTSTOP:
			return "停止中";
		case BOTPAUSE:
			return "一時停止";
		case NOTRESV:
			return "応答無し";
		default:
			return "";
		}
	}

	protected void onTwitterException(TwitterException e) {
		// System.out.println("[Twitterエラー]:"+ User_Name + "：" +
		// e.getErrorCode());
		String sql = "insert into log (User_ID ,Code,Data) " + "value (?,?,?)";

		try {
			Connection con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setLong(1, User_ID);
			pstat.setLong(2, e.getErrorCode());
			pstat.setString(3, e.getLocalizedMessage());
			pstat.executeUpdate();
		} catch (SQLException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		switch (e.getErrorCode()) {
		default:
			// System.out.println("[Twitterエラー]:" + e.getLocalizedMessage());
			// pauseBot();
			break;
		case 88:
		case 185:
		case 261:
		case 226:
		case 420:
		case 429:
			pauseBot();
			break;
		case 326:
			stopBot();
			break;
		}
	}

	public void pauseBot() {
		stopBot();
		setBotAcountStatus(enumBotAcountStatus.BOTPAUSE);
		BotRestartTimer npt = new BotRestartTimer();
		npt.setName("[一時停止再起動タイマースレッド:" + User_ID + "]");
		pauseFuture = scheduledexec.schedule(npt, pauseTime, TimeUnit.MINUTES);
		Date date = new Date();
		System.out.println("一時停止[" + date.toString() + "]：" + toString());
	}

	// アクセストークンをＤＢから取得する
	static public ResultSet GetAccessToken() {
		try {
			String sql = "SELECT * FROM accesstokentable where bot_enable = 1;";
			Connection con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			return pstat.executeQuery();

		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
	}

	// アクセストークンをDBに保存する
	static boolean InsertAccessToken(long User_ID, String consumerKey, String consumerSecret, String accessToken,
			String accessTokenSecret, boolean bot_enable) {

		// String User_ID_Str = "\'" + String.valueOf(User_ID) + "\'";
		// Access_Token = "\'" + Access_Token + "\'";
		// Access_Token_Secret = "\'" + Access_Token_Secret + "\'";
		String sql = "INSERT INTO accesstokentable"
				+ " (User_ID, Consumer_Key, Consumer_Secret, Access_Token ,Access_Token_Secret ,bot_enable)"
				+ " VALUES ( ?, ?, ?, ?, ?, ?)" + " ON DUPLICATE KEY UPDATE" + " Consumer_Key= ?, "
				+ " Consumer_Secret = ?," + "Access_Token = ?," + "Access_Token_Secret = ?," + "bot_enable = ?";

		try {
			Connection con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setLong(1, User_ID);
			pstat.setString(2, consumerKey);
			pstat.setString(3, consumerSecret);
			pstat.setString(4, accessToken);
			pstat.setString(5, accessTokenSecret);
			pstat.setBoolean(6, bot_enable);
			pstat.setString(7, consumerKey);
			pstat.setString(8, consumerSecret);
			pstat.setString(9, accessToken);
			pstat.setString(10, accessTokenSecret);
			pstat.setBoolean(11, bot_enable);

			pstat.executeUpdate();
			con.close();
		} catch (SQLException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean setBotEnable() {
		// String User_ID_Str = "\'" + String.valueOf(User_ID) + "\'";
		// Access_Token = "\'" + Access_Token + "\'";
		// Access_Token_Secret = "\'" + Access_Token_Secret + "\'";
		String sql = "update accesstokentable SET bot_enable = ? where User_ID = ? ;";
		// String sql = "DELETE FROM accesstokentable WHERE User_ID = ? ;";

		try {
			Connection con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setBoolean(1, Enable);
			pstat.setLong(2, User_ID);
			pstat.executeUpdate();
			con.close();
		} catch (SQLException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean deleteBotDB() {
		// String User_ID_Str = "\'" + String.valueOf(User_ID) + "\'";
		// Access_Token = "\'" + Access_Token + "\'";
		// Access_Token_Secret = "\'" + Access_Token_Secret + "\'";
		// String sql = "update accesstokentable SET bot_enable = 0 where
		// User_ID = ? ;";
		String sql = "DELETE FROM accesstokentable WHERE User_ID = ? ;";

		try {
			Connection con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setLong(1, User_ID);
			pstat.executeUpdate();
			con.close();
		} catch (SQLException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
			return false;
		}
		return true;
	}

	public void upDateBot() {
		String sql = "UPDATE accesstokentable SET" + " normal_post_interval = ?," + " pause_time = ?,"
				+ " replytoRT = ?" + " WHERE User_ID = ?;";
		try {
			Connection con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setLong(1, normalPostInterval);
			pstat.setLong(2, pauseTime);
			pstat.setBoolean(3, replyRt);
			pstat.setLong(4, User_ID);
			pstat.execute();
			con.close();
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public enumBotAcountStatus getEnumBotAcountStatus() {
		return botAcountStatus;
	}

	public void setBotAcountStatus(enumBotAcountStatus botAcountStatus) {
		this.botAcountStatus = botAcountStatus;
	}
	@Override
	public String toString() {
		// TODO 自動生成されたメソッド・スタブ
		return (user != null) ? user.getName() :  "" + this.User_ID ;
	}

	private HashSet<BotAccount> getSongUsers(PostTweet pt) throws SQLException {
		HashSet<BotAccount> botAccounts = new HashSet<>();
		for (songData song : Songs.GetSong(pt.getSong_ID())) {
			botAccounts.add(KbmUtil.BotAccount(song.User_ID));
		}
		return botAccounts;
	}
}
