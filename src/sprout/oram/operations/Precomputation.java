package sprout.oram.operations;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bouncycastle.math.ec.ECPoint;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.SR;
import sprout.oram.PID;
import sprout.oram.PreData;
import sprout.oram.TID;
import sprout.util.Util;

public class Precomputation extends TreeOperation<Object, Object> {

	public Precomputation(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public Object executeCharlieSubTree(Communication debbie,
			Communication eddie, Object unused) {
		// OPRF
		PreData.oprf_oprf = OPRFHelper.getOPRF();
		PreData.oprf_gy = new ECPoint[levels][][];

		timing.stopwatch[PID.oprf][TID.offline].start();
		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);

			PreData.oprf_gy[i] = PreData.oprf_oprf.preparePairs(pathBuckets);
		}
		timing.stopwatch[PID.oprf][TID.offline].stop();

		// PET
		PreData.pet_alpha = new BigInteger[levels][];
		PreData.pet_gamma = new BigInteger[levels][];
		PreData.pet_delta = new BigInteger[levels][];

		timing.stopwatch[PID.pet][TID.offline_read].start();
		for (int index = 0; index <= h; index++) {
			PreData.pet_alpha[index] = debbie.readBigIntegerArray();
			PreData.pet_gamma[index] = debbie.readBigIntegerArray();
			PreData.pet_delta[index] = debbie.readBigIntegerArray();
		}
		timing.stopwatch[PID.pet][TID.offline_read].stop();

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

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object executeDebbieSubTree(Communication charlie,
			Communication eddie, Object unused) {
		// DecryptPath
		PreData.decrypt_sigma = (List<Integer>[]) new List[levels];

		timing.stopwatch[PID.decrypt][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			PreData.decrypt_sigma[i] = new ArrayList<Integer>();
			for (int j = 0; j < pathBuckets; j++)
				PreData.decrypt_sigma[i].add(j);
			Collections.shuffle(PreData.decrypt_sigma[i], SR.rand);
		}
		timing.stopwatch[PID.decrypt][TID.offline].stop();

		timing.stopwatch[PID.decrypt][TID.offline_write].start();
		for (int index = 0; index <= h; index++) {
			eddie.write(PreData.decrypt_sigma[index]);
		}
		timing.stopwatch[PID.decrypt][TID.offline_write].stop();

		// PET
		PreData.pet_alpha = new BigInteger[levels][];
		PreData.pet_beta = new BigInteger[levels][];
		PreData.pet_tau = new BigInteger[levels][];
		PreData.pet_r = new BigInteger[levels][];
		PreData.pet_gamma = new BigInteger[levels][];
		PreData.pet_delta = new BigInteger[levels][];

		timing.stopwatch[PID.pet][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			PreData.pet_alpha[i] = new BigInteger[pathTuples];
			PreData.pet_beta[i] = new BigInteger[pathTuples];
			PreData.pet_tau[i] = new BigInteger[pathTuples];
			PreData.pet_r[i] = new BigInteger[pathTuples];
			PreData.pet_gamma[i] = new BigInteger[pathTuples];
			PreData.pet_delta[i] = new BigInteger[pathTuples];

			for (int j = 0; j < pathTuples; j++) {
				PreData.pet_alpha[i][j] = Util.nextBigInteger(SR.p); // [0,
																		// p-1],
																		// Z_p
				PreData.pet_beta[i][j] = Util.nextBigInteger(SR.p); // [0, p-1],
																	// Z_p
				PreData.pet_tau[i][j] = Util.nextBigInteger(SR.p); // [0, p-1],
																	// Z_p
				PreData.pet_r[i][j] = Util.nextBigInteger(
						SR.p.subtract(BigInteger.ONE)).add(BigInteger.ONE); // [1,
																			// p-1],
																			// Z_p*
				// gama_j <- (alpha_j * beta_j - tau_j) mod p
				PreData.pet_gamma[i][j] = PreData.pet_alpha[i][j]
						.multiply(PreData.pet_beta[i][j])
						.subtract(PreData.pet_tau[i][j]).mod(SR.p);
				// delta_j <- (beta_j + r_j) mod p
				PreData.pet_delta[i][j] = PreData.pet_beta[i][j].add(
						PreData.pet_r[i][j]).mod(SR.p);
			}
		}
		timing.stopwatch[PID.pet][TID.offline].stop();

		timing.stopwatch[PID.pet][TID.offline_write].start();
		for (int index = 0; index <= h; index++) {
			charlie.write(PreData.pet_alpha[index]);
			charlie.write(PreData.pet_gamma[index]);
			charlie.write(PreData.pet_delta[index]);
		}
		for (int index = 0; index <= h; index++) {
			eddie.write(PreData.pet_beta[index]);
			eddie.write(PreData.pet_tau[index]);
			eddie.write(PreData.pet_r[index]);
		}
		timing.stopwatch[PID.pet][TID.offline_write].stop();

		// AOT
		PreData.aot_k = new byte[2][][];

		timing.stopwatch[PID.aot][TID.offline_read].start();
		for (int j = 0; j < 2; j++)
			PreData.aot_k[j] = eddie.readDoubleByteArray();
		timing.stopwatch[PID.aot][TID.offline_read].stop();

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
					.getInversePermutation(PreData.decrypt_sigma[i]);
		}
		timing.stopwatch[PID.reshuffle][TID.offline].stop();

		timing.stopwatch[PID.reshuffle][TID.offline_write].start();
		charlie.write(PreData.reshuffle_s1);
		eddie.write(PreData.reshuffle_s2);
		timing.stopwatch[PID.reshuffle][TID.offline_write].stop();

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object executeEddieSubTree(Communication charlie,
			Communication debbie, Object unused) {
		// DecryptPath
		PreData.decrypt_sigma = (List<Integer>[]) new List[levels];

		timing.stopwatch[PID.decrypt][TID.offline_read].start();
		for (int index = 0; index <= h; index++) {
			PreData.decrypt_sigma[index] = debbie.readListInt();
		}
		timing.stopwatch[PID.decrypt][TID.offline_read].stop();

		// PET
		PreData.pet_beta = new BigInteger[levels][];
		PreData.pet_tau = new BigInteger[levels][];
		PreData.pet_r = new BigInteger[levels][];

		timing.stopwatch[PID.pet][TID.offline_read].start();
		for (int index = 0; index <= h; index++) {
			PreData.pet_beta[index] = debbie.readBigIntegerArray();
			PreData.pet_tau[index] = debbie.readBigIntegerArray();
			PreData.pet_r[index] = debbie.readBigIntegerArray();
		}
		timing.stopwatch[PID.pet][TID.offline_read].stop();

		// AOT
		PreData.aot_k = new byte[2][levels][16];

		timing.stopwatch[PID.aot][TID.offline].start();
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);

			for (int j = 0; j < 2; j++)
				SR.rand.nextBytes(PreData.aot_k[j][i]);
		}
		timing.stopwatch[PID.aot][TID.offline].stop();

		timing.stopwatch[PID.aot][TID.offline_write].start();
		for (int j = 0; j < 2; j++)
			debbie.write(PreData.aot_k[j]);
		timing.stopwatch[PID.aot][TID.offline_write].stop();

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
					.getInversePermutation(PreData.decrypt_sigma[i]);
		}
		timing.stopwatch[PID.reshuffle][TID.offline].stop();

		return null;
	}
}
