package sprout.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;

public class DDHGroup {

	private String q;       // prime order of the group
	private BigInteger mod;
	private BigInteger gen;

	public DDHGroup(int primeBits) {
		this.mod = new BigInteger(primeBits, 1, SecureRandom.getInstance("SHA1PRNG"));
		this.q = BigInteger.toString();
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
			gen = gen.add(BigInteger.ONE); // increment and try again...
		}
	}

	public BigInteger randomElement() {
		SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");

		BigInteger elem;
		do {
			elem = new BigInteger(q, rand);
		} while (elem.compareTo(mod) >= 0);

		return elem;
	}
}