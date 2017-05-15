package kusoBotMaker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Properties;

public class KbmConnectThread extends Thread {
	protected String serverHost;
	protected Integer serverport;

	KbmConnectThread() {
		getPropatyServer();
	}

	private void getPropatyServer() {

		Properties properties = KbmUtil.properties;
		serverHost = "localhost";
		serverport = 56565;

		// 値の取得
		serverHost = properties.getProperty("SERVERHOST");
		String port = properties.getProperty("SERVERPORT");

		try {
			serverport = new Integer(port);
		} catch (NumberFormatException e) {
			// TODO: handle exception
			System.out.println(port);
			e.printStackTrace();
		}

	}


	/** 送信スレッド */
	class Sender {
		private SocketInfo socketInfo;
		private ObjectOutputStream writer = null;

		public Sender(SocketInfo sInfo) throws IOException {
			socketInfo = sInfo;
			writer = socketInfo.createSender();
		}

		private void Send(DataContainer dataContainer) {
			try {
				writer.writeObject(dataContainer);
				writer.flush();
				System.out.println("[" + new Date().toString() + "] " + "ステータス送信:" + dataContainer.toString());
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}

	}

	/** 受信スレッド */
	class ReceiverThread extends Thread {
		private SocketInfo socketInfo;
		private ObjectInputStream reader;

		public ReceiverThread(SocketInfo sInfo) throws IOException {
			socketInfo = sInfo;
			reader = socketInfo.createReciver();
		}

		public void run() {
			for (;;) {
				// 受信する

				DataContainer container = null;
				try {
					// if(reader.)
					container = (kusoBotMaker.DataContainer) reader.readObject();
					System.out.println("[" + new Date().toString() + "] " + "ステータス受信:" + container.toString());
					if(container.socketMode == enumSocketMode.CLOSE_SOCKET)
					{
						break;
					}
					execContainer(container);
				} catch (ClassNotFoundException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (SocketException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
					break;
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
					break;
				}

			}
		}
	}
	protected Sender sender;

	public void sendContainer(DataContainer container) {

		if(sender != null)
		{
			sender.Send(container);
		}
	}
	public void closeSocket()
	{
		sender.Send(new DataContainer(enumSocketMode.CLOSE_SOCKET, null));
	}
	public void execContainer(DataContainer container) {

	}
}

/** ソケット情報 */
class SocketInfo {
	private Socket socket;
	private boolean writerClosed;
	private boolean readerClosed;

	public SocketInfo(Socket s) {
		socket = s;
		writerClosed = false;
		readerClosed = false;
	}

	public ObjectInputStream createReciver() throws IOException {
		return new ObjectInputStream(socket.getInputStream());
	}

	public ObjectOutputStream createSender() throws IOException {
		return new ObjectOutputStream(socket.getOutputStream());
	}

	public void closeWriter(ObjectOutputStream writer) throws IOException {
		sleep(20 * 1000);
		if (writer != null) {
			writer.close();
		}
		writerClosed = true;
		closeSocket();
	}

	private void sleep(int i) {
		// TODO 自動生成されたメソッド・スタブ
		try {
			Thread.sleep(i * 1000);
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void closeReader(ObjectInputStream reader) throws IOException {
		sleep(20 * 1000);
		if (reader != null) {
			reader.close();
		}
		readerClosed = true;
		closeSocket();
	}

	private void closeSocket() throws IOException {
		if (writerClosed && readerClosed) {
			socket.close();
		}
	}
}
