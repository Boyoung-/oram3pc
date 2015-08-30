package sprout.oram.operations;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import YaoGC.Circuit;
import YaoGC.F2ET_Wplus2_Wplus2;
import YaoGC.F2FT_2Wplus2_Wplus2;
import YaoGC.State;
import YaoGC.Wire;
import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.oram.TID;
import sprout.oram.Tree;
import sprout.util.Timing;
import sprout.util.Util;

public class Precomputation extends TreeOperation<Object, Object> {

	public Precomputation(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object executeCharlieSubTree(Communication debbie,
			Communication eddie, Tree OT, Object unused, Timing localTiming) {
		// Access
		timing.stopwatch[PID.access][TID.offline].start();
		PreData.access_Li = new BigInteger[levels];
		timing.stopwatch[PID.access][TID.offline].stop();

		// Reshuffle
		timing.stopwatch[PID.reshuf][TID.offline_read].start();
		byte[][] reshuffle_s1 = debbie.readDoubleByteArray();
		timing.stopwatch[PID.reshuf][TID.offline_read].stop();

		timing.stopwatch[PID.reshuf][TID.offline].start();
		PreData.reshuffle_p = new BigInteger[levels];
		PreData.reshuffle_a_p = new BigInteger[levels][];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			PRG G = new PRG(pathBuckets * bucketBits);
			timing.stopwatch[PID.aes_prg][TID.offline].start();
			PreData.reshuffle_p[i] = new BigInteger(1,
					G.compute(reshuffle_s1[i]));
			timing.stopwatch[PID.aes_prg][TID.offline].stop();
		}
		timing.stopwatch[PID.reshuf][TID.offline].stop();

		timing.stopwatch[PID.reshuf][TID.offline_read].start();
		for (int index = 0; index < levels; index++) {
			PreData.reshuffle_a_p[index] = debbie.readBigIntegerArray();
		}
		timing.stopwatch[PID.reshuf][TID.offline_read].stop();

		// PPT
		timing.stopwatch[PID.ppt][TID.offline].start();
		PreData.ppt_sC_Li_p = new BigInteger[levels];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			PreData.ppt_sC_Li_p[i] = new BigInteger(lBits, SR.rand);
		}

		PreData.ppt_r = new BigInteger[levels][];
		PreData.ppt_alpha = new int[levels];
		timing.stopwatch[PID.ppt][TID.offline].stop();

		timing.stopwatch[PID.ppt][TID.offline_read].start();
		for (int index = 0; index < levels; index++) {
			PreData.ppt_alpha[index] = debbie.readInt();
			PreData.ppt_r[index] = debbie.readBigIntegerArray();
		}
		timing.stopwatch[PID.ppt][TID.offline_read].stop();

		// XOT
		timing.stopwatch[PID.ssxot][TID.offline].start();
		PreData.xot_pi = (List<Integer>[][]) new List[1][levels];
		PreData.xot_r = new BigInteger[1][levels][];
		timing.stopwatch[PID.ssxot][TID.offline].stop();

		timing.stopwatch[PID.ssxot][TID.offline_read].start();
		for (int index = 0; index < levels; index++) {
			PreData.xot_pi[0][index] = debbie.readListInt();
			PreData.xot_r[0][index] = debbie.readBigIntegerArray();
		}
		timing.stopwatch[PID.ssxot][TID.offline_read].stop();

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object executeDebbieSubTree(Communication charlie,
			Communication eddie, Tree OT, Object unused, Timing localTiming) {
		// SSCOT
		timing.stopwatch[PID.sscot][TID.offline].start();
		PreData.sscot_k = new byte[levels][16];
		PreData.sscot_k_p = new byte[levels][16];
		PreData.sscot_r = new BigInteger[levels][];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			SR.rand.nextBytes(PreData.sscot_k[i]);
			SR.rand.nextBytes(PreData.sscot_k_p[i]);
			PreData.sscot_r[i] = new BigInteger[pathTuples];
			for (int j = 0; j < pathTuples; j++) {
				PreData.sscot_r[i][j] = new BigInteger(SR.kBits, SR.rand);
			}
		}
		timing.stopwatch[PID.sscot][TID.offline].stop();

		timing.stopwatch[PID.sscot][TID.offline_write].start();
		for (int index = 0; index < levels; index++) {
			eddie.write(PreData.sscot_r[index]);			
		}
		eddie.write(PreData.sscot_k);
		eddie.write(PreData.sscot_k_p);
		timing.stopwatch[PID.sscot][TID.offline_write].stop();

		// SSIOT
		timing.stopwatch[PID.ssiot][TID.offline].start();
		PreData.ssiot_k = new byte[levels][16];
		PreData.ssiot_k_p = new byte[levels][16];
		PreData.ssiot_r = new BigInteger[levels];

		for (int index = 0; index < levels; index++) {
			SR.rand.nextBytes(PreData.ssiot_k[index]);
			SR.rand.nextBytes(PreData.ssiot_k_p[index]);
			PreData.ssiot_r[index] = new BigInteger(SR.kBits, SR.rand);
		}
		timing.stopwatch[PID.ssiot][TID.offline].stop();

		timing.stopwatch[PID.ssiot][TID.offline_write].start();
		eddie.write(PreData.ssiot_k);
		eddie.write(PreData.ssiot_k_p);
		eddie.write(PreData.ssiot_r);
		timing.stopwatch[PID.ssiot][TID.offline_write].stop();

		// Access
		timing.stopwatch[PID.access][TID.offline].start();
		PreData.access_Li = new BigInteger[levels];
		PreData.access_sigma = (List<Integer>[]) new List[levels];
		PreData.access_p = new BigInteger[levels][];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			PreData.access_sigma[i] = new ArrayList<Integer>();
			for (int j = 0; j < pathBuckets; j++)
				PreData.access_sigma[i].add(j);
			Collections.shuffle(PreData.access_sigma[i], SR.rand);
			
			PreData.access_p[i] = new BigInteger[pathBuckets];
			for (int j=0; j<pathBuckets; j++)
				PreData.access_p[i][j] = new BigInteger(bucketBits, SR.rand);
		}
		timing.stopwatch[PID.access][TID.offline].stop();

		timing.stopwatch[PID.access][TID.offline_write].start();
		for (int index = 0; index < levels; index++) {
			eddie.write(PreData.access_sigma[index]);
			eddie.write(PreData.access_p[index]);
		}
		timing.stopwatch[PID.access][TID.offline_write].stop();

		// Reshuffle
		timing.stopwatch[PID.reshuf][TID.offline].start();
		byte[][] reshuffle_s1 = new byte[levels][16];
		byte[][] reshuffle_s2 = new byte[levels][16];
		PreData.reshuffle_p = new BigInteger[levels];
		PreData.reshuffle_r = new BigInteger[levels];
		PreData.reshuffle_a_p = new BigInteger[levels][];
		PreData.reshuffle_pi = (List<Integer>[]) new List[levels];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			PreData.reshuffle_pi[i] = Util
					.getInversePermutation(PreData.access_sigma[i]);
			SR.rand.nextBytes(reshuffle_s1[i]);
			SR.rand.nextBytes(reshuffle_s2[i]);
			PRG G = new PRG(pathBuckets * bucketBits);
			timing.stopwatch[PID.aes_prg][TID.offline].start();
			PreData.reshuffle_p[i] = new BigInteger(1,
					G.compute(reshuffle_s1[i]));
			PreData.reshuffle_r[i] = new BigInteger(1,
					G.compute(reshuffle_s2[i]));
			timing.stopwatch[PID.aes_prg][TID.offline].stop();
			BigInteger a_all = PreData.reshuffle_p[i]
					.xor(PreData.reshuffle_r[i]);
			PreData.reshuffle_a_p[i] = new BigInteger[pathBuckets];
			BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(
					BigInteger.ONE);
			BigInteger tmp = a_all;
			for (int j = pathBuckets - 1; j >= 0; j--) {
				PreData.reshuffle_a_p[i][j] = tmp.and(helper);
				tmp = tmp.shiftRight(bucketBits);
			}
			PreData.reshuffle_a_p[i] = Util.permute(PreData.reshuffle_a_p[i],
					PreData.reshuffle_pi[i]);
		}
		timing.stopwatch[PID.reshuf][TID.offline].stop();

		timing.stopwatch[PID.reshuf][TID.offline_write].start();
		eddie.write(reshuffle_s2);
		charlie.write(reshuffle_s1);
		for (int index = 0; index < levels; index++) {
			charlie.write(PreData.reshuffle_a_p[index]);
		}
		timing.stopwatch[PID.reshuf][TID.offline_write].stop();

		// PPT
		timing.stopwatch[PID.ppt][TID.offline_read].start();
		PreData.ppt_sE_Li_p = eddie.readBigIntegerArray();
		timing.stopwatch[PID.ppt][TID.offline_read].stop();

		timing.stopwatch[PID.ppt][TID.offline].start();
		PreData.ppt_r = new BigInteger[levels][twotaupow];
		PreData.ppt_r_p = new BigInteger[levels][twotaupow];
		PreData.ppt_alpha = new int[levels];

		for (int index = 0; index < levels-1; index++) {
			loadTreeSpecificParameters(index);
			for (int j = 0; j < twotaupow; j++) {
				PreData.ppt_r[i][j] = new BigInteger(d_ip1, SR.rand);
				PreData.ppt_r_p[i][j] = PreData.ppt_r[i][j];
			}
			PreData.ppt_alpha[i] = SR.rand.nextInt(twotaupow);
			PreData.ppt_r_p[i][PreData.ppt_alpha[i]] = PreData.ppt_r[i][PreData.ppt_alpha[i]]
					.xor(PreData.ppt_sE_Li_p[i+1]);
		}
		timing.stopwatch[PID.ppt][TID.offline].stop();

		timing.stopwatch[PID.ppt][TID.offline_write].start();
		for (int index = 0; index < levels; index++) {
			charlie.write(PreData.ppt_alpha[index]);
			charlie.write(PreData.ppt_r[index]);
			eddie.write(PreData.ppt_r_p[index]);
		}
		timing.stopwatch[PID.ppt][TID.offline_write].stop();

		// XOT
		timing.stopwatch[PID.ssxot][TID.offline].start();
		PreData.xot_pi = (List<Integer>[][]) new List[2][levels];
		PreData.xot_pi_ivs = (List<Integer>[][]) new List[2][levels];
		PreData.xot_r = new BigInteger[2][levels][];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			int l = tupleBits;
			int k = w * pathBuckets;
			int N = k + 2;

			for (int id = 0; id < 2; id++) {
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
			}
		}
		timing.stopwatch[PID.ssxot][TID.offline].stop();

		timing.stopwatch[PID.ssxot][TID.offline_write].start();
		for (int index = 0; index < levels; index++) {
			charlie.write(PreData.xot_pi[0][index]);
			charlie.write(PreData.xot_r[0][index]);
			eddie.write(PreData.xot_pi[1][index]);
			eddie.write(PreData.xot_r[1][index]);
		}
		timing.stopwatch[PID.ssxot][TID.offline_write].stop();

		// SSXOT
		timing.stopwatch[PID.ssxot][TID.offline].start();
		PreData.ssxot_delta = new BigInteger[levels][];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			int l = tupleBits;
			int k = w * pathBuckets;

			PreData.ssxot_delta[i] = new BigInteger[k];
			for (int o = 0; o < k; o++) {
				PreData.ssxot_delta[i][o] = new BigInteger(l, SR.rand);
			}
		}
		timing.stopwatch[PID.ssxot][TID.offline].stop();

		// GCF
		timing.stopwatch[PID.gcf][TID.offline].start();
		PreData.gcf_gc_D = new Circuit[levels][];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			PreData.gcf_gc_D[i] = new Circuit[d_i + 1];
			for (int j = 0; j < d_i + 1; j++) {
				int ww = (j != d_i) ? w : (w * expen);
				Circuit.isForGarbling = false;
				Circuit.timing = timing;
				Circuit.setSender(eddie);
				PreData.gcf_gc_D[i][j] = (j != d_i) ? new F2FT_2Wplus2_Wplus2(
						ww, 1, 1) : new F2ET_Wplus2_Wplus2(ww, 1, 1);
				try {
					PreData.gcf_gc_D[i][j].build();
				} catch (Exception e) {
					e.printStackTrace();
				}
				for (int k = 0; k < PreData.gcf_gc_D[i][j].outputWires.length; k++)
					// TODO: not a good way; should define a function
					PreData.gcf_gc_D[i][j].outputWires[k].outBitEncPair = new BigInteger[2];

				PreData.gcf_gc_D[i][j].receiveTruthTables();
			}
		}
		timing.stopwatch[PID.gcf][TID.offline].stop();

		// Eviction
		timing.stopwatch[PID.evict][TID.offline].start();
		PreData.evict_upxi = new BigInteger[levels][];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			PreData.evict_upxi[i] = new BigInteger[pathBuckets];
			for (int j=0; j<pathBuckets; j++) {
				PreData.evict_upxi[i][j] = new BigInteger(bucketBits, SR.rand);
			}
		}
		timing.stopwatch[PID.evict][TID.offline].stop();

		timing.stopwatch[PID.evict][TID.offline_write].start();
		for (int index = 0; index < levels; index++) {
			eddie.write(PreData.evict_upxi[index]);
		}
		timing.stopwatch[PID.evict][TID.offline_write].stop();

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object executeEddieSubTree(Communication charlie,
			Communication debbie, Tree OT, Object unused, Timing localTiming) {
		// SSCOT
		timing.stopwatch[PID.sscot][TID.offline].start();
		PreData.sscot_r = new BigInteger[levels][];
		timing.stopwatch[PID.sscot][TID.offline].stop();

		timing.stopwatch[PID.sscot][TID.offline_read].start();
		for (int index = 0; index < levels; index++) {
			PreData.sscot_r[index] = debbie.readBigIntegerArray();
		}
		PreData.sscot_k = debbie.readDoubleByteArray();
		PreData.sscot_k_p = debbie.readDoubleByteArray();
		timing.stopwatch[PID.sscot][TID.offline_read].stop();

		// SSIOT
		timing.stopwatch[PID.ssiot][TID.offline_read].start();
		PreData.ssiot_k = debbie.readDoubleByteArray();
		PreData.ssiot_k_p = debbie.readDoubleByteArray();
		PreData.ssiot_r = debbie.readBigIntegerArray();
		timing.stopwatch[PID.ssiot][TID.offline_read].stop();

		// Access
		timing.stopwatch[PID.access][TID.offline].start();
		PreData.access_Li = new BigInteger[levels];
		PreData.access_sigma = (List<Integer>[]) new List[levels];
		PreData.access_p = new BigInteger[levels][];
		timing.stopwatch[PID.access][TID.offline].stop();

		timing.stopwatch[PID.access][TID.offline_read].start();
		for (int index = 0; index < levels; index++) {
			PreData.access_sigma[index] = debbie.readListInt();
			PreData.access_p[index] = debbie.readBigIntegerArray();
		}
		timing.stopwatch[PID.access][TID.offline_read].stop();

		// Reshuffle
		timing.stopwatch[PID.reshuf][TID.offline_read].start();
		byte[][] reshuffle_s2 = debbie.readDoubleByteArray();
		timing.stopwatch[PID.reshuf][TID.offline_read].stop();

		timing.stopwatch[PID.reshuf][TID.offline].start();
		PreData.reshuffle_r = new BigInteger[levels];
		PreData.reshuffle_pi = (List<Integer>[]) new List[levels];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			PreData.reshuffle_pi[i] = Util
					.getInversePermutation(PreData.access_sigma[i]);
			PRG G = new PRG(pathBuckets * bucketBits);
			timing.stopwatch[PID.aes_prg][TID.offline].start();
			PreData.reshuffle_r[i] = new BigInteger(1,
					G.compute(reshuffle_s2[i]));
			timing.stopwatch[PID.aes_prg][TID.offline].stop();
		}
		timing.stopwatch[PID.reshuf][TID.offline].stop();

		// PPT
		timing.stopwatch[PID.ppt][TID.offline].start();
		PreData.ppt_sE_Li_p = new BigInteger[levels];
		PreData.ppt_r_p = new BigInteger[levels][];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			PreData.ppt_sE_Li_p[i] = new BigInteger(lBits, SR.rand);
		}
		timing.stopwatch[PID.ppt][TID.offline].stop();

		timing.stopwatch[PID.ppt][TID.offline_write].start();
		debbie.write(PreData.ppt_sE_Li_p);
		for (int index = 0; index < levels; index++) {
			PreData.ppt_r_p[index] = debbie.readBigIntegerArray();
		}
		timing.stopwatch[PID.ppt][TID.offline_write].stop();

		// XOT
		timing.stopwatch[PID.ssxot][TID.offline].start();
		PreData.xot_pi = (List<Integer>[][]) new List[1][levels];
		PreData.xot_r = new BigInteger[1][levels][];
		timing.stopwatch[PID.ssxot][TID.offline].stop();

		timing.stopwatch[PID.ssxot][TID.offline_read].start();
		for (int index = 0; index < levels; index++) {
			PreData.xot_pi[0][index] = debbie.readListInt();
			PreData.xot_r[0][index] = debbie.readBigIntegerArray();
		}
		timing.stopwatch[PID.ssxot][TID.offline_read].stop();

		// GCF
		timing.stopwatch[PID.gcf][TID.offline].start();
		PreData.gcf_gc_E = new Circuit[levels][];
		PreData.gcf_lbs = new BigInteger[levels][][][];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			PreData.gcf_gc_E[i] = new Circuit[d_i + 1];
			PreData.gcf_lbs[i] = new BigInteger[d_i + 1][][];
			for (int j = 0; j < d_i + 1; j++) {
				int ww = (j != d_i) ? w : (w * expen);
				int tmp1 = SR.rand.nextInt(ww);
				int tmp2 = SR.rand.nextInt(ww);
				int s1 = Math.min(tmp1, tmp2);
				int s2 = Math.max(tmp1, tmp2);
				Circuit.isForGarbling = true;
				Circuit.timing = timing;
				Circuit.setReceiver(debbie);
				PreData.gcf_gc_E[i][j] = (j != d_i) ? new F2FT_2Wplus2_Wplus2(
						ww, s1, s2) : new F2ET_Wplus2_Wplus2(ww, s1, s2);
				try {
					PreData.gcf_gc_E[i][j].build();
				} catch (Exception e) {
					e.printStackTrace();
				}
				for (int k = 0; k < PreData.gcf_gc_E[i][j].outputWires.length; k++)
					// TODO: not a good way; should define a function
					PreData.gcf_gc_E[i][j].outputWires[k].outBitEncPair = new BigInteger[2];

				int n = (j != d_i) ? (w * 2 + 2) : (w * expen + 2);
				PreData.gcf_lbs[i][j] = new BigInteger[n][2];
				BigInteger[] K_E = new BigInteger[n];
				for (int k = 0; k < n; k++) {
					BigInteger glb0 = new BigInteger(Wire.labelBitLength,
							SR.rand);
					BigInteger glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
					PreData.gcf_lbs[i][j][k][0] = glb0;
					PreData.gcf_lbs[i][j][k][1] = glb1;
					K_E[k] = PreData.gcf_lbs[i][j][k][0];
				}

				State in_E = State.fromLabels(K_E);
				PreData.gcf_gc_E[i][j].sendTruthTables(in_E);
			}
		}
		timing.stopwatch[PID.gcf][TID.offline].stop();

		// Eviction
		timing.stopwatch[PID.evict][TID.offline].start();
		PreData.evict_upxi = new BigInteger[levels][];
		timing.stopwatch[PID.evict][TID.offline].stop();

		timing.stopwatch[PID.evict][TID.offline_read].start();
		for (int index = 0; index < levels; index++) {
			PreData.evict_upxi[index] = debbie.readBigIntegerArray();
		}
		timing.stopwatch[PID.evict][TID.offline_read].stop();

		return null;
	}

	// for testing correctness
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing PreComputation  #####");

		if (party == Party.Eddie) {
			System.out.println("Eddie starts");
			executeEddieSubTree(con1, con2, null, null, null);
			System.out.println("Eddie ends");
		} else if (party == Party.Debbie) {
			System.out.println("Debbie starts");
			executeDebbieSubTree(con1, con2, null, null, null);
			System.out.println("Debbie ends");
		} else if (party == Party.Charlie) {
			System.out.println("Charlie starts");
			executeCharlieSubTree(con1, con2, null, null, null);
			System.out.println("Charlie ends");
		}

		System.out.println("#####  Testing PreComputation Finished  #####");
	}
}
