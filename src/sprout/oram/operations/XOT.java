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

	public BigInteger[] executeCharlie(Communication debbie, Communication eddie, int id, int i, int N, int k, int l) {
		// protocol
		// step 1
		timing.stopwatch[PID.xot][TID.online_read].start();
		BigInteger[] a = eddie.readBigIntegerArray();

		
		// step 2
		Integer[] j = debbie.readIntegerArray();
		BigInteger[] p = debbie.readBigIntegerArray();
		timing.stopwatch[PID.xot][TID.online_read].stop();

		
		// step 3
		timing.stopwatch[PID.xot][TID.online].start();
		BigInteger[] z = new BigInteger[k];
		for (int o = 0; o < k; o++)
			z[o] = a[j[o]].xor(p[o]);
		timing.stopwatch[PID.xot][TID.online].stop();

		
		return z;
	}

	public void executeDebbie(Communication charlie, Communication eddie, int id, int i, int N, int k, int l, Integer[] ii, BigInteger[] delta) {
		// protocol
		// step 2
		timing.stopwatch[PID.xot][TID.online].start();
		Integer[] j = new Integer[k];
		BigInteger[] p = new BigInteger[k];
		for (int o = 0; o < k; o++) {
			j[o] = PreData.xot_pi_ivs[id][i].get(ii[o]);
			p[o] = PreData.xot_r[id][i][j[o]].xor(delta[o]);
		}
		timing.stopwatch[PID.xot][TID.online].stop();

		timing.stopwatch[PID.xot][TID.online_write].start();
		charlie.write(j);
		charlie.write(p);
		timing.stopwatch[PID.xot][TID.online_write].stop();
	}

	public void executeEddie(Communication charlie, Communication debbie, int id, int i, int N, int k, int l, BigInteger[] m) {
		// protocol
		// step 1
		timing.stopwatch[PID.xot][TID.online].start();
		BigInteger[] a = new BigInteger[N];
		for (int o = 0; o < N; o++)
			a[o] = m[PreData.xot_pi[0][i].get(o)]
					.xor(PreData.xot_r[0][i][o]);
		timing.stopwatch[PID.xot][TID.online].stop();

		timing.stopwatch[PID.xot][TID.online_write].start();
		charlie.write(a);
		timing.stopwatch[PID.xot][TID.online_write].stop();
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
			
			for (int o=0; o<N; o++) {
				m[o] = new BigInteger(l, SR.rand);
			}
			List<Integer> tmp = new ArrayList<Integer>();
			for (int o=0; o<k; o++) {
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

			executeEddie(con1, con2, id, i, N, k, l, m);

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

			executeDebbie(con1, con2, id, i, N, k, l, ii, delta);
		} else if (party == Party.Charlie) {
			int i = con2.readInt();
			int N = con2.readInt();
			int k = con2.readInt();
			int l = con2.readInt();

			BigInteger[] z = executeCharlie(con1, con2, id, i, N, k, l);

			con2.write(z);
		}

		System.out.println("#####  Testing XOT Finished  #####");
	}
}
