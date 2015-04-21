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
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.oram.Tree;
import sprout.util.Timing;
import sprout.util.Util;

// TODO: make a util function to return a random permutation
// TODO: make permutation array of int instead of list of int

public class Reshuffle extends
		TreeOperation<BigInteger, BigInteger> {

	public Reshuffle() {
		super(null, null);
	}

	public Reshuffle(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public BigInteger executeCharlieSubTree(Communication debbie,
			Communication eddie, Tree unused, BigInteger sC_P, Timing localTiming) {
		// i = 0 case: no reshuffle needed
		if (i == 0) {
			return sC_P;
		}

		// protocol
		// step 1
		BigInteger z = sC_P.xor(PreData.reshuffle_p[i]);
		
		eddie.write(z);

		BigInteger sC_pi_P = BigInteger.ZERO;
		for (int j = 0; j < pathBuckets; j++)
			sC_pi_P = sC_pi_P.shiftLeft(bucketBits).xor(
					PreData.reshuffle_a_p[i][j]);

		return sC_pi_P;
	}

	@Override
	public BigInteger executeDebbieSubTree(Communication charlie,
			Communication eddie, Tree unused, BigInteger unused2, Timing localTiming) {
		// protocol
		// debbie does nothing online
		return null;
	}

	@Override
	public BigInteger executeEddieSubTree(Communication charlie,
			Communication debbie, Tree unused, BigInteger sE_P, Timing localTiming) {
		// i = 0 case: no shuffle needed
		if (i == 0) {
			return sE_P;
		}

		// protocol
		// step 1
		BigInteger z = charlie.readBigInteger();

		// step 2
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
		BigInteger sE_pi_P = BigInteger.ZERO;
		for (int j = 0; j < pathBuckets; j++)
			sE_pi_P = sE_pi_P.shiftLeft(bucketBits).xor(b[j]);

		return sE_pi_P;
	}
	
	// for testing correctness
	@SuppressWarnings("unchecked")
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing Reshuffle  #####");

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
			PreData.reshuffle_p[i] = new BigInteger(pathBuckets * bucketBits, SR.rand);
			PreData.reshuffle_r[i] = new BigInteger(pathBuckets * bucketBits, SR.rand);
			BigInteger a_all = PreData.reshuffle_p[i].xor(PreData.reshuffle_r[i]);
			PreData.reshuffle_a_p[i] = new BigInteger[pathBuckets];
			BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(
					BigInteger.ONE);
			BigInteger tmp = a_all;
			for (int j = pathBuckets - 1; j >= 0; j--) {
				PreData.reshuffle_a_p[i][j] = tmp.and(helper);
				tmp = tmp.shiftRight(bucketBits);
			}
			PreData.reshuffle_a_p[i] = Util.permute(PreData.reshuffle_a_p[i], PreData.reshuffle_pi[i]);
			
			BigInteger sC_P = new BigInteger(pathBuckets * bucketBits, SR.rand);
			BigInteger sE_P = new BigInteger(pathBuckets * bucketBits, SR.rand);

			con1.write(i);
			con1.write(PreData.reshuffle_p[i]);
			con1.write(PreData.reshuffle_a_p[i]);
			con1.write(sC_P);
			
			con2.write(i);
			
			BigInteger sE_pi_P = executeEddieSubTree(con1, con2, null, sE_P, null);
			
			BigInteger sC_pi_P = con1.readBigInteger();
			
			tmp = sC_P.xor(sE_P);
			BigInteger[] path = new BigInteger[pathBuckets];
			for (int j = pathBuckets - 1; j >= 0; j--) {
				path[j] = tmp.and(helper);
				tmp = tmp.shiftRight(bucketBits);
			}
			path = Util.permute(path, PreData.reshuffle_pi[i]);
			BigInteger pi_path = BigInteger.ZERO;
			for (int j = 0; j < pathBuckets; j++)
				pi_path = pi_path.shiftLeft(bucketBits).xor(path[j]);
			
			System.out.println("levels = " + levels);
			System.out.println("i = " + i);
			if (pi_path.compareTo(sE_pi_P.xor(sC_pi_P)) == 0) {
				System.out.println("Reshuffle test passed");
			} else {
				System.out.println("Reshuffle test failed");
			}
		} else if (party == Party.Debbie) {
			int i = con2.readInt();
			
			loadTreeSpecificParameters(i);
			executeDebbieSubTree(con1, con2, null, null, null);
		} else if (party == Party.Charlie) {
			int levels = ForestMetadata.getLevels();
			PreData.reshuffle_p = new BigInteger[levels];
			PreData.reshuffle_a_p = new BigInteger[levels][];
			
			int i = con2.readInt();	
			PreData.reshuffle_p[i] = con2.readBigInteger();
			PreData.reshuffle_a_p[i] = con2.readBigIntegerArray();
			BigInteger sC_P = con2.readBigInteger();

			loadTreeSpecificParameters(i);
			BigInteger sC_pi_P = executeCharlieSubTree(con1, con2, null, sC_P, null);
			
			con2.write(sC_pi_P);
		}

		System.out.println("#####  Testing Reshuffle Finished  #####");
	}
}
