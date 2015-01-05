package sprout.oram.operations;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECPoint;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.SR;
import sprout.crypto.oprf.OPRF;
import sprout.oram.PID;
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

		sanityCheck();

		// protocol
		// step 1
		// D sends s and x to E
		// D sends c to C
		timing.stopwatch[PID.encrypt][TID.online_read].start();
		BigInteger[] c = debbie.readBigIntegerArray();
		timing.stopwatch[PID.encrypt][TID.online_read].stop();

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
			d[j] = c[j].xor(secretC_B[j]);
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
		charlie.countBandwidth = true;
		eddie.countBandwidth = true;
		charlie.bandwidth[PID.encrypt].start();
		eddie.bandwidth[PID.encrypt].start();

		sanityCheck();

		try {
			timing.stopwatch[PID.encrypt][TID.offline].start();
			OPRF oprf = OPRFHelper.getOPRF(false);
			// protocol
			// step 1
			// party D
			ECPoint[] x = new ECPoint[pathBuckets];
			ECPoint[] v = new ECPoint[pathBuckets];
			BigInteger r;
			PRG G1 = new PRG(bucketBits * pathBuckets);
			BigInteger[] a = new BigInteger[pathBuckets];
			BigInteger[] b = new BigInteger[pathBuckets];
			BigInteger[] c = new BigInteger[pathBuckets];
			timing.stopwatch[PID.encrypt][TID.offline].stop();

			timing.stopwatch[PID.encrypt][TID.online].start();
			byte[] s = SR.rand.generateSeed(16);
			for (int j = 0; j < pathBuckets; j++) {
				// This computation is repeated in oprf in some form
				r = oprf.randomExponent();
				x[j] = oprf.getG().multiply(r);
				v[j] = oprf.getY().multiply(r);
				// below is a version using only the exposed methods, however
				// the above should eventually be used
				// once same base optimizations are implemented
				// x[j] = oprf.randomPoint();
				// v[j] = oprf.evaluate(x[j]).getResult();
			}
			PRG G2 = new PRG(bucketBits);
			BigInteger a_all = new BigInteger(1, G1.compute(s));
			BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(
					BigInteger.ONE);
			BigInteger tmp = a_all;
			for (int j = pathBuckets - 1; j >= 0; j--) {
				a[j] = tmp.and(helper);
				tmp = tmp.shiftRight(bucketBits);
				b[j] = new BigInteger(1, G2.compute(v[j].getEncoded()));
				c[j] = a[j].xor(b[j]);
			}
			timing.stopwatch[PID.encrypt][TID.online].stop();
			// D sends s and x to E
			// D sends c to C
			timing.stopwatch[PID.encrypt][TID.online_write].start();
			eddie.write(s);
			eddie.write(x);
			charlie.write(c);
			timing.stopwatch[PID.encrypt][TID.online_write].stop();
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Error in EncryptPath charlie and eddie will probably hang");
		}

		charlie.countBandwidth = false;
		eddie.countBandwidth = false;
		charlie.bandwidth[PID.encrypt].stop();
		eddie.bandwidth[PID.encrypt].stop();

		return null;
	}

	@Override
	public EPath executeEddieSubTree(Communication charlie,
			Communication debbie, BigInteger secretE_P) {
		charlie.countBandwidth = true;
		debbie.countBandwidth = true;
		charlie.bandwidth[PID.encrypt].start();
		debbie.bandwidth[PID.encrypt].start();

		sanityCheck();

		// TODO: move unnecessary stuff out of try
		try {
			// protocol
			// step 1
			// D sends s and x to E
			// D sends c to C
			timing.stopwatch[PID.encrypt][TID.online_read].start();
			byte[] s = debbie.read();
			ECPoint[] x = debbie.readECPointArray();

			// Step 2
			// C sends d to E
			BigInteger[] d = charlie.readBigIntegerArray();
			timing.stopwatch[PID.encrypt][TID.online_read].stop();

			// step 3
			// party E
			// regeneration of a[]
			PRG G1 = new PRG(bucketBits * pathBuckets);
			BigInteger[] a = new BigInteger[pathBuckets];
			timing.stopwatch[PID.encrypt][TID.online].start();
			BigInteger a_all = new BigInteger(1, G1.compute(s));
			BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(
					BigInteger.ONE);
			BigInteger tmp = a_all;
			for (int j = pathBuckets - 1; j >= 0; j--) {
				a[j] = tmp.and(helper);
				tmp = tmp.shiftRight(bucketBits);
			}
			// end generation of a[]

			BigInteger[] secretE_B = new BigInteger[pathBuckets];
			BigInteger[] Bbar = new BigInteger[pathBuckets];
			tmp = secretE_P;
			for (int j = pathBuckets - 1; j >= 0; j--) {
				secretE_B[j] = tmp.and(helper);
				tmp = tmp.shiftRight(bucketBits);
				Bbar[j] = secretE_B[j].xor(a[j]).xor(d[j]);
			}
			timing.stopwatch[PID.encrypt][TID.online].stop();

			charlie.countBandwidth = false;
			debbie.countBandwidth = false;
			charlie.bandwidth[PID.encrypt].stop();
			debbie.bandwidth[PID.encrypt].stop();

			// E outputs encrypted path
			return new EPath(x, Bbar);
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Exception in EncryptPath Eddie, others probably hanging");
			return null;
		}
	}
}
