package sprout.oram.operations;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Bucket;
import sprout.oram.BucketException;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.oram.TID;
import sprout.oram.Tree;
import sprout.oram.TreeException;
import sprout.util.Timing;
import sprout.util.Util;

public class Retrieve extends Operation {

	private int currTree;

	public Retrieve(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public BigInteger[] executeCharlie(Communication debbie,
			Communication eddie, BigInteger Li, BigInteger Nip1) {
		// Access
		Access access = new Access(debbie, eddie);
		access.loadTreeSpecificParameters(currTree);
		AOutput AOut = access.executeCharlieSubTree(debbie, eddie,
				new BigInteger[] { Li, Nip1 });
		BigInteger[] output = new BigInteger[] { AOut.Lip1, AOut.secretC_Ti };

		// PostProcessT
		BigInteger secretC_Ti = AOut.secretC_Ti;
		BigInteger secretC_Li_p = PreData.ppt_sC_Li_p[currTree];
		BigInteger secretC_Lip1_p = null;
		if (currTree < ForestMetadata.getLevels() - 1)
			secretC_Lip1_p = PreData.ppt_sC_Li_p[currTree + 1];
		BigInteger Lip1 = AOut.Lip1;
		int h = ForestMetadata.getLevels() - 1;
		int tau = ForestMetadata.getTau();
		int Nip1Bits;
		if (currTree < h - 1) {
			Nip1Bits = (currTree + 1) * tau;
		} else {
			Nip1Bits = ForestMetadata.getLastNBits();
		}
		BigInteger Nip1_pr = Util.getSubBits(Nip1, 0,
				Nip1Bits - ForestMetadata.getNBits(currTree));
		PostProcessT ppt = new PostProcessT(debbie, eddie);
		ppt.loadTreeSpecificParameters(currTree);
		BigInteger secretC_Ti_p = ppt.executeCharlieSubTree(debbie, eddie,
				new BigInteger[] { Li, secretC_Ti, secretC_Li_p,
						secretC_Lip1_p, Lip1, Nip1_pr });

		// Reshuffle
		BigInteger secretC_P_p = AOut.secretC_P_p;
		Reshuffle rs = new Reshuffle(debbie, eddie);
		rs.loadTreeSpecificParameters(currTree);
		List<Integer> tmp = null;
		BigInteger secretC_pi_P = rs.executeCharlieSubTree(debbie, eddie,
				Pair.of(secretC_P_p, tmp));

		// Eviction
		Eviction evict = new Eviction(debbie, eddie);
		evict.loadTreeSpecificParameters(currTree);
		BigInteger secretC_P_pp = evict.executeCharlieSubTree(debbie, eddie,
				new BigInteger[] { secretC_pi_P, secretC_Ti_p });
		if (currTree == 0)
			secretC_P_pp = secretC_Ti_p;

		// EncryptPath
		EncryptPath ep = new EncryptPath(debbie, eddie);
		ep.loadTreeSpecificParameters(currTree);
		ep.executeCharlieSubTree(debbie, eddie, secretC_P_pp);

		return output;
	}

	public void executeDebbie(Communication charlie, Communication eddie,
			BigInteger k) {
		// Access
		Access access = new Access(charlie, eddie);
		access.loadTreeSpecificParameters(currTree);
		access.executeDebbieSubTree(charlie, eddie, new BigInteger[] { k });

		// PostProcessT
		PostProcessT ppt = new PostProcessT(charlie, eddie);
		ppt.loadTreeSpecificParameters(currTree);
		ppt.executeDebbieSubTree(charlie, eddie, new BigInteger[] {});

		// Reshuffle
		Reshuffle rs = new Reshuffle(charlie, eddie);
		rs.loadTreeSpecificParameters(currTree);
		BigInteger tmp = null;
		List<Integer> tmp2 = null;
		rs.executeDebbieSubTree(charlie, eddie, Pair.of(tmp, tmp2));

		// Eviction
		Eviction evict = new Eviction(charlie, eddie);
		evict.loadTreeSpecificParameters(currTree);
		evict.executeDebbieSubTree(charlie, eddie, new BigInteger[] {});

		// EncryptPath
		EncryptPath ep = new EncryptPath(charlie, eddie);
		ep.loadTreeSpecificParameters(currTree);
		ep.executeDebbieSubTree(charlie, eddie, k);
	}

	public void executeEddie(Communication charlie, Communication debbie,
			Tree OT, BigInteger Li) {
		// Access
		Access access = new Access(charlie, debbie);
		access.loadTreeSpecificParameters(currTree);
		AOutput AOut = access.executeEddieSubTree(charlie, debbie,
				new BigInteger[] {});

		// PostProcessT
		BigInteger secretE_Ti = AOut.secretE_Ti;
		BigInteger secretE_Li_p = PreData.ppt_sE_Li_p[currTree];
		BigInteger secretE_Lip1_p = null;
		if (currTree < ForestMetadata.getLevels() - 1)
			secretE_Lip1_p = PreData.ppt_sE_Li_p[currTree + 1];
		PostProcessT ppt = new PostProcessT(charlie, debbie);
		ppt.loadTreeSpecificParameters(currTree);
		BigInteger secretE_Ti_p = ppt.executeEddieSubTree(charlie, debbie,
				new BigInteger[] { secretE_Ti, secretE_Li_p, secretE_Lip1_p });

		// Reshuffle
		BigInteger secretE_P_p = AOut.secretE_P_p;
		List<Integer> tmp = null;
		Reshuffle rs = new Reshuffle(charlie, debbie);
		rs.loadTreeSpecificParameters(currTree);
		BigInteger secretE_pi_P = rs.executeEddieSubTree(charlie, debbie,
				Pair.of(secretE_P_p, tmp));

		// Eviction
		Eviction evict = new Eviction(charlie, debbie);
		evict.loadTreeSpecificParameters(currTree);
		BigInteger secretE_P_pp = evict.executeEddieSubTree(charlie, debbie,
				new BigInteger[] { secretE_pi_P, secretE_Ti_p, Li });
		if (currTree == 0)
			secretE_P_pp = secretE_Ti_p;

		// EncryptPath
		EncryptPath ep = new EncryptPath(charlie, debbie);
		ep.loadTreeSpecificParameters(currTree);
		EPath EPOut = ep.executeEddieSubTree(charlie, debbie, secretE_P_pp);

		// put encrypted path back to tree
		Bucket[] buckets = new Bucket[EPOut.x.length];
		for (int j = 0; j < EPOut.x.length; j++) {
			try {
				buckets[j] = new Bucket(currTree, EPOut.x[j].getEncoded(),
						Util.rmSignBit(EPOut.Bbar[j].toByteArray()));
			} catch (BucketException e) {
				e.printStackTrace();
			}
		}
		try {
			OT.setBucketsOnPath(buckets, Li);
		} catch (TreeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run(Party party, Forest forest) throws ForestException {
		long numInsert = Math.min(ForestMetadata.getNumInsert(),
				ForestMetadata.getAddressSpace());
		if (numInsert == 0L) {
			System.err.println("No record in the forest");
			return;
		}

		if (ifSanityCheck())
			System.out.println("Sanity check enabled\n");

		timing = new Timing();
		timing.init();
		con1.bandWidthSwitch = true;
		con2.bandWidthSwitch = true;

		int h = ForestMetadata.getLevels() - 1;
		int tau = ForestMetadata.getTau();
		int lastNBits = ForestMetadata.getLastNBits();
		int shiftN = lastNBits % tau;
		if (shiftN == 0)
			shiftN = tau;

		int records = 6; // how many random records we want to test retrieval
		int retrievals = 5; // for each record, how many repeated retrievals we
							// want to do

		for (int test = 0; test < records; test++) {
			BigInteger N = null;
			if (party == Party.Charlie) {
				if (numInsert == -1)
					N = new BigInteger(lastNBits, SR.rand);
				else
					N = Util.nextBigInteger(BigInteger.valueOf(numInsert));
			}

			for (long exec = 0; exec < retrievals; exec++) {
				// pre-computation
				if (party == Party.Charlie)
					new Precomputation(con1, con2).executeCharlieSubTree(con1,
							con2, null);
				else if (party == Party.Debbie)
					new Precomputation(con1, con2).executeDebbieSubTree(con1,
							con2, null);
				else if (party == Party.Eddie)
					new Precomputation(con1, con2).executeEddieSubTree(con1,
							con2, null);
				else {
					System.err.println("No such party");
					return;
				}

				BigInteger Li = null;
				if (party == Party.Charlie)
					System.out.println(test + ": stored record is: "
							+ N.longValue() + " (N="
							+ Util.addZero(N.toString(2), lastNBits) + ")");
				System.out.println("Execution cycle: " + exec);

				for (int i = 0; i <= h; i++) {
					currTree = i;

					switch (party) {
					case Charlie:
						// String Ni = N;
						BigInteger Ni;
						if (i < h - 1) {
							Ni = Util.getSubBits(N, lastNBits - (i + 1) * tau,
									lastNBits);
						} else {
							Ni = N;
						}

						System.out.println("i="
								+ i
								+ ", Li="
								+ (Li == null ? "" : Util.addZero(
										Li.toString(2),
										ForestMetadata.getLBits(i))));
						con2.write(Li);
						BigInteger[] outC = executeCharlie(con1, con2, Li, Ni);
						Li = outC[0];
						if (i == h) {
							BigInteger D = Util.getSubBits(outC[1], 0,
									ForestMetadata.getDataSize() * 8);
							System.out.println("Retrieved record is: " + D);
							System.out.println("Is record correct: "
									+ (D.compareTo(D) == 0 ? "YES" : "NO!!")
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
						executeDebbie(con1, con2, null);
						break;
					case Eddie:
						Li = con1.readBigInteger();
						executeEddie(con1, con2, forest.getTree(currTree), (Li));
						break;
					}
				}

				// only need to count bandwidth once
				con1.bandWidthSwitch = false;
				con2.bandWidthSwitch = false;
			}

			if (test == 0 && records > 1)
				timing.init(); // abandon the timing of the first several
								// retrievals
		}

		// average timing
		int cycles = 0;
		if (records > 1)
			cycles = (records - 1) * retrievals; // first round is abandoned
		else if (records == 1)
			cycles = retrievals;
		else
			return;

		try {
			switch (party) {
			case Charlie:
				break;
			case Debbie:
				timing.stopwatch[PID.gcf][TID.online] = timing.stopwatch[PID.gcf][TID.online]
						.subtract(timing.stopwatch[PID.gcf][TID.offline_read]);
				break;
			case Eddie:
				timing.stopwatch[PID.gcf][TID.online] = timing.stopwatch[PID.gcf][TID.online]
						.subtract(timing.stopwatch[PID.gcf][TID.offline_write]);
				break;
			}

			int t = ForestMetadata.getTau();
			int n = ForestMetadata.getLastNBits();
			int w = ForestMetadata.getBucketDepth();
			int d = ForestMetadata.getDataSize();

			timing.divide(cycles);
			timing.writeToFile("stats/timing-" + party + "-t" + t + "n" + n
					+ "w" + w + "d" + d);
			con1.writeBandwidthToFile("stats/" + party + "-bandwidth-1" + "-t"
					+ t + "n" + n + "w" + w + "d" + d);
			con2.writeBandwidthToFile("stats/" + party + "-bandwidth-2" + "-t"
					+ t + "n" + n + "w" + w + "d" + d);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
