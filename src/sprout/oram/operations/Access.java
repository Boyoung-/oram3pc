package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Bucket;
import sprout.oram.BucketException;
import sprout.oram.ForestMetadata;
import sprout.oram.PID;
import sprout.oram.PreData;
import sprout.oram.TID;
import sprout.oram.Tree;
import sprout.oram.TreeException;
import sprout.util.Timing;
import sprout.util.Util;

public class Access extends TreeOperation<AOutput, BigInteger[]> {

	public Access(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public AOutput executeCharlieSubTree(Communication debbie,
			Communication eddie, Tree unused, BigInteger[] args, Timing localTiming) {
		//BigInteger sC_Nip1 = args[1];

		//int Nip1Bits = (i < h - 1) ? (i + 1) * tau : ForestMetadata
		//		.getLastNBits();
		//BigInteger Ni = Util.getSubBits(Nip1, Nip1Bits - nBits, Nip1Bits);
		//BigInteger Nip1_pr = Util.getSubBits(Nip1, 0, Nip1Bits - nBits);

		//debbie.countBandwidth = true;
		//eddie.countBandwidth = true;
		//debbie.bandwidth[PID.access].start();
		//eddie.bandwidth[PID.access].start();

		// protocol
		// step 1
		timing.stopwatch[PID.access][TID.online_write].start();
		BigInteger Li = args[0];
		debbie.write(Li);
		eddie.write(Li);
		timing.stopwatch[PID.access][TID.online_write].stop();
		
		timing.stopwatch[PID.access][TID.online_read].start();
		BigInteger[] sC_sig_P = debbie.readBigIntegerArray();
		timing.stopwatch[PID.access][TID.online_read].stop();
		

		// step 3
		BigInteger sC_Nip1 = args[1];
		int Nip1Bits = (i < h - 1) ? (i + 1) * tau : ForestMetadata.getLastNBits();
		BigInteger sC_Ni = Util.getSubBits(sC_Nip1, Nip1Bits - nBits, Nip1Bits);
		
		debbie.write(sC_Ni);
		
		
		
		// step 3
		timing.stopwatch[PID.access][TID.online].start();
		BigInteger secretC_P = DecOut.secretC_P[0];
		for (int j = 1; j < DecOut.secretC_P.length; j++)
			secretC_P = secretC_P.shiftLeft(bucketBits)
					.xor(DecOut.secretC_P[j]);
		timing.stopwatch[PID.access][TID.online].stop();

		// step 3
		// party C and E
		int j_1 = 0; // i = 0 case; as the j_1 = 1 in the write up
		//BigInteger[] a = new BigInteger[pathTuples];
		//BigInteger[] c = new BigInteger[pathTuples];
		if (i > 0) {
			PET pet = new PET(debbie, eddie);
			j_1 = pet.executeCharlie(debbie, eddie);
		}
		if (j_1 < 0) {
			try {
				throw new Exception("PET error!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		

		// step 4
		AOT aot = new AOT(debbie, eddie);
		BigInteger fbar;
		if (i == 0)
			fbar = BigInteger.ZERO;
		else {
			fbar = aot.executeC(debbie, eddie, j_1, pathTuples);
			// outputs fbar for C
		}

		// step 5
		int j_2 = 0;
		BigInteger ybar_j2 = null;
		if (i < h) {
			// AOT(E, C, D)
			j_2 = Nip1_pr.intValue();
			ybar_j2 = aot.executeC(debbie, eddie, j_2, twotaupow);
			// outputs ybar_j2 for C
		}

		// step 6
		// party C
		timing.stopwatch[PID.access][TID.online].start();
		BigInteger ybar = BigInteger.ZERO;
		for (int o = 0; o < twotaupow; o++) {
			ybar = ybar.shiftLeft(d_ip1);
			if (i < h && o == j_2)
				ybar = ybar.xor(ybar_j2);
		}

		BigInteger secretC_Aj1;
		if (i == 0)
			secretC_Aj1 = secretC_P;
		else
			secretC_Aj1 = Util.getSubBits(secretC_P, (pathTuples - j_1 - 1)
					* tupleBits, (pathTuples - j_1 - 1) * tupleBits + aBits);
		BigInteger Abar = secretC_Aj1.xor(fbar).xor(ybar);

		BigInteger d = null;
		BigInteger Lip1 = null;
		if (i < h) {
			Lip1 = Util.getSubBits(Abar, (twotaupow - j_2 - 1) * d_ip1,
					(twotaupow - j_2) * d_ip1);
		} else {
			d = Abar;
		}

		BigInteger secretC_Ti = secretC_Aj1.xor(fbar);
		if (i > 0)
			secretC_Ti = Ni.shiftLeft(lBits + aBits).xor(Li.shiftLeft(aBits))
					.xor(secretC_Ti).setBit(tupleBits - 1);
		BigInteger secretC_P_p = null;
		if (i > 0) {
			boolean flipBit = !secretC_P.testBit((pathTuples - j_1) * tupleBits
					- 1);
			BigInteger newTuple = new BigInteger(tupleBits - 1, SR.rand);
			if (flipBit)
				newTuple = newTuple.setBit(tupleBits - 1);
			secretC_P_p = Util.setSubBits(secretC_P, newTuple, (pathTuples
					- j_1 - 1)
					* tupleBits, (pathTuples - j_1) * tupleBits);
		}
		timing.stopwatch[PID.access][TID.online].stop();

		debbie.bandwidth[PID.access].stop();
		eddie.bandwidth[PID.access].stop();
		debbie.countBandwidth = false;
		eddie.countBandwidth = false;

		// C outputs Lip1, secretC_Ti, secretC_P_p
		return new AOutput(Lip1, null, secretC_Ti, null, secretC_P_p, null, d);
	}

	@Override
	public AOutput executeDebbieSubTree(Communication charlie,
			Communication eddie, Tree sD_OT, BigInteger[] unused, Timing localTiming) {
		//charlie.countBandwidth = true;
		//eddie.countBandwidth = true;
		//charlie.bandwidth[PID.access].start();
		//eddie.bandwidth[PID.access].start();

		// protocol
		// step 1
		timing.stopwatch[PID.access][TID.online_read].start();
		BigInteger Li = charlie.readBigInteger();
		timing.stopwatch[PID.access][TID.online_read].stop();
		
		timing.stopwatch[PID.access][TID.online].start();
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
		timing.stopwatch[PID.access][TID.online].stop();
		
		timing.stopwatch[PID.access][TID.online_write].start();
		charlie.write(sD_sig_P);
		timing.stopwatch[PID.access][TID.online_write].stop();
		

		// step 3
		BigInteger sD_Ni = charlie.readBigInteger();	
		
		BigInteger sD_sig_P_all = sD_sig_P[0];
		for (int j = 1; j < sD_sig_P.length; j++)
			sD_sig_P_all = sD_sig_P_all.shiftLeft(bucketBits).xor(sD_sig_P[j]);
		BigInteger[] a = new BigInteger[pathTuples];
		BigInteger[] c = new BigInteger[pathTuples];
		BigInteger helper;
		BigInteger tmp;
		if (i > 0) {
			helper = BigInteger.ONE.shiftLeft(1 + nBits).subtract(BigInteger.ONE);
			tmp = sD_sig_P_all.shiftRight(lBits + aBits);
			for (int j = pathTuples - 1; j >= 0; j--) {
				a[j] = tmp.and(helper);
				c[j] = a[j].xor(sD_Ni.setBit(nBits));
				tmp = tmp.shiftRight(tupleBits);
			}
			PET pet = new PET(charlie, eddie);
			pet.executeDebbie(charlie, eddie, c, i);
		}
		
		
		
		
		
		// step 2
		AOT aot = new AOT(charlie, eddie);
		if (i > 0) {
			// step 3
			PET pet = new PET(charlie, eddie);
			pet.executeDebbie(charlie, eddie);
			// PET outputs j_1 for C

			// step 4
			aot.executeD(charlie, eddie, aBits, i, 0);
		}

		// step 5
		if (i < h) {
			// AOT(E, C, D)
			aot.executeD(charlie, eddie, d_ip1, i, 1);
			// outputs ybar_j2 for C
		}

		//charlie.bandwidth[PID.access].stop();
		//eddie.bandwidth[PID.access].stop();
		//charlie.countBandwidth = false;
		//eddie.countBandwidth = false;

		return null;
	}

	@Override
	public AOutput executeEddieSubTree(Communication charlie,
			Communication debbie, Tree sE_OT, BigInteger[] args, Timing localTiming) {
		//charlie.countBandwidth = true;
		//debbie.countBandwidth = true;
		//charlie.bandwidth[PID.access].start();
		//debbie.bandwidth[PID.access].start();
		//BigInteger sE_Nip1 = args[0];

		// protocol
		// step 1
		timing.stopwatch[PID.access][TID.online_read].start();
		BigInteger Li = charlie.readBigInteger();
		timing.stopwatch[PID.access][TID.online_read].stop();
		
		timing.stopwatch[PID.access][TID.online].start();
		Bucket[] sE_buckets = sE_OT.getBucketsOnPath(Li);
		BigInteger[] sE_P = new BigInteger[sE_buckets.length];
		for (int j = 0; j < sE_buckets.length; j++) {
			sE_P[j] = new BigInteger(1, sE_buckets[j].getByteTuples());
		}
		BigInteger[] sE_sig_P = Util.permute(sE_P, PreData.access_sigma[i]);
		timing.stopwatch[PID.access][TID.online].stop();
		

		// step 2
		timing.stopwatch[PID.access][TID.online].start();
		BigInteger sE_sig_P_all = sE_sig_P[0];
		for (int j = 1; j < sE_sig_P.length; j++)
			sE_sig_P_all = sE_sig_P_all.shiftLeft(bucketBits).xor(sE_sig_P[j]);
		BigInteger[] y = new BigInteger[twotaupow];
		BigInteger y_all;
		if (i == 0)
			y_all = sE_sig_P_all;
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
		timing.stopwatch[PID.access][TID.online].stop();
		
		
		// step 3
		BigInteger sE_Nip1 = args[0];
		int Nip1Bits = (i < h - 1) ? (i + 1) * tau : ForestMetadata.getLastNBits();
		BigInteger sE_Ni = Util.getSubBits(sE_Nip1, Nip1Bits - nBits, Nip1Bits);

		BigInteger[] e = new BigInteger[pathTuples];
		BigInteger[] b = new BigInteger[pathTuples];
		if (i > 0) {
			helper = BigInteger.ONE.shiftLeft(1 + nBits).subtract(BigInteger.ONE);
			tmp = sE_sig_P_all.shiftRight(lBits + aBits);
			for (int j = pathTuples - 1; j >= 0; j--) {
				e[j] = tmp.and(helper);
				b[j] = e[j].xor(sE_Ni);
				tmp = tmp.shiftRight(tupleBits);
			}
			PET pet = new PET(charlie, debbie);
			pet.executeEddie(charlie, debbie, b, i);
		}
		
		


		//BigInteger secretE_Ti = y_all;
		//BigInteger secretE_P_p = null;
		//if (i > 0)
		//	secretE_P_p = secretE_P;
		
		// step 4
		// party E
		AOT aot = new AOT(charlie, debbie);
		if (i > 0) {
			timing.stopwatch[PID.access][TID.online].start();
			BigInteger[] e = new BigInteger[pathTuples];
			BigInteger[] f = new BigInteger[pathTuples];
			helper = BigInteger.ONE.shiftLeft(aBits).subtract(BigInteger.ONE);
			tmp = secretE_P;
			for (int o = pathTuples - 1; o >= 0; o--) {
				e[o] = tmp.and(helper);
				tmp = tmp.shiftRight(tupleBits);
				f[o] = e[o].xor(y_all);
			}
			timing.stopwatch[PID.access][TID.online].stop();

			// AOT(E, C, D)
			aot.executeE(charlie, debbie, f, aBits, i, 0);
			// outputs fbar for C
		}

		// step 5
		if (i < h) {
			// AOT(E, C, D)
			aot.executeE(charlie, debbie, y, d_ip1, i, 1);
			// outputs ybar_j2 for C
		}

		charlie.bandwidth[PID.access].stop();
		debbie.bandwidth[PID.access].stop();
		charlie.countBandwidth = false;
		debbie.countBandwidth = false;

		// E outputs secretE_Ti and secretE_P_p
		return new AOutput(null, DecOut.p, null, secretE_Ti, null, secretE_P_p,
				null);
	}
}
