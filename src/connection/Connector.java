/*
 * Essa classe 'e responsavel por conectar o cliente com o servidor 
 * Essa classe possui o main
 */
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
	// Argumentos de conexao com o servidor como a porta padrao e o socket
	// especifico
	private static Connector instance;
	private static Socket clientSocket;
	public static final int PORT = 9797;
	public String host;
	// readLock 'e uma variavel responsavel por garantir que a mensagem recebida do
	// servidor ser'a lida (funciona como um semaforo)
	public boolean readLock = false;
	// variavel reponsavel por checar a situacao da conexao com o servidor, caso
	// haja algum erro de conexao com o servidor essa variavel fecha formalmente e
	// com ela
	// notificamos o usuario
	public boolean Alive = true;
	// variaveis para realizar a encriptacao e desencriptacao
	static long primeP = 13;
	static long primeQ = 31;
	static long keyE = 11;
	// essa chave normalmente 'e passada como uma multiplicacao, foi criada dessa
	// forma com o intuito evidenciar que sao as chaves P e Q do servidor
	static long serverKey = 19 * 23;

	static long serverKeyE = 7;
	public static Decryptor dec = new Decryptor(primeP, primeQ, keyE);
	public static Encryptor enc = new Encryptor(serverKey, serverKeyE);

	// Ao criar o objeto de conexao o socket 'e criado
	private Connector(String host) throws UnknownHostException, IOException {
		this.host = host;
		clientSocket = new Socket(host, PORT);
	}

	// singleton para a conexao
	public static Connector getInstance(String host) throws UnknownHostException, IOException {
		if (instance == null)
			return instance = new Connector(host);
		return instance;
	}

	// metodo responsavel para enviar as mensagens ao servidor de maneira apropriada
	public void send(String msg) throws IOException {
		OutputStream info = clientSocket.getOutputStream();
		// encriptamos a mensagem
		msg = enc.encrypt(msg);
		info.write(msg.getBytes());
		// enviamos
		info.flush();
	}

	// realiza a conexao com o servidor assim como o recebimento das mensagens do
	// mesmo
	public static void connect() {
		try {
			Scanner sc = new Scanner(System.in);
			System.out.println("Digite o endereco IP do host:");
			String host = sc.nextLine();
			// 172.115.13.95
			System.out.println("Connecting to host...");
			// chama o construtor para criar o socket e pedir a conexao para o servidor
			Connector c = Connector.getInstance(host);
			System.out.println("Connected to " + Connector.clientSocket.getInetAddress());
			InputStream is = clientSocket.getInputStream();
			// Cria uma thread de execucao para tratar as camadas do cliente ao servidor
			Thread job = new Thread(new Runnable() {

				@Override
				public void run() {
					System.out.println("Type a command:");
					// interface de conexao do cliente com o servidor
					LOOP: while (true) {
						try {
							String s = "";
							System.out.print("> ");
							// se a mensagem nao for um keep alive***
							/*
							 * Foi descoberto empiricamente no desenrolar desse projeto que o keep alive no
							 * java 'e realizado por meio do envio de caracteres nulos.
							 * 
							 */
							if (!(s = sc.nextLine().trim()).equals("") && !s.isEmpty() && s.length() > 0) {
								// envie a mensagem
								c.send(s);
								// prepare a leitura
								c.readLock = true;
								try {
									// espere 500ms para a chegada da resposta do servidor
									Thread.sleep(500);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								// enquanto estiver lendo a mensagem
								while (c.readLock) {
									// Desencripte a mensagem
									String d = dec.decrypt(is);
//									try {
//										Thread.sleep(500);
//									} catch (InterruptedException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
									// se a mensagem for de desligamento entao feche a conexao
									if (d.trim().equals("end")) {
										System.out.println("Connection closed");
										c.Alive = false;
										c.readLock = false;
										sc.close();
										Connector.clientSocket.close();
										break LOOP;
									} else {
										// caso contrario mostre a mensagem do servidor
										System.out.println(d);
										System.out.println();
										c.readLock = false;
									}
								}
							}
						} catch (IOException e) {
							// algum erro e' gerado se ou o servidor digitado esta nao foi encontrado ou se
							// o servidor ficou indisponivel no meio da troca de mensagens
							// TODO Auto-generated catch block
							c.Alive = false;
							System.out.println("Connection with server lost. Caused by: " + e.getMessage());
							
							try {
								if (!Connector.clientSocket.isClosed()) {
									Connector.clientSocket.close();
									sc.close();
								}
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							System.exit(0);
							break LOOP;
						}
					}

				}
			});
			job.start();

//			try {
//				job.join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Server unavailable or not found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Connector.connect();
	}
}
