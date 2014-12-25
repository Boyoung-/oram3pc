package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.util.Util;

public class IOT extends Operation {

	public IOT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public void executeS(Communication R, Communication I, BigInteger[] m,
			int length) {
		I.countBandwidth = false;
		R.countBandwidth = false;

		// parameters
		int N = m.length;
		int l = length; // bits of each item in m

		I.write(N);
		I.write(l);

		// Pre-computed inputs
		Integer[] pi = I.readIntegerArray();
		BigInteger[] r = I.readBigIntegerArray();

		I.countBandwidth = true;
		R.countBandwidth = true;
		I.bandwidth[PID.iot].start();
		R.bandwidth[PID.iot].start();

		sanityCheck();

		// protocol
		// step 1
		// party S
		BigInteger[] a = new BigInteger[N];
		timing.iot_online.start();
		for (int o = 0; o < N; o++)
			a[o] = m[pi[o]].xor(r[o]);
		timing.iot_online.stop();

		// S sends a to R
		// sanityCheck(R);
		timing.iot_write.start();
		R.write(a);
		timing.iot_write.stop();

		I.countBandwidth = false;
		R.countBandwidth = false;
		I.bandwidth[PID.iot].stop();
		R.bandwidth[PID.iot].stop();
	}

	public BigInteger[] executeR(Communication I, Communication S) {
		I.countBandwidth = false;
		S.countBandwidth = false;

		// parameters // TODO: should not be transmitted
		int k = I.readInt();

		I.countBandwidth = true;
		S.countBandwidth = true;
		I.bandwidth[PID.iot].start();
		S.bandwidth[PID.iot].start();

		sanityCheck();

		// protocol
		// step 1
		// S sends a to R
		// sanityCheck(S);
		timing.iot_read.start();
		BigInteger[] a = S.readBigIntegerArray();
		timing.iot_read.stop();

		// step 2
		// I sends j and p to R
		// sanityCheck(I);
		timing.iot_read.start();
		Integer[] j = I.readIntegerArray();
		BigInteger[] p = I.readBigIntegerArray();
		timing.iot_read.stop();

		// step 3
		// party R
		BigInteger[] z = new BigInteger[k];
		timing.iot_online.start();
		for (int o = 0; o < k; o++)
			z[o] = a[j[o]].xor(p[o]);
		timing.iot_online.stop();

		I.countBandwidth = false;
		S.countBandwidth = false;
		I.bandwidth[PID.iot].stop();
		S.bandwidth[PID.iot].stop();

		// R output z
		return z;
	}

	public void executeI(Communication R, Communication S, Integer[] i,
			BigInteger[] delta) throws NoSuchAlgorithmException {
		S.countBandwidth = false;
		R.countBandwidth = false;

		// parameters // TODO: these should not be transmitted at execution time
		int k = i.length;
		R.write(k);
		int N = S.readInt();
		int l = S.readInt();

		// pre-computed inputs
		// party I
		List<Integer> pi = new ArrayList<Integer>();
		for (int o = 0; o < N; o++)
			pi.add(o);
		Collections.shuffle(pi, SR.rand);
		List<Integer> pi_ivs = Util.getInversePermutation(pi); // inverse
																// permutation
		byte[] s = SR.rand.generateSeed(16);
		PRG G = new PRG(N * l);
		BigInteger r_all = new BigInteger(1, G.compute(s));
		BigInteger[] r = new BigInteger[N];
		BigInteger helper = BigInteger.ONE.shiftLeft(l)
				.subtract(BigInteger.ONE);
		BigInteger tmp = r_all;
		for (int o = N - 1; o >= 0; o--) {
			r[o] = tmp.and(helper);
			tmp = tmp.shiftRight(l);
		}
		// I sends S pi and r
		S.write(pi.toArray(new Integer[0]));
		S.write(r);

		S.countBandwidth = true;
		R.countBandwidth = true;
		S.bandwidth[PID.iot].start();
		R.bandwidth[PID.iot].start();

		sanityCheck();

		// protocol
		// step 2
		// party I
		Integer[] j = new Integer[k];
		BigInteger[] p = new BigInteger[k];
		timing.iot_online.start();
		for (int o = 0; o < k; o++) {
			j[o] = pi_ivs.get(i[o]);
			p[o] = r[j[o]].xor(delta[o]);
		}
		timing.iot_online.stop();

		// I sends j and p to R
		// sanityCheck(R);
		timing.iot_write.start();
		R.write(j);
		R.write(p);
		timing.iot_write.stop();

		S.countBandwidth = false;
		R.countBandwidth = false;
		S.bandwidth[PID.iot].stop();
		R.bandwidth[PID.iot].stop();
	}

	@Override
	public void run(Party party, Forest forest) throws ForestException {
	}

}
