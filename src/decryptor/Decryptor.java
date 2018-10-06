package decryptor;

import java.io.InputStream;
import java.math.BigDecimal;

public class Decryptor {
	private long primeP, primeQ, primeE;
	private long prod;
	private static long dValue;
	private Thread pinCalc;
	int threadCount = 10;
	Thread[] workloads = new Thread[threadCount];
	public Decryptor(long primeP, long primeQ, long keyE) {
		this.primeE = keyE;
		this.primeP = primeP;
		this.primeQ = primeQ;
		prod = primeP * primeQ;
		pinCalc = new Thread(new Runnable() {
			@Override
			public void run() {
				long baseline = prod/threadCount;
				long start = 0;
				for(int i = 1;i<threadCount;i++) {
					workloads[i-1] = newJob(start, start+baseline);
					start = start+baseline -1;
				}
				workloads[threadCount-1]=newJob(start, prod);
				for(Thread t : workloads) {
					t.start();
				}
			}
		});
		pinCalc.start();
	}
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
	private Thread newJob(long dInitValue,long dMaxValue) {
		return new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				calculateDValue(dInitValue, dMaxValue);
			}
		});
	}
	private synchronized void calculateDValue(long dInitValue,long dMaxValue) {
		boolean foundD = false;
		long d = dInitValue;
		while (!foundD) {
			if(dMaxValue == d) return;
			d++;
			foundD = ((primeE * d) % ((primeP - 1) * (primeQ - 1)) == 1);
		}
		pinCalc.interrupt();
		dValue = d;
		System.out.println(d);
	}
	public String decrypt(InputStream is) {
		try {
			boolean foundD = false;
			while(!foundD) {
				foundD = ((primeE * dValue) % ((primeP - 1) * (primeQ - 1)) == 1);
				finish();
				Thread.sleep(50);
			}
			byte[] info = new byte[is.available()];
			is.read(info);
			String s = new String(info);
			s  = s.trim();
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
