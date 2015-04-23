package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Bucket;
import sprout.oram.BucketException;
import sprout.oram.PreData;
import sprout.oram.Tree;
import sprout.oram.TreeException;
import sprout.util.Timing;
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
			Communication eddie, Tree unused, BigInteger[] args, Timing localTiming) {
		if (i == 0)
			return null;

		GCF gcf = new GCF(debbie, eddie);
		
		// protocol
		// step 1
		BigInteger sC_P_p = args[0];
		BigInteger sC_T_p = args[1];

		for (int j = 0; j < d_i; j++) {
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

			gcf.executeCharlie(debbie, eddie, i, j, w * 2 + 2, sC_input);
		}

		// step 2
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

		gcf.executeCharlie(debbie, eddie, i, d_i, w * expen + 2, sC_fb);

		// step 3
		int k = w * pathBuckets;

		// step 4
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

		// step 5
		SSXOT ssxot = new SSXOT(debbie, eddie);
		BigInteger[] sC_P_pp = ssxot.executeCharlie(debbie, eddie, i, k+2, k, tupleBits, sC_a);

		BigInteger secretC_P_pp = BigInteger.ZERO;
		for (int j = 0; j < sC_P_pp.length; j++)
			secretC_P_pp = secretC_P_pp.shiftLeft(tupleBits).xor(sC_P_pp[j]);
		
		
		// new
		debbie.write(secretC_P_pp);
		debbie.write(PreData.access_Li[i]);
		
		return secretC_P_pp;
	}

	@Override
	public BigInteger executeDebbieSubTree(Communication charlie,
			Communication eddie, Tree OT, BigInteger[] unused2, Timing localTiming) {
		if (i == 0)
			return null;

		GCF gcf = new GCF(charlie, eddie);
		
		// protocol
		// step 1
		int[] alpha1_j = new int[d_i];
		int[] alpha2_j = new int[d_i];
		for (int j = 0; j < d_i; j++) {
			BigInteger GCFOutput = gcf
					.executeDebbie(charlie, eddie, i, j, w * 2 + 2);

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
		}

		// step 2
		BigInteger GCFOutput = gcf.executeDebbie(charlie, eddie, i, d_i, w * expen + 2);

		int alpha1_d;
		for (alpha1_d = 0; alpha1_d < w * expen; alpha1_d++)
			if (GCFOutput.testBit(w * expen - alpha1_d - 1))
				break;
		int alpha2_d;
		for (alpha2_d = alpha1_d + 1; alpha2_d < w * expen; alpha2_d++)
			if (GCFOutput.testBit(w * expen - alpha2_d - 1))
				break;
		
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
		Integer[] I = beta;

		// step 5
		SSXOT ssxot = new SSXOT(charlie, eddie);
		ssxot.executeDebbie(charlie, eddie, i, k + 2, k, tupleBits, I);
		
		
		// new
				BigInteger tmp = charlie.readBigInteger();
				BigInteger Li = charlie.readBigInteger();
				BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(BigInteger.ONE);
				Bucket[] buckets = new Bucket[pathBuckets];
				for (int j=pathBuckets-1; j>=0; j--) {
					BigInteger content = tmp.and(helper);
					tmp = tmp.shiftRight(bucketBits);
					try {
						buckets[j] = new Bucket(i, Util.rmSignBit(content.toByteArray()));
					} catch (BucketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					OT.setBucketsOnPath(buckets, Li);
				} catch (TreeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		return null;
	}

	@Override
	public BigInteger executeEddieSubTree(Communication charlie,
			Communication debbie, Tree OT, BigInteger[] args, Timing localTiming) {
		if (i == 0)
			return null;

		GCF gcf = new GCF(charlie, debbie);

		// protocol
		// step 1
		BigInteger sE_P_p = args[0];
		BigInteger sE_T_p = args[1];

		for (int j = 0; j < d_i; j++) {
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
						^ (PreData.access_Li[i].testBit(lBits - j - 1) ? 1 : 0);
				sE_dir = sE_dir.shiftLeft(1);
				if (bit == 1)
					sE_dir = sE_dir.setBit(0);
			}
			BigInteger sE_input = sE_dir.shiftLeft(w).xor(sE_fb);

			gcf.executeEddie(charlie, debbie, i, j, w * 2 + 2, sE_input);
		}

		// step 2
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

		gcf.executeEddie(charlie, debbie, i, d_i, w * expen + 2, sE_fb);

		// step 3
		int k = w * pathBuckets;

		// step 4
		BigInteger[] sE_a = new BigInteger[k + 2];
		for (int j = 0; j < pathBuckets; j++)
			for (int l = 0; l < w; l++) {
				sE_a[w * j + l] = Util.getSubBits(sE_P_p, (pathBuckets - j - 1)
						* bucketBits + (w - l - 1) * tupleBits, (pathBuckets
						- j - 1)
						* bucketBits + (w - l) * tupleBits);
			}
		sE_a[k] = sE_T_p;
		sE_a[k + 1] = new BigInteger(tupleBits - 1, SR.rand);

		// step 5
		SSXOT ssxot = new SSXOT(charlie, debbie);
		BigInteger[] sE_P_pp = ssxot.executeEddie(charlie, debbie, i, k+2, k, tupleBits, sE_a);

		BigInteger secretE_P_pp = BigInteger.ZERO;
		for (int j = 0; j < sE_P_pp.length; j++)
			secretE_P_pp = secretE_P_pp.shiftLeft(tupleBits).xor(sE_P_pp[j]);
		

		
		// new
		BigInteger tmp = secretE_P_pp;
		BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(BigInteger.ONE);
		Bucket[] buckets = new Bucket[pathBuckets];
		for (int j=pathBuckets-1; j>=0; j--) {
			BigInteger content = tmp.and(helper);
			tmp = tmp.shiftRight(bucketBits);
			try {
				buckets[j] = new Bucket(i, Util.rmSignBit(content.toByteArray()));
			} catch (BucketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			OT.setBucketsOnPath(buckets, PreData.access_Li[i]);
		} catch (TreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

		return secretE_P_pp;
	}
}
