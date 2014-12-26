package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.ForestMetadata;
import sprout.oram.PID;
import sprout.util.Util;

public class Access extends TreeOperation<AOutput, BigInteger[]> {

	public Access(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public AOutput executeCharlieSubTree(Communication debbie,
			Communication eddie, BigInteger[] args) {

		// prepare
		BigInteger Li = args[0];
		BigInteger Nip1 = args[1];

		int Nip1Bits = (i < h - 1) ? (i + 1) * tau : ForestMetadata
				.getLastNBits();
		BigInteger Ni = Util.getSubBits(Nip1, Nip1Bits - nBits, Nip1Bits);
		BigInteger Nip1_pr = Util.getSubBits(Nip1, 0, Nip1Bits - nBits);

		debbie.countBandwidth = true;
		eddie.countBandwidth = true;
		debbie.bandwidth[PID.access].start();
		eddie.bandwidth[PID.access].start();

		sanityCheck();

		// protocol
		// step 1
		// run DecryptPath on C's input Li, E's input OT_i, and D's input k
		DecryptPath dp = new DecryptPath(debbie, eddie);
		dp.loadTreeSpecificParameters(i);
		timing.decrypt.start();
		DPOutput DecOut = dp.executeCharlieSubTree(debbie, eddie, Li);
		timing.decrypt.stop();
		BigInteger secretC_P = DecOut.secretC_P[0];
		for (int j = 1; j < DecOut.secretC_P.length; j++)
			secretC_P = secretC_P.shiftLeft(bucketBits)
					.xor(DecOut.secretC_P[j]);

		// step 3
		// party C and E
		int j_1 = 0; // i = 0 case; as the j_1 = 1 in the write up
		BigInteger[] a = new BigInteger[pathTuples];
		BigInteger[] c = new BigInteger[pathTuples];
		if (i > 0) {
			timing.access_online.start();
			BigInteger helper = BigInteger.ONE.shiftLeft(1 + nBits).subtract(
					BigInteger.ONE);
			BigInteger tmp = secretC_P.shiftRight(lBits + aBits);
			for (int j = pathTuples - 1; j >= 0; j--) {
				a[j] = tmp.and(helper);
				tmp = tmp.shiftRight(tupleBits);
				c[j] = Ni.setBit(nBits).xor(a[j]);
			}
			timing.access_online.stop();
			// sanityCheck()();
			PET pet = new PET(debbie, eddie);
			timing.pet.start();
			j_1 = pet.executeCharlie(debbie, eddie, c);
			timing.pet.stop();
			// PET outputs j_1 for C
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
			// sanityCheck();
			timing.aot.start();
			fbar = aot.executeC(debbie, eddie, j_1);
			timing.aot.stop();
			// outputs fbar for C
		}

		// step 5
		int j_2 = 0;
		BigInteger ybar_j2 = null;
		if (i < h) {
			// AOT(E, C, D)
			// sanityCheck();
			j_2 = Nip1_pr.intValue();
			timing.aot.start();
			ybar_j2 = aot.executeC(debbie, eddie, j_2);
			timing.aot.stop();
			// outputs ybar_j2 for C
		}

		// step 6
		// party C
		BigInteger ybar = BigInteger.ZERO;
		timing.access_online.start();
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
			/*
			 * BigInteger tmp1 = Util.getSubBits(secretC_P, (pathTuples - j_1)
			 * tupleBits, pathTuples * tupleBits); BigInteger tmp2 =
			 * Util.getSubBits(secretC_P, 0, (pathTuples - j_1 - 1) *
			 * tupleBits); secretC_P_p = tmp1 .shiftLeft((pathTuples - j_1) *
			 * tupleBits) .xor(newTuple.shiftLeft((pathTuples - j_1 - 1) *
			 * tupleBits)) .xor(tmp2);
			 */
			secretC_P_p = Util.setSubBits(secretC_P, newTuple, (pathTuples
					- j_1 - 1)
					* tupleBits, (pathTuples - j_1) * tupleBits);
		}
		timing.access_online.stop();

		debbie.bandwidth[PID.access].stop();
		eddie.bandwidth[PID.access].stop();
		debbie.countBandwidth = false;
		eddie.countBandwidth = false;

		// sanityCheck();
		// C outputs Lip1, secretC_Ti, secretC_P_p
		return new AOutput(Lip1, null, secretC_Ti, null, secretC_P_p, null, d);
	}

	@Override
	public AOutput executeDebbieSubTree(Communication charlie,
			Communication eddie, BigInteger[] args) {
		BigInteger k = args[0];

		charlie.countBandwidth = true;
		eddie.countBandwidth = true;
		charlie.bandwidth[PID.access].start();
		eddie.bandwidth[PID.access].start();

		sanityCheck();

		// protocol
		// step 1
		// run DecryptPath on C's input Li, E's input OT_i, and D's input k
		DecryptPath dp = new DecryptPath(charlie, eddie);
		dp.loadTreeSpecificParameters(i);
		timing.decrypt.start();
		dp.executeDebbieSubTree(charlie, eddie, k);
		timing.decrypt.stop();
		// DecryptPath outpus sigma and secretE_P for E and secretC_P for C

		AOT aot = new AOT(charlie, eddie);
		if (i > 0) {
			// step 3
			// sanityCheck();
			PET pet = new PET(charlie, eddie);
			timing.pet.start();
			pet.executeDebbie(charlie, eddie, pathTuples);
			timing.pet.stop();
			// PET outputs j_1 for C

			// sanityCheck();
			// step 4
			timing.aot.start();
			aot.executeD(charlie, eddie);
			timing.aot.stop();
		}

		// step 5
		if (i < h) {
			// AOT(E, C, D)
			// sanityCheck();
			timing.aot.start();
			aot.executeD(charlie, eddie);
			timing.aot.stop();
			// outputs ybar_j2 for C
		}

		charlie.bandwidth[PID.access].stop();
		eddie.bandwidth[PID.access].stop();
		charlie.countBandwidth = false;
		eddie.countBandwidth = false;

		// sanityCheck();
		// return new AOutput();
		return null;
	}

	@Override
	public AOutput executeEddieSubTree(Communication charlie,
			Communication debbie, BigInteger[] args_unused) {
		charlie.countBandwidth = true;
		debbie.countBandwidth = true;
		charlie.bandwidth[PID.access].start();
		debbie.bandwidth[PID.access].start();

		sanityCheck();

		// protocol
		// step 1
		// run DecryptPath on C's input Li, E's input OT_i, and D's input k
		DecryptPath dp = new DecryptPath(charlie, debbie);
		dp.loadTreeSpecificParameters(i);
		timing.decrypt.start();
		DPOutput DecOut = dp.executeEddieSubTree(charlie, debbie, null);
		timing.decrypt.stop();
		BigInteger secretE_P = DecOut.secretE_P[0];
		for (int j = 1; j < DecOut.secretE_P.length; j++)
			secretE_P = secretE_P.shiftLeft(bucketBits)
					.xor(DecOut.secretE_P[j]);
		// DecryptPath outpus sigma and secretE_P for E and secretC_P for C

		// step 2
		// party E
		timing.access_online.start();
		BigInteger[] y = new BigInteger[twotaupow];
		BigInteger y_all;
		if (i == 0)
			y_all = secretE_P;
		else if (i < h)
			y_all = new BigInteger(aBits, SR.rand);
		else
			y_all = BigInteger.ZERO;
		BigInteger helper = BigInteger.ONE.shiftLeft(d_ip1).subtract(
				BigInteger.ONE);
		BigInteger tmp = y_all;
		for (int o = twotaupow - 1; o >= 0; o--) {
			y[o] = tmp.and(helper);
			tmp = tmp.shiftRight(d_ip1);
		}

		BigInteger secretE_Ti = y_all;
		BigInteger secretE_P_p = null;
		if (i > 0)
			secretE_P_p = secretE_P;
		timing.access_online.stop();

		// step 3
		// party C and E
		BigInteger[] b = new BigInteger[pathTuples];
		if (i > 0) {
			timing.access_online.start();
			helper = BigInteger.ONE.shiftLeft(1 + nBits).subtract(
					BigInteger.ONE);
			tmp = secretE_P.shiftRight(lBits + aBits);
			for (int j = pathTuples - 1; j >= 0; j--) {
				b[j] = tmp.and(helper);
				tmp = tmp.shiftRight(tupleBits);
			}
			timing.access_online.stop();
			// sanityCheck();
			PET pet = new PET(charlie, debbie);
			timing.pet.start();
			pet.executeEddie(charlie, debbie, b);
			timing.pet.stop();
			// PET outputs j_1 for C
		}

		// step 4
		// party E
		AOT aot = new AOT(charlie, debbie);
		if (i > 0) {
			timing.access_online.start();
			BigInteger[] e = new BigInteger[pathTuples];
			BigInteger[] f = new BigInteger[pathTuples];
			helper = BigInteger.ONE.shiftLeft(aBits).subtract(
					BigInteger.ONE);
			tmp = secretE_P;
			for (int o = pathTuples - 1; o >= 0; o--) {
				e[o] = tmp.and(helper);
				tmp = tmp.shiftRight(tupleBits);
				f[o] = e[o].xor(y_all);
			}
			timing.access_online.stop();

			// sanityCheck();
			// AOT(E, C, D)
			timing.aot.start();
			aot.executeE(charlie, debbie, f, aBits);
			timing.aot.stop();
			// outputs fbar for C
		}

		// step 5
		if (i < h) {
			// AOT(E, C, D)
			// sanityCheck();
			timing.aot.start();
			aot.executeE(charlie, debbie, y, d_ip1);
			timing.aot.stop();
			// outputs ybar_j2 for C
		}

		charlie.bandwidth[PID.access].stop();
		debbie.bandwidth[PID.access].stop();
		charlie.countBandwidth = false;
		debbie.countBandwidth = false;

		// sanityCheck();
		// E outputs secretE_Ti and secretE_P_p
		return new AOutput(null, DecOut.p, null, secretE_Ti, null, secretE_P_p,
				null);
	}
}
