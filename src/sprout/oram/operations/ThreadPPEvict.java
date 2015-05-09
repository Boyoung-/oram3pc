package sprout.oram.operations;

import java.math.BigInteger;

import org.apache.commons.lang3.tuple.Pair;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.util.StopWatch;
import sprout.util.Timing;
import sprout.util.Util;

public class ThreadPPEvict extends Operation {

	public ThreadPPEvict(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public Pair<PPEvict, BigInteger[]> executeCharlie(Communication debbie,
			Communication eddie, int i, BigInteger Li, BigInteger sC_Nip1) {
		// Access
		Access access = new Access(debbie, eddie);
		access.loadTreeSpecificParameters(i);
		AOutput AOut = access.executeCharlieSubTree(debbie, eddie, null,
				new BigInteger[] { Li, sC_Nip1 }, null);
		BigInteger[] output = new BigInteger[] { AOut.Lip1, AOut.data };

		// PP+Evict
		Timing localTiming = new Timing();
		PPEvict thread = new PPEvict(Party.Charlie, i, null, localTiming,
				new BigInteger[] { AOut.sC_Ti, Li, AOut.Lip1, AOut.j_2,
						AOut.sC_sig_P_p });
		thread.start();

		return Pair.of(thread, output);
	}

	public PPEvict executeDebbie(Communication charlie, Communication eddie,
			int i, Tree sD_OT, BigInteger sD_Nip1) {
		// Access
		Access access = new Access(charlie, eddie);
		access.loadTreeSpecificParameters(i);
		access.executeDebbieSubTree(charlie, eddie, sD_OT,
				new BigInteger[] { sD_Nip1 }, null);

		// PP+Evict
		Timing localTiming = new Timing();
		PPEvict thread = new PPEvict(Party.Debbie, i, sD_OT, localTiming, null);
		thread.start();

		return thread;
	}

	public PPEvict executeEddie(Communication charlie, Communication debbie,
			int i, Tree sE_OT, BigInteger sE_Nip1) {
		// Access
		Access access = new Access(charlie, debbie);
		access.loadTreeSpecificParameters(i);
		AOutput AOut = access.executeEddieSubTree(charlie, debbie, sE_OT,
				new BigInteger[] { sE_Nip1 }, null);

		// PP+Evict
		Timing localTiming = new Timing();
		PPEvict thread = new PPEvict(Party.Eddie, i, sE_OT, localTiming,
				new BigInteger[] { AOut.sE_Ti, AOut.sE_sig_P_p });
		thread.start();

		return thread;
	}

	@Override
	public void run(Party party, Forest forest) throws ForestException {
		int records = 1; // how many random records we want to test retrieval
		int retrievals = 100; // for each record, how many repeated retrievals
								// we
								// want to do
		/*
		 * if (records < 2) { System.err.println(
		 * "Number of records must be at least 2 for average timing"); return; }
		 * else if (retrievals < 1) {
		 * System.err.println("Number of retrievals must be at least 1");
		 * return; }
		 */

		long numInsert = Math.min(ForestMetadata.getNumInsert(),
				ForestMetadata.getAddressSpace());
		if (numInsert == 0L) {
			System.err.println("No record in the forest");
			return;
		}

		// int cycles = (records - 1) * retrievals; // first round timing is
		// abandoned
		int numTrees = ForestMetadata.getLevels();
		int h = numTrees - 1;
		int tau = ForestMetadata.getTau();
		int lastNBits = ForestMetadata.getLastNBits();
		int shiftN = lastNBits % tau;
		if (shiftN == 0)
			shiftN = tau;

		// timing stuff
		timing = new Timing();

		// threads init
		PPEvict[] threads = new PPEvict[numTrees];

		// turn on bandwidth measurement
		// con1.bandWidthSwitch = true;
		// con2.bandWidthSwitch = true;

		if (ifSanityCheck())
			System.out.println("Sanity check enabled\n");

		StopWatch bp_whole = new StopWatch("ballpark_whole");
		StopWatch bp_online = new StopWatch("ballpark_online");

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
					N = new BigInteger(lastNBits, SR.rand);
					sC_N = new BigInteger(lastNBits, SR.rand);
				} else {
					N = Util.nextBigInteger(BigInteger.valueOf(numInsert));
					sC_N = Util.nextBigInteger(BigInteger.valueOf(numInsert));
				}

				// debug
				// N = BigInteger.valueOf(3);

				sE_N = N.xor(sC_N);
				con1.write(sC_N);
				con2.write(sE_N);
			} else if (party == Party.Debbie)
				sD_N = con1.readBigInteger();
			else if (party == Party.Eddie)
				sE_N = con1.readBigInteger();

			for (long retri = 0; retri < retrievals; retri++) {
				bp_whole.start();

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

				bp_online.start();

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
						Pair<PPEvict, BigInteger[]> outPair = executeCharlie(
								con1, con2, i, Li, sC_Ni);
						BigInteger[] outC = outPair.getRight();
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
						threads[i] = outPair.getLeft();
						break;
					case Debbie:
						BigInteger sD_Ni;
						if (i < h - 1) {
							sD_Ni = Util.getSubBits(sD_N, lastNBits - (i + 1)
									* tau, lastNBits);
						} else
							sD_Ni = sD_N;
						threads[i] = executeDebbie(con1, con2, i,
								forest.getTree(i), sD_Ni);
						break;
					case Eddie:
						BigInteger sE_Ni;
						if (i < h - 1) {
							sE_Ni = Util.getSubBits(sE_N, lastNBits - (i + 1)
									* tau, lastNBits);
						} else
							sE_Ni = sE_N;
						threads[i] = executeEddie(con1, con2, i,
								forest.getTree(i), sE_Ni);
						break;
					}
				}

				// wait for all threads to terminate
				// so timing data can be gathered

				for (int i = 0; i < numTrees; i++)
					try {
						threads[i].join();
						timing = timing.add(threads[i].getTiming());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				// only need to count bandwidth once
				con1.bandWidthSwitch = false;
				con2.bandWidthSwitch = false;

				bp_online.stop();
				bp_whole.stop();

				if (retri == (retrievals / 2) - 1) {
					bp_online.reset();
					bp_whole.reset();
					timing.reset();
				}
			}

			// abandon the timing of the first several retrievals
			// assert records > 1
			if (rec == 0) {
				;
				// wholeExecution.start();
				// bp_online.reset();
				// bp_whole.reset();
			}
		}

		System.out.println(bp_whole.toTab());
		System.out.println(bp_online.toTab());

		System.out.println("-------------------------");
		System.out.println(timing.toTab());
	}
}
