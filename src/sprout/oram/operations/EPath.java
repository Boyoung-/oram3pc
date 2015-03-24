package sprout.oram.operations;

import java.math.BigInteger;
import java.util.Arrays;

import org.bouncycastle.math.ec.ECPoint;

import sprout.crypto.SR;

public class EPath {
	ECPoint[] x;
	BigInteger[] Bbar;
	BigInteger[] Bbar2;

	EPath(ECPoint[] xx, BigInteger[] bb) {
		if (xx != null && xx.length != bb.length)
			try {
				throw new Exception("Lengths not equal");
			} catch (Exception e) {
				e.printStackTrace();
			}
		if (xx != null)
			x = xx.clone();
		Bbar = bb.clone();
	}
	
	/*
	EPath(BigInteger[] bb, BigInteger[] bb2) {
		if (bb2.length != bb.length)
			try {
				throw new Exception("Lengths not equal");
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		Bbar = bb.clone();
		Bbar2 = bb2.clone();
	}
	*/

	// random generation for testing purpose
	EPath(int n, int l) {
		x = new ECPoint[n];
		Bbar = new BigInteger[n];
		for (int i = 0; i < n; i++) {
			x[i] = OPRFHelper.getOPRF().randomPoint();
			Bbar[i] = new BigInteger(l, SR.rand);
		}
	}

	@Override
	public String toString() {
		String out = "";
		if (x != null)
			out += "x: " + Arrays.toString(x) + "\n";
		if (Bbar != null)
			out += "Bbar: " + Arrays.toString(Bbar) + "\n";

		return out;
	}
}
