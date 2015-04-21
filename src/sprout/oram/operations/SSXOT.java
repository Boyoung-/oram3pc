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
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.util.Util;

// TODO: Possible parallelization opportunity in running each IOT
public class SSXOT extends Operation {

	public SSXOT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public BigInteger[] executeCharlie(Communication debbie, Communication eddie, int i, int N, int k, int l, BigInteger[] sC_m) {
		XOT xot = new XOT(debbie, eddie);
		
		// protocol
		// step 1
		xot.executeEddie(eddie, debbie, 0, i, N, k, l, sC_m);
		
		
		// step 2
		BigInteger[] b = xot.executeCharlie(debbie, eddie, 1, i, N, k, l);
		
		
		return b;
	}

	public void executeDebbie(Communication charlie, Communication eddie, int i, int N, int k, int l, Integer[] ii) {
		XOT xot = new XOT(charlie, eddie);
		
		// protocol
		// step 1
		xot.executeDebbie(eddie, charlie, 0, i, N, k, l, ii, PreData.ssxot_delta[i]);

		
		// step 2
		xot.executeDebbie(charlie, eddie, 1, i, N, k, l, ii, PreData.ssxot_delta[i]);
	}

	public BigInteger[] executeEddie(Communication charlie, Communication debbie, int i, int N, int k, int l, BigInteger[] sE_m) {
		XOT xot = new XOT(charlie, debbie);
		
		// protocol
		// step 1
		BigInteger[] a = xot.executeCharlie(debbie, charlie, 0, i, N, k, l);

		
		// step 2
		xot.executeEddie(charlie, debbie, 1, i, N, k, l, sE_m);
		
		
		return a;
	}

	// for testing correctness
	@SuppressWarnings("unchecked")
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing SSXOT  #####");

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
			
			BigInteger[] a = executeEddie(con1, con2, i, N, k, l, sE_m);
			
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
			
			executeDebbie(con1, con2, i, N, k, l, ii);
			
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
			
			BigInteger[] b = executeCharlie(con1, con2, i, N, k, l, sC_m);

			con2.write(b);
		}

		System.out.println("#####  Testing SSXOT Finished  #####");
	}
}
