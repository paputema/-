package kusoBotMaker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



public class Songs {

	// 歌のリスト取得
	public static List<SongTitleData> GetSongTitle() throws SQLException {

		Connection con = Access_db.Connect_db();

		PreparedStatement pstat = con.prepareStatement("SELECT * from song_list");

		List<SongTitleData> songlist = new ArrayList<SongTitleData>();
		pstat.execute();
		ResultSet rs = pstat.executeQuery();


		while (rs.next()) {
			songlist.add(new SongTitleData(rs.getLong("Song_id"),rs.getString("Song_Title")));
		}

		rs.close();
		pstat.close();
		con.close();

		return songlist;

	}
	//歌のタイトルの取得
	public static String GetSongTitle(long song_ID){

		Connection con = Access_db.Connect_db();

		PreparedStatement pstat;
		try {
			pstat = con.prepareStatement("SELECT * from song_list where Song_id = ?");

			List<SongTitleData> songlist = new ArrayList<SongTitleData>();
			pstat.setLong(1, song_ID);
			pstat.execute();
			ResultSet rs = pstat.executeQuery();

			while (rs.next()) {
				songlist.add(new SongTitleData(rs.getLong("Song_id"), rs.getString("Song_Title")));
			}

			rs.close();
			pstat.close();
			con.close();

				String rtstr =  (songlist.size() > 0) ? songlist.get(0).song_Title : "";


			return rtstr;
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;

	}


	//歌の取得
	public static List<songData> GetSong(long song_ID) throws SQLException {

		Connection con = Access_db.Connect_db();

		PreparedStatement pstat = con.prepareStatement("SELECT * from song_text where Song_id = ?");

		List<songData> rtsong = new ArrayList<songData>();
		pstat.setLong(1, song_ID);
		pstat.execute();
		ResultSet rs = pstat.executeQuery();

		while (rs.next()) {
			rtsong.add(new songData(rs.getLong("User_ID"),rs.getLong("song_Sequence"),rs.getString("Post_str"),rs.getLong("Delay")));
		}

		rs.close();
		pstat.close();
		con.close();

		return rtsong;

	}



	public static void InsertSongTitle(String song_Title) {
		String sql = "insert into song_list (Song_Title)"
					+ "value (?)";
		Connection con = Access_db.Connect_db();
		try {
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setString(1, song_Title);
			pstat.executeUpdate();
			con.close();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	public static int DeleteSongTitle(long song_ID) {
		String sql = "delete from song_list where Song_id = ?";
		int rtint = 0;
		Connection con = Access_db.Connect_db();
		try {
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setLong(1, song_ID);
			rtint = pstat.executeUpdate();
			con.close();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return rtint;
	}
	public static void InsertSong(long song_ID,long user_ID,String Post_str,long Delay) {
		String sql = "insert into song_text (Song_id,User_ID,Post_str,Delay)"
					+ "value (?,?,?,?)";
		Connection con = Access_db.Connect_db();
		try {
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setLong(1, song_ID);
			pstat.setLong(2, user_ID);
			pstat.setString(3, Post_str);
			pstat.setLong(4, Delay);
			pstat.executeUpdate();
			con.close();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	public static void DeleteSong(long song_ID,long song_Sequence) {
		String sql = "delete from song_text where song_ID = ? and song_Sequence = ?";
		Connection con = Access_db.Connect_db();
		try {
			java.sql.PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setLong(1, song_ID);
			pstat.setLong(2, song_Sequence);
			pstat.executeUpdate();
			con.close();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

}

