package sprout.crypto;

import java.math.BigInteger;
import java.security.MessageDigest;

import org.bouncycastle.math.ec.ECPoint;

import sprout.util.Util;

public class PRG {
	private MessageDigest digest = SR.digest;
	private int l; // output bit length

	public PRG(int l) {
		this.l = l;
	}

	public byte[] compute(ECPoint ecp) {
		return compute(ecp.getEncoded());
	}

	public byte[] compute(byte[] seed) {
		byte[] output = null;
		if (l <= 160) {
			output = leq160(seed, 0, l);
		} else {
			output = greater160(seed);
		}

		return output;
	}

	private byte[] leq160(byte[] input, int index, int np) {
		digest.update(input);
		if (index > 0)
			digest.update(BigInteger.valueOf(index).toByteArray());
		byte[] ctext = digest.digest();
		if (np == 160)
			return ctext;

		byte[] output = Util.getSubBits(new BigInteger(1, ctext), 0, np)
				.toByteArray();

		int outBytes = (np + 7) / 8;
		if (output.length > outBytes)
			output = Util.rmSignBit(output);
		else if (output.length < outBytes) {
			byte[] tmp = new byte[outBytes];
			System.arraycopy(output, 0, tmp, outBytes - output.length,
					output.length);
			output = tmp;
		}

		return output;
	}

	private byte[] greater160(byte[] input) {
		int n = l / 160;
		byte[] front = new byte[n * 20];
		for (int i = 0; i < n; i++) {
			byte[] seg = leq160(input, i + 1, 160);
			System.arraycopy(seg, 0, front, i * seg.length, seg.length);
		}

		int np = l % 160;
		if (np == 0)
			return front;

		byte[] back = leq160(input, n + 1, np);
		byte[] output = new BigInteger(1, front).shiftLeft(np)
				.or(new BigInteger(1, back)).toByteArray();

		int outBytes = (l + 7) / 8;
		if (output.length > outBytes)
			output = Util.rmSignBit(output);
		else if (output.length < outBytes) {
			byte[] tmp = new byte[outBytes];
			System.arraycopy(output, 0, tmp, outBytes - output.length,
					output.length);
			output = tmp;
		}

		return output;
	}

	/*
	 * private SecureRandom rand; private int l; // TODO: delete this or remove
	 * bits from args?
	 * 
	 * public PRG(int l) throws NoSuchAlgorithmException { this.rand =
	 * SecureRandom.getInstance("SHA1PRNG"); this.l = l; }
	 * 
	 * public PRG(int l, byte[] seed) throws NoSuchAlgorithmException {
	 * this.rand = SecureRandom.getInstance("SHA1PRNG");
	 * this.rand.setSeed(seed); this.l = l; }
	 * 
	 * private byte[] generateBytes(int bits) { byte[] bytes = new byte[(bits +
	 * 7) / 8]; this.rand.nextBytes(bytes); return bytes; }
	 * 
	 * public byte[] generateBytes(int bits, byte[] seed) {
	 * this.rand.setSeed(seed); return generateBytes(bits); }
	 * 
	 * public byte[] generateBytes(int bits, BigInteger seed) { return
	 * generateBytes(bits, seed.toByteArray()); }
	 * 
	 * public byte[] generateBytes(int bits, ECPoint seed) { return
	 * generateBytes(bits, seed.getEncoded()); }
	 * 
	 * private String generateBitString(int bits) { return Util.addZero(new
	 * BigInteger(bits, rand).toString(2), bits); }
	 * 
	 * public String generateBitString(int bits, byte[] seed) {
	 * rand.setSeed(seed); return generateBitString(bits); }
	 * 
	 * public String generateBitString(int bits, ECPoint seed) { return
	 * generateBitString(bits, seed.getEncoded()); }
	 * 
	 * public String generateBitString(int bits, BigInteger seed) { return
	 * generateBitString(bits, seed.toByteArray()); }
	 */

	public String generateBitString(int bits, byte[] seed) {
		return Util.addZero(new BigInteger(1, compute(seed)).toString(2), bits);
	}

	public String generateBitString(int bits, ECPoint seed) {
		return generateBitString(bits, seed.getEncoded());
	}

	public String generateBitString(int bits, BigInteger seed) {
		return generateBitString(bits, seed.toByteArray());
	}

	public static void main(String[] args) {
		int n = 10;
		int outBits = 1000;
		byte[][] input = new byte[n][16];
		byte[][] output = new byte[n][];
		PRG G = new PRG(outBits);

		for (int i = 0; i < n; i++) {
			SR.rand.nextBytes(input[i]);
			output[i] = G.compute(input[i]);
			System.out.println(new BigInteger(1, output[i]).toString(16));
		}

		for (int i = 0; i < n; i++) {
			byte[] tmp = G.compute(input[i]);
			System.out.println(new BigInteger(1, tmp).compareTo(new BigInteger(
					1, output[i])) == 0);
		}
	}
}
