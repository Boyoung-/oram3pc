package sprout.oram.operations;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.oram.Tree;
import sprout.util.Timing;

public class Precomputation extends TreeOperation<Object, Object> {

	public Precomputation(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public Object executeCharlieSubTree(Communication debbie,
			Communication eddie, Tree OT, Object unused, Timing localTiming) {

		/*
		// PPT
		PreData.ppt_sC_Li_p = new BigInteger[levels];

		timing.stopwatch[PID.ppt][TID.offline].start();
		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);

			PreData.ppt_sC_Li_p[i] = new BigInteger(lBits, SR.rand);
		}
		timing.stopwatch[PID.ppt][TID.offline].stop();

		// Reshuffle
		PreData.reshuffle_p1 = new byte[levels][];

		timing.stopwatch[PID.reshuffle][TID.offline_read].start();
		PreData.reshuffle_s1 = debbie.readDoubleByteArray();
		timing.stopwatch[PID.reshuffle][TID.offline_read].stop();

		timing.stopwatch[PID.reshuffle][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			PRG G = new PRG(pathBuckets * bucketBits);
			PreData.reshuffle_p1[i] = G.compute(PreData.reshuffle_s1[i]);
		}
		timing.stopwatch[PID.reshuffle][TID.offline].stop();

		// IOT
		PreData.iot_pi = (List<Integer>[][]) new List[1][levels];
		PreData.iot_r = new BigInteger[1][levels][];
		PreData.iot_s = new byte[1][][];

		timing.stopwatch[PID.iot][TID.offline_read].start();
		PreData.iot_s[0] = debbie.readDoubleByteArray();
		for (int index = 0; index <= h; index++)
			PreData.iot_pi[0][index] = debbie.readListInt();
		timing.stopwatch[PID.iot][TID.offline_read].stop();

		timing.stopwatch[PID.iot][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			int l = tupleBits;
			int k = w * pathBuckets;
			int N = k + 2;
			PRG G = new PRG(N * l);
			BigInteger r_all = new BigInteger(1, G.compute(PreData.iot_s[0][i]));
			PreData.iot_r[0][i] = new BigInteger[N];
			BigInteger helper = BigInteger.ONE.shiftLeft(l).subtract(
					BigInteger.ONE);
			BigInteger tmp = r_all;
			for (int o = N - 1; o >= 0; o--) {
				PreData.iot_r[0][i][o] = tmp.and(helper);
				tmp = tmp.shiftRight(l);
			}
		}
		timing.stopwatch[PID.iot][TID.offline].stop();

		// Encrypt
		PreData.encrypt_c = new BigInteger[levels][];

		timing.stopwatch[PID.encrypt][TID.offline_read].start();
		for (int index = 0; index <= h; index++)
			PreData.encrypt_c[index] = debbie.readBigIntegerArray();
		timing.stopwatch[PID.encrypt][TID.offline_read].stop();
		

		//debbie.countBandwidth = false;
		//eddie.countBandwidth = false;
		//debbie.bandwidth[PID.pre].stop();
		//eddie.bandwidth[PID.pre].stop();
		 */

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object executeDebbieSubTree(Communication charlie,
			Communication eddie, Tree OT, Object unused, Timing localTiming) {
		// SSCOT
		PreData.sscot_k = new byte[levels][16];
		PreData.sscot_k_p = new byte[levels][16];
		PreData.sscot_r = new BigInteger[levels][];
		
		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			SR.rand.nextBytes(PreData.sscot_k[i]);
			SR.rand.nextBytes(PreData.sscot_k_p[i]);
			PreData.sscot_r[i] = new BigInteger[pathTuples];
			for (int j=0; j<pathTuples; j++) {
				PreData.sscot_r[i][j] = new BigInteger(SR.kBits, SR.rand);
			}
			
			eddie.write(PreData.sscot_r[i]);
		}
		
		eddie.write(PreData.sscot_k);
		eddie.write(PreData.sscot_k_p);
		
		
		// SSIOT
		PreData.ssiot_k = new byte[levels][16];
		PreData.ssiot_k_p = new byte[levels][16];
		PreData.ssiot_r = new BigInteger[levels];

		for (int index = 0; index < levels; index++) {
			SR.rand.nextBytes(PreData.ssiot_k[index]);
			SR.rand.nextBytes(PreData.ssiot_k_p[index]);
			PreData.ssiot_r[index] = new BigInteger(SR.kBits, SR.rand);
		}

		eddie.write(PreData.ssiot_k);
		eddie.write(PreData.ssiot_k_p);
		eddie.write(PreData.ssiot_r);
		
		
		// Access
		PreData.access_sigma = (List<Integer>[]) new List[levels];
		PreData.access_p = new BigInteger[levels];

		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			PreData.access_sigma[i] = new ArrayList<Integer>();
			for (int j = 0; j < pathBuckets; j++)
				PreData.access_sigma[i].add(j);
			Collections.shuffle(PreData.access_sigma[i], SR.rand);
			PreData.access_p[i] = new BigInteger(pathTuples * tupleBits, SR.rand);
		}

		for (int index = 0; index < levels; index++) {
			eddie.write(PreData.access_sigma[index]);
		}
		eddie.write(PreData.access_p);
		
		
		
		/*
		

		// PET
		PreData.pet_alpha = new BigInteger[levels][];
		PreData.pet_k = new byte[levels][16];

		//timing.stopwatch[PID.pet][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			PreData.pet_alpha[i] = new BigInteger[pathTuples];

			for (int j = 0; j < pathTuples; j++) {
				PreData.pet_alpha[i][j] = new BigInteger(1+nBits, SR.rand);
			}
			
			SR.rand.nextBytes(PreData.pet_k[i]);
		}
		//timing.stopwatch[PID.pet][TID.offline].stop();

		//timing.stopwatch[PID.pet][TID.offline_write].start();
		for (int index = 0; index <= h; index++) {
			eddie.write(PreData.pet_alpha[index]);
		}
		eddie.write(PreData.pet_k);

		// AOT
		PreData.aot_k = new byte[levels][16];
		for (int index = 0; index <= h; index++) {
			SR.rand.nextBytes(PreData.aot_k[index]);
		}
		eddie.write(PreData.aot_k);
		
		// AOTSS
		PreData.aotss_k = new byte[levels][16];
		for (int index = 0; index <= h; index++) {
			SR.rand.nextBytes(PreData.aotss_k[index]);
		}
		eddie.write(PreData.aotss_k);
		*/
		
		
		/*
		// Reshuffle
		PreData.reshuffle_s1 = new byte[levels][];
		PreData.reshuffle_s2 = new byte[levels][];
		PreData.reshuffle_p1 = new byte[levels][];
		PreData.reshuffle_p2 = new byte[levels][];
		PreData.reshuffle_a = new BigInteger[levels][];
		PreData.reshuffle_pi = (List<Integer>[]) new List[levels];

		timing.stopwatch[PID.reshuffle][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			PreData.reshuffle_s1[i] = SR.rand.generateSeed(16);
			PreData.reshuffle_s2[i] = SR.rand.generateSeed(16);
			PRG G = new PRG(pathBuckets * bucketBits);
			PreData.reshuffle_p1[i] = G.compute(PreData.reshuffle_s1[i]);
			PreData.reshuffle_p2[i] = G.compute(PreData.reshuffle_s2[i]);
			BigInteger a_all = new BigInteger(1, PreData.reshuffle_p1[i])
					.xor(new BigInteger(1, PreData.reshuffle_p2[i]));
			PreData.reshuffle_a[i] = new BigInteger[pathBuckets];
			BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(
					BigInteger.ONE);
			BigInteger tmp = a_all;
			for (int j = pathBuckets - 1; j >= 0; j--) {
				PreData.reshuffle_a[i][j] = tmp.and(helper);
				tmp = tmp.shiftRight(bucketBits);
			}
			PreData.reshuffle_pi[i] = Util
					.getInversePermutation(PreData.access_sigma[i]);
		}
		timing.stopwatch[PID.reshuffle][TID.offline].stop();

		timing.stopwatch[PID.reshuffle][TID.offline_write].start();
		charlie.write(PreData.reshuffle_s1);
		eddie.write(PreData.reshuffle_s2);
		timing.stopwatch[PID.reshuffle][TID.offline_write].stop();

		// GCF
		PreData.gcf_gc_D = new Circuit[levels][];

		timing.stopwatch[PID.gcf][TID.offline].start();
		for (int index = 0; index <= h; index++) {
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
				PreData.gcf_gc_D[i][j].passTruthTables();
			}
		}
		timing.stopwatch[PID.gcf][TID.offline].stop();

		// IOT
		PreData.iot_pi = (List<Integer>[][]) new List[2][levels];
		PreData.iot_pi_ivs = (List<Integer>[][]) new List[2][levels];
		PreData.iot_r = new BigInteger[2][levels][];
		PreData.iot_s = new byte[2][levels][];

		timing.stopwatch[PID.iot][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			int l = tupleBits;
			int k = w * pathBuckets;
			int N = k + 2;
			PRG G = new PRG(N * l);
			for (int id = 0; id < 2; id++) {
				PreData.iot_pi[id][i] = new ArrayList<Integer>(); // TODO: make
																	// a util
																	// function
																	// (convert
																	// to
																	// Integer[])
				for (int o = 0; o < N; o++)
					PreData.iot_pi[id][i].add(o);
				Collections.shuffle(PreData.iot_pi[id][i], SR.rand);
				PreData.iot_pi_ivs[id][i] = Util
						.getInversePermutation(PreData.iot_pi[id][i]);
				PreData.iot_s[id][i] = SR.rand.generateSeed(16);
				BigInteger r_all = new BigInteger(1,
						G.compute(PreData.iot_s[id][i]));
				PreData.iot_r[id][i] = new BigInteger[N];
				BigInteger helper = BigInteger.ONE.shiftLeft(l).subtract(
						BigInteger.ONE);
				BigInteger tmp = r_all;
				for (int o = N - 1; o >= 0; o--) {
					PreData.iot_r[id][i][o] = tmp.and(helper);
					tmp = tmp.shiftRight(l);
				}
			}
		}
		timing.stopwatch[PID.iot][TID.offline].stop();

		timing.stopwatch[PID.iot][TID.offline_write].start();
		eddie.write(PreData.iot_s[0]);
		for (int index = 0; index <= h; index++)
			eddie.write(PreData.iot_pi[0][index]);
		charlie.write(PreData.iot_s[1]);
		for (int index = 0; index <= h; index++)
			charlie.write(PreData.iot_pi[1][index]);
		timing.stopwatch[PID.iot][TID.offline_write].stop();

		// Encrypt
		PreData.encrypt_s = new byte[levels][];
		PreData.encrypt_x = new ECPoint[levels][];
		PreData.encrypt_c = new BigInteger[levels][];

		timing.stopwatch[PID.encrypt][TID.offline].start();
		OPRF oprf = OPRFHelper.getOPRF(false);
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			PRG G1 = new PRG(bucketBits * pathBuckets);
			PRG G2 = new PRG(bucketBits);
			PreData.encrypt_x[i] = new ECPoint[pathBuckets];
			ECPoint[] v = new ECPoint[pathBuckets];
			BigInteger r;
			BigInteger[] a = new BigInteger[pathBuckets];
			BigInteger[] b = new BigInteger[pathBuckets];
			PreData.encrypt_c[i] = new BigInteger[pathBuckets];

			PreData.encrypt_s[i] = SR.rand.generateSeed(16);
			for (int j = 0; j < pathBuckets; j++) {
				r = oprf.randomExponent();
				PreData.encrypt_x[i][j] = oprf.getG().multiply(r);
				v[j] = oprf.getY().multiply(r);
			}
			BigInteger a_all = new BigInteger(1,
					G1.compute(PreData.encrypt_s[i]));
			BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(
					BigInteger.ONE);
			BigInteger tmp = a_all;
			for (int j = pathBuckets - 1; j >= 0; j--) {
				a[j] = tmp.and(helper);
				tmp = tmp.shiftRight(bucketBits);
				b[j] = new BigInteger(1, G2.compute(v[j]));
				PreData.encrypt_c[i][j] = a[j].xor(b[j]);
			}
		}
		timing.stopwatch[PID.encrypt][TID.offline].stop();

		timing.stopwatch[PID.encrypt][TID.offline_write].start();
		eddie.write(PreData.encrypt_s);
		for (int index = 0; index <= h; index++)
			eddie.write(PreData.encrypt_x[index]);
		for (int index = 0; index <= h; index++)
			charlie.write(PreData.encrypt_c[index]);
		timing.stopwatch[PID.encrypt][TID.offline_write].stop();
		

		//charlie.countBandwidth = false;
		//eddie.countBandwidth = false;
		//charlie.bandwidth[PID.pre].stop();
		//eddie.bandwidth[PID.pre].stop();
		 * 
		 */

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object executeEddieSubTree(Communication charlie,
			Communication debbie, Tree OT, Object unused, Timing localTiming) {
		// SSCOT
		PreData.sscot_r = new BigInteger[levels][];
		
		for (int index = 0; index < levels; index++) {
			PreData.sscot_r[index] = debbie.readBigIntegerArray();
		}
		
		PreData.sscot_k = debbie.readDoubleByteArray();
		PreData.sscot_k_p = debbie.readDoubleByteArray();
		
		
		// SSIOT
		PreData.ssiot_k = debbie.readDoubleByteArray();
		PreData.ssiot_k_p = debbie.readDoubleByteArray();
		PreData.ssiot_r = debbie.readBigIntegerArray();
		
		
		// Access
		PreData.access_sigma = (List<Integer>[]) new List[levels];

		for (int index = 0; index < levels; index++) {
			PreData.access_sigma[index] = debbie.readListInt();
		}
		PreData.access_p = debbie.readBigIntegerArray();
		
		
		
		/*
		

		// PET
		PreData.pet_alpha = new BigInteger[levels][];

		//timing.stopwatch[PID.pet][TID.offline_read].start();
		for (int index = 0; index <= h; index++) {
			PreData.pet_alpha[index] = debbie.readBigIntegerArray();
		}
		PreData.pet_k = debbie.readDoubleByteArray();
		//timing.stopwatch[PID.pet][TID.offline_read].stop();

		// AOT
		PreData.aot_k = debbie.readDoubleByteArray();
		
		// AOTSS
		PreData.aotss_k = debbie.readDoubleByteArray();
		*/

		
		/*
		// PPT
		PreData.ppt_sE_Li_p = new BigInteger[levels];

		timing.stopwatch[PID.ppt][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			PreData.ppt_sE_Li_p[i] = new BigInteger(lBits, SR.rand);
		}
		timing.stopwatch[PID.ppt][TID.offline].stop();

		// Reshuffle
		PreData.reshuffle_p2 = new byte[levels][];
		PreData.reshuffle_pi = (List<Integer>[]) new List[levels];

		timing.stopwatch[PID.reshuffle][TID.offline_read].start();
		PreData.reshuffle_s2 = debbie.readDoubleByteArray();
		timing.stopwatch[PID.reshuffle][TID.offline_read].stop();

		timing.stopwatch[PID.reshuffle][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			PRG G = new PRG(pathBuckets * bucketBits);
			PreData.reshuffle_p2[i] = G.compute(PreData.reshuffle_s2[i]);
			PreData.reshuffle_pi[i] = Util
					.getInversePermutation(PreData.access_sigma[i]);
		}
		timing.stopwatch[PID.reshuffle][TID.offline].stop();

		// GCF
		PreData.gcf_gc_E = new Circuit[levels][];
		PreData.gcf_lbs = new BigInteger[levels][][][];

		timing.stopwatch[PID.gcf][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			PreData.gcf_gc_E[i] = new Circuit[d_i + 1];
			PreData.gcf_lbs[i] = new BigInteger[d_i + 1][][];
			for (int j = 0; j < d_i + 1; j++) {
				int ww = (j != d_i) ? w : (w * expen);
				int tmp1 = SR.rand.nextInt(ww) + 1;
				int tmp2 = SR.rand.nextInt(ww) + 1;
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
				PreData.gcf_gc_E[i][j].passTruthTables();

				int n = (j != d_i) ? (w * 2 + 2) : (w * expen + 2);
				PreData.gcf_lbs[i][j] = new BigInteger[n][2];
				for (int k = 0; k < n; k++) {
					BigInteger glb0 = new BigInteger(Wire.labelBitLength,
							SR.rand);
					BigInteger glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
					PreData.gcf_lbs[i][j][k][0] = glb0;
					PreData.gcf_lbs[i][j][k][1] = glb1;
				}
			}
		}
		timing.stopwatch[PID.gcf][TID.offline].stop();

		// IOT
		PreData.iot_pi = (List<Integer>[][]) new List[1][levels];
		PreData.iot_r = new BigInteger[1][levels][];
		PreData.iot_s = new byte[1][][];

		timing.stopwatch[PID.iot][TID.offline_read].start();
		PreData.iot_s[0] = debbie.readDoubleByteArray();
		for (int index = 0; index <= h; index++)
			PreData.iot_pi[0][index] = debbie.readListInt();
		timing.stopwatch[PID.iot][TID.offline_read].stop();

		timing.stopwatch[PID.iot][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			int l = tupleBits;
			int k = w * pathBuckets;
			int N = k + 2;
			PRG G = new PRG(N * l);
			BigInteger r_all = new BigInteger(1, G.compute(PreData.iot_s[0][i]));
			PreData.iot_r[0][i] = new BigInteger[N];
			BigInteger helper = BigInteger.ONE.shiftLeft(l).subtract(
					BigInteger.ONE);
			BigInteger tmp = r_all;
			for (int o = N - 1; o >= 0; o--) {
				PreData.iot_r[0][i][o] = tmp.and(helper);
				tmp = tmp.shiftRight(l);
			}
		}
		timing.stopwatch[PID.iot][TID.offline].stop();

		// Encrypt
		PreData.encrypt_s = new byte[levels][];
		PreData.encrypt_x = new ECPoint[levels][];
		PreData.encrypt_a = new BigInteger[levels][];

		timing.stopwatch[PID.encrypt][TID.offline_read].start();
		PreData.encrypt_s = debbie.readDoubleByteArray();
		for (int index = 0; index <= h; index++)
			PreData.encrypt_x[index] = debbie.readECPointArray();
		timing.stopwatch[PID.encrypt][TID.offline_read].stop();

		timing.stopwatch[PID.encrypt][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			PreData.encrypt_a[i] = new BigInteger[pathBuckets];
			PRG G1 = new PRG(bucketBits * pathBuckets);
			BigInteger a_all = new BigInteger(1,
					G1.compute(PreData.encrypt_s[i]));
			BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(
					BigInteger.ONE);
			BigInteger tmp = a_all;
			for (int j = pathBuckets - 1; j >= 0; j--) {
				PreData.encrypt_a[i][j] = tmp.and(helper);
				tmp = tmp.shiftRight(bucketBits);
			}
		}
		timing.stopwatch[PID.encrypt][TID.offline].stop();
		
		//debbie.countBandwidth = false;
		//charlie.countBandwidth = false;
		//debbie.bandwidth[PID.pre].stop();
		//charlie.bandwidth[PID.pre].stop();
		 * 
		 */

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
