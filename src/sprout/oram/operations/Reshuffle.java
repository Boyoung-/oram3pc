package sprout.oram.operations;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.oram.TID;
import sprout.oram.Tree;
import sprout.util.Timing;
import sprout.util.Util;

// TODO: make a util function to return a random permutation
// TODO: make permutation array of int instead of list of int

public class Reshuffle extends TreeOperation<BigInteger[], BigInteger> {

	public Reshuffle() {
		super(null, null);
	}

	public Reshuffle(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public BigInteger[] executeCharlieSubTree(Communication debbie,
			Communication eddie, Tree unused, BigInteger sC_P,
			Timing localTiming) {		
		// i = 0 case: no reshuffle needed
		if (i == 0) {
			return new BigInteger[]{sC_P};
		}

		// protocol
		// step 1
		localTiming.stopwatch[PID.reshuf][TID.online].start();
		BigInteger z = sC_P.xor(PreData.reshuffle_p[i]);
		byte[] msg_z = z.toByteArray();
		localTiming.stopwatch[PID.reshuf][TID.online].stop();

		localTiming.stopwatch[PID.reshuf][TID.online_write].start();
		//eddie.write(z);
		eddie.write(msg_z, PID.reshuf);
		localTiming.stopwatch[PID.reshuf][TID.online_write].stop();
		
		return PreData.reshuffle_a_p[i];
	}

	@Override
	public BigInteger[] executeDebbieSubTree(Communication charlie,
			Communication eddie, Tree unused, BigInteger unused2,
			Timing localTiming) {
		// protocol
		// debbie does nothing online
		return null;
	}

	@Override
	public BigInteger[] executeEddieSubTree(Communication charlie,
			Communication debbie, Tree unused, BigInteger sE_P,
			Timing localTiming) {
		// i = 0 case: no shuffle needed
		if (i == 0) {
			return new BigInteger[]{sE_P};
		}

		// protocol
		// step 1
		localTiming.stopwatch[PID.reshuf][TID.online_read].start();
		//BigInteger z = charlie.readBigInteger();
		byte[] msg_z = charlie.read();
		localTiming.stopwatch[PID.reshuf][TID.online_read].stop();

		// step 2
		localTiming.stopwatch[PID.reshuf][TID.online].start();
		BigInteger z = new BigInteger(1, msg_z);
		
		BigInteger b_all = sE_P.xor(z).xor(PreData.reshuffle_r[i]);
		BigInteger[] b = new BigInteger[pathBuckets];
		BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(
				BigInteger.ONE);
		BigInteger tmp = b_all;
		for (int j = pathBuckets - 1; j >= 0; j--) {
			b[j] = tmp.and(helper);
			tmp = tmp.shiftRight(bucketBits);
		}
		b = Util.permute(b, PreData.reshuffle_pi[i]);
		localTiming.stopwatch[PID.reshuf][TID.online].stop();

		return b;
	}

	
	// for testing correctness
	@SuppressWarnings("unchecked")
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing Reshuffle  #####");

		timing = new Timing();

		if (party == Party.Eddie) {
			int levels = ForestMetadata.getLevels();
			int i = SR.rand.nextInt(levels - 1) + 1;
			PreData.reshuffle_p = new BigInteger[levels];
			PreData.reshuffle_r = new BigInteger[levels];
			PreData.reshuffle_a_p = new BigInteger[levels][];
			PreData.reshuffle_pi = (List<Integer>[]) new List[levels];

			loadTreeSpecificParameters(i);
			PreData.reshuffle_pi[i] = new ArrayList<Integer>();
			for (int j = 0; j < pathBuckets; j++)
				PreData.reshuffle_pi[i].add(j);
			Collections.shuffle(PreData.reshuffle_pi[i], SR.rand);
			PreData.reshuffle_p[i] = new BigInteger(pathBuckets * bucketBits,
					SR.rand);
			PreData.reshuffle_r[i] = new BigInteger(pathBuckets * bucketBits,
					SR.rand);
			BigInteger a_all = PreData.reshuffle_p[i]
					.xor(PreData.reshuffle_r[i]);
			PreData.reshuffle_a_p[i] = new BigInteger[pathBuckets];
			BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(
					BigInteger.ONE);
			BigInteger tmp = a_all;
			for (int j = pathBuckets - 1; j >= 0; j--) {
				PreData.reshuffle_a_p[i][j] = tmp.and(helper);
				tmp = tmp.shiftRight(bucketBits);
			}
			PreData.reshuffle_a_p[i] = Util.permute(PreData.reshuffle_a_p[i],
					PreData.reshuffle_pi[i]);

			BigInteger sC_P = new BigInteger(pathBuckets * bucketBits, SR.rand);
			BigInteger sE_P = new BigInteger(pathBuckets * bucketBits, SR.rand);

			con1.write(i);
			con1.write(PreData.reshuffle_p[i]);
			con1.write(PreData.reshuffle_a_p[i]);
			con1.write(sC_P);

			con2.write(i);

			BigInteger[] sE_pi_P = executeEddieSubTree(con1, con2, null, sE_P,
					timing);

			BigInteger[] sC_pi_P = con1.readBigIntegerArray();

			tmp = sC_P.xor(sE_P);
			BigInteger[] path = new BigInteger[pathBuckets];
			for (int j = pathBuckets - 1; j >= 0; j--) {
				path[j] = tmp.and(helper);
				tmp = tmp.shiftRight(bucketBits);
			}
			path = Util.permute(path, PreData.reshuffle_pi[i]);
			
			int t;
			for (t=0; t<pathBuckets; t++) {
				if (sE_pi_P[t].xor(sC_pi_P[t]).compareTo(path[t]) != 0)
					break;
			}

			System.out.println("levels = " + levels);
			System.out.println("i = " + i);
			if (t == pathBuckets) {
				System.out.println("Reshuffle test passed");
			} else {
				System.out.println("Reshuffle test failed");
			}
		} else if (party == Party.Debbie) {
			int i = con2.readInt();

			loadTreeSpecificParameters(i);
			executeDebbieSubTree(con1, con2, null, null, timing);
		} else if (party == Party.Charlie) {
			int levels = ForestMetadata.getLevels();
			PreData.reshuffle_p = new BigInteger[levels];
			PreData.reshuffle_a_p = new BigInteger[levels][];

			int i = con2.readInt();
			PreData.reshuffle_p[i] = con2.readBigInteger();
			PreData.reshuffle_a_p[i] = con2.readBigIntegerArray();
			BigInteger sC_P = con2.readBigInteger();

			loadTreeSpecificParameters(i);
			BigInteger[] sC_pi_P = executeCharlieSubTree(con1, con2, null, sC_P,
					timing);

			con2.write(sC_pi_P);
		}

		System.out.println("#####  Testing Reshuffle Finished  #####");
	}
}
