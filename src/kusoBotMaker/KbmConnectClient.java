package kusoBotMaker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** クライアント */
public class KbmConnectClient  extends KbmConnectThread{
	public void run(){
		Socket socket = null;
		try {
			socket = new Socket();
			InetSocketAddress socketAddress = new InetSocketAddress(serverHost,(int)serverport);
			socket.connect(socketAddress, 10000);
			SocketInfo sInfo = new SocketInfo(socket);
			//(new SenderThread(sInfo)).start();
			sender = new Sender(sInfo);
			(new ReceiverThread(sInfo)).start();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			// 処理なし
		}
	}
}