package sprout.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import sprout.crypto.CryptoException;

import java.math.BigInteger;

public class PRG {

	private SecureRandom rand;
	private int l;
	
	public PRG(int l) throws NoSuchAlgorithmException {
		this.rand = SecureRandom.getInstance("SHA1PRNG");
		this.l = l;
	}
	
	public PRG(int l, byte[] seed) throws NoSuchAlgorithmException {
		this.rand = SecureRandom.getInstance("SHA1PRNG");
		this.rand.setSeed(seed);
		this.l = l;
	}

	public byte[] generateBytes(int m) {
		byte[] bytes = new byte[(m + 7) / 8];
		this.rand.nextBytes(bytes);
		return bytes;
	}

	public byte[] generateBytes(int m, byte[] seed) {
		this.rand.setSeed(seed);
		return generateBytes(m);
	}

	public byte[] generateBytes(int m, BigInteger seed) {
		return generateBytes(m, seed.toByteArray());
	}

	public String generateBitString(int m) {
		byte[] bytes = generateBytes(m);
		return new BigInteger(bytes).toString(2).substring(0, m); // bit string of length m
	}

	public String generateBitString(int m, byte[] seed) {
		byte[] bytes = generateBytes(m, seed);
		return new BigInteger(bytes).toString(2).substring(0, m); // bit string of length m
	}

	public String generateBitString(int m, BigInteger seed) {
		byte[] bytes = generateBytes(m, seed);
		return new BigInteger(bytes).toString(2).substring(0, m); // bit string of length m
	}
}
