package sprout.oram.operations;

import java.math.BigInteger;

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

public class PostProcessT extends TreeOperation<BigInteger, BigInteger[]> {

	public PostProcessT() {
		super(null, null);
	}

	public PostProcessT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public BigInteger executeCharlieSubTree(Communication debbie,
			Communication eddie, Tree unused, BigInteger[] args,
			Timing localTiming) {

		localTiming.stopwatch[PID.ppt][TID.online].start();
		BigInteger sC_Ti = args[0];
		BigInteger Li = args[1];
		BigInteger Lip1 = args[2];
		int j_2 = args[3].intValue();

		if (i == h) {
			BigInteger sC_Ti_p = Li.xor(PreData.ppt_sC_Li_p[i])
					.shiftLeft(aBits).xor(sC_Ti);
			localTiming.stopwatch[PID.ppt][TID.online].stop();
			return sC_Ti_p;
		}

		// protocol
		// step 1
		int delta = (PreData.ppt_alpha[i] - j_2 + twotaupow) % twotaupow;
		byte[] msg_delta = BigInteger.valueOf(delta).toByteArray();
		localTiming.stopwatch[PID.ppt][TID.online].stop();

		localTiming.stopwatch[PID.ppt][TID.online_write].start();
		//eddie.write(delta);
		eddie.write(msg_delta, PID.ppt);
		localTiming.stopwatch[PID.ppt][TID.online_write].stop();

		// step 2
		localTiming.stopwatch[PID.ppt][TID.online].start();
		BigInteger[] c = new BigInteger[twotaupow];
		BigInteger c_all = BigInteger.ZERO;
		for (int t = 0; t < twotaupow; t++) {
			c[t] = PreData.ppt_r[i][(t + delta) % twotaupow];
			if (t == j_2) {
				c[t] = c[t].xor(Lip1).xor(PreData.ppt_sC_Li_p[i+1]);
			}
			c_all = c_all.shiftLeft(d_ip1).xor(c[t]);
		}

		BigInteger sC_Ti_p;
		if (i == 0) {
			sC_Ti_p = sC_Ti.xor(c_all);
		} else {
			sC_Ti_p = sC_Ti.xor(Li.xor(PreData.ppt_sC_Li_p[i]).shiftLeft(aBits)
					.xor(c_all));
		}
		localTiming.stopwatch[PID.ppt][TID.online].stop();

		return sC_Ti_p;
	}

	@Override
	public BigInteger executeDebbieSubTree(Communication charlie,
			Communication eddie, Tree unused, BigInteger[] unused2,
			Timing localTiming) {
		return null;
	}

	@Override
	public BigInteger executeEddieSubTree(Communication charlie,
			Communication debbie, Tree unused, BigInteger[] args,
			Timing localTiming) {
		localTiming.stopwatch[PID.ppt][TID.online].start();
		BigInteger sE_Ti = args[0];

		if (i == h) {
			BigInteger sE_Ti_p = PreData.ppt_sE_Li_p[i].shiftLeft(aBits).xor(
					sE_Ti);
			localTiming.stopwatch[PID.ppt][TID.online].stop();
			return sE_Ti_p;
		}
		localTiming.stopwatch[PID.ppt][TID.online].stop();

		// protocol
		// step 1
		localTiming.stopwatch[PID.ppt][TID.online_read].start();
		//int delta = charlie.readInt();
		byte[] msg_delta = charlie.read();
		localTiming.stopwatch[PID.ppt][TID.online_read].stop();

		// step 3
		localTiming.stopwatch[PID.ppt][TID.online].start();
		int delta = new BigInteger(1, msg_delta).intValue();
		
		BigInteger[] e = new BigInteger[twotaupow];
		BigInteger e_all = BigInteger.ZERO;
		for (int t = 0; t < twotaupow; t++) {
			e[t] = PreData.ppt_r_p[i][(t + delta) % twotaupow];
			e_all = e_all.shiftLeft(d_ip1).xor(e[t]);
		}

		BigInteger sE_Ti_p;
		if (i == 0) {
			sE_Ti_p = sE_Ti.xor(e_all);
		} else {
			sE_Ti_p = sE_Ti.xor(PreData.ppt_sE_Li_p[i].shiftLeft(aBits).xor(
					e_all));
		}
		localTiming.stopwatch[PID.ppt][TID.online].stop();

		return sE_Ti_p;
	}

	// for testing correctness
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing PPT  #####");

		timing = new Timing();

		if (party == Party.Eddie) {
			int levels = ForestMetadata.getLevels();
			int i = SR.rand.nextInt(levels-1);
			PreData.ppt_sC_Li_p = new BigInteger[levels];
			PreData.ppt_sE_Li_p = new BigInteger[levels];
			PreData.ppt_r = new BigInteger[levels][twotaupow];
			PreData.ppt_r_p = new BigInteger[levels][twotaupow];
			PreData.ppt_alpha = new int[levels];

			loadTreeSpecificParameters(i);
			PreData.ppt_sC_Li_p[i] = new BigInteger(d_i, SR.rand);
			PreData.ppt_sC_Li_p[i+1] = new BigInteger(d_i, SR.rand);
			PreData.ppt_sE_Li_p[i] = new BigInteger(d_i, SR.rand);
			PreData.ppt_sE_Li_p[i+1] = new BigInteger(d_i, SR.rand);
			PreData.ppt_alpha[i] = SR.rand.nextInt(twotaupow);
			for (int j = 0; j < twotaupow; j++) {
				PreData.ppt_r[i][j] = new BigInteger(d_ip1, SR.rand);
				PreData.ppt_r_p[i][j] = PreData.ppt_r[i][j];
			}
			PreData.ppt_r_p[i][PreData.ppt_alpha[i]] = PreData.ppt_r[i][PreData.ppt_alpha[i]]
					.xor(PreData.ppt_sE_Li_p[i+1]);

			BigInteger sC_Ti = new BigInteger(tupleBits, SR.rand);
			BigInteger sE_Ti = new BigInteger(tupleBits, SR.rand);
			int j_2 = SR.rand.nextInt(twotaupow);
			BigInteger Ti = sC_Ti.xor(sE_Ti);
			BigInteger Li = Util.getSubBits(Ti, aBits, aBits + lBits);
			BigInteger Lip1 = Util.getSubBits(Ti,
					(twotaupow - j_2 - 1) * d_ip1, (twotaupow - j_2) * d_ip1);

			con1.write(i);
			con1.write(PreData.ppt_sC_Li_p[i]);
			con1.write(PreData.ppt_sC_Li_p[i+1]);
			con1.write(PreData.ppt_alpha[i]);
			con1.write(PreData.ppt_r[i]);
			con1.write(sC_Ti);
			con1.write(Li);
			con1.write(Lip1);
			con1.write(j_2);

			con2.write(i);

			BigInteger sE_Ti_p = executeEddieSubTree(con1, con2, null,
					new BigInteger[] { sE_Ti }, timing);

			BigInteger sC_Ti_p = con1.readBigInteger();

			BigInteger Ti_p = sE_Ti_p.xor(sC_Ti_p);
			BigInteger Li_p = Util.getSubBits(Ti_p, aBits, aBits + lBits);
			BigInteger Lip1_p = Util.getSubBits(Ti_p, (twotaupow - j_2 - 1)
					* d_ip1, (twotaupow - j_2) * d_ip1);

			System.out.println("levels = " + levels);
			System.out.println("i = " + i);
			if (i == 0) {
				if (PreData.ppt_sC_Li_p[i+1].xor(PreData.ppt_sE_Li_p[i+1])
						.compareTo(Lip1_p) == 0) {
					System.out.println("PPT test passed:");
				} else {
					System.out.println("PPT test failed:");
				}
				System.out.println("j_2=\t" + j_2);
				System.out.println("Lip1=\t"
						+ Util.addZero(Lip1.toString(2), d_ip1));
				System.out.println("Lip1_p=\t"
						+ Util.addZero(
								PreData.ppt_sC_Li_p[i+1].xor(
										PreData.ppt_sE_Li_p[i+1]).toString(2),
								d_ip1));
			} else if (i < h) {
				if (PreData.ppt_sC_Li_p[i].xor(PreData.ppt_sE_Li_p[i])
						.compareTo(Li_p) == 0
						&& PreData.ppt_sC_Li_p[i+1].xor(
								PreData.ppt_sE_Li_p[i+1]).compareTo(Lip1_p) == 0) {
					System.out.println("PPT test passed:");
				} else {
					System.out.println("PPT test failed:");
				}
				System.out.println("Li=\t" + Util.addZero(Li.toString(2), d_i));
				System.out.println("Li_p=\t"
						+ Util.addZero(
								PreData.ppt_sC_Li_p[i].xor(
										PreData.ppt_sE_Li_p[i]).toString(2),
								d_i));
				System.out.println("j_2=\t" + j_2);
				System.out.println("Lip1=\t"
						+ Util.addZero(Lip1.toString(2), d_ip1));
				System.out.println("Lip1_p=\t"
						+ Util.addZero(
								PreData.ppt_sC_Li_p[i+1].xor(
										PreData.ppt_sE_Li_p[i+1]).toString(2),
								d_ip1));
			} else {
				if (PreData.ppt_sC_Li_p[i].xor(PreData.ppt_sE_Li_p[i])
						.compareTo(Li_p) == 0) {
					System.out.println("PPT test passed:");
				} else {
					System.out.println("PPT test failed:");
				}
				System.out.println("Li=\t" + Util.addZero(Li.toString(2), d_i));
				System.out.println("Li_p=\t"
						+ Util.addZero(
								PreData.ppt_sC_Li_p[i].xor(
										PreData.ppt_sE_Li_p[i]).toString(2),
								d_i));
			}
			String format;
			if (i == 0)
				format = "";
			else
				format = "F";
			for (int j = 0; j < nBits; j++)
				format += "N";
			for (int j = 0; j < lBits; j++)
				format += "L";
			for (int j = 0; j < aBits; j++)
				format += "A";
			System.out.println("\t" + format);
			System.out.println("Ti=\t"
					+ Util.addZero(Ti.toString(2), tupleBits));
			System.out.println("Ti_p=\t"
					+ Util.addZero(Ti_p.toString(2), tupleBits));
		} else if (party == Party.Debbie) {
			int i = con2.readInt();

			loadTreeSpecificParameters(i);
			executeDebbieSubTree(con1, con2, null, null, timing);
		} else if (party == Party.Charlie) {
			int levels = ForestMetadata.getLevels();
			PreData.ppt_sC_Li_p = new BigInteger[levels];
			PreData.ppt_r = new BigInteger[levels][];
			PreData.ppt_alpha = new int[levels];

			int i = con2.readInt();
			PreData.ppt_sC_Li_p[i] = con2.readBigInteger();
			PreData.ppt_sC_Li_p[i+1] = con2.readBigInteger();
			PreData.ppt_alpha[i] = con2.readInt();
			PreData.ppt_r[i] = con2.readBigIntegerArray();
			BigInteger sC_Ti = con2.readBigInteger();
			BigInteger Li = con2.readBigInteger();
			BigInteger Lip1 = con2.readBigInteger();
			int j_2 = con2.readInt();

			loadTreeSpecificParameters(i);
			BigInteger sC_Ti_p = executeCharlieSubTree(
					con1,
					con2,
					null,
					new BigInteger[] { sC_Ti, Li, Lip1, BigInteger.valueOf(j_2) },
					timing);

			con2.write(sC_Ti_p);
		}

		System.out.println("#####  Testing PPT Finished  #####");
	}
}
