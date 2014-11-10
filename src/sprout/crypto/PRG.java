package sprout.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import sprout.util.Util;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECPoint;

public class PRG {

	private SecureRandom rand;
	private int l; // TODO: delete this or remove bits from args?
	
	public PRG(int l) throws NoSuchAlgorithmException {
		this.rand = SecureRandom.getInstance("SHA1PRNG");
		this.l = l;
	}
	
	public PRG(int l, byte[] seed) throws NoSuchAlgorithmException {
		this.rand = SecureRandom.getInstance("SHA1PRNG");
		this.rand.setSeed(seed);
		this.l = l;
	}

	public byte[] generateBytes(int bits) {
		byte[] bytes = new byte[(bits + 7) / 8];
		this.rand.nextBytes(bytes);
		return bytes;
	}

	public byte[] generateBytes(int bits, byte[] seed) {
		this.rand.setSeed(seed);
		return generateBytes(bits);
	}

	public byte[] generateBytes(int bits, BigInteger seed) {
		return generateBytes(bits, seed.toByteArray());
	}
	
	public byte[] generateBytes(int bits, ECPoint seed) {
		return generateBytes(bits, seed.getEncoded());
	}
	
	public String generateBitString(int bits) {
		return Util.addZero(new BigInteger(bits, rand).toString(2), bits);
	}
	
	public String generateBitString(int bits, byte[] seed) {
		rand.setSeed(seed);
		return generateBitString(bits);
	}

	public String generateBitString(int bits, ECPoint seed) {
	  return generateBitString(bits, seed.getEncoded());
	}
	
	public String generateBitString(int bits, BigInteger seed) {
		return generateBitString(bits, seed.toByteArray());
	}
}
