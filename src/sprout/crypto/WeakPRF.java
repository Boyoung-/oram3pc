package sprout.crypto;

import java.math.BigInteger;

// Weak PRF defined built upon a DDHPRG G with output length m bits (as a String)
public class WeakPRF {

	private BigInteger k = null;
	private int l = 0; // in BITS

	public WeakPRF(BigInteger k, int l) {
		this.k = k;

	}

	// generate pseudorandom string of m bits
	// f_k^l(x) = G^l(x^k)
	public String generate(int m, BigInteger seed) {
		return null;
	}

}
