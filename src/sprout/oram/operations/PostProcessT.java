package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.oram.PreData;
import sprout.oram.Tree;
import sprout.util.Timing;
import sprout.util.Util;

// TODO: This operation is unlike the other TreeOperations we may want to 

public class PostProcessT extends TreeOperation<BigInteger, BigInteger[]> {

	public PostProcessT() {
		super(null, null);
	}

	public PostProcessT(Communication con1, Communication con2) {
		super(con1, con2);
	}
	
	@Override
	public BigInteger executeCharlieSubTree(Communication debbie,
			Communication eddie, Tree unused, BigInteger[] args, Timing localTiming) {
		BigInteger sC_Ti = args[0];
		BigInteger Li = args[1];
		BigInteger Lip1 = args[2];
		int j_2 = args[3].intValue();
		

		if (i == h) {
			BigInteger sC_Ti_p = Li.xor(PreData.ppt_sC_Li_p[i]).shiftLeft(aBits).xor(sC_Ti);
			return sC_Ti_p;
		}
		

		// protocol
		// step 1
		int sigma = PreData.ppt_alpha[i] - j_2;
		if (sigma < 0) {
			sigma += twotaupow;
		}
		
		eddie.write(sigma);
		
		
		// step 2
		BigInteger sC_A = Util.getSubBits(sC_Ti, 0, aBits);
		BigInteger[] c = new BigInteger[twotaupow];
		BigInteger c_all = BigInteger.ZERO;
		for (int t=0; t<twotaupow; t++) {
			c[t] = PreData.ppt_r[i][(t+sigma)%twotaupow];
			if (t == j_2) {
				c[t] = c[t].xor(Lip1).xor(PreData.ppt_sC_Lip1_p[i]);
			}
			c_all = c_all.shiftLeft(d_ip1).xor(c[t]);
 		}
		
		BigInteger sC_Ti_p;
		if (i == 0) {
			sC_Ti_p = c_all;
		}
		else {
			sC_Ti_p = sC_Ti.xor(Li.xor(PreData.ppt_sC_Li_p[i]).shiftLeft(aBits).xor(sC_A.xor(c_all)));
		}
		
		
		return sC_Ti_p;
	}

	@Override
	public BigInteger executeDebbieSubTree(Communication charlie,
			Communication eddie, Tree unused, BigInteger[] unused2, Timing localTiming) {
		return null;
	}

	@Override
	public BigInteger executeEddieSubTree(Communication charlie,
			Communication debbie, Tree unused, BigInteger[] args, Timing localTiming) {		
		BigInteger sE_Ti = args[0];
		

		if (i == h) {
			BigInteger sE_Ti_p = PreData.ppt_sE_Li_p[i].shiftLeft(aBits).xor(sE_Ti);
			return sE_Ti_p;
		}
		
		
		// protocol
		// step 1
		int sigma = charlie.readInt();
		
		
		// step 3
		BigInteger sE_A = Util.getSubBits(sE_Ti, 0, aBits);
		BigInteger[] e = new BigInteger[twotaupow];
		BigInteger e_all = BigInteger.ZERO;
		for (int t=0; t<twotaupow; t++) {
			e[t] = PreData.ppt_r_p[i][(t+sigma)%twotaupow];
			e_all = e_all.shiftLeft(d_ip1).xor(e[t]);
 		}
		
		BigInteger sE_Ti_p;
		if (i == 0) {
			sE_Ti_p = e_all;
		}
		else {
			sE_Ti_p = sE_Ti.xor(PreData.ppt_sE_Li_p[i].shiftLeft(aBits).xor(sE_A.xor(e_all)));
		}
		
		
		return sE_Ti_p;
	}
}
