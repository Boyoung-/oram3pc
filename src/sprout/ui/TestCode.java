package sprout.ui;

import java.math.BigInteger;

import sprout.crypto.SR;
import sprout.util.StopWatch;

public class TestCode {
	private static byte[] xor(byte[] a, byte[] b) {
		byte[] small = a;
		byte[] big = b;
		if (a.length > b.length) {	
			small = b;
			big = a;
		}
		
		int lenDiff = big.length - small.length;
		byte[] out = new byte[big.length];
		
		int i=0;
		for (; i<lenDiff; i++) {
			out[i] = big[i];
		}
		for (; i<big.length; i++) {
			out[i] = (byte) (small[i-lenDiff] ^ big[i]);
		}
		
		return out;
	}
	
	private static byte[] and(byte[] a, byte[] b) {
		byte[] small = a;
		byte[] big = b;
		if (a.length > b.length) {	
			small = b;
			big = a;
		}
		
		int lenDiff = big.length - small.length;
		byte[] out = new byte[small.length];
		
		for (int i=0; i<small.length; i++) {
			out[i] = (byte) (small[i] & big[i+lenDiff]);
		}
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		byte[] a = new byte[10000];
		byte[] b = new byte[a.length];
		SR.rand.nextBytes(a);
		SR.rand.nextBytes(b);
		BigInteger ba = new BigInteger(1, a);
		BigInteger bb = new BigInteger(1, b);
		StopWatch sw1 = new StopWatch();
		StopWatch sw2 = new StopWatch();
		
		sw1.start();
		for (int i=0; i<100000; i++)
			xor(a, b);
		sw1.stop();
		
		sw2.start();
		for (int i=0; i<100000; i++)
			ba.xor(bb);
		sw2.stop();
		
		System.out.println(sw1);
		System.out.println(sw2);
		
	}
}
