package sprout.oram.operations;

import java.math.BigInteger;

import org.apache.commons.lang3.tuple.Pair;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Bucket;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.oram.TID;
import sprout.oram.Tree;
import sprout.util.Timing;
import sprout.util.Util;

public class Access extends TreeOperation<AOutput, BigInteger[]> {

	public Access(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public AOutput executeCharlieSubTree(Communication debbie,
			Communication eddie, Tree unused, BigInteger[] args,
			Timing localTiming) {
		// protocol
		// step 1
		timing.stopwatch[PID.access][TID.online].start();
		PreData.access_Li[i] = args[0];
		BigInteger sC_Nip1 = args[1];
		int Nip1Bits = (i < h - 1) ? (i + 1) * tau : ForestMetadata
				.getLastNBits();
		BigInteger sC_Ni = Util.getSubBits(sC_Nip1, Nip1Bits - nBits, Nip1Bits);
		
		byte[] msg_Li = null;
		if (i > 0)
			msg_Li = PreData.access_Li[i].toByteArray();
		timing.stopwatch[PID.access][TID.online].stop();

		if (i > 0) {
			timing.stopwatch[PID.access][TID.online_write].start();
			// debbie.write(PreData.access_Li[i]);
			// eddie.write(PreData.access_Li[i]);
			debbie.write(msg_Li, PID.access);
			eddie.write(msg_Li, PID.access);
			timing.stopwatch[PID.access][TID.online_write].stop();
		}

		timing.stopwatch[PID.access][TID.online_read].start();
		//BigInteger sC_sig_P_all_p = debbie.readBigInteger();
		byte[] msg_path = debbie.read();
		timing.stopwatch[PID.access][TID.online_read].stop();		

		// step 2
		timing.stopwatch[PID.access][TID.online].start();
		BigInteger sC_sig_P_all_p = new BigInteger(1, msg_path);
		
		int j_1 = 0;
		BigInteger z;
		timing.stopwatch[PID.access][TID.online].stop();
		if (i == 0) {
			z = sC_sig_P_all_p;
		} else {
			SSCOT sscot = new SSCOT(debbie, eddie);
			Pair<Integer, BigInteger> je = sscot.executeCharlie(debbie, eddie,
					i, pathTuples, aBits, 1 + nBits);
			if (je == null) {
				try {
					throw new Exception("SSCOT error!");
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			timing.stopwatch[PID.access][TID.online].start();
			j_1 = je.getLeft();
			BigInteger eBar = je.getRight();

			BigInteger helper = BigInteger.ONE.shiftLeft(aBits).subtract(
					BigInteger.ONE);
			BigInteger dBar = sC_sig_P_all_p.shiftRight(
					(pathTuples - j_1 - 1) * tupleBits).and(helper);
			z = eBar.xor(dBar);
			timing.stopwatch[PID.access][TID.online].stop();
		}

		// step 3
		int j_2 = 0;
		BigInteger y_j2 = null;
		if (i < h) {
			SSIOT ssiot = new SSIOT(debbie, eddie);
			Pair<Integer, BigInteger> jy = ssiot.executeCharlie(debbie, eddie,
					i, twotaupow, aBits / twotaupow, tau);
			j_2 = jy.getLeft();
			y_j2 = jy.getRight();
		}

		// step 4
		timing.stopwatch[PID.access][TID.online].start();
		BigInteger d = null;
		BigInteger Lip1 = null;
		if (i < h) {
			Lip1 = y_j2.xor(Util.getSubBits(z, (twotaupow - j_2 - 1) * d_ip1,
					(twotaupow - j_2) * d_ip1));
		} else {
			d = z;
		}

		BigInteger sC_Ti;
		BigInteger sC_sig_P_p;
		if (i == 0) {
			sC_Ti = z;
			sC_sig_P_p = null;
		} else {
			sC_Ti = sC_Ni.shiftLeft(lBits + aBits).xor(z).setBit(tupleBits - 1);

			boolean flipBit = !sC_sig_P_all_p.testBit((pathTuples - j_1)
					* tupleBits - 1);
			BigInteger newTuple = new BigInteger(tupleBits - 1, SR.rand);
			if (flipBit)
				newTuple = newTuple.setBit(tupleBits - 1);
			sC_sig_P_p = Util.setSubBits(sC_sig_P_all_p, newTuple, (pathTuples - j_1 - 1) * tupleBits, (pathTuples - j_1) * tupleBits);
		}
		timing.stopwatch[PID.access][TID.online].stop();

		return new AOutput(Lip1, sC_Ti, null, sC_sig_P_p, null, d,
				BigInteger.valueOf(j_2));
	}

	@Override
	public AOutput executeDebbieSubTree(Communication charlie,
			Communication eddie, Tree sD_OT, BigInteger[] args,
			Timing localTiming) {
		BigInteger sD_Nip1 = args[0];

		// protocol
		// step 1
		byte[] msg_Li = null;
		
		if (i > 0) {
			timing.stopwatch[PID.access][TID.online_read].start();
			// PreData.access_Li[i] = charlie.readBigInteger();
			msg_Li = charlie.read();
			timing.stopwatch[PID.access][TID.online_read].stop();
		}

		timing.stopwatch[PID.access][TID.online].start();
		if (i == 0) {
			PreData.access_Li[i] = null;
		}
		else {
			PreData.access_Li[i] = new BigInteger(1, msg_Li);
		}
		
		Bucket[] sD_buckets;
		if (!Forest.loadPathCheat())
			sD_buckets = sD_OT.getBucketsOnPath(PreData.access_Li[i]);
		else
			sD_buckets = Forest.getPathBuckets(i);
		
		BigInteger[] sD_P = new BigInteger[sD_buckets.length];
		for (int j = 0; j < sD_buckets.length; j++) {
			sD_P[j] = new BigInteger(1, sD_buckets[j].getByteTuples());
		}
		BigInteger[] sD_sig_P = Util.permute(sD_P, PreData.access_sigma[i]);
		
		for (int j=0; j<sD_sig_P.length; j++)
			sD_sig_P[j] = sD_sig_P[j].xor(PreData.access_p[i][j]);

		BigInteger sD_sig_P_all = sD_sig_P[0];
		for (int j = 1; j < sD_sig_P.length; j++)
			sD_sig_P_all = sD_sig_P_all.shiftLeft(bucketBits).xor(sD_sig_P[j]);
		
		byte[] msg_path = sD_sig_P_all.toByteArray();
		timing.stopwatch[PID.access][TID.online].stop();

		timing.stopwatch[PID.access][TID.online_write].start();
		//charlie.write(sD_sig_P_all);
		charlie.write(msg_path, PID.access);
		timing.stopwatch[PID.access][TID.online_write].stop();

		// step 2
		timing.stopwatch[PID.access][TID.online].start();
		int Nip1Bits = (i < h - 1) ? (i + 1) * tau : ForestMetadata
				.getLastNBits();
		BigInteger sD_Ni = Util.getSubBits(sD_Nip1, Nip1Bits - nBits, Nip1Bits);

		BigInteger[] a = new BigInteger[pathTuples];
		BigInteger[] share1N = new BigInteger[pathTuples];
		BigInteger helper;
		BigInteger tmp;
		if (i > 0) {
			helper = BigInteger.ONE.shiftLeft(1 + nBits).subtract(
					BigInteger.ONE);
			for (int j=0; j<pathBuckets; j++) {
				tmp = sD_sig_P[j].shiftRight(lBits + aBits);
				for (int t = w - 1; t >= 0; t--) {
					share1N[j*w+t] = tmp.and(helper);
					tmp = tmp.shiftRight(tupleBits);
					a[j*w+t] = share1N[j*w+t].xor(sD_Ni.setBit(nBits));
				}
			}
			timing.stopwatch[PID.access][TID.online].stop();

			SSCOT sscot = new SSCOT(charlie, eddie);
			sscot.executeDebbie(charlie, eddie, i, pathTuples, aBits,
					1 + nBits, a);
		} else
			timing.stopwatch[PID.access][TID.online].stop();

		// step 3
		if (i < h) {
			BigInteger sD_Nip1_pr = Util.getSubBits(sD_Nip1, 0, Nip1Bits
					- nBits);
			SSIOT ssiot = new SSIOT(charlie, eddie);
			ssiot.executeDebbie(charlie, eddie, i, twotaupow,
					aBits / twotaupow, tau, sD_Nip1_pr);
		}

		return null;
	}

	@Override
	public AOutput executeEddieSubTree(Communication charlie,
			Communication debbie, Tree sE_OT, BigInteger[] args,
			Timing localTiming) {
		// protocol
		// step 1
		byte[] msg_Li = null;
		
		if (i > 0) {
			timing.stopwatch[PID.access][TID.online_read].start();
			// PreData.access_Li[i] = charlie.readBigInteger();
			msg_Li = charlie.read();
			timing.stopwatch[PID.access][TID.online_read].stop();
		}

		timing.stopwatch[PID.access][TID.online].start();
		if (i == 0) {
			PreData.access_Li[i] = null;
		}
		else {
			PreData.access_Li[i] = new BigInteger(1, msg_Li);
		}
		
		Bucket[] sE_buckets;
		if (!Forest.loadPathCheat())
			sE_buckets = sE_OT.getBucketsOnPath(PreData.access_Li[i]);
		else
			sE_buckets = Forest.getPathBuckets(i);
		
		
		BigInteger[] sE_P = new BigInteger[sE_buckets.length];
		for (int j = 0; j < sE_buckets.length; j++) {
			sE_P[j] = new BigInteger(1, sE_buckets[j].getByteTuples());
		}
		BigInteger[] sE_sig_P = Util.permute(sE_P, PreData.access_sigma[i]);
		
		for (int j=0; j<sE_sig_P.length; j++)
			sE_sig_P[j] = sE_sig_P[j].xor(PreData.access_p[i][j]);

		BigInteger sE_sig_P_all = sE_sig_P[0];
		for (int j = 1; j < sE_sig_P.length; j++)
			sE_sig_P_all = sE_sig_P_all.shiftLeft(bucketBits).xor(sE_sig_P[j]);
		
		BigInteger[] y = new BigInteger[twotaupow];
		BigInteger y_all;
		if (i == 0)
			y_all = sE_sig_P_all;
		else if (i < h)
			y_all = new BigInteger(aBits, SR.rand);
		else
			y_all = BigInteger.ZERO;
		BigInteger helper = BigInteger.ONE.shiftLeft(d_ip1).subtract(
				BigInteger.ONE);
		BigInteger tmp = y_all;
		for (int o = twotaupow - 1; o >= 0; o--) {
			y[o] = tmp.and(helper);
			tmp = tmp.shiftRight(d_ip1);
		}

		// step 2
		BigInteger sE_Nip1 = args[0];
		int Nip1Bits = (i < h - 1) ? (i + 1) * tau : ForestMetadata
				.getLastNBits();
		BigInteger sE_Ni = Util.getSubBits(sE_Nip1, Nip1Bits - nBits, Nip1Bits);

		BigInteger[] e = new BigInteger[pathTuples];
		BigInteger[] b = new BigInteger[pathTuples];
		BigInteger[] share1N = new BigInteger[pathTuples];
		BigInteger[] shareA = new BigInteger[pathTuples];
		BigInteger helper1N;
		BigInteger helperA;
		if (i > 0) {
			helper1N = BigInteger.ONE.shiftLeft(1 + nBits).subtract(
					BigInteger.ONE);
			helperA = BigInteger.ONE.shiftLeft(aBits).subtract(BigInteger.ONE);
			for (int j=0; j<pathBuckets; j++) {
				tmp = sE_sig_P[j];
				for (int t = w - 1; t >= 0; t--) {
					shareA[j * w + t] = tmp.and(helperA);
					tmp = tmp.shiftRight(lBits + aBits);
					share1N[j * w + t] = tmp.and(helper1N);
					tmp = tmp.shiftRight(1 + nBits);
					e[j * w + t] = shareA[j * w + t].xor(y_all);
					b[j * w + t] = share1N[j * w + t].xor(sE_Ni);
				}
			}
			timing.stopwatch[PID.access][TID.online].stop();

			SSCOT sscot = new SSCOT(charlie, debbie);
			sscot.executeEddie(charlie, debbie, i, pathTuples, aBits,
					1 + nBits, e, b);
		} else
			timing.stopwatch[PID.access][TID.online].stop();

		// step 3
		if (i < h) {
			BigInteger sE_Nip1_pr = Util.getSubBits(sE_Nip1, 0, Nip1Bits
					- nBits);
			SSIOT ssiot = new SSIOT(charlie, debbie);
			ssiot.executeEddie(charlie, debbie, i, twotaupow,
					aBits / twotaupow, tau, y, sE_Nip1_pr);
		}

		// step 4
		timing.stopwatch[PID.access][TID.online].start();
		BigInteger sE_Ti;
		BigInteger sE_sig_P_p;
		if (i == 0) {
			sE_Ti = y_all;
			sE_sig_P_p = null;
		} else {
			sE_Ti = sE_Ni.shiftLeft(lBits + aBits)
					.xor(PreData.access_Li[i].shiftLeft(aBits)).xor(y_all);
			sE_sig_P_p = sE_sig_P_all;
		}
		timing.stopwatch[PID.access][TID.online].stop();

		return new AOutput(null, null, sE_Ti, null, sE_sig_P_p, null, null);
	}

	// for testing correctness
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		int records = 10; // how many random records we want to test retrieval
		int retrievals = 5; // for each record, how many repeated retrievals we
							// want to do

		long numInsert = Math.min(ForestMetadata.getNumInsert(),
				ForestMetadata.getAddressSpace());
		if (numInsert == 0L) {
			System.err.println("No record in the forest");
			return;
		}

		int numTrees = ForestMetadata.getLevels();
		int h = numTrees - 1;
		int tau = ForestMetadata.getTau();
		int lastNBits = ForestMetadata.getLastNBits();
		int shiftN = lastNBits % tau;
		if (shiftN == 0)
			shiftN = tau;

		timing = new Timing();

		// //////////////////////////////////////////
		// ////// main execution starts /////////
		// //////////////////////////////////////////

		for (int rec = 0; rec < records; rec++) {
			// retrieve a record by picking a random N
			BigInteger N = null;
			BigInteger sC_N = null;
			BigInteger sE_N = null;
			BigInteger sD_N = null;
			if (party == Party.Charlie) {
				if (numInsert == -1) {
					if (Forest.loadPathCheat())
						N = BigInteger.ZERO;
					else
						N = new BigInteger(lastNBits, SR.rand);
					sC_N = new BigInteger(lastNBits, SR.rand);
				} else {
					if (Forest.loadPathCheat())
						N = BigInteger.ZERO;
					else
						N = Util.nextBigInteger(BigInteger.valueOf(numInsert));
					sC_N = Util.nextBigInteger(BigInteger.valueOf(numInsert));
				}

				sE_N = N.xor(sC_N);
				con1.write(sC_N);
				con2.write(sE_N);
			} else if (party == Party.Debbie)
				sD_N = con1.readBigInteger();
			else if (party == Party.Eddie)
				sE_N = con1.readBigInteger();

			for (long retri = 0; retri < retrievals; retri++) {
				// pre-computation
				if (party == Party.Charlie)
					new Precomputation(con1, con2).executeCharlieSubTree(con1,
							con2, null, null, null);
				else if (party == Party.Debbie)
					new Precomputation(con1, con2).executeDebbieSubTree(con1,
							con2, null, null, null);
				else if (party == Party.Eddie)
					new Precomputation(con1, con2).executeEddieSubTree(con1,
							con2, null, null, null);
				else {
					System.err.println("No such party: " + party);
					return;
				}

				System.out.println("Record " + rec + ": retrieval " + retri);
				if (party == Party.Charlie)
					System.out.println("N=" + N.longValue() + " ("
							+ Util.addZero(N.toString(2), lastNBits) + ")");

				// sync so online protocols for all parties start at the same
				// time
				sanityCheck();

				// for each retrieval, execute protocols on each tree
				BigInteger Li = null;
				for (int i = 0; i < numTrees; i++) {
					switch (party) {
					case Charlie:
						BigInteger sC_Ni;
						if (i < h - 1) {
							sC_Ni = Util.getSubBits(sC_N, lastNBits - (i + 1)
									* tau, lastNBits);
						} else
							sC_Ni = sC_N;

						System.out.println("i="
								+ i
								+ ", Li="
								+ (Li == null ? "" : Util.addZero(
										Li.toString(2),
										ForestMetadata.getLBits(i))));
						loadTreeSpecificParameters(i);
						AOutput AOut = executeCharlieSubTree(con1, con2, null,
								new BigInteger[] { Li, sC_Ni }, null);
						BigInteger[] outC = new BigInteger[] { AOut.Lip1,
								AOut.data };
						Li = outC[0];
						if (i == h) {
							BigInteger D = Util.getSubBits(outC[1], 0,
									ForestMetadata.getDataSize() * 8);
							System.out.println("Retrieved record is: " + D);
							System.out.println("Is record correct: "
									+ (D.compareTo(N) == 0 ? "YES" : "NO!!")
									+ "\n");
							if (D.compareTo(N) != 0)
								try {
									throw new Exception("Retrieval error");
								} catch (Exception e) {
									e.printStackTrace();
								}
						}
						break;
					case Debbie:
						BigInteger sD_Ni;
						if (i < h - 1) {
							sD_Ni = Util.getSubBits(sD_N, lastNBits - (i + 1)
									* tau, lastNBits);
						} else
							sD_Ni = sD_N;
						loadTreeSpecificParameters(i);
						executeDebbieSubTree(con1, con2, forest.getTree(i),
								new BigInteger[] { sD_Ni }, null);
						break;
					case Eddie:
						BigInteger sE_Ni;
						if (i < h - 1) {
							sE_Ni = Util.getSubBits(sE_N, lastNBits - (i + 1)
									* tau, lastNBits);
						} else
							sE_Ni = sE_N;
						loadTreeSpecificParameters(i);
						executeEddieSubTree(con1, con2, forest.getTree(i),
								new BigInteger[] { sE_Ni }, null);
						break;
					}
				}
			}
		}
	}
}
