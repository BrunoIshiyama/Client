package connection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connector {
	private static Connector instance;
	private static Socket clientSocket;
	public static final int PORT = 9797;
	public String host;
	private Connector(String host) throws UnknownHostException, IOException {
		this.host = host;
		clientSocket = new Socket(host, PORT);
	}
	public static Connector getInstance(String host) throws UnknownHostException, IOException {
		if(instance == null) return instance = new Connector(host);
		return instance;
	}
	public void send(String msg) throws IOException {
		OutputStream info = clientSocket.getOutputStream();
		info.write(msg.getBytes());
		info.flush();
	}
	public static void main(String[] args) {
		try {
			Connector c = Connector.getInstance("172.115.13.95");
			c.send("Hello World");
			c.clientSocket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
