package sprout.oram.operations;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class AOutput {
	BigInteger Lip1;
	List<Integer> p;
	BigInteger secretC_Ti;
	BigInteger secretE_Ti;
	BigInteger secretC_P_p;
	BigInteger secretE_P_p;
	BigInteger data;

	AOutput() {
	}

	AOutput(BigInteger l, List<Integer> per, BigInteger ct, BigInteger et,
			BigInteger cp, BigInteger ep, BigInteger d) {
		Lip1 = l;
		if (per != null)
			p = new ArrayList<Integer>(per);
		secretC_Ti = ct;
		secretE_Ti = et;
		secretC_P_p = cp;
		secretE_P_p = ep;
		data = d;
	}
}
