package connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Connector {
	private static Connector instance;
	private static Socket clientSocket;
	public static final int PORT = 9797;
	public String host;
	public boolean readLock = false;
	public boolean keepAlive = true;
	private Connector(String host) throws UnknownHostException, IOException {
		this.host = host;
		clientSocket = new Socket(host, PORT);
	}

	public static Connector getInstance(String host) throws UnknownHostException, IOException {
		if (instance == null)
			return instance = new Connector(host);
		return instance;
	}

	public void send(String msg) throws IOException {
		OutputStream info = clientSocket.getOutputStream();
		info.write(msg.getBytes());
		info.flush();
	}

	public static void main(String[] args) {
		try {
			// 172.115.13.95
			System.out.println("Connecting to host...");
			Connector c = Connector.getInstance("192.168.0.102");
			System.out.println("Connected to "+Connector.clientSocket.getInetAddress());
			InputStream is = clientSocket.getInputStream();
			Scanner sc = new Scanner(System.in);
			Thread job = new Thread(new Runnable() {

				@Override
				public void run() {
					System.out.println("Type a command:");
					LOOP:
					while (true) {
						try {
							String s = "";
							if (!(s=sc.nextLine().trim()).equals("")&&!s.isEmpty()&&s.length()>0) {
								c.send(s);
								System.out.println("next " + s);
								c.readLock = true;
								while (c.readLock) {
									byte[] bytes = new byte[is.available()];
									is.read(bytes);
									try {
										Thread.sleep(400);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									String d = new String(bytes);
									if (d.equals("end")) {
										System.out.println("Connection closed");
										c.keepAlive= false;
										c.readLock = false;
										sc.close();
										Connector.clientSocket.close();
										break LOOP;
									}else {
										System.out.println("SERVER: "+d);
										System.out.println();
										c.readLock = false;
									}
								}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
			});
			job.start();
			while (c.keepAlive) {}
			job.interrupt();
			Connector.clientSocket.close();
			System.exit(0);
			
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
