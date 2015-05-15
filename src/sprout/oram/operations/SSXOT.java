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

public class SSXOT extends Operation {

	public SSXOT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public BigInteger[] executeCharlie(Communication debbie,
			Communication eddie, Timing localTiming, int i, int N, int k,
			int l, BigInteger[] sC_m) {
		/*
		 * XOT xot = new XOT(debbie, eddie);
		 * 
		 * // protocol // step 1 xot.executeEddie(eddie, debbie, localTiming, 0,
		 * i, N, k, l, sC_m);
		 * 
		 * 
		 * // step 2 BigInteger[] b = xot.executeCharlie(debbie, eddie,
		 * localTiming, 1, i, N, k, l);
		 * 
		 * return b;
		 */

		// XOT 1
		localTiming.stopwatch[PID.ssxot][TID.online].start();
		int aBytes = (l + 7) / 8;
		byte[][] a = new byte[N][];
		byte[] msg_a = new byte[N * aBytes];

		for (int o = 0; o < N; o++) {
			a[o] = sC_m[PreData.xot_pi[0][i].get(o)]
					.xor(PreData.xot_r[0][i][o]).toByteArray();
			if (a[o].length < aBytes)
				System.arraycopy(a[o], 0, msg_a,
						(o + 1) * aBytes - a[o].length, a[o].length);
			else
				System.arraycopy(a[o], a[o].length - aBytes, msg_a, o * aBytes,
						aBytes);
		}
		localTiming.stopwatch[PID.ssxot][TID.online].stop();

		localTiming.stopwatch[PID.ssxot][TID.online_write].start();
		eddie.write(msg_a, PID.ssxot);
		localTiming.stopwatch[PID.ssxot][TID.online_write].stop();

		// XOT 2
		localTiming.stopwatch[PID.ssxot][TID.online_read].start();
		msg_a = eddie.read();

		byte[] msg_j = debbie.read();
		byte[] msg_p = debbie.read();
		localTiming.stopwatch[PID.ssxot][TID.online_read].stop();

		localTiming.stopwatch[PID.ssxot][TID.online].start();
		int[] j = new int[k];
		BigInteger[] p = new BigInteger[k];
		BigInteger[] aa = new BigInteger[N];
		BigInteger[] z = new BigInteger[k];
		int pBytes = (l + 7) / 8;

		for (int o = 0; o < N; o++) {
			aa[o] = new BigInteger(1, Arrays.copyOfRange(msg_a, o * pBytes,
					(o + 1) * pBytes));
		}

		for (int o = 0; o < k; o++) {
			j[o] = new BigInteger(1, Arrays.copyOfRange(msg_j, o * 4,
					(o + 1) * 4)).intValue();
			p[o] = new BigInteger(1, Arrays.copyOfRange(msg_p, o * pBytes,
					(o + 1) * pBytes));
			z[o] = aa[j[o]].xor(p[o]);
		}
		localTiming.stopwatch[PID.ssxot][TID.online].stop();

		return z;
	}

	public void executeDebbie(Communication charlie, Communication eddie,
			Timing localTiming, int i, int N, int k, int l, Integer[] ii) {
		/*
		 * XOT xot = new XOT(charlie, eddie);
		 * 
		 * // protocol // step 1 xot.executeDebbie(eddie, charlie, localTiming,
		 * 0, i, N, k, l, ii, PreData.ssxot_delta[i]);
		 * 
		 * 
		 * // step 2 xot.executeDebbie(charlie, eddie, localTiming, 1, i, N, k,
		 * l, ii, PreData.ssxot_delta[i]);
		 */

		// XOT 1
		localTiming.stopwatch[PID.ssxot][TID.online].start();
		int pBytes = (l + 7) / 8;
		int[] j = new int[k];
		byte[][] j_bytes = new byte[k][];
		byte[] msg_j = new byte[k * 4];
		byte[][] p = new byte[k][];
		byte[] msg_p = new byte[k * pBytes];

		for (int o = 0; o < k; o++) {
			j[o] = PreData.xot_pi_ivs[0][i].get(ii[o]);
			j_bytes[o] = BigInteger
					.valueOf(PreData.xot_pi_ivs[0][i].get(ii[o])).toByteArray();
			p[o] = PreData.xot_r[0][i][j[o]].xor(PreData.ssxot_delta[i][o])
					.toByteArray();

			System.arraycopy(j_bytes[o], 0, msg_j, (o + 1) * 4
					- j_bytes[o].length, j_bytes[o].length);
			if (p[o].length < pBytes)
				System.arraycopy(p[o], 0, msg_p,
						(o + 1) * pBytes - p[o].length, p[o].length);
			else
				System.arraycopy(p[o], p[o].length - pBytes, msg_p, o * pBytes,
						pBytes);
		}
		localTiming.stopwatch[PID.ssxot][TID.online].stop();

		localTiming.stopwatch[PID.ssxot][TID.online_write].start();
		eddie.write(msg_j, PID.ssxot);
		eddie.write(msg_p, PID.ssxot);
		localTiming.stopwatch[PID.ssxot][TID.online_write].stop();

		// XOT 2
		localTiming.stopwatch[PID.ssxot][TID.online].start();
		msg_j = new byte[k * 4];
		msg_p = new byte[k * pBytes];

		for (int o = 0; o < k; o++) {
			j[o] = PreData.xot_pi_ivs[1][i].get(ii[o]);
			j_bytes[o] = BigInteger
					.valueOf(PreData.xot_pi_ivs[1][i].get(ii[o])).toByteArray();
			p[o] = PreData.xot_r[1][i][j[o]].xor(PreData.ssxot_delta[i][o])
					.toByteArray();

			System.arraycopy(j_bytes[o], 0, msg_j, (o + 1) * 4
					- j_bytes[o].length, j_bytes[o].length);
			if (p[o].length < pBytes)
				System.arraycopy(p[o], 0, msg_p,
						(o + 1) * pBytes - p[o].length, p[o].length);
			else
				System.arraycopy(p[o], p[o].length - pBytes, msg_p, o * pBytes,
						pBytes);
		}
		localTiming.stopwatch[PID.ssxot][TID.online].stop();

		localTiming.stopwatch[PID.ssxot][TID.online_write].start();
		charlie.write(msg_j, PID.ssxot);
		charlie.write(msg_p, PID.ssxot);
		localTiming.stopwatch[PID.ssxot][TID.online_write].stop();
	}

	public BigInteger[] executeEddie(Communication charlie,
			Communication debbie, Timing localTiming, int i, int N, int k,
			int l, BigInteger[] sE_m) {
		/*
		 * XOT xot = new XOT(charlie, debbie);
		 * 
		 * // protocol // step 1 BigInteger[] a = xot.executeCharlie(debbie,
		 * charlie, localTiming, 0, i, N, k, l);
		 * 
		 * 
		 * // step 2 xot.executeEddie(charlie, debbie, localTiming, 1, i, N, k,
		 * l, sE_m);
		 * 
		 * return a;
		 */

		// XOT 2
		localTiming.stopwatch[PID.ssxot][TID.online].start();
		int aBytes = (l + 7) / 8;
		byte[][] a = new byte[N][];
		byte[] msg_a = new byte[N * aBytes];

		for (int o = 0; o < N; o++) {
			a[o] = sE_m[PreData.xot_pi[0][i].get(o)]
					.xor(PreData.xot_r[0][i][o]).toByteArray();
			if (a[o].length < aBytes)
				System.arraycopy(a[o], 0, msg_a,
						(o + 1) * aBytes - a[o].length, a[o].length);
			else
				System.arraycopy(a[o], a[o].length - aBytes, msg_a, o * aBytes,
						aBytes);
		}
		localTiming.stopwatch[PID.ssxot][TID.online].stop();

		localTiming.stopwatch[PID.ssxot][TID.online_write].start();
		charlie.write(msg_a, PID.ssxot);
		localTiming.stopwatch[PID.ssxot][TID.online_write].stop();

		// XOT 1
		localTiming.stopwatch[PID.ssxot][TID.online_read].start();
		msg_a = charlie.read();

		byte[] msg_j = debbie.read();
		byte[] msg_p = debbie.read();
		localTiming.stopwatch[PID.ssxot][TID.online_read].stop();

		localTiming.stopwatch[PID.ssxot][TID.online].start();
		int[] j = new int[k];
		BigInteger[] p = new BigInteger[k];
		BigInteger[] aa = new BigInteger[N];
		BigInteger[] z = new BigInteger[k];
		int pBytes = (l + 7) / 8;

		for (int o = 0; o < N; o++) {
			aa[o] = new BigInteger(1, Arrays.copyOfRange(msg_a, o * pBytes,
					(o + 1) * pBytes));
		}

		for (int o = 0; o < k; o++) {
			j[o] = new BigInteger(1, Arrays.copyOfRange(msg_j, o * 4,
					(o + 1) * 4)).intValue();
			p[o] = new BigInteger(1, Arrays.copyOfRange(msg_p, o * pBytes,
					(o + 1) * pBytes));
			z[o] = aa[j[o]].xor(p[o]);
		}
		localTiming.stopwatch[PID.ssxot][TID.online].stop();

		return z;
	}

	// for testing correctness
	@SuppressWarnings("unchecked")
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing SSXOT  #####");

		timing = new Timing();

		for (int iii = 0; iii < 20; iii++) {

			int levels = ForestMetadata.getLevels();
			if (party == Party.Eddie) {
				PreData.xot_pi = (List<Integer>[][]) new List[2][levels];
				PreData.xot_r = new BigInteger[2][levels][];

				int i = con2.readInt();
				int N = con2.readInt();
				int k = con2.readInt();
				int l = con2.readInt();
				PreData.xot_pi[0][i] = con2.readListInt();
				PreData.xot_r[0][i] = con2.readBigIntegerArray();
				BigInteger[] sE_m = con2.readBigIntegerArray();

				BigInteger[] a = executeEddie(con1, con2, timing, i, N, k, l,
						sE_m);

				BigInteger[] b = con1.readBigIntegerArray();
				BigInteger[] sC_m = con2.readBigIntegerArray();
				Integer[] ii = con2.readIntegerArray();

				System.out.println("i = " + i);
				int j;
				for (j = 0; j < k; j++) {
					if (sC_m[ii[j]].xor(sE_m[ii[j]]).compareTo(a[j].xor(b[j])) != 0) {
						System.out.println("SSXOT test failed");
						break;
					}
				}
				if (j == k) {
					System.out.println("SSXOT test passed");
				}
			} else if (party == Party.Debbie) {
				int i = SR.rand.nextInt(levels - 1) + 1;
				int k = SR.rand.nextInt(50) + 150; // 150-199
				int N = k + 2;
				int l = ForestMetadata.getTupleBits(i);

				PreData.xot_pi = (List<Integer>[][]) new List[2][levels];
				PreData.xot_pi_ivs = (List<Integer>[][]) new List[2][levels];
				PreData.xot_r = new BigInteger[2][levels][N];
				PreData.ssxot_delta = new BigInteger[levels][k];

				for (int id = 0; id < 2; id++) {
					PreData.xot_pi[id][i] = new ArrayList<Integer>();
					for (int o = 0; o < N; o++)
						PreData.xot_pi[id][i].add(o);
					Collections.shuffle(PreData.xot_pi[id][i], SR.rand);
					PreData.xot_pi_ivs[id][i] = Util
							.getInversePermutation(PreData.xot_pi[id][i]);
					for (int o = 0; o < N; o++) {
						PreData.xot_r[id][i][o] = new BigInteger(l, SR.rand);
					}
				}
				for (int o = 0; o < k; o++) {
					PreData.ssxot_delta[i][o] = new BigInteger(l, SR.rand);
				}

				BigInteger[] sC_m = new BigInteger[N];
				BigInteger[] sE_m = new BigInteger[N];
				Integer[] ii = new Integer[k];

				for (int o = 0; o < N; o++) {
					sC_m[o] = new BigInteger(l, SR.rand);
					sE_m[o] = new BigInteger(l, SR.rand);
				}
				List<Integer> tmp = new ArrayList<Integer>();
				for (int o = 0; o < k; o++) {
					tmp.add(o);
				}
				Collections.shuffle(tmp, SR.rand);
				tmp.toArray(ii);

				con1.write(i);
				con1.write(N);
				con1.write(k);
				con1.write(l);
				con1.write(PreData.xot_pi[0][i]);
				con1.write(PreData.xot_r[0][i]);
				con1.write(sC_m);

				con2.write(i);
				con2.write(N);
				con2.write(k);
				con2.write(l);
				con2.write(PreData.xot_pi[1][i]);
				con2.write(PreData.xot_r[1][i]);
				con2.write(sE_m);

				executeDebbie(con1, con2, timing, i, N, k, l, ii);

				con2.write(sC_m);
				con2.write(ii);
			} else if (party == Party.Charlie) {
				PreData.xot_pi = (List<Integer>[][]) new List[2][levels];
				PreData.xot_r = new BigInteger[2][levels][];

				int i = con1.readInt();
				int N = con1.readInt();
				int k = con1.readInt();
				int l = con1.readInt();
				PreData.xot_pi[0][i] = con1.readListInt();
				PreData.xot_r[0][i] = con1.readBigIntegerArray();
				BigInteger[] sC_m = con1.readBigIntegerArray();

				BigInteger[] b = executeCharlie(con1, con2, timing, i, N, k, l,
						sC_m);

				con2.write(b);
			}

		}

		System.out.println("#####  Testing SSXOT Finished  #####");
	}
}
