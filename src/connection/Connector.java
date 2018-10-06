package connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import decryptor.Decryptor;
import encryptor.Encryptor;

public class Connector {
	private static Connector instance;
	private static Socket clientSocket;
	public static final int PORT = 9797;
	public String host;
	public boolean readLock = false;
	public boolean Alive = true;
	static long primeP = 13;
	static long primeQ = 31;
	static long keyE = 11;
	static long serverKey = 19*23;
	static long serverKeyE = 7;
	public static Decryptor dec = new Decryptor(primeP, primeQ, keyE);
	public static Encryptor enc = new Encryptor(serverKey, serverKeyE);
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
		msg = enc.encrypt(msg);
		info.write(msg.getBytes());
		info.flush();
	}

	public static void main(String[] args) {
		try {
			// 172.115.13.95
			System.out.println("Connecting to host...");
			Connector c = Connector.getInstance("192.168.0.110");
			System.out.println("Connected to " + Connector.clientSocket.getInetAddress());
			InputStream is = clientSocket.getInputStream();
			Scanner sc = new Scanner(System.in);
			Thread job = new Thread(new Runnable() {

				@Override
				public void run() {
					System.out.println("Type a command:");
					LOOP: while (true) {
						try {
							String s = "";
							System.out.print("> ");
							if (!(s = sc.nextLine().trim()).equals("") && !s.isEmpty() && s.length() > 0) {
								c.send(s);
								c.readLock = true;
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								while (c.readLock) {
									String d = dec.decrypt(is);
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									if (d.trim().equals("end")) {
										System.out.println("Connection closed");
										c.Alive = false;
										c.readLock = false;
										break LOOP;
									} else {
										System.out.println(d);
										System.out.println();
										c.readLock = false;
									}
								}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							c.Alive = false;
							System.out.println("Connection with server lost");
							break LOOP;
						}
					}

				}
			});
			job.start();

			try {
				job.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sc.close();
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
