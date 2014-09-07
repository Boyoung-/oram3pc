package sprout.crypto;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class DDHGroup {

	private String q; // prime order of the group
	private int bitLength;
	private BigInteger mod;
	private BigInteger gen;

	public DDHGroup(int primeBits) throws NoSuchAlgorithmException {
		// this creates a -probable- prime, but we want certainty 1
		this.mod = new BigInteger(primeBits, 1, SecureRandom.getInstance("SHA1PRNG"));
		this.q = mod.toString();
		this.bitLength = mod.bitLength();
		setGenerator();
	}

	public DDHGroup(String q) {
		this.q = new String(q);
		this.mod = new BigInteger(q);
		setGenerator();
	}

	private void setGenerator() {
		this.gen = new BigInteger("3");
		while (gen.gcd(mod) == BigInteger.ZERO) {
			gen = gen.add(BigInteger.ONE); 
		}
	}

	public BigInteger randomElement() throws NoSuchAlgorithmException {
		SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");

		BigInteger elem;
		do {
			elem = new BigInteger(bitLength, rand);
		} while (elem.compareTo(mod) >= 0);

		return elem;
	}
}