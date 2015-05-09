package sprout.oram.operations;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bouncycastle.util.Arrays;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.oram.TID;
import sprout.util.Timing;
import sprout.util.Util;

public class XOT extends Operation {

	public XOT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public BigInteger[] executeCharlie(Communication debbie,
			Communication eddie, Timing localTiming, int id, int i, int N,
			int k, int l) {
		// protocol
		// step 1
		localTiming.stopwatch[PID.xot][TID.online_read].start();
		byte[] msg_a = eddie.read();

		// step 2
		byte[] msg_j = debbie.read();
		byte[] msg_p = debbie.read();
		localTiming.stopwatch[PID.xot][TID.online_read].stop();

		// step 3
		localTiming.stopwatch[PID.xot][TID.online].start();
		int[] j = new int[k];
		BigInteger[] p = new BigInteger[k];
		BigInteger[] a = new BigInteger[N];
		BigInteger[] z = new BigInteger[k];
		int pBytes = (l + 7) / 8;

		for (int o = 0; o < N; o++) {
			a[o] = new BigInteger(1, Arrays.copyOfRange(msg_a, o * pBytes,
					(o + 1) * pBytes));
		}

		for (int o = 0; o < k; o++) {
			j[o] = new BigInteger(1, Arrays.copyOfRange(msg_j, o * 4,
					(o + 1) * 4)).intValue();
			p[o] = new BigInteger(1, Arrays.copyOfRange(msg_p, o * pBytes,
					(o + 1) * pBytes));
			z[o] = a[j[o]].xor(p[o]);
		}
		localTiming.stopwatch[PID.xot][TID.online].stop();

		return z;
	}

	public void executeDebbie(Communication charlie, Communication eddie,
			Timing localTiming, int id, int i, int N, int k, int l,
			Integer[] ii, BigInteger[] delta) {
		// protocol
		// step 2
		localTiming.stopwatch[PID.xot][TID.online].start();
		int pBytes = (l + 7) / 8;
		int[] j = new int[k];
		byte[][] j_bytes = new byte[k][];
		byte[] msg_j = new byte[k * 4];
		byte[][] p = new byte[k][];
		byte[] msg_p = new byte[k * pBytes];

		for (int o = 0; o < k; o++) {
			j[o] = PreData.xot_pi_ivs[id][i].get(ii[o]);
			j_bytes[o] = BigInteger.valueOf(
					PreData.xot_pi_ivs[id][i].get(ii[o])).toByteArray();
			p[o] = PreData.xot_r[id][i][j[o]].xor(delta[o]).toByteArray();

			System.arraycopy(j_bytes[o], 0, msg_j, (o + 1) * 4
					- j_bytes[o].length, j_bytes[o].length);
			if (p[o].length < pBytes)
				System.arraycopy(p[o], 0, msg_p,
						(o + 1) * pBytes - p[o].length, p[o].length);
			else
				System.arraycopy(p[o], p[o].length - pBytes, msg_p, o * pBytes,
						pBytes);
		}
		localTiming.stopwatch[PID.xot][TID.online].stop();

		localTiming.stopwatch[PID.xot][TID.online_write].start();
		charlie.write(msg_j);
		charlie.write(msg_p);
		localTiming.stopwatch[PID.xot][TID.online_write].stop();
	}

	public void executeEddie(Communication charlie, Communication debbie,
			Timing localTiming, int id, int i, int N, int k, int l,
			BigInteger[] m) {
		// protocol
		// step 1
		localTiming.stopwatch[PID.xot][TID.online].start();
		int aBytes = (l + 7) / 8;
		byte[][] a = new byte[N][];
		byte[] msg_a = new byte[N * aBytes];

		for (int o = 0; o < N; o++) {
			a[o] = m[PreData.xot_pi[0][i].get(o)].xor(PreData.xot_r[0][i][o])
					.toByteArray();
			if (a[o].length < aBytes)
				System.arraycopy(a[o], 0, msg_a,
						(o + 1) * aBytes - a[o].length, a[o].length);
			else
				System.arraycopy(a[o], a[o].length - aBytes, msg_a, o * aBytes,
						aBytes);
		}
		localTiming.stopwatch[PID.xot][TID.online].stop();

		localTiming.stopwatch[PID.xot][TID.online_write].start();
		charlie.write(msg_a);
		localTiming.stopwatch[PID.xot][TID.online_write].stop();
	}

	// for testing correctness
	@SuppressWarnings("unchecked")
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing XOT  #####");

		timing = new Timing();

		int levels = ForestMetadata.getLevels();
		int id = 0;
		if (party == Party.Eddie) {
			int i = SR.rand.nextInt(levels - 1) + 1;
			int k = SR.rand.nextInt(50) + 150; // 150-199
			int N = k + 2;
			int l = ForestMetadata.getTupleBits(i);

			PreData.xot_pi = (List<Integer>[][]) new List[2][levels];
			PreData.xot_pi_ivs = (List<Integer>[][]) new List[2][levels];
			PreData.xot_r = new BigInteger[2][levels][];

			PreData.xot_pi[id][i] = new ArrayList<Integer>();
			for (int o = 0; o < N; o++)
				PreData.xot_pi[id][i].add(o);
			Collections.shuffle(PreData.xot_pi[id][i], SR.rand);
			PreData.xot_pi_ivs[id][i] = Util
					.getInversePermutation(PreData.xot_pi[id][i]);
			PreData.xot_r[id][i] = new BigInteger[N];
			for (int o = 0; o < N; o++) {
				PreData.xot_r[id][i][o] = new BigInteger(l, SR.rand);
			}

			BigInteger[] m = new BigInteger[N];
			Integer[] ii = new Integer[k];
			BigInteger[] delta = new BigInteger[k];

			for (int o = 0; o < N; o++) {
				m[o] = new BigInteger(l, SR.rand);
			}
			List<Integer> tmp = new ArrayList<Integer>();
			for (int o = 0; o < k; o++) {
				delta[o] = new BigInteger(l, SR.rand);
				tmp.add(o);
			}
			Collections.shuffle(tmp, SR.rand);
			tmp.toArray(ii);

			con2.write(i);
			con2.write(N);
			con2.write(k);
			con2.write(l);
			con2.write(PreData.xot_pi_ivs[id][i]);
			con2.write(PreData.xot_r[id][i]);
			con2.write(ii);
			con2.write(delta);

			con1.write(i);
			con1.write(N);
			con1.write(k);
			con1.write(l);

			executeEddie(con1, con2, timing, id, i, N, k, l, m);

			BigInteger[] z = con1.readBigIntegerArray();

			System.out.println("i = " + i);
			int j;
			for (j = 0; j < k; j++) {
				if (m[ii[j]].xor(delta[j]).compareTo(z[j]) != 0) {
					System.out.println("XOT test failed");
					break;
				}
			}
			if (j == k) {
				System.out.println("XOT test passed");
			}
		} else if (party == Party.Debbie) {
			PreData.xot_pi_ivs = (List<Integer>[][]) new List[2][levels];
			PreData.xot_r = new BigInteger[2][levels][];

			int i = con2.readInt();
			int N = con2.readInt();
			int k = con2.readInt();
			int l = con2.readInt();
			PreData.xot_pi_ivs[id][i] = con2.readListInt();
			PreData.xot_r[id][i] = con2.readBigIntegerArray();
			Integer[] ii = con2.readIntegerArray();
			BigInteger[] delta = con2.readBigIntegerArray();

			executeDebbie(con1, con2, timing, id, i, N, k, l, ii, delta);
		} else if (party == Party.Charlie) {
			int i = con2.readInt();
			int N = con2.readInt();
			int k = con2.readInt();
			int l = con2.readInt();

			BigInteger[] z = executeCharlie(con1, con2, timing, id, i, N, k, l);

			con2.write(z);
		}

		System.out.println("#####  Testing XOT Finished  #####");
	}
}
