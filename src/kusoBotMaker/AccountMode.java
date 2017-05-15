package kusoBotMaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class AccountMode {
	 static boolean execModeChange(BotAccount botAccount,String mode_name)
	{
		Twitter twitter = botAccount.twitter;
		try {
			java.util.List<AccountMode> AccountModes = AccountMode.getSetAccountModesByType(twitter.getId(), mode_name);
			Random rnd = new Random();
			long now = new Date().getTime();
			int size = AccountModes.size();
			if (size == 0) {
				return false;
			}
			rnd.setSeed(now);
			AccountMode amode = AccountModes.get(rnd.nextInt(size));
			amode.updateTwitterProfile(botAccount);
			amode.updateAccountmode (twitter);
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;
	}
	 static private String getNowMMdd ()
	{
		return new SimpleDateFormat("MMdd").format(new Date());
	}
	 static private String getNowwMMFE ()
	{
		return "w" + new SimpleDateFormat("MMFE").format(new Date());
	}
	 static void execModeChangeDay(BotAccount botAccount)
	{
		 if(!execModeChange(botAccount,getNowMMdd()) && !execModeChange(botAccount,getNowwMMFE()))
		 {
			 execModeChangeRun(botAccount);
		 }
	}
	 static void execModeChangeRun(BotAccount botAccount)
	{
		execModeChange(botAccount,"通常");

	}
	 static void execModeChangeStop(BotAccount botAccount)
	{
		execModeChange(botAccount,"停止");
	}
	 static public List<AccountMode> getAccountModesList (ResultSet rs)
	{
		List<AccountMode> rt = new ArrayList<AccountMode>() ;
		try {
			while (rs.next()) {
				rt.add(new AccountMode(rs));
			}
			rs.close();
			return rt;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	 static private ResultSet getResultSetAccountMode(long userID) throws SQLException , TwitterException
	{
		String sql =  "SELECT * FROM accountmode WHERE User_ID = ? ;" ;
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(sql);
		pstat.setLong(1, userID);
		return  pstat.executeQuery();
	}
	 static private ResultSet getResultSetAccountModeByType (long userID, String mode_type) throws SQLException, TwitterException
	{
		String sql =  "SELECT * FROM accountmode WHERE User_ID = ? AND ? LIKE mode_type ;" ;
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(sql);
		pstat.setLong(1, userID);
		pstat.setString(2, mode_type);
		return  pstat.executeQuery();
	}
	static private ResultSet getResultSetModeType (long userID) throws SQLException, TwitterException
	{
		String sql =  "SELECT  DISTINCT mode_type FROM accountmode WHERE User_ID = ?;" ;
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(sql);
		pstat.setLong(1, userID);
		return  pstat.executeQuery();
	}
	static public boolean deleteMode (long id) throws SQLException, TwitterException
	{
		String sql =  "DELETE from accountmode where ID = ?;" ;
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(sql);
		pstat.setLong(1, id);
		return  pstat.execute();
	}
	static public List<String> getSetModeType (long userID)
	{
		try {
			List<String> rt = new ArrayList<>();
			ResultSet rs = getResultSetModeType(userID);
			while (rs.next()) {
				rt.add(rs.getString("mode_type"));
			}
			rs.close();
			return rt;
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
	}












	static public List<AccountMode> getSetAccountModes (long userID)
	{
		try {
			return getAccountModesList(getResultSetAccountMode(userID));
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
	}

	static public List<AccountMode> getSetAccountModesByType (long userID, String mode_type)
	{
		try {
			return getAccountModesList(getResultSetAccountModeByType(userID, mode_type));
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
	}

	public long id ;


	public String mode_name ;
	public String mode_type ;
	public String profile_iconp_path  ;
	public String user_description ;

	long User_ID ;
	public String user_location ;

	public String user_name ;

	public String user_url ;


	public AccountMode() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public AccountMode(long id ,long User_ID ,	String mode_name ,	String mode_type ,	String user_name ,	String	user_url ,	String user_location ,	String user_description ,String profile_iconp_path){
		this.id = id;
		this.User_ID = User_ID;
		this.mode_name = mode_name;
		this.mode_type = mode_type;
		this.user_name =user_name;
		this.user_url =user_url;
		this.user_location = user_location;
		this.user_description  = user_description;
		this.profile_iconp_path  = profile_iconp_path;
	}
	public AccountMode(long User_ID ,	String mode_name ,	String mode_type ,	String user_name ,	String	user_url ,	String user_location ,	String user_description ,String profile_iconp_path){
		this(-1,User_ID ,mode_name ,mode_type ,	user_name ,	user_url ,user_location ,user_description ,profile_iconp_path);
	}
	public AccountMode(ResultSet rs) throws SQLException
	{
		if (rs.isBeforeFirst())
		{
			rs.next();
		}
		this.id = rs.getLong("id");
		this.User_ID = rs.getLong("User_ID");
		this.mode_name = rs.getString("mode_name");
		this.mode_type = rs.getString("mode_type");
		this.user_name =rs.getString("user_name");
		this.user_url =rs.getString("user_url");
		this.user_location = rs.getString("user_location");
		this.user_description  = rs.getString("user_description");
		this.profile_iconp_path  = rs.getString("profile_icon_path");

	}

	public int insertDb () throws SQLException
	{

		String sql = "insert into accountmode (User_ID,mode_name,mode_type,user_name,user_url,user_location,user_description,profile_icon_path) value (?,?,?,?,?,?,?,?) "
				+ "ON DUPLICATE KEY UPDATE User_ID = ? , mode_name = ?"  ;
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(sql);
		pstat.setLong(1, User_ID);
		pstat.setString(2, mode_name);
		pstat.setString(3, mode_type);
		pstat.setString(4, user_name);
		pstat.setString(5, user_url);
		pstat.setString(6, user_location);
		pstat.setString(7, user_description);
		pstat.setString(8, profile_iconp_path);
		pstat.setLong(9, User_ID);
		pstat.setString(10, mode_name);
		int rt = pstat.executeUpdate();
		con.close();
		return rt;
	}
	public int updateAccountmode (Twitter twitter)
	{
		String sql = "UPDATE accesstokentable SET"
				+ " mode_name = ?"
				+ " WHERE User_ID = ?;";
		int rt;
		try {
			Connection con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setString(1, mode_name);
			pstat.setLong(2, twitter.getId());
			rt = pstat.executeUpdate();
			con.close();

		} catch (SQLException | IllegalStateException | TwitterException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return 0;
		}
		return rt;
	}


	public void getIconDb()  {
		String sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		File file =new File ("boticonsFromDB" + "/" + User_ID + "/" + sdf + ".jpg") ;
		file.getParentFile().mkdirs();


		//File newFilw = new File("Download/" + Title); // 保存先
		//newFilw.mkdirs();
		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream(file);
			String sql = "select * from accounticonimage where id = ?";
			Connection con = Access_db.Connect_db();
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setLong(1, id);
			ResultSet resultSet = pstat.executeQuery();
			while (resultSet.next()) {
				byte[] b = (resultSet.getBytes("Icon"));
				fileOutputStream.write(b);
				fileOutputStream.close();
				profile_iconp_path = file.getAbsolutePath();
			}
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.getCause();
		}




	}

	public int updateIconDb () throws  SQLException
	{
		byte[] bytes = null;
        try {
			FileInputStream fis = new FileInputStream(profile_iconp_path);
			FileChannel channel = fis.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
			channel.read(buffer);
			buffer.clear();
			bytes = new byte[buffer.capacity()];
			buffer.get(bytes);
			channel.close();
			fis.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		// 登録
        String sql = "INSERT INTO accounticonimage (id,Icon) VALUES (?,?)"
        		+ "ON DUPLICATE KEY UPDATE Icon = ?"  ;
        Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(sql);
        pstat.setLong(1, id);
        pstat.setBytes(2, bytes);
        pstat.setBytes(3, bytes);
        int rt = pstat.executeUpdate();
        con.close();
		return rt;

	}

	public int updateDb () throws SQLException
	{
		//String sql = "insert into accountmode (ID,User_ID,mode_name,mode_type,user_name,user_url,user_location,user_description,profile_icon_path) value (?,?,?,?,?,?,?,?,?) "
		//		+ "ON DUPLICATE KEY UPDATE ID = ? " ;

		String sql = "UPDATE accountmode SET"
				+ " mode_name = ?,"
				+ " mode_type = ?,"
				+ " user_name = ?,"
				+ " user_url = ?,"
				+ " user_location = ?,"
				+ " user_description = ?,"
				+ " profile_icon_path = ?"
				+ " WHERE id = ?;";
		Connection con = Access_db.Connect_db();
		java.sql.PreparedStatement pstat = con.prepareStatement(sql);
		//pstat.setLong(1, id);
		//pstat.setLong(2, User_ID);
		pstat.setString(1, mode_name);
		pstat.setString(2, mode_type);
		pstat.setString(3, user_name);
		pstat.setString(4, user_url);
		pstat.setString(5, user_location);
		pstat.setString(6, user_description);
		pstat.setString(7, profile_iconp_path);
		pstat.setLong(8, id);
		int rt = pstat.executeUpdate();
		con.close();
		return rt;
	}
	public void updateTwitterProfile (BotAccount botAccount) {
		Twitter twitter = botAccount.twitter;

		File file = new File(profile_iconp_path);
		if(profile_iconp_path != null && !profile_iconp_path.equals("") && !file.exists())
		{
			getIconDb();
			file = new File(profile_iconp_path);
		}

		try {
			twitter.updateProfileImage(file);
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			//e.printStackTrace();

		}
		try {
			twitter.updateProfile(user_name, user_url, user_location, user_description);
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			//e.printStackTrace();
		}
	}
}
