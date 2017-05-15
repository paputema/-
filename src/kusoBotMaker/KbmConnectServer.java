package kusoBotMaker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** サーバ */
public class KbmConnectServer  extends KbmConnectThread{
	ServerSocket serverSocket;
	public void run(){
		try {
			serverSocket = new ServerSocket(serverport);
			for (;;)
			{
				Socket socket = serverSocket.accept();
		        SocketInfo sInfo = new SocketInfo(socket);
		        //(new SenderThread(sInfo)).start();
		        sender = new Sender(sInfo);
		        (new ReceiverThread(sInfo)).start();
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

}