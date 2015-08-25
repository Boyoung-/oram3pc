package sprout.oram.operations;

import java.math.BigInteger;

import org.bouncycastle.util.Arrays;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Bucket;
import sprout.oram.Forest;
import sprout.oram.PID;
import sprout.oram.PreData;
import sprout.oram.TID;
import sprout.oram.Tree;
import sprout.util.Timing;
import sprout.util.Util;

public class Eviction extends TreeOperation<BigInteger, BigInteger[]> {

	public Eviction() {
		super(null, null);
	}

	public Eviction(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public BigInteger executeCharlieSubTree(Communication debbie,
			Communication eddie, Tree unused, BigInteger sC_T_p, BigInteger[] sC_P_p,
			Timing localTiming) {
		if (i == 0) {
			localTiming.stopwatch[PID.evict][TID.online_write].start();
			//debbie.write(args[1]);
			debbie.write(sC_T_p.toByteArray(), PID.evict);
			localTiming.stopwatch[PID.evict][TID.online_write].stop();

			return null;
		}

		GCF gcf = new GCF(debbie, eddie);

		// protocol
		// step 1
		for (int j = 0; j < d_i; j++) {
			localTiming.stopwatch[PID.evict][TID.online].start();
			BigInteger sC_fb = BigInteger.ZERO;
			BigInteger sC_dir = BigInteger.ZERO;
			for (int l = 0; l < w; l++) {
				sC_fb = sC_fb.shiftLeft(1);
				if (sC_P_p[j].testBit((w-l) * tupleBits - 1))
					sC_fb = sC_fb.setBit(0);
				sC_dir = sC_dir.shiftLeft(1);
				if (sC_P_p[j].testBit((w-l) * tupleBits - 1 - nBits - j - 1))
					sC_dir = sC_dir.setBit(0);
			}
			BigInteger sC_input = sC_dir.shiftLeft(w).xor(sC_fb);
			localTiming.stopwatch[PID.evict][TID.online].stop();

			gcf.executeCharlie(debbie, eddie, localTiming, i, j, w * 2 + 2,
					sC_input);
		}

		// step 2
		localTiming.stopwatch[PID.evict][TID.online].start();
		BigInteger sC_fb = BigInteger.ZERO;
		for (int j = d_i; j < pathBuckets; j++) {
			for (int l = 0; l < w; l++) {
				sC_fb = sC_fb.shiftLeft(1);
				if (sC_P_p[j].testBit((w-l) * tupleBits - 1))
					sC_fb = sC_fb.setBit(0);
			}
		}
		localTiming.stopwatch[PID.evict][TID.online].stop();

		gcf.executeCharlie(debbie, eddie, localTiming, i, d_i, w * expen + 2,
				sC_fb);

		// step 3
		localTiming.stopwatch[PID.evict][TID.online].start();
		int k = w * pathBuckets;

		// step 4
		BigInteger helper = BigInteger.ONE.shiftLeft(tupleBits).subtract(
				BigInteger.ONE);
		BigInteger[] sC_a = new BigInteger[k + 2];
		for (int j = 0; j < pathBuckets; j++) {
			BigInteger tmp = sC_P_p[j];
			for (int l=w-1; l>=0; l--) {
				sC_a[w * j + l] = tmp.and(helper);
				tmp = tmp.shiftRight(tupleBits);
			}
		}
		sC_a[k] = sC_T_p;
		sC_a[k + 1] = new BigInteger(tupleBits - 1, SR.rand);
		localTiming.stopwatch[PID.evict][TID.online].stop();

		// step 5
		SSXOT ssxot = new SSXOT(debbie, eddie);
		BigInteger[] sC_P_pp = ssxot.executeCharlie(debbie, eddie, localTiming,
				i, k + 2, k, tupleBits, sC_a);

		localTiming.stopwatch[PID.evict][TID.online].start();
		int tupleBytes = (tupleBits+7) / 8;
		byte[] msg_path = new byte[tupleBytes*pathTuples];
		for (int j=0; j<pathTuples; j++) {
			byte[] tuple = sC_P_pp[j].toByteArray();
			if (tuple.length < tupleBytes)
				System.arraycopy(tuple, 0, msg_path, (j + 1) * tupleBytes
						- tuple.length, tuple.length);
			else
				System.arraycopy(tuple, tuple.length - tupleBytes, msg_path, j
						* tupleBytes, tupleBytes);
		}
		localTiming.stopwatch[PID.evict][TID.online].stop();

		// step 6
		localTiming.stopwatch[PID.evict][TID.online_write].start();
		//debbie.write(secretC_P_pp);
		debbie.write(msg_path, PID.evict);
		localTiming.stopwatch[PID.evict][TID.online_write].stop();
		
		return null;
	}

	public BigInteger executeDebbieSubTree(Communication charlie,
			Communication eddie, Tree OT, BigInteger unused, BigInteger[] unused2,
			Timing localTiming) {
		if (i == 0) {
			byte[] msg_tuple = null;
			localTiming.stopwatch[PID.evict][TID.online_read].start();
			//BigInteger sD_Ti_p = charlie.readBigInteger();
			msg_tuple = charlie.read();
			localTiming.stopwatch[PID.evict][TID.online_read].stop();

			localTiming.stopwatch[PID.evict][TID.online].start();
			BigInteger sD_Ti_p = new BigInteger(1, msg_tuple);
			
			Bucket[] buckets = new Bucket[] { new Bucket(i,
					Util.rmSignBit(sD_Ti_p.xor(PreData.evict_upxi[i][0])
							.toByteArray())) };
			BigInteger Li = null;
			if (!Forest.loadPathCheat())
				OT.setBucketsOnPath(buckets, Li);
			localTiming.stopwatch[PID.evict][TID.online].stop();

			return null;
		}

		GCF gcf = new GCF(charlie, eddie);

		// protocol
		// step 1
		int[] alpha1_j = new int[d_i];
		int[] alpha2_j = new int[d_i];
		for (int j = 0; j < d_i; j++) {
			BigInteger GCFOutput = gcf.executeDebbie(charlie, eddie,
					localTiming, i, j, w * 2 + 2);

			localTiming.stopwatch[PID.evict][TID.online].start();
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
			localTiming.stopwatch[PID.evict][TID.online].stop();
		}

		// step 2
		BigInteger GCFOutput = gcf.executeDebbie(charlie, eddie, localTiming,
				i, d_i, w * expen + 2);

		localTiming.stopwatch[PID.evict][TID.online].start();
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
		localTiming.stopwatch[PID.evict][TID.online].stop();

		// step 5
		SSXOT ssxot = new SSXOT(charlie, eddie);
		ssxot.executeDebbie(charlie, eddie, localTiming, i, k + 2, k,
				tupleBits, I);

		// step 6
		localTiming.stopwatch[PID.evict][TID.online_read].start();
		//BigInteger secretD_P_pp = charlie.readBigInteger();
		byte[] msg_path = charlie.read();
		localTiming.stopwatch[PID.evict][TID.online_read].stop();

		localTiming.stopwatch[PID.evict][TID.online].start();
		int tupleBytes = (tupleBits+7)/8;
		Bucket[] buckets = new Bucket[pathBuckets];
		for (int j=0; j<pathBuckets; j++) {
			BigInteger content = BigInteger.ZERO;
			for (int t=0; t<w; t++) {
				BigInteger tuple = new BigInteger(1, Arrays.copyOfRange(msg_path, (j*w+t)*tupleBytes, (j*w+t+1)*tupleBytes));
				content = content.shiftLeft(tupleBits).xor(tuple);
			}
			content = content.xor(PreData.evict_upxi[i][j]);
			buckets[j] = new Bucket(i, Util.rmSignBit(content.toByteArray()));
		}
		
		if (!Forest.loadPathCheat())
			OT.setBucketsOnPath(buckets, PreData.access_Li[i]);
		localTiming.stopwatch[PID.evict][TID.online].stop();
		
		return null;
	}

	public BigInteger executeEddieSubTree(Communication charlie,
			Communication debbie, Tree OT, BigInteger sE_T_p, BigInteger[] sE_P_p, Timing localTiming) {
		if (i == 0) {
			localTiming.stopwatch[PID.evict][TID.online].start();
			Bucket[] buckets = new Bucket[] { new Bucket(i,	Util.rmSignBit(sE_T_p.xor(PreData.evict_upxi[i][0]).toByteArray())) };
			BigInteger Li = null;
			if (!Forest.loadPathCheat())
				OT.setBucketsOnPath(buckets, Li);
			localTiming.stopwatch[PID.evict][TID.online].stop();

			return null;
		}

		GCF gcf = new GCF(charlie, debbie);

		// protocol
		// step 1
		for (int j = 0; j < d_i; j++) {
			localTiming.stopwatch[PID.evict][TID.online].start();
			BigInteger sE_fb = BigInteger.ZERO;
			BigInteger sE_dir = BigInteger.ZERO;
			for (int l = 0; l < w; l++) {
				sE_fb = sE_fb.shiftLeft(1);
				if (sE_P_p[j].testBit((w-l) * tupleBits - 1))
					sE_fb = sE_fb.setBit(0);
				sE_dir = sE_dir.shiftLeft(1);
				int bit = (sE_P_p[j].testBit((w-l) * tupleBits - 1 - nBits - j - 1) ? 1 : 0)
						^ 1	^ (PreData.access_Li[i].testBit(lBits - j - 1) ? 1 : 0);
				if (bit == 1)
					sE_dir = sE_dir.setBit(0);
			}
			BigInteger sE_input = sE_dir.shiftLeft(w).xor(sE_fb);
			localTiming.stopwatch[PID.evict][TID.online].stop();

			gcf.executeEddie(charlie, debbie, localTiming, i, j, w * 2 + 2,
					sE_input);
		}

		// step 2
		localTiming.stopwatch[PID.evict][TID.online].start();
		BigInteger sE_fb = BigInteger.ZERO;
		for (int j = d_i; j < pathBuckets; j++) {
			for (int l = 0; l < w; l++) {
				sE_fb = sE_fb.shiftLeft(1);
				if (sE_P_p[j].testBit((w-l) * tupleBits - 1))
					sE_fb = sE_fb.setBit(0);
			}
		}
		localTiming.stopwatch[PID.evict][TID.online].stop();

		gcf.executeEddie(charlie, debbie, localTiming, i, d_i, w * expen + 2,
				sE_fb);

		// step 3
		localTiming.stopwatch[PID.evict][TID.online].start();
		int k = w * pathBuckets;

		// step 4
		BigInteger tmp;
		BigInteger helper = BigInteger.ONE.shiftLeft(tupleBits).subtract(
				BigInteger.ONE);
		BigInteger[] sE_a = new BigInteger[k + 2];
		for (int j = 0; j < pathBuckets; j++) {
			tmp = sE_P_p[j];
			for (int l=w-1; l>=0; l--) {
				sE_a[w * j + l] = tmp.and(helper);
				tmp = tmp.shiftRight(tupleBits);
			}
		}
		sE_a[k] = sE_T_p;
		sE_a[k + 1] = new BigInteger(tupleBits - 1, SR.rand);
		localTiming.stopwatch[PID.evict][TID.online].stop();

		// step 5
		SSXOT ssxot = new SSXOT(charlie, debbie);
		BigInteger[] sE_P_pp = ssxot.executeEddie(charlie, debbie, localTiming,
				i, k + 2, k, tupleBits, sE_a);

		localTiming.stopwatch[PID.evict][TID.online].start();
		Bucket[] buckets = new Bucket[pathBuckets];
		for (int j=0; j<pathBuckets; j++) {
			BigInteger content = BigInteger.ZERO;
			for (int t=0; t<w; t++) {
				content = content.shiftLeft(tupleBits).xor(sE_P_pp[j*w+t]);
			}
			content  = content.xor(PreData.evict_upxi[i][j]);
			buckets[j] = new Bucket(i, Util.rmSignBit(content.toByteArray()));
		}
		
		if (!Forest.loadPathCheat())
			OT.setBucketsOnPath(buckets, PreData.access_Li[i]);
		localTiming.stopwatch[PID.evict][TID.online].stop();
		
		return null;
	}

	@Override
	public BigInteger executeCharlieSubTree(Communication debbie,
			Communication eddie, Tree OT, BigInteger[] args, Timing localTiming) {
		return null;
	}

	@Override
	public BigInteger executeDebbieSubTree(Communication charlie,
			Communication eddie, Tree OT, BigInteger[] args, Timing localTiming) {
		return null;
	}

	@Override
	public BigInteger executeEddieSubTree(Communication charlie,
			Communication debbie, Tree OT, BigInteger[] args, Timing localTiming) {
		return null;
	}
}
