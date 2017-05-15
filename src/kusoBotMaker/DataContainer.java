package kusoBotMaker;

import java.io.Serializable;

public class DataContainer implements Serializable {
	public DataContainer(enumSocketMode socketMode, Long botAccountID) {
		super();
		this.socketMode = socketMode;
		this.botAccountID = botAccountID;
		this.botAcountStatus = enumBotAcountStatus.NOTRESV;
	}


	public DataContainer(long botAccountID, enumBotAcountStatus status) {
		// TODO 自動生成されたコンストラクター・スタブ

		this.socketMode = enumSocketMode.BOT_STATUS;
		this.botAccountID = botAccountID;
		this.botAcountStatus = status;
	}

	public DataContainer() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

	enumSocketMode socketMode;
	public enumBotAcountStatus getBotAcountStatus() {
		return botAcountStatus;
	}

	public void setBotAcountStatus(enumBotAcountStatus botAcountStatus) {
		this.botAcountStatus = botAcountStatus;
	}

	public enumSocketMode getSocketMode() {
		return socketMode;
	}

	public Long getBotAccountID() {
		return botAccountID;
	}

	Long botAccountID;
	enumBotAcountStatus botAcountStatus;
	@Override
	public String toString() {
		// TODO 自動生成されたメソッド・スタブ

		if(socketMode == enumSocketMode.CLOSE_SOCKET)
		{
			String string = socketMode.toString();
			return string;
		}
		BotAccount botAccount = KbmUtil.BotAccount(botAccountID);
		if(botAccount == null)
		{
			return "null";
		}
		String string = "ユーザー名：" + ((botAccount.user != null) ? botAccount.user.getName() : botAccount.User_ID)
			+ "/ステータス：" + botAcountStatus.toString()
			+ "/モード：" + socketMode.toString();
		return string;
	}

}