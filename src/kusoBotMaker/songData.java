package kusoBotMaker;

public class songData {
	public songData(long user_ID,long song_Sequence, String post_str, long delay) {
		super();
		User_ID = user_ID;
		Song_Sequence = song_Sequence;
		Post_str = post_str;
		Delay = delay;
	}

	public long User_ID;
	public String Post_str;
	public long Song_Sequence;
	public long Delay;
	public songData() {
	}
}