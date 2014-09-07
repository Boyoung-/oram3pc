package sprout.crypto;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import sprout.crypto.PRG;

// Weak PRF defined built upon a DDHPRG G with output length m bits (as a String)
public class WeakPRF {

	private BigInteger k;
	private PRG G;

	public WeakPRF(BigInteger k, int l) throws NoSuchAlgorithmException {
		this.k = k;
		this.G = new PRG(l);
	}

	// generate pseudorandom string of m bits
	// f_k^l(x) = G^l(x^k)
	public String compute(int m, BigInteger seed) {
		return G.generateBitString(m, seed);
	}
}
