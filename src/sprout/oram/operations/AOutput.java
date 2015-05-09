package sprout.oram.operations;

import java.math.BigInteger;

public class AOutput {
	BigInteger Lip1;
	BigInteger sC_Ti;
	BigInteger sE_Ti;
	BigInteger sC_sig_P_p;
	BigInteger sE_sig_P_p;
	BigInteger data;
	BigInteger j_2;

	AOutput() {
	}

	AOutput(BigInteger l, BigInteger ct, BigInteger et, BigInteger cp,
			BigInteger ep, BigInteger d, BigInteger j2) {
		Lip1 = l;
		sC_Ti = ct;
		sE_Ti = et;
		sC_sig_P_p = cp;
		sE_sig_P_p = ep;
		data = d;
		j_2 = j2;
	}
}
