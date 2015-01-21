package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.oram.PID;
import sprout.oram.PreData;
import sprout.oram.TID;

public class EncryptPath extends TreeOperation<EPath, BigInteger> {

	public EncryptPath() {
		super(null, null);
	}

	public EncryptPath(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public EPath executeCharlieSubTree(Communication debbie,
			Communication eddie, BigInteger secretC_P) {
		debbie.countBandwidth = true;
		eddie.countBandwidth = true;
		debbie.bandwidth[PID.encrypt].start();
		eddie.bandwidth[PID.encrypt].start();

		// sanityCheck();

		// protocol
		// step 1
		// D sends s and x to E
		// D sends c to C

		// step 2
		// party C
		BigInteger[] secretC_B = new BigInteger[pathBuckets];
		BigInteger[] d = new BigInteger[pathBuckets];
		timing.stopwatch[PID.encrypt][TID.online].start();
		BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(
				BigInteger.ONE);
		BigInteger tmp = secretC_P;
		for (int j = pathBuckets - 1; j >= 0; j--) {
			secretC_B[j] = tmp.and(helper);
			tmp = tmp.shiftRight(bucketBits);
			d[j] = PreData.encrypt_c[i][j].xor(secretC_B[j]);
		}
		timing.stopwatch[PID.encrypt][TID.online].stop();
		// C sends d to E
		timing.stopwatch[PID.encrypt][TID.online_write].start();
		eddie.write(d);
		timing.stopwatch[PID.encrypt][TID.online_write].stop();

		debbie.countBandwidth = false;
		eddie.countBandwidth = false;
		debbie.bandwidth[PID.encrypt].stop();
		eddie.bandwidth[PID.encrypt].stop();

		return null;
	}

	@Override
	public EPath executeDebbieSubTree(Communication charlie,
			Communication eddie, BigInteger k) {
		// debbie does nothing online
		return null;
	}

	@Override
	public EPath executeEddieSubTree(Communication charlie,
			Communication debbie, BigInteger secretE_P) {
		charlie.countBandwidth = true;
		debbie.countBandwidth = true;
		charlie.bandwidth[PID.encrypt].start();
		debbie.bandwidth[PID.encrypt].start();

		// sanityCheck();

		// protocol
		// step 1
		// D sends s and x to E
		// D sends c to C

		// Step 2
		// C sends d to E
		timing.stopwatch[PID.encrypt][TID.online_read].start();
		BigInteger[] d = charlie.readBigIntegerArray();
		timing.stopwatch[PID.encrypt][TID.online_read].stop();

		// step 3
		// party E
		timing.stopwatch[PID.encrypt][TID.online].start();
		BigInteger[] secretE_B = new BigInteger[pathBuckets];
		BigInteger[] Bbar = new BigInteger[pathBuckets];
		BigInteger tmp = secretE_P;
		BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(
				BigInteger.ONE);
		for (int j = pathBuckets - 1; j >= 0; j--) {
			secretE_B[j] = tmp.and(helper);
			tmp = tmp.shiftRight(bucketBits);
			Bbar[j] = secretE_B[j].xor(PreData.encrypt_a[i][j]).xor(d[j]);
		}
		timing.stopwatch[PID.encrypt][TID.online].stop();

		charlie.countBandwidth = false;
		debbie.countBandwidth = false;
		charlie.bandwidth[PID.encrypt].stop();
		debbie.bandwidth[PID.encrypt].stop();

		// E outputs encrypted path
		return new EPath(PreData.encrypt_x[i], Bbar);
	}
}
