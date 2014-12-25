package sprout.oram.operations;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DPOutput {
	public BigInteger[] secretC_P;
	public BigInteger[] secretE_P;
	List<Integer> p;

	DPOutput() {
	}

	DPOutput(BigInteger[] c, BigInteger[] e, List<Integer> per) {
		if (c != null)
			secretC_P = c.clone();
		if (e != null)
			secretE_P = e.clone();
		if (per != null)
			p = new ArrayList<Integer>(per);
	}
}
