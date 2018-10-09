/*
 * essa classe 'e responsavel por encriptar as mensagens
 */
package encryptor;

import java.math.BigDecimal;
		
public class Encryptor {
	// chaves para encriptacao
	private long publicKeyN;
	private long publicKeyE;
	
	public Encryptor(long pkN,long pkE) {
		publicKeyE = pkE;
		publicKeyN = pkN;
	}
	// metodo responsavel por converter cada caractere da mensagem em valores ASCII 
	private long[] convertMessage(String msg) {
		long[] convMsg = new long[msg.length()];
		for(int i = 0;i<convMsg.length;i++) {
			convMsg[i]= msg.charAt(i);
		}
		return convMsg;
	}
	// metodo responsavel por encriptar a mensagem
	public String encrypt(String msg) {
		long[] convMsg = convertMessage(msg);
		long[] eMsg = new long[convMsg.length];
		//para cada caractere da mensagem
		for(int i = 0 ; i<eMsg.length ; i++) {
			BigDecimal bd = new BigDecimal(convMsg[i]);
			bd = bd.pow((int)publicKeyE);
			long g = bd.divideAndRemainder(new BigDecimal(publicKeyN))[1].longValue();
			eMsg[i] =g;
		}
		StringBuilder sendString = new StringBuilder();
		sendString.append(eMsg[0]);
		for(int i = 1;i<eMsg.length;i++) {
			sendString.append(","+eMsg[i]);
		}
		return sendString.toString();
	}
	
}
