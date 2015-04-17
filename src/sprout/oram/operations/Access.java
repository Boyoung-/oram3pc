package sprout.oram.operations;

import java.math.BigInteger;

import org.apache.commons.lang3.tuple.Pair;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Bucket;
import sprout.oram.BucketException;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.oram.Tree;
import sprout.oram.TreeException;
import sprout.util.Timing;
import sprout.util.Util;

// TODO: rm all try catch

public class Access extends TreeOperation<AOutput, BigInteger[]> {

	public Access(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public AOutput executeCharlieSubTree(Communication debbie,
			Communication eddie, Tree unused, BigInteger[] args, Timing localTiming) {
		// protocol
		// step 1
		BigInteger Li = args[0];
		debbie.write(Li);
		eddie.write(Li);
		
		BigInteger sC_sig_P_all_p = debbie.readBigInteger();
		
		
		// step 2
		BigInteger sC_Nip1 = args[1];
		int Nip1Bits = (i < h - 1) ? (i + 1) * tau : ForestMetadata.getLastNBits();
		BigInteger sC_Ni = Util.getSubBits(sC_Nip1, Nip1Bits - nBits, Nip1Bits);
		
		debbie.write(sC_Ni);
		
		int j_1 = 0;
		BigInteger eBar = BigInteger.ZERO; // TODO: i=0 case???
		BigInteger z = BigInteger.ZERO; // TODO: i=0 case???
		if (i > 0) {
			SSCOT sscot = new SSCOT(debbie, eddie);
			Pair<Integer, BigInteger> je = sscot.executeCharlie(debbie, eddie, i, pathTuples, aBits, 1+nBits);
			if (je == null) {
				try {
					throw new Exception("SSCOT error!");
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			j_1 = je.getLeft();
			eBar = je.getRight();
			
			BigInteger helper = BigInteger.ONE.shiftLeft(aBits).subtract(BigInteger.ONE);
			BigInteger dBar = sC_sig_P_all_p.shiftRight((pathTuples-j_1-1)*tupleBits).add(helper);
			z = eBar.xor(dBar);
		}
		
		
		
		
		
		
		
		
		
		
		BigInteger[] sC_sig_P = debbie.readBigIntegerArray();
		

		// step 3
		
		
		
		// step 4
		BigInteger fbar = BigInteger.ZERO;;
		if (i > 0) {
			AOT aot = new AOT(debbie, eddie);
			fbar = aot.executeCharlie(debbie, eddie, j_1);
		}


		// step 5
		int j_2 = 0;
		BigInteger ybar_j2 = null;
		if (i < h) {
			BigInteger sC_Nip1_pr = Util.getSubBits(sC_Nip1, 0, Nip1Bits - nBits);
			j_2 = sC_Nip1_pr.intValue();
			AOTSS aotss = new AOTSS(debbie, eddie);
			ybar_j2 = aotss.executeCharlie(debbie, eddie, sC_Nip1_pr);
		}
		System.out.println("Charlie: i=" + i + ", j_2=" + j_2);

		
		// step 6
		BigInteger ybar = BigInteger.ZERO;
		for (int o = 0; o < twotaupow; o++) {
			ybar = ybar.shiftLeft(d_ip1);
			if (i < h && o == j_2)
				ybar = ybar.xor(ybar_j2);
		}

		BigInteger sC_sig_P_all = sC_sig_P[0];
		for (int j = 1; j < sC_sig_P.length; j++)
			sC_sig_P_all = sC_sig_P_all.shiftLeft(bucketBits).xor(sC_sig_P[j]);
		BigInteger sC_Aj1;
		if (i == 0)
			sC_Aj1 = sC_sig_P_all;
		else
			sC_Aj1 = Util.getSubBits(sC_sig_P_all, (pathTuples - j_1 - 1)
					* tupleBits, (pathTuples - j_1 - 1) * tupleBits + aBits);
		BigInteger Abar = sC_Aj1.xor(fbar).xor(ybar);

		BigInteger d = null;
		BigInteger Lip1 = null;
		if (i < h) {
			Lip1 = Util.getSubBits(Abar, (twotaupow - j_2 - 1) * d_ip1,
					(twotaupow - j_2) * d_ip1);
		} else {
			d = Abar;
		}

		BigInteger sC_Ti = sC_Aj1.xor(fbar);
		if (i > 0)
			sC_Ti = sC_Ni.shiftLeft(lBits + aBits).xor(Li.shiftLeft(aBits))
					.xor(sC_Ti).setBit(tupleBits - 1);
		
		BigInteger sC_sig_P_p = null;
		if (i > 0) {
			boolean flipBit = !sC_sig_P_all.testBit((pathTuples - j_1) * tupleBits
					- 1);
			BigInteger newTuple = new BigInteger(tupleBits - 1, SR.rand);
			if (flipBit)
				newTuple = newTuple.setBit(tupleBits - 1);
			sC_sig_P_p = Util.setSubBits(sC_sig_P_all, newTuple, (pathTuples
					- j_1 - 1)
					* tupleBits, (pathTuples - j_1) * tupleBits);
		}
		

		return new AOutput(Lip1, sC_Ti, null, sC_sig_P_p, null, d);
	}

	@Override
	public AOutput executeDebbieSubTree(Communication charlie,
			Communication eddie, Tree sD_OT, BigInteger[] unused, Timing localTiming) {
		// protocol
		// step 1
		BigInteger Li = charlie.readBigInteger();
		if (i > 0)
			System.out.println("Debbie: i=" + i + ", Li=" + Util.addZero(Li.toString(2), lBits));
		
		Bucket[] sD_buckets = null;
		try {
			sD_buckets = sD_OT.getBucketsOnPath(Li);
		} catch (TreeException | BucketException e) {
			e.printStackTrace();
		}
		BigInteger[] sD_P = new BigInteger[sD_buckets.length];
		for (int j = 0; j < sD_buckets.length; j++) {
			sD_P[j] = new BigInteger(1, sD_buckets[j].getByteTuples());
		}
		BigInteger[] sD_sig_P = Util.permute(sD_P, PreData.access_sigma[i]);
		
		BigInteger sD_sig_P_all = sD_sig_P[0];
		for (int j = 1; j < sD_sig_P.length; j++)
			sD_sig_P_all = sD_sig_P_all.shiftLeft(bucketBits).xor(sD_sig_P[j]);
		BigInteger sD_sig_P_all_p = sD_sig_P_all.xor(PreData.access_p[i]);
		
		charlie.write(sD_sig_P_all_p);
		
		
		// step 2
		BigInteger sD_Ni = charlie.readBigInteger();

		BigInteger[] a = new BigInteger[pathTuples];
		BigInteger[] share1N = new BigInteger[pathTuples];
		BigInteger helper;
		BigInteger tmp;
		if (i > 0) {
			//System.out.println("Debbie: i=" + i + ", Ni="
			//		+ Util.addZero(sD_Ni.toString(2), nBits));
			helper = BigInteger.ONE.shiftLeft(1 + nBits).subtract(BigInteger.ONE);
			tmp = sD_sig_P_all_p.shiftRight(lBits + aBits);
			for (int j = pathTuples - 1; j >= 0; j--) {
				share1N[j] = tmp.and(helper);
				tmp = tmp.shiftRight(tupleBits);
				a[j] = share1N[j].xor(sD_Ni.setBit(nBits));
			}
			
			SSCOT sscot = new SSCOT(charlie, eddie);
			sscot.executeDebbie(charlie, eddie, i, pathTuples, aBits, 1+nBits, a);
		}
		
		
		
		
		
		
		
		charlie.write(sD_sig_P);
		

		// step 3
		
		
		
		// step 4
		if (i > 0) {
			AOT aot = new AOT(charlie, eddie);
			aot.executeDebbie(charlie, eddie, i);
		}
		
		
		// step 5
		if (i < h) {
			AOTSS aotss = new AOTSS(charlie, eddie);
			aotss.executeDebbie(charlie, eddie, i, d_ip1);
		}
		

		return null;
	}

	@Override
	public AOutput executeEddieSubTree(Communication charlie,
			Communication debbie, Tree sE_OT, BigInteger[] args, Timing localTiming) {
		// protocol
		// step 1
		BigInteger Li = charlie.readBigInteger();
		//if (i > 0)
		//	System.out.println("Eddie: i=" + i + ", Li=" + Util.addZero(Li.toString(2), lBits));
		
		Bucket[] sE_buckets = null;
		try {
			sE_buckets = sE_OT.getBucketsOnPath(Li);
		} catch (TreeException | BucketException e1) {
			e1.printStackTrace();
		}
		BigInteger[] sE_P = new BigInteger[sE_buckets.length];
		for (int j = 0; j < sE_buckets.length; j++) {
			sE_P[j] = new BigInteger(1, sE_buckets[j].getByteTuples());
		}
		BigInteger[] sE_sig_P = Util.permute(sE_P, PreData.access_sigma[i]);
		
		BigInteger sE_sig_P_all = sE_sig_P[0];
		for (int j = 1; j < sE_sig_P.length; j++)
			sE_sig_P_all = sE_sig_P_all.shiftLeft(bucketBits).xor(sE_sig_P[j]);
		BigInteger sE_sig_P_all_p = sE_sig_P_all.xor(PreData.access_p[i]);
		
			
		BigInteger[] y = new BigInteger[twotaupow];
		BigInteger y_all;
		if (i == 0)
			y_all = sE_sig_P_all_p;
		else if (i < h)
			y_all = new BigInteger(aBits, SR.rand);
		else
			y_all = BigInteger.ZERO;
		BigInteger helper = BigInteger.ONE.shiftLeft(d_ip1).subtract(BigInteger.ONE);
		BigInteger tmp = y_all;
		for (int o = twotaupow - 1; o >= 0; o--) {
			y[o] = tmp.and(helper);
			tmp = tmp.shiftRight(d_ip1);
		}
		
		
		// step 2
		BigInteger sE_Nip1 = args[0];
		int Nip1Bits = (i < h - 1) ? (i + 1) * tau : ForestMetadata.getLastNBits();
		BigInteger sE_Ni = Util.getSubBits(sE_Nip1, Nip1Bits - nBits, Nip1Bits);

		BigInteger[] e = new BigInteger[pathTuples];
		BigInteger[] b = new BigInteger[pathTuples];
		BigInteger[] share1N = new BigInteger[pathTuples];
		BigInteger[] shareA = new BigInteger[pathTuples];
		BigInteger helper1N;
		BigInteger helperA;
		if (i > 0) {
			//System.out.println("Eddie: i=" + i + ", Ni=" + Util.addZero(sE_Ni.toString(2), nBits));
			helper1N = BigInteger.ONE.shiftLeft(1 + nBits).subtract(BigInteger.ONE);
			helperA = BigInteger.ONE.shiftLeft(aBits).subtract(BigInteger.ONE);
			tmp = sE_sig_P_all_p;
			for (int j = pathTuples - 1; j >= 0; j--) {
				shareA[j] = tmp.and(helperA);
				tmp = tmp.shiftRight(lBits + aBits);
				share1N[j] = tmp.and(helper1N);
				tmp = tmp.shiftRight(1 + nBits);
				e[j] = shareA[j].xor(y_all);
				b[j] = share1N[j].xor(sE_Ni);
			}
			
			SSCOT sscot = new SSCOT(charlie, debbie);
			sscot.executeEddie(charlie, debbie, i, pathTuples, aBits, 1+nBits, e, b);
		}
		
		
		
		
		
		
		
		// step 4
		if (i > 0) {
			BigInteger[] e = new BigInteger[pathTuples];
			BigInteger[] f = new BigInteger[pathTuples];
			helper = BigInteger.ONE.shiftLeft(aBits).subtract(BigInteger.ONE);
			tmp = sE_sig_P_all;
			for (int j=pathTuples-1; j>=0; j--) {
				e[j] = tmp.and(helper);
				f[j] = e[j].xor(y_all);
				tmp = tmp.shiftRight(tupleBits);
			}
			
			AOT aot = new AOT(charlie, debbie);
			aot.executeEddie(charlie, debbie, i, f);
		}
		
		
		// step 5
		if (i < h) {
			BigInteger sE_Nip1_pr = Util.getSubBits(sE_Nip1, 0, Nip1Bits - nBits);
			AOTSS aotss = new AOTSS(charlie, debbie);
			aotss.executeEddie(charlie, debbie, i, d_ip1, y, sE_Nip1_pr);
		}
		
		
		// step 6
		BigInteger sE_Ti = y_all;
		BigInteger sE_sig_P_p = null;
		if (i > 0) {
			sE_Ti = sE_Ni.shiftLeft(lBits + aBits).xor(sE_Ti);
			sE_sig_P_p = sE_sig_P_all;
		}
		

		return new AOutput(null, null, sE_Ti, null, sE_sig_P_p, null);
	}

	// for testing correctness
	@Override
	public void run(Party party, Forest forest) throws ForestException {
	}
}
