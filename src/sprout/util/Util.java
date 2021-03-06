package sprout.util;

import java.math.BigInteger;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.math.ec.ECPoint;

import sprout.crypto.SR;

public class Util {
	public static FileHandler fh = null;
	public static Logger LOG = initLog();

	// Flag to toggle output of debug print statements
	public static boolean debugEnabled = true;

	public static Logger initLog() {
		Logger log = Logger.getLogger("");

		log.setLevel(Level.ALL);

		// Remove the console logger
		for (Handler h : log.getHandlers()) {
			log.removeHandler(h);
		}

		return log;
	}

	public static void setLogFile(String file) {
		if (fh != null) {
			LOG.removeHandler(fh);
		}

		try {
			fh = new FileHandler(file, false);
		} catch (Exception e) {

			e.printStackTrace();
			return;
		}

		fh.setFormatter(new SimpleFormatter());

		LOG.addHandler(fh);
	}

	public static byte[] longToByteArray(long n) {
		return (byte[]) ByteBuffer.allocate(Long.SIZE / 8).putLong(n).rewind()
				.array();
	}

	public static long byteArrayToLong(byte[] n) {
		// Fill out the byte array to make it <long byte number> long
		byte[] full = new byte[Long.SIZE / 8];
		int offset = 0;
		for (int i = 0; i < full.length - n.length; i++) {
			full[offset++] = 0x00;
		}
		for (int i = 0; i < n.length; i++) {
			full[offset++] = n[i];
		}

		ByteBuffer bb = ByteBuffer.allocate(Long.SIZE / 8);
		bb.put(full);
		bb.rewind();
		return bb.getLong();
	}

	public static String byteArrayToString(byte[] ba) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < ba.length; i++) {
			builder.append(Integer.toHexString(ba[i]));
		}
		return builder.toString();
	}

	public static String toKaryString(long l, int k, int width) {
		String karyRep = Long.toString(l, k);
		if (karyRep.length() < (width + 1)) {
			StringBuilder builder = new StringBuilder(karyRep);
			while (builder.length() != width) {
				builder.insert(0, "0");
			}
			karyRep = builder.toString();
		}

		assert (karyRep.length() == width);

		return karyRep;
	}

	public static String byteArraytoKaryString(byte[] bytes, int k) {
		BigInteger bi = new BigInteger(bytes);
		return bi.toString(k);
	}

	public static String byteArraytoKaryString(byte[] bytes, int k, int width) {
		BigInteger bi = new BigInteger(bytes);
		String rep = bi.toString(k);
		if (rep.length() < width) {
			while (rep.length() != width) {
				rep = "0" + rep;
			}
		}

		assert (rep.length() == width);

		return rep;
	}

	public static byte[] bitsToByteArray(String bits) {
		BigInteger bi = new BigInteger(bits, 2);
		return bi.toByteArray();
	}

	public static void error(String m) {
		System.err.println(m);
		LOG.severe(m);
	}

	public static void error(String m, Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		LOG.severe(m + "\n" + e.getMessage() + "\n" + sw);
	}

	public static void disp(String m) {
		System.out.println(m);
		LOG.info(m);
	}

	public static void disp(byte[] b) {
		System.out.print("[");
		for (int i = 0; i < b.length - 2; i++) {
			System.out.print(b[i] + " ");
		}
		System.out.println("" + b[b.length - 1] + "]");
	}

	public static void warn(String m) {
		LOG.warning(m);
	}

	public static void debug(String m) {
		LOG.fine(m);
	}

	public static String addZero(String s, int l) {
		for (int i = s.length(); i < l; i++)
			s = "0" + s;
		return s;
	}

	public static byte[] addOrRmBits(byte[] b, int bits) {
		int outBytes = (bits + 7) / 8;
		if (b.length > outBytes)
			return Util.rmSignBit(b);
		else if (b.length < outBytes) {
			byte[] tmp = new byte[outBytes];
			System.arraycopy(b, 0, tmp, outBytes - b.length, b.length);
			return tmp;
		} else
			return b;
	}

	public static byte[] addZeroBits(byte[] b, int bits) {
		int outBytes = (bits + 7) / 8;
		if (b.length < outBytes) {
			byte[] tmp = new byte[outBytes];
			System.arraycopy(b, 0, tmp, outBytes - b.length, b.length);
			return tmp;
		} else
			return b;
	}

	public static List<Integer> getInversePermutation(List<Integer> p) {
		List<Integer> p_new = new ArrayList<Integer>(p);
		for (int i = 0; i < p.size(); i++)
			p_new.set(p.get(i), i);
		return p_new;
	}

	public static <T> T[] permute(T[] arr, List<Integer> p) {
		T[] arr_new = arr.clone();
		for (int i = 0; i < arr.length; i++) {
			arr_new[p.get(i)] = arr[i];
		}
		return arr_new;
	}

	// should be abandoned
	/*
	 * public static <T> T[] reversePermutation(T[] arr, List<Integer> p) { T[]
	 * arr_new = arr.clone(); for (int i = 0; i < arr.length; i++) { arr_new[i]
	 * = arr[p.get(i)]; } return arr_new; }
	 */

	public static <T> void printArrV(T[] arr) {
		for (int i = 0; i < arr.length; i++)
			System.out.println(arr[i]);
	}

	public static <T> void printArrH(T[] arr) {
		for (int i = 0; i < arr.length; i++)
			System.out.print(arr[i] + " ");
		System.out.println();
	}

	public static <T> void printListV(List<T> l) {
		for (int i = 0; i < l.size(); i++)
			System.out.println(l.get(i));
	}

	public static <T> void printListH(List<T> l) {
		for (int i = 0; i < l.size(); i++)
			System.out.print(l.get(i) + " ");
		System.out.println();
	}

	public static BigInteger nextBigInteger(BigInteger range) {
		BigInteger r;
		do {
			r = new BigInteger(range.bitLength(), SR.rand);
		} while (r.compareTo(range) >= 0);
		return r;
	}

	/*
	 * public static byte[] getSubBits(byte[] b, int i, int j) { if (i == j)
	 * return new byte[]{}; BigInteger bi = Util.getSubBits(new BigInteger(1,
	 * b), i, j); return Util.addOrRmBits(bi.toByteArray(), j-i); }
	 */

	public static BigInteger getSubBits(BigInteger n, int i, int j) {
		return BigInteger.ONE.shiftLeft(j - i).subtract(BigInteger.ONE)
				.shiftLeft(i).and(n).shiftRight(i);
	}

	public static BigInteger setSubBits(BigInteger target, BigInteger input,
			int i, int j) {
		/*
		for (int k = 0; k < j - i; k++) {
			if (input.testBit(k))
				target = target.setBit(i + k);
			else
				target = target.clearBit(i + k);
		}
		*/
		if (input.bitLength() > (j-i)) {
			try {
				throw new Exception("input longer than the relpacing bits");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		BigInteger original = Util.getSubBits(target, i, j);
		return target.xor(original.shiftLeft(i)).xor(input.shiftLeft(i));
	}

	public static byte[] rmSignBit(byte[] arr) {
		if (arr[0] == 0)
			return Arrays.copyOfRange(arr, 1, arr.length);
		return arr;
	}

	public static long nextLong(long n) {
		long bits, val;
		do {
			bits = (SR.rand.nextLong() << 1) >>> 1;
			val = bits % n;
		} while (bits - val + (n - 1) < 0L);
		return val;
	}

	public static long nextLong(long start, long end) {
		return nextLong(end - start) + start;
	}

	public static ECPoint byteArrayToECPoint(byte[] bytes) {
		return NISTNamedCurves.getByName("P-224").getCurve().decodePoint(bytes);
	}

	public static byte[][] cloneMatrix(byte[][] m) {
		if (m == null)
			return null;
		byte[][] out = new byte[m.length][];
		for (int i = 0; i < m.length; i++)
			out[i] = m[i].clone();
		return out;
	}

	/*
	 * public static <T> T[][] cloneMatrix(T[][] m) { if (m == null) return
	 * null; T[][] out = m.clone(); for (int i=0; i<m.length; i++) out[i] =
	 * m[i].clone(); return out; }
	 */
	
	public static byte[] xor(byte[] a, byte[] b) {
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
	
	public static byte[] and(byte[] a, byte[] b) {
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
}
