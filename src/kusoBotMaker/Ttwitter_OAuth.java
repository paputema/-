package kusoBotMaker;

import java.net.URI;
import java.net.URISyntaxException;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;


public class Ttwitter_OAuth {

	Twitter twitter;
	RequestToken requestToken ;
	public AccessToken accessToken;
	public String consumerKey;
	public String consumerSecret;

	public Ttwitter_OAuth (String isConsumer_Key,String isConsumerSecret) throws TwitterException{
		consumerKey = isConsumer_Key;
		consumerSecret = isConsumerSecret;
		ConfigurationBuilder builder = new ConfigurationBuilder();
		//builder.setDebugEnabled(true);
		String stnull = null;

		builder.setOAuthConsumerKey(stnull);
		builder.setOAuthConsumerSecret(stnull);
		builder.setOAuthAccessToken(stnull);
		builder.setOAuthAccessTokenSecret(stnull);

		TwitterFactory twitterfactory = new TwitterFactory(builder.build());
		twitter = twitterfactory.getInstance();


		twitter.setOAuthConsumer(isConsumer_Key,isConsumerSecret);


			requestToken = twitter.getOAuthRequestToken();

		open_requestToken_url();
	}

	public URI open_requestToken_url()
	{


		try {
			URI url = new URI(requestToken.getAuthenticationURL());
			//Desktop desktop = Desktop.getDesktop();
			//desktop.browse(url);
			return url;
		} catch ( URISyntaxException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
	}
	public String open_requestToken_str()
	{


		//Desktop desktop = Desktop.getDesktop();
		//desktop.browse(url);
		return requestToken.getAuthenticationURL();
	}
	public boolean pin(String is_pin) {
		try {
			accessToken = twitter.getOAuthAccessToken(requestToken, is_pin);
			//Access_db i_db = new Access_db();
			//return BotAccount.InsertAccessToken(accessToken.getUserId(), accessToken.getToken(),accessToken.getTokenSecret(),true);
			return BotAccount.InsertAccessToken(accessToken.getUserId(),consumerKey ,consumerSecret, accessToken.getToken(),accessToken.getTokenSecret(),true);
			} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return false;
	}



}
