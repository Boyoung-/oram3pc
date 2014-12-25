package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.util.Util;

public class Eviction extends TreeOperation<BigInteger, BigInteger[]> {

	public Eviction() {
		super(null, null);
	}

	public Eviction(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public BigInteger executeCharlieSubTree(Communication debbie,
			Communication eddie, BigInteger unused1, Tree unused2,
			BigInteger[] extraArgs) {
		if (i == 0)
			return null;

		GCF gcf = new GCF(debbie, eddie);

		debbie.countBandwidth = true;
		eddie.countBandwidth = true;
		debbie.bandwidth[PID.eviction].start();
		eddie.bandwidth[PID.eviction].start();

		sanityCheck();

		// protocol
		// step 1
		BigInteger sC_P_p = extraArgs[0];
		BigInteger sC_T_p = extraArgs[1];

		for (int j = 0; j < d_i; j++) {
			timing.eviction_online.start();
			BigInteger sC_fb = BigInteger.ZERO;
			BigInteger sC_dir = BigInteger.ZERO;
			BigInteger sC_bucket = Util.getSubBits(sC_P_p,
					(pathBuckets - j - 1) * bucketBits, (pathBuckets - j)
							* bucketBits);
			for (int l = 0; l < w; l++) {
				BigInteger sC_tuple = Util.getSubBits(sC_bucket, (w - l - 1)
						* tupleBits, (w - l) * tupleBits);
				sC_fb = sC_fb.shiftLeft(1);
				if (sC_tuple.testBit(tupleBits - 1))
					sC_fb = sC_fb.setBit(0);
				int bit = (sC_tuple.testBit(lBits - j - 1 + aBits) ? 1 : 0);
				sC_dir = sC_dir.shiftLeft(1);
				if (bit == 1)
					sC_dir = sC_dir.setBit(0);
			}
			BigInteger sC_input = sC_dir.shiftLeft(w).xor(sC_fb);
			timing.eviction_online.stop();

			timing.gcf.start();
			gcf.executeC(debbie, eddie, w * 2 + 2, sC_input);
			timing.gcf.stop();
		}

		// step 2
		timing.eviction_online.start();
		BigInteger sC_fb = BigInteger.ZERO;
		for (int j = d_i; j < pathBuckets; j++) {
			BigInteger sC_bucket = Util.getSubBits(sC_P_p,
					(pathBuckets - j - 1) * bucketBits, (pathBuckets - j)
							* bucketBits);
			for (int l = 0; l < w; l++) {
				sC_fb = sC_fb.shiftLeft(1);
				if (sC_bucket.testBit((w - l) * tupleBits - 1))
					sC_fb = sC_fb.setBit(0);
			}
		}
		timing.eviction_online.stop();

		timing.gcf.start();
		gcf.executeC(debbie, eddie, w * expen + 2, sC_fb);
		timing.gcf.stop();

		// step 3
		int k = w * pathBuckets;

		// step 4
		timing.eviction_online.start();
		BigInteger[] sC_a = new BigInteger[k + 2];
		for (int j = 0; j < pathBuckets; j++)
			for (int l = 0; l < w; l++) {
				sC_a[w * j + l] = Util.getSubBits(sC_P_p, (pathBuckets - j - 1)
						* bucketBits + (w - l - 1) * tupleBits, (pathBuckets
						- j - 1)
						* bucketBits + (w - l) * tupleBits);
			}
		sC_a[k] = sC_T_p;
		sC_a[k + 1] = new BigInteger(tupleBits - 1, SR.rand);
		timing.eviction_online.stop();

		// step 5
		SSOT ssot = new SSOT(debbie, eddie);
		timing.ssot.start();
		BigInteger[] sC_P_pp = ssot.executeC(debbie, eddie, sC_a, tupleBits);
		timing.ssot.stop();

		timing.eviction_online.start();
		BigInteger secretC_P_pp = BigInteger.ZERO;
		for (int j = 0; j < sC_P_pp.length; j++)
			secretC_P_pp = secretC_P_pp.shiftLeft(tupleBits).xor(sC_P_pp[j]);
		timing.eviction_online.stop();

		debbie.countBandwidth = false;
		eddie.countBandwidth = false;
		debbie.bandwidth[PID.eviction].stop();
		eddie.bandwidth[PID.eviction].stop();

		return secretC_P_pp;
	}

	@Override
	public BigInteger executeDebbieSubTree(Communication charlie,
			Communication eddie, BigInteger unused1, Tree unused2,
			BigInteger[] unused3) {
		if (i == 0)
			return null;

		GCF gcf = new GCF(charlie, eddie);

		charlie.countBandwidth = true;
		eddie.countBandwidth = true;
		charlie.bandwidth[PID.eviction].start();
		eddie.bandwidth[PID.eviction].start();

		sanityCheck();

		// protocol
		// step 1
		int[] alpha1_j = new int[d_i];
		int[] alpha2_j = new int[d_i];
		for (int j = 0; j < d_i; j++) {
			timing.gcf.start();
			BigInteger GCFOutput = gcf.executeD(charlie, eddie, "F2FT",
					w * 2 + 2);
			timing.gcf.stop();

			timing.eviction_online.start();
			for (alpha1_j[j] = 0; alpha1_j[j] < w; alpha1_j[j]++)
				if (GCFOutput.testBit(w - alpha1_j[j] - 1))
					break;
			if (alpha1_j[j] == w)
				alpha1_j[j] = SR.rand.nextInt(w);

			for (alpha2_j[j] = alpha1_j[j] + 1; alpha2_j[j] < w; alpha2_j[j]++)
				if (GCFOutput.testBit(w - alpha2_j[j] - 1))
					break;
			while (alpha2_j[j] == w || alpha2_j[j] == alpha1_j[j])
				alpha2_j[j] = SR.rand.nextInt(w);
			timing.eviction_online.stop();
		}

		// step 2
		timing.gcf.start();
		BigInteger GCFOutput = gcf.executeD(charlie, eddie, "F2ET", w * expen
				+ 2);
		timing.gcf.stop();

		timing.eviction_online.start();
		int alpha1_d;
		for (alpha1_d = 0; alpha1_d < w * expen; alpha1_d++)
			if (GCFOutput.testBit(w * expen - alpha1_d - 1))
				break;
		int alpha2_d;
		for (alpha2_d = alpha1_d + 1; alpha2_d < w * expen; alpha2_d++)
			if (GCFOutput.testBit(w * expen - alpha2_d - 1))
				break;
		timing.eviction_online.stop();
		if (alpha2_d == w * expen) {
			try {
				throw new Exception("Overflow!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// step 3
		int k = w * pathBuckets;
		Integer[] beta = new Integer[k];
		timing.eviction_online.start();
		for (int j = 0; j < pathBuckets; j++)
			for (int l = 0; l < w; l++) {
				if (j == 0 && l == alpha1_j[0])
					beta[w * j + l] = k;
				else if (j == 0 && l == alpha2_j[0])
					beta[w * j + l] = k + 1;
				else if (1 <= j && j <= (d_i - 1) && l == alpha1_j[j])
					beta[w * j + l] = w * (j - 1) + alpha1_j[j - 1];
				else if (1 <= j && j <= (d_i - 1) && l == alpha2_j[j])
					beta[w * j + l] = w * (j - 1) + alpha2_j[j - 1];
				else if (j >= d_i && (w * (j - d_i) + l) == alpha1_d)
					beta[w * j + l] = w * (d_i - 1) + alpha1_j[d_i - 1];
				else if (j >= d_i && (w * (j - d_i) + l) == alpha2_d)
					beta[w * j + l] = w * (d_i - 1) + alpha2_j[d_i - 1];
				else
					beta[w * j + l] = w * j + l;
			}
		timing.eviction_online.stop();
		Integer[] I = beta;

		// step 5
		try {
			SSOT ssot = new SSOT(charlie, eddie);
			timing.ssot.start();
			ssot.executeI(charlie, eddie, I);
			timing.ssot.stop();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		charlie.countBandwidth = false;
		eddie.countBandwidth = false;
		charlie.bandwidth[PID.eviction].stop();
		eddie.bandwidth[PID.eviction].stop();

		return null;
	}

	@Override
	public BigInteger executeEddieSubTree(Communication charlie,
			Communication debbie, Tree unused, BigInteger[] extraArgs) {
		if (i == 0)
			return null;

		GCF gcf = new GCF(charlie, debbie);

		charlie.countBandwidth = true;
		debbie.countBandwidth = true;
		charlie.bandwidth[PID.eviction].start();
		debbie.bandwidth[PID.eviction].start();

		sanityCheck();

		// protocol
		// step 1
		BigInteger sE_P_p = extraArgs[0];
		BigInteger sE_T_p = extraArgs[1];
		BigInteger Li = extraArgs[2];

		for (int j = 0; j < d_i; j++) {
			timing.eviction_online.start();
			BigInteger sE_fb = BigInteger.ZERO;
			BigInteger sE_dir = BigInteger.ZERO;
			BigInteger sE_bucket = Util.getSubBits(sE_P_p,
					(pathBuckets - j - 1) * bucketBits, (pathBuckets - j)
							* bucketBits);
			for (int l = 0; l < w; l++) {
				BigInteger sE_tuple = Util.getSubBits(sE_bucket, (w - l - 1)
						* tupleBits, (w - l) * tupleBits);
				sE_fb = sE_fb.shiftLeft(1);
				if (sE_tuple.testBit(tupleBits - 1))
					sE_fb = sE_fb.setBit(0);
				int bit = (sE_tuple.testBit(lBits - j - 1 + aBits) ? 1 : 0) ^ 1
						^ (Li.testBit(lBits - j - 1) ? 1 : 0);
				sE_dir = sE_dir.shiftLeft(1);
				if (bit == 1)
					sE_dir = sE_dir.setBit(0);
			}
			BigInteger sE_input = sE_dir.shiftLeft(w).xor(sE_fb);
			timing.eviction_online.stop();

			timing.gcf.start();
			gcf.executeE(charlie, debbie, "F2FT", w * 2 + 2, sE_input);
			timing.gcf.stop();
		}

		// step 2
		timing.eviction_online.start();
		BigInteger sE_fb = BigInteger.ZERO;
		for (int j = d_i; j < pathBuckets; j++) {
			BigInteger sE_bucket = Util.getSubBits(sE_P_p,
					(pathBuckets - j - 1) * bucketBits, (pathBuckets - j)
							* bucketBits);
			for (int l = 0; l < w; l++) {
				sE_fb = sE_fb.shiftLeft(1);
				if (sE_bucket.testBit((w - l) * tupleBits - 1))
					sE_fb = sE_fb.setBit(0);
			}
		}
		timing.eviction_online.stop();

		timing.gcf.start();
		gcf.executeE(charlie, debbie, "F2ET", w * expen + 2, sE_fb);
		timing.gcf.stop();

		// step 3
		int k = w * pathBuckets;

		// step 4
		BigInteger[] sE_a = new BigInteger[k + 2];
		timing.eviction_online.start();
		for (int j = 0; j < pathBuckets; j++)
			for (int l = 0; l < w; l++) {
				sE_a[w * j + l] = Util.getSubBits(sE_P_p, (pathBuckets - j - 1)
						* bucketBits + (w - l - 1) * tupleBits, (pathBuckets
						- j - 1)
						* bucketBits + (w - l) * tupleBits);
			}
		sE_a[k] = sE_T_p;
		sE_a[k + 1] = new BigInteger(tupleBits - 1, SR.rand);
		timing.eviction_online.stop();

		// step 5
		SSOT ssot = new SSOT(charlie, debbie);
		timing.ssot.start();
		BigInteger[] sE_P_pp = ssot.executeE(charlie, debbie, sE_a, tupleBits);
		timing.ssot.stop();

		timing.eviction_online.start();
		BigInteger secretE_P_pp = BigInteger.ZERO;
		for (int j = 0; j < sE_P_pp.length; j++)
			secretE_P_pp = secretE_P_pp.shiftLeft(tupleBits).xor(sE_P_pp[j]);
		timing.eviction_online.stop();

		charlie.countBandwidth = false;
		debbie.countBandwidth = false;
		charlie.bandwidth[PID.eviction].stop();
		debbie.bandwidth[PID.eviction].stop();

		return secretE_P_pp;
	}

	/*
	 * @Override public String[] prepareArgs(Party party) { // Randomly generate
	 * a secret String s_P_p = Util.addZero(new
	 * BigInteger(pathBuckets*bucketBits, SR.rand).toString(2),
	 * pathBuckets*bucketBits); String s_T_p = Util.addZero(new
	 * BigInteger(tupleBits, SR.rand).toString(2), tupleBits); String Li =
	 * Util.addZero(new BigInteger(lBits, SR.rand).toString(2), lBits);
	 * 
	 * return new String[]{s_P_p, s_T_p, Li}; }
	 */
}
