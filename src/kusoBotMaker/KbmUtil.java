package kusoBotMaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class KbmUtil {
	public static Properties properties = getProperties();

	private static Properties getProperties()
	{
		String jarPath = System.getProperty("java.class.path");
		Properties properties = new Properties();

	try {

		//String file = jarPath + "/kbm.properties";
		String file = "kbm.properties";
		InputStream inputStream = new FileInputStream(file);
		properties.load(inputStream);
		inputStream.close();
	} catch (IOException e) {
		// TODO 自動生成された catch ブロック
		e.printStackTrace();
		String dirPath = jarPath.substring(0, jarPath.lastIndexOf(File.separator)+1);
		String file = dirPath + "kbm.properties";
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
			properties.load(inputStream);
			inputStream.close();
			System.out.println(file);
		} catch (FileNotFoundException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
	}
	return properties;
	}




	static private Map<Long,Long> mapReplyHistory = new HashMap<Long,Long>();
	static private Map<Long, User> mapUser = new HashMap<Long, User>();
	static public void init()
	{
		mapReplyHistory = null;
		mapReplyHistory = new HashMap<Long,Long>();
		mapUser = null;
		mapUser = new HashMap<Long, User>();
	}

	static public String getNickname(long my_id, long target_id)  {
		// TODO 自動生成されたメソッド・スタブ
		try {
			Connection con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement ( "Select * from nickname WHERE User_ID = ? AND Friends_ID = ? ORDER BY id DESC;");
			pstat.setLong(1, my_id);
			pstat.setLong(2, target_id);
			ResultSet rt = pstat.executeQuery();
			if(rt.next()){
				return rt.getNString("Nickname");
			}
			con.close();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			//e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return "";
	}
	static public Map<Long,String> getNicknameMap(long my_id)  {
		// TODO 自動生成されたメソッド・スタブ
		Map<Long,String> rt = new HashMap<>();
		try {
			Connection con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement ( "Select * from nickname WHERE User_ID = ?;");
			pstat.setLong(1, my_id);
			ResultSet rs = pstat.executeQuery();
			while(rs.next()){
				rt.put(rs.getLong("Friends_ID"), rs.getString("Nickname"));
			}
			con.close();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			//e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return rt;
	}


	static public User getUser(Long userID,Twitter twitter) {
		User user = mapUser.get(userID);
		try {
			if(userID != -1)
			{
				user = (user != null) ? user : twitter.showUser(userID);
				setUser(user);
			}
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			user = null;
		}

		return user;
	}
	static public void BlockAll(List<Long> blocklist)
	{
		BlockAll blockAll = new BlockAll(blocklist);
		blockAll.setDaemon(true);
		blockAll.setName("一括ブロック");
		blockAll.start();
	}
	static private class BlockAll extends Thread
	{
		public BlockAll(List<Long> blocklist) {
			super();
			this.blocklist = blocklist;
		}

		List<Long> blocklist;
		@Override
		public void run() {
			// TODO 自動生成されたメソッド・スタブ
			super.run();
			for (BotAccount account : botAccounts.values()) {
				if (account != null && account.twitter != null) {
					new Thread(new Runnable() {
						public void run() {
							for (Long TargetID : blocklist) {
								try {

									System.out.println(account.user.getName() + "が"
											+ account.twitter.createBlock(TargetID).getName() + "をブロック");
								} catch (TwitterException e) {
									// TODO 自動生成された catch ブロック
									try {
										if(e.getErrorCode() == 88)
										{
										System.out.println(account.user.getName() + "[スリープ]"
												+ e.getRateLimitStatus().getResetTimeInSeconds());
										sleep(e.getRateLimitStatus().getResetTimeInSeconds() * 1000);
										}else
										{
											e.printStackTrace();
										}
									} catch (InterruptedException e1) {
										// TODO 自動生成された catch ブロック
										e1.printStackTrace();
									}
								}
							}
							// System.out.println(KbmUtil.getUser(account.User_ID).getName()
							// + " : " + KbmUtil.getUser(TargetID).getName());
						}
					}).start();

				}
			}

		}

	}


	static public User getUser(Long userID) {
		return mapUser.get(userID);
	}

	static	public User getUser(Long userID, Function<? super Long,? extends User> func) {
		return mapUser.computeIfAbsent(userID,func);
	}
	static public boolean isLessReplyLooplimit (long fromstatusId, long limit)
	{
		int count = 0;
		Long tostatusId = mapReplyHistory.get(fromstatusId);
		if (tostatusId == null)
		{
			return (count < limit);
		}
		else
		{
			count++;
			return isLessReplyLooplimit (tostatusId, limit ,count);
		}



	}

	static public Map<Long,BotAccount> botAccounts = new HashMap<Long, BotAccount>();
	static public void addbotAccounts(BotAccount botAccount)
	{
		botAccounts.put(botAccount.User_ID, botAccount);
	}
	static public void delbotAccounts(BotAccount botAccount)
	{
		botAccounts.remove(botAccount.User_ID);
	}
	static public BotAccount BotAccount(long user_ID)
	{
		return botAccounts.get(user_ID);
	}
	static public List<User> getBotUser() {
		List<User> rtList = new ArrayList<User>();

		for(BotAccount user : botAccounts.values())
		{
			rtList.add(user.user);
		}
		return rtList;
	}

	private static boolean isLessReplyLooplimit(Long fromstatusId, long limit, int count) {
		// TODO 自動生成されたメソッド・スタブ
		Long tostatusId = mapReplyHistory.get(fromstatusId);
		if (tostatusId == null || (count >= limit))
		{
			return (count < limit);
		}
		else
		{
			count++;
			return isLessReplyLooplimit (tostatusId, limit ,count);
		}
	}
	static public void setReplyHistory (Status status)
	{
		long fromstatusId = status.getId();
		long tostatusId =  status.getInReplyToStatusId();
		if (tostatusId != -1)
		{
			mapReplyHistory.put(fromstatusId,tostatusId);
		}

	}
	public static void setUser(User user) {
		mapUser.put(user.getId(), user);
	}

	static public void updateNickname(long User_ID, long Friends_ID, String Nickname) {
		// TODO 自動生成されたメソッド・スタブ
		Connection con;

		try {
			con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement("insert into nickname (User_ID, Friends_ID, nickname ) value (?,?,?)"
					+ "ON DUPLICATE KEY UPDATE nickname = ?");
			pstat.setLong(1, User_ID);
			pstat.setLong(2, Friends_ID);
			pstat.setString(3, Nickname);
			pstat.setString(4, Nickname);
			pstat.executeUpdate();
			con.close();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			System.out.println(e.getMessage());
		}
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO 自動生成されたメソッド・スタブ
		super.finalize();
	}
	public static void deleteNickname(long user_ID, long friends_ID, String nickname) {
		// TODO 自動生成されたメソッド・スタブ
		try {
			Connection con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement ("DELETE FROM nickname WHERE User_ID = ? AND Friends_ID = ?");
			pstat.setLong(1, user_ID);
			pstat.setLong(2, friends_ID);
			pstat.executeUpdate();
			con.close();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			System.out.println(e.getMessage());
		}
	}
}
