/*
 * Essa classe 'e responsavel por realizar a decriptacao das mensagens trocadas
 */
package decryptor;

import java.io.InputStream;
import java.math.BigDecimal;

public class Decryptor {
	//Valores para calculo de encriptacao
	private long primeP, primeQ, primeE;
	private long prod;
	private static long dValue;
	// Thread para realizacao de calculos simultaneos de D
	private Thread pinCalc;
	// Quantidade de threads que serao utilizadas para o calculo de d
	int threadCount = 10;
	Thread[] workloads = new Thread[threadCount];
	
	public Decryptor(long primeP, long primeQ, long keyE) {
		this.primeE = keyE;
		this.primeP = primeP;
		this.primeQ = primeQ;
		prod = primeP * primeQ;
		// Thread responsavel por orquestrar a criacao das threads para calculo do valor D
		pinCalc = new Thread(new Runnable() {
			@Override
			public void run() {
				//divida o trabalho em threadCount partes
				long baseline = prod/threadCount;
				long start = 0;
				for(int i = 1;i<threadCount;i++) {
					workloads[i-1] = newJob(start, start+baseline);
					start = start+baseline -1;
				}
				workloads[threadCount-1]=newJob(start, prod);
				// inicie as threads 
				for(Thread t : workloads) {
					t.start();
				}
			}
		});
		pinCalc.start();
	}
	//metodo responsavel por forcar com que as threads terminem
	private void finish() {
		for(Thread t : workloads) {
			try {
				if(t.isAlive())
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	// metodo responsavel por estabelecer o trabalho que sera realizado pelas threads
	private Thread newJob(long dInitValue,long dMaxValue) {
		return new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				calculateDValue(dInitValue, dMaxValue);
			}
		});
	}
	//metodo sincrono que sera responsavel por calcular o valor de d
	// ele 'e sincrono pelo fato de que caso uma thread ache o valor correto de d ela 'notifique' as outras threads para que finalizem sua execucao
	private synchronized void calculateDValue(long dInitValue,long dMaxValue) {
		boolean foundD = false;
		long d = dInitValue;
		// enquanto d nao for encontrado
		while (!foundD) {
			if(dMaxValue == d) return;
			d++;
			foundD = ((primeE * d) % ((primeP - 1) * (primeQ - 1)) == 1);
		}
		// se acharmos d pare a thread principal 
		pinCalc.interrupt();
		dValue = d;
	}
	// metodo que de fato orquestra a decripcao da mensagem
	public String decrypt(InputStream is) {
		try {
			//confirmacao de que o valor de d foi encontrado
			boolean foundD = false;
			while(!foundD) {
				foundD = ((primeE * dValue) % ((primeP - 1) * (primeQ - 1)) == 1);
				finish();
				Thread.sleep(50);
			}
			//pegue o stream de dados e transforme em string
			byte[] info = new byte[is.available()];
			is.read(info);
			String s = new String(info);
			s  = s.trim();
			// cada caractere vem codificado e separado por virgula
			// quebre a string nas virgulas e decripte cada caractere
			String[] eChars = s.split(",");
			long[] chars = new long[eChars.length];
			for(int i = 0;i<eChars.length;i++) {
				if(eChars[i].isEmpty()) continue;
				long charValue = new Long(eChars[i]);
				BigDecimal bd = new BigDecimal(charValue);
				bd = bd.pow((int)dValue);
				long g = bd.divideAndRemainder(new BigDecimal(prod))[1].longValue();
				chars[i] = g;
			}
			//leia o array de caracteres e junte-os para formular a string original
			StringBuilder sb = new StringBuilder();
			for(long letter : chars) {
				sb.append((char)(letter));
			}
			return sb.toString();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}

}
