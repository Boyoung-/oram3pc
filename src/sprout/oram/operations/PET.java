package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.oram.TID;

import org.apache.commons.lang3.NotImplementedException;

// TODO: PET doesn't need all of the paramaters that the other operations have, can we make it more generic?
// TODO: optimize pre-computation by just sending seeds
public class PET extends Operation {

	public PET(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public void executeDebbie(Communication charlie, Communication eddie, BigInteger[] c, int i) {
		// Debbie does nothing online

		// sanityCheck();
	}

	public Integer executeCharlie(Communication debbie, Communication eddie,
			BigInteger[] c, int i) {
		debbie.countBandwidth = true;
		eddie.countBandwidth = true;
		debbie.bandwidth[PID.pet].start();
		eddie.bandwidth[PID.pet].start();

		// sanityCheck();

		// Protocol
		// step 1
		// party C
		int n = PreData.pet_alpha[i].length;
		BigInteger[] u = new BigInteger[n];
		timing.stopwatch[PID.pet][TID.online].start();
		for (int j = 0; j < n; j++) {
			// u_j <- (alpha_j - c_j) mod p
			u[j] = PreData.pet_alpha[i][j].subtract(c[j]).mod(SR.p);
		}
		timing.stopwatch[PID.pet][TID.online].stop();
		// C sends u to E
		timing.stopwatch[PID.pet][TID.online_write].start();
		eddie.write(u);
		timing.stopwatch[PID.pet][TID.online_write].stop();

		// step 2
		// E sends w to C
		timing.stopwatch[PID.pet][TID.online_read].start();
		BigInteger[] w = eddie.readBigIntegerArray();
		timing.stopwatch[PID.pet][TID.online_read].stop();

		// step 3
		// party C
		BigInteger[] v = new BigInteger[n];
		timing.stopwatch[PID.pet][TID.online].start();
		for (int j = 0; j < n; j++) {
			// v_j <- (c_j * delta_j + w_j - gama_j) mod p
			v[j] = c[j].multiply(PreData.pet_delta[i][j]).add(w[j])
					.subtract(PreData.pet_gamma[i][j]).mod(SR.p);

			if (v[j].longValue() == 0L) {
				timing.stopwatch[PID.pet][TID.online].stop();
				debbie.bandwidth[PID.pet].stop();
				eddie.bandwidth[PID.pet].stop();
				debbie.countBandwidth = false;
				eddie.countBandwidth = false;
				// C outputs j s.t. v[j] = 0
				return j;
			}
		}
		timing.stopwatch[PID.pet][TID.online].stop();

		debbie.bandwidth[PID.pet].stop();
		eddie.bandwidth[PID.pet].stop();
		debbie.countBandwidth = false;
		eddie.countBandwidth = false;

		// this means error
		return -1;
	}

	public void executeEddie(Communication charlie, Communication debbie,
			BigInteger[] b, int i) {
		charlie.countBandwidth = true;
		debbie.countBandwidth = true;
		debbie.bandwidth[PID.pet].start();
		charlie.bandwidth[PID.pet].start();

		// sanityCheck();

		// step 1
		// C sends u to E
		timing.stopwatch[PID.pet][TID.online_read].start();
		BigInteger[] u = charlie.readBigIntegerArray();
		timing.stopwatch[PID.pet][TID.online_read].stop();

		// step 2
		// party E
		int n = PreData.pet_beta[i].length;
		BigInteger[] w = new BigInteger[n];
		timing.stopwatch[PID.pet][TID.online].start();
		for (int j = 0; j < n; j++) {
			// w_j <- (beta_j * u_j - r_j * b_j - tau_j) mod p
			w[j] = PreData.pet_beta[i][j].multiply(u[j])
					.subtract(PreData.pet_r[i][j].multiply(b[j]))
					.subtract(PreData.pet_tau[i][j]).mod(SR.p);
		}
		timing.stopwatch[PID.pet][TID.online].stop();

		// E sends w to C
		timing.stopwatch[PID.pet][TID.online_write].start();
		charlie.write(w);
		timing.stopwatch[PID.pet][TID.online_write].stop();

		debbie.bandwidth[PID.pet].stop();
		charlie.bandwidth[PID.pet].stop();
		charlie.countBandwidth = false;
		debbie.countBandwidth = false;
	}

	@Override
	public void run(Party party, Forest unused) throws ForestException {
		throw new NotImplementedException("No testing for PET yet");
	}
}
