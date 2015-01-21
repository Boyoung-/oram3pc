package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.oram.TID;

public class IOT extends Operation {

	public IOT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public void executeS(Communication R, Communication I, BigInteger[] m,
			int index) {
		int N = m.length;

		I.countBandwidth = true;
		R.countBandwidth = true;
		I.bandwidth[PID.iot].start();
		R.bandwidth[PID.iot].start();

		sanityCheck();

		// protocol
		// step 1
		// party S
		BigInteger[] a = new BigInteger[N];
		timing.stopwatch[PID.iot][TID.online].start();
		for (int o = 0; o < N; o++)
			a[o] = m[PreData.iot_pi[0][index].get(o)]
					.xor(PreData.iot_r[0][index][o]);
		timing.stopwatch[PID.iot][TID.online].stop();

		// S sends a to R
		timing.stopwatch[PID.iot][TID.online_write].start();
		R.write(a);
		timing.stopwatch[PID.iot][TID.online_write].stop();

		I.countBandwidth = false;
		R.countBandwidth = false;
		I.bandwidth[PID.iot].stop();
		R.bandwidth[PID.iot].stop();
	}

	public BigInteger[] executeR(Communication I, Communication S, int index) {
		int k = PreData.iot_r[0][index].length - 2;

		I.countBandwidth = true;
		S.countBandwidth = true;
		I.bandwidth[PID.iot].start();
		S.bandwidth[PID.iot].start();

		sanityCheck();

		// protocol
		// step 1
		// S sends a to R
		timing.stopwatch[PID.iot][TID.online_read].start();
		BigInteger[] a = S.readBigIntegerArray();

		// step 2
		// I sends j and p to R
		Integer[] j = I.readIntegerArray();
		BigInteger[] p = I.readBigIntegerArray();
		timing.stopwatch[PID.iot][TID.online_read].stop();

		// step 3
		// party R
		BigInteger[] z = new BigInteger[k];
		timing.stopwatch[PID.iot][TID.online].start();
		for (int o = 0; o < k; o++)
			z[o] = a[j[o]].xor(p[o]);
		timing.stopwatch[PID.iot][TID.online].stop();

		I.countBandwidth = false;
		S.countBandwidth = false;
		I.bandwidth[PID.iot].stop();
		S.bandwidth[PID.iot].stop();

		// R output z
		return z;
	}

	public void executeI(Communication R, Communication S, Integer[] i,
			BigInteger[] delta, int index, int id)
			throws NoSuchAlgorithmException {
		int k = PreData.iot_r[0][index].length - 2;

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
		timing.stopwatch[PID.iot][TID.online].start();
		for (int o = 0; o < k; o++) {
			j[o] = PreData.iot_pi_ivs[id][index].get(i[o]);
			p[o] = PreData.iot_r[id][index][j[o]].xor(delta[o]);
		}
		timing.stopwatch[PID.iot][TID.online].stop();

		// I sends j and p to R
		timing.stopwatch[PID.iot][TID.online_write].start();
		R.write(j);
		R.write(p);
		timing.stopwatch[PID.iot][TID.online_write].stop();

		S.countBandwidth = false;
		R.countBandwidth = false;
		S.bandwidth[PID.iot].stop();
		R.bandwidth[PID.iot].stop();
	}

	@Override
	public void run(Party party, Forest forest) throws ForestException {
	}

}
