package sprout.oram.operations;

import java.math.BigInteger;
import java.util.List;

import sprout.communication.Communication;
import sprout.oram.PID;
import sprout.oram.PreData;
import sprout.oram.TID;
import sprout.util.Timing;
import sprout.util.Util;

import org.apache.commons.lang3.tuple.Pair;

public class Reshuffle extends
		TreeOperation<BigInteger, Pair<BigInteger, List<Integer>>> {

	public Reshuffle() {
		super(null, null);
	}

	public Reshuffle(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public BigInteger executeCharlieSubTree(Communication debbie,
			Communication eddie, Timing localTiming, Pair<BigInteger, List<Integer>> args) {
		BigInteger secretC_P = args.getLeft();

		// i = 0 case: no shuffle needed
		if (i == 0) {
			return secretC_P;
		}

		debbie.countBandwidth = true;
		eddie.countBandwidth = true;
		debbie.bandwidth[PID.reshuffle].start();
		eddie.bandwidth[PID.reshuffle].start();

		//sanityCheck();

		// protocol
		// step 1
		// party C
		localTiming.stopwatch[PID.reshuffle][TID.online].start();
		BigInteger z = secretC_P
				.xor(new BigInteger(1, PreData.reshuffle_p1[i]));
		localTiming.stopwatch[PID.reshuffle][TID.online].stop();
		// C sends z to E
		localTiming.stopwatch[PID.reshuffle][TID.online_write].start();
		eddie.write(z);
		localTiming.stopwatch[PID.reshuffle][TID.online_write].stop();

		// step 2 & 3
		// D sends secretC_pi_P to C
		// C outputs secretC_pi_P
		localTiming.stopwatch[PID.reshuffle][TID.online_read].start();
		BigInteger[] secretC_pi_P_arr = debbie.readBigIntegerArray();
		localTiming.stopwatch[PID.reshuffle][TID.online_read].stop();

		localTiming.stopwatch[PID.reshuffle][TID.online].start();
		BigInteger secretC_pi_P = BigInteger.ZERO;
		for (int j = 0; j < pathBuckets; j++)
			secretC_pi_P = secretC_pi_P.shiftLeft(bucketBits).xor(
					secretC_pi_P_arr[j]);
		localTiming.stopwatch[PID.reshuffle][TID.online].stop();

		debbie.countBandwidth = false;
		eddie.countBandwidth = false;
		debbie.bandwidth[PID.reshuffle].stop();
		eddie.bandwidth[PID.reshuffle].stop();

		return secretC_pi_P;
	}

	@Override
	public BigInteger executeDebbieSubTree(Communication charlie,
			Communication eddie, Timing localTiming, Pair<BigInteger, List<Integer>> args_unused) {
		// i = 0 case: no shuffle needed
		if (i == 0) {
			return null;
		}

		charlie.countBandwidth = true;
		eddie.countBandwidth = true;
		charlie.bandwidth[PID.reshuffle].start();
		eddie.bandwidth[PID.reshuffle].start();

		//sanityCheck();

		// protocol
		// step 1
		// C sends D s1

		// step 2
		// party D
		localTiming.stopwatch[PID.reshuffle][TID.online].start();
		BigInteger[] secretC_pi_P = Util.permute(PreData.reshuffle_a[i],
				PreData.reshuffle_pi[i]);
		localTiming.stopwatch[PID.reshuffle][TID.online].stop();

		// D sends secretC_pi_P to C
		// D sends s2 to E
		localTiming.stopwatch[PID.reshuffle][TID.online_write].start();
		charlie.write(secretC_pi_P);
		localTiming.stopwatch[PID.reshuffle][TID.online_write].stop();

		charlie.countBandwidth = false;
		eddie.countBandwidth = false;
		charlie.bandwidth[PID.reshuffle].stop();
		eddie.bandwidth[PID.reshuffle].stop();

		return null;
	}

	@Override
	public BigInteger executeEddieSubTree(Communication charlie,
			Communication debbie, Timing localTiming, Pair<BigInteger, List<Integer>> args) {
		BigInteger secretE_P = args.getLeft();

		// i = 0 case: no shuffle needed
		if (i == 0) {
			return secretE_P;
		}

		charlie.countBandwidth = true;
		debbie.countBandwidth = true;
		charlie.bandwidth[PID.reshuffle].start();
		debbie.bandwidth[PID.reshuffle].start();

		//sanityCheck();

		// protocol
		// step 1
		// C sends E z
		localTiming.stopwatch[PID.reshuffle][TID.online_read].start();
		BigInteger z = charlie.readBigInteger();
		localTiming.stopwatch[PID.reshuffle][TID.online_read].stop();

		// step 2
		// D sends s2 to E

		// step 4
		// party E
		localTiming.stopwatch[PID.reshuffle][TID.online].start();
		BigInteger b_all = secretE_P.xor(z).xor(
				new BigInteger(1, PreData.reshuffle_p2[i]));
		BigInteger[] b = new BigInteger[pathBuckets];
		BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(
				BigInteger.ONE);
		BigInteger tmp = b_all;
		for (int j = pathBuckets - 1; j >= 0; j--) {
			b[j] = tmp.and(helper);
			tmp = tmp.shiftRight(bucketBits);
		}
		BigInteger[] secretE_pi_P_arr = Util
				.permute(b, PreData.reshuffle_pi[i]);
		BigInteger secretE_pi_P = BigInteger.ZERO;
		for (int j = 0; j < pathBuckets; j++)
			secretE_pi_P = secretE_pi_P.shiftLeft(bucketBits).xor(
					secretE_pi_P_arr[j]);
		localTiming.stopwatch[PID.reshuffle][TID.online].stop();

		charlie.countBandwidth = false;
		debbie.countBandwidth = false;
		charlie.bandwidth[PID.reshuffle].stop();
		debbie.bandwidth[PID.reshuffle].stop();

		// E outputs secretE_pi_P
		return secretE_pi_P;
	}
}
