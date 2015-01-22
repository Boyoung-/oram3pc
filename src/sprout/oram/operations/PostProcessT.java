package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.SR;
import sprout.oram.ForestMetadata;
import sprout.oram.PID;
import sprout.oram.TID;

// TODO: This operation is unlike the other TreeOperations we may want to 
//   Extend Operation ourselves, or redefine execute & run
public class PostProcessT extends TreeOperation<BigInteger, BigInteger[]> {

	public PostProcessT() {
		super(null, null);
	}

	public PostProcessT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public BigInteger executeCharlieSubTree(Communication debbie,
			Communication eddie, BigInteger[] args) {
		if (args.length != 6) {
			throw new IllegalArgumentException(
					"Must supply Li, sC_Ti, sC_Li_p, sC_Lip1_p, Lip1, Nip1_pr to charlie");
		}

		BigInteger Li = args[0];
		BigInteger secretC_Ti = args[1];
		BigInteger secretC_Li_p = args[2];
		BigInteger secretC_Lip1_p = args[3];
		BigInteger Lip1 = args[4];
		BigInteger Nip1_pr = args[5];
		int Nip1_pr_int = 0;
		if (i < h)
			Nip1_pr_int = Nip1_pr.intValue();

		// protocol
		// i = 0 case
		if (i == 0) {
			Li = null;
			secretC_Li_p = null;
		}

		// protocol doesn't run for i=h case
		if (i == h) {
			int d_size = ForestMetadata.getABits(i);
			// party C
			timing.stopwatch[PID.ppt][TID.online].start();
			BigInteger triangle_C = Li.xor(secretC_Li_p).shiftLeft(d_size);
			BigInteger secretC_Ti_p = secretC_Ti.xor(triangle_C);
			timing.stopwatch[PID.ppt][TID.online].stop();
			return secretC_Ti_p;
		}

		debbie.countBandwidth = true;
		eddie.countBandwidth = true;
		debbie.bandwidth[PID.ppt].start();
		eddie.bandwidth[PID.ppt].start();

		//sanityCheck();

		// step 1
		// E sends delta_C to C
		timing.stopwatch[PID.ppt][TID.online_read].start();
		BigInteger delta_C = eddie.readBigInteger();
		timing.stopwatch[PID.ppt][TID.online_read].stop();

		// step 2
		// party C
		timing.stopwatch[PID.ppt][TID.online].start();
		int alpha = SR.rand.nextInt(twotaupow) + 1; // [1, 2^tau]
		int j_p = BigInteger.valueOf(Nip1_pr_int + alpha)
				.mod(BigInteger.valueOf(twotaupow)).intValue();
		timing.stopwatch[PID.ppt][TID.online].stop();

		timing.stopwatch[PID.ppt][TID.online_write].start();
		// C sends j_p to D
		debbie.write(j_p);
		// C sends alpha to E
		eddie.write(alpha);
		timing.stopwatch[PID.ppt][TID.online_write].stop();

		// step 3
		// D sends s to C
		timing.stopwatch[PID.ppt][TID.online_read].start();
		byte[] s = debbie.read();
		timing.stopwatch[PID.ppt][TID.online_read].stop();

		// step 4
		// party C
		PRG G = new PRG(aBits);

		timing.stopwatch[PID.ppt][TID.online].start();
		BigInteger[] a = new BigInteger[twotaupow];
		BigInteger a_all = new BigInteger(1, G.compute(s));
		BigInteger helper = BigInteger.ONE.shiftLeft(d_ip1).subtract(
				BigInteger.ONE);
		BigInteger tmp = a_all;
		for (int k = twotaupow - 1; k >= 0; k--) {
			a[k] = tmp.and(helper);
			tmp = tmp.shiftRight(d_ip1);
		}

		BigInteger[] e = new BigInteger[twotaupow];
		BigInteger A_C = BigInteger.ZERO;
		for (int k = 0; k < twotaupow; k++) {
			e[k] = a[BigInteger.valueOf(k + alpha)
					.mod(BigInteger.valueOf(twotaupow)).intValue()];
			if (k == Nip1_pr_int)
				e[k] = e[k].xor(Lip1).xor(secretC_Lip1_p).xor(delta_C);
			A_C = A_C.shiftLeft(d_ip1).xor(e[k]);
		}
		BigInteger triangle_C;
		if (i == 0)
			triangle_C = A_C;
		else
			triangle_C = Li.xor(secretC_Li_p).shiftLeft(aBits).xor(A_C);
		BigInteger secretC_Ti_p = secretC_Ti.xor(triangle_C);
		timing.stopwatch[PID.ppt][TID.online].stop();

		debbie.countBandwidth = false;
		eddie.countBandwidth = false;
		debbie.bandwidth[PID.ppt].stop();
		eddie.bandwidth[PID.ppt].stop();

		// C outputs secretC_Ti_p
		return secretC_Ti_p;
	}

	@Override
	public BigInteger executeDebbieSubTree(Communication charlie,
			Communication eddie, BigInteger[] args_unused) {
		if (i == h) {
			return null;
		}

		charlie.countBandwidth = true;
		eddie.countBandwidth = true;
		charlie.bandwidth[PID.ppt].start();
		eddie.bandwidth[PID.ppt].start();

		//sanityCheck();

		// step 1
		// E sends delta_D to D
		timing.stopwatch[PID.ppt][TID.online_read].start();
		BigInteger delta_D = eddie.readBigInteger();

		// step 2
		// C sends j_p to D
		int j_p = charlie.readInt();
		timing.stopwatch[PID.ppt][TID.online_read].stop();

		// step 3
		// party D
		PRG G = new PRG(aBits);
		timing.stopwatch[PID.ppt][TID.online].start();
		byte[] s = SR.rand.generateSeed(16); // 128 bits

		BigInteger[] a = new BigInteger[twotaupow];
		BigInteger[] a_p = new BigInteger[twotaupow];
		BigInteger a_all = new BigInteger(1, G.compute(s));
		BigInteger helper = BigInteger.ONE.shiftLeft(d_ip1).subtract(
				BigInteger.ONE);
		BigInteger tmp = a_all;
		for (int k = twotaupow - 1; k >= 0; k--) {
			a[k] = tmp.and(helper);
			tmp = tmp.shiftRight(d_ip1);
			if (k != j_p)
				a_p[k] = a[k];
			else
				a_p[k] = a[k].xor(delta_D);
		}
		timing.stopwatch[PID.ppt][TID.online].stop();

		timing.stopwatch[PID.ppt][TID.online_write].start();
		// timing.post_write.start();
		// D sends s to C
		charlie.write(s);
		// D sends a_p to E
		eddie.write(a_p);
		timing.stopwatch[PID.ppt][TID.online_write].stop();
		// timing.post_write.stop();

		charlie.countBandwidth = false;
		eddie.countBandwidth = false;
		charlie.bandwidth[PID.ppt].stop();
		eddie.bandwidth[PID.ppt].stop();

		return null;
	}

	@Override
	public BigInteger executeEddieSubTree(Communication charlie,
			Communication debbie, BigInteger[] args) {
		if (args.length != 3) {
			throw new IllegalArgumentException(
					"Must supply sE_Ti, sE_Li_p, and sE_Lip1_p to eddie");
		}

		BigInteger secretE_Ti = args[0];
		BigInteger secretE_Li_p = args[1];
		BigInteger secretE_Lip1_p = args[2];

		// protocol
		// i = 0 case
		if (i == 0) {
			secretE_Li_p = null;
		}

		// protocol doesn't run for i=h case
		if (i == h) {
			int d_size = ForestMetadata.getABits(i);
			// party E
			timing.stopwatch[PID.ppt][TID.online].start();
			BigInteger triangle_E = secretE_Li_p.shiftLeft(d_size);
			BigInteger secretE_Ti_p = secretE_Ti.xor(triangle_E);
			timing.stopwatch[PID.ppt][TID.online].stop();
			return secretE_Ti_p;
		}

		charlie.countBandwidth = true;
		debbie.countBandwidth = true;
		charlie.bandwidth[PID.ppt].start();
		debbie.bandwidth[PID.ppt].start();

		//sanityCheck();

		// step 1
		// party E
		timing.stopwatch[PID.ppt][TID.online].start();
		BigInteger delta_D = new BigInteger(d_ip1, SR.rand);
		BigInteger delta_C = delta_D.xor(secretE_Lip1_p);
		timing.stopwatch[PID.ppt][TID.online].stop();
		// E sends delta_C to C and delta_D to D
		timing.stopwatch[PID.ppt][TID.online_write].start();
		debbie.write(delta_D);
		charlie.write(delta_C);
		timing.stopwatch[PID.ppt][TID.online_write].stop();

		// step 2
		// C sends alpha to E
		timing.stopwatch[PID.ppt][TID.online_read].start();
		int alpha = charlie.readInt();

		// step 3
		// D sends a_p to E
		BigInteger[] a_p = debbie.readBigIntegerArray();
		timing.stopwatch[PID.ppt][TID.online_read].stop();

		// step 5
		// party E
		timing.stopwatch[PID.ppt][TID.online].start();
		BigInteger A_E = BigInteger.ZERO;
		for (int k = 0; k < twotaupow; k++) {
			A_E = A_E.shiftLeft(d_ip1).xor(
					a_p[BigInteger.valueOf(k + alpha)
							.mod(BigInteger.valueOf(twotaupow)).intValue()]);
		}
		BigInteger triangle_E;
		if (i == 0)
			triangle_E = A_E;
		else
			triangle_E = secretE_Li_p.shiftLeft(aBits).xor(A_E);
		BigInteger secretE_Ti_p = secretE_Ti.xor(triangle_E);
		timing.stopwatch[PID.ppt][TID.online].stop();

		charlie.bandwidth[PID.ppt].stop();
		debbie.bandwidth[PID.ppt].stop();
		charlie.countBandwidth = false;
		debbie.countBandwidth = false;

		// E outputs secretE_Ti_p
		return secretE_Ti_p;
	}
}
