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
import sprout.util.StopWatch;
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
		PostProcessT ppt = new PostProcessT(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree]);
		ppt.loadTreeSpecificParameters(currTree);
		
		//sanityCheck();
		BigInteger secretC_Ti_p = ppt.executeCharlieSubTree(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree],
				new BigInteger[] { Li, secretC_Ti, secretC_Li_p,
						secretC_Lip1_p, Lip1, Nip1_pr });

		// Reshuffle
		BigInteger secretC_P_p = AOut.secretC_P_p;
		Reshuffle rs = new Reshuffle(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree]);
		rs.loadTreeSpecificParameters(currTree);
		List<Integer> tmp = null;
		BigInteger secretC_pi_P = rs.executeCharlieSubTree(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree],
				Pair.of(secretC_P_p, tmp));

		// Eviction
		Eviction evict = new Eviction(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree]);
		evict.loadTreeSpecificParameters(currTree);
		BigInteger secretC_P_pp = evict.executeCharlieSubTree(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree],
				new BigInteger[] { secretC_pi_P, secretC_Ti_p });
		if (currTree == 0)
			secretC_P_pp = secretC_Ti_p;

		// EncryptPath
		EncryptPath ep = new EncryptPath(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree]);
		ep.loadTreeSpecificParameters(currTree);
		ep.executeCharlieSubTree(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree], secretC_P_pp);

		return output;
	}

	public void executeDebbie(Communication charlie, Communication eddie,
			BigInteger k) {
		// Access
		Access access = new Access(charlie, eddie);
		access.loadTreeSpecificParameters(currTree);
		access.executeDebbieSubTree(charlie, eddie, new BigInteger[] { k });
		
		//System.out.println("Debbie: cycle " + currTree + " finished!!!!!!!!!!");
		
		PPEvict thread = new PPEvict(Party.Debbie, null, null, new BigInteger[] { k }, currTree);
		thread.start();

		/*
		// PostProcessT
		PostProcessT ppt = new PostProcessT(charlie, eddie);
		ppt.loadTreeSpecificParameters(currTree);
		
		//sanityCheck();
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
		*/
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
		PostProcessT ppt = new PostProcessT(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree]);
		ppt.loadTreeSpecificParameters(currTree);
		
		//sanityCheck();
		BigInteger secretE_Ti_p = ppt.executeEddieSubTree(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree],
				new BigInteger[] { secretE_Ti, secretE_Li_p, secretE_Lip1_p });

		// Reshuffle
		BigInteger secretE_P_p = AOut.secretE_P_p;
		List<Integer> tmp = null;
		Reshuffle rs = new Reshuffle(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree]);
		rs.loadTreeSpecificParameters(currTree);
		BigInteger secretE_pi_P = rs.executeEddieSubTree(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree],
				Pair.of(secretE_P_p, tmp));

		// Eviction
		Eviction evict = new Eviction(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree]);
		evict.loadTreeSpecificParameters(currTree);
		BigInteger secretE_P_pp = evict.executeEddieSubTree(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree],
				new BigInteger[] { secretE_pi_P, secretE_Ti_p, Li });
		if (currTree == 0)
			secretE_P_pp = secretE_Ti_p;

		// EncryptPath
		EncryptPath ep = new EncryptPath(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree]);
		ep.loadTreeSpecificParameters(currTree);
		EPath EPOut = ep.executeEddieSubTree(PPEvict.threadCon1[currTree], PPEvict.threadCon2[currTree], secretE_P_pp);

		// put encrypted path back to tree
		Bucket[] buckets = new Bucket[EPOut.x.length];
		timing.stopwatch[PID.encrypt][TID.online].start();
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
		timing.stopwatch[PID.encrypt][TID.online].stop();
	}

	@Override
	public void run(Party party, Forest forest) throws ForestException {
		int records = 4; // how many random records we want to test retrieval
		int retrievals = 8; // for each record, how many repeated retrievals we
							// want to do

		// average timing
		int cycles = 0;
		if (records > 1)
			cycles = (records - 1) * retrievals; // first round is abandoned
		else if (records == 1)
			cycles = retrievals;
		else
			return;
		
		
		
		long numInsert = Math.min(ForestMetadata.getNumInsert(),
				ForestMetadata.getAddressSpace());
		if (numInsert == 0L) {
			System.err.println("No record in the forest");
			return;
		}

		if (ifSanityCheck())
			System.out.println("Sanity check enabled\n");

		timing = new Timing();
		//timing.init();
		
		Timing[] individualTiming = new Timing[cycles];
		Timing wholeTiming = new Timing();

		int h = ForestMetadata.getLevels() - 1;
		int tau = ForestMetadata.getTau();
		int lastNBits = ForestMetadata.getLastNBits();
		int shiftN = lastNBits % tau;
		if (shiftN == 0)
			shiftN = tau;
		
		StopWatch wholeExecution = new StopWatch("Whole Execution");
		//wholeAccess.start();

		for (int test = 0; test < records; test++) {
			BigInteger N = null;
			if (party == Party.Charlie) {
				if (numInsert == -1)
					N = new BigInteger(lastNBits, SR.rand);
				else
					N = Util.nextBigInteger(BigInteger.valueOf(numInsert));
			}

			for (long exec = 0; exec < retrievals; exec++) {
				timing.init();
				
				if (test == 0 && exec == 0) {
					con1.bandWidthSwitch = true;
					con2.bandWidthSwitch = true;
				}
				
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
				
				sanityCheck();

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
				
				// get individual timing
				if (test > 0) {
					individualTiming[(int) ((test-1)*retrievals+exec)] = new Timing(timing);
					individualTiming[(int) ((test-1)*retrievals+exec)].divide(1000000);
					wholeTiming = wholeTiming.add(timing);
				}
			}

			if (test == 0 && records > 1) {
				//timing.init(); // abandon the timing of the first several
								// retrievals
				wholeExecution.start();
			}
		}
		
		wholeExecution.stop();
		wholeTiming.divide(cycles);
		Timing avgTiming = new Timing(wholeTiming);
		avgTiming.divide(1000000);
		
		
		StopWatch avgOffline = avgTiming.groupOffline();
		StopWatch avgOffline_write = avgTiming.groupOffline_write();
		StopWatch avgOffline_read = avgTiming.groupOffline_read();
		StopWatch avgAccess = avgTiming.groupAccess();
		StopWatch avgAccess_write = avgTiming.groupAccess_write();
		StopWatch avgAccess_read = avgTiming.groupAccess_read();
		StopWatch avgPE = avgTiming.groupPE();
		StopWatch avgPE_write = avgTiming.groupPE_write();
		StopWatch avgPE_read = avgTiming.groupPE_read();
		
		StopWatch[] indOffline = new StopWatch[cycles];
		StopWatch[] indOffline_write = new StopWatch[cycles];
		StopWatch[] indOffline_read = new StopWatch[cycles];
		StopWatch[] indAccess = new StopWatch[cycles];
		StopWatch[] indAccess_write = new StopWatch[cycles];
		StopWatch[] indAccess_read = new StopWatch[cycles];
		StopWatch[] indPE = new StopWatch[cycles];
		StopWatch[] indPE_write = new StopWatch[cycles];
		StopWatch[] indPE_read = new StopWatch[cycles];
		for (int i=0; i<cycles; i++) {
			indOffline[i] = individualTiming[i].groupOffline();
			indOffline_write[i] = individualTiming[i].groupOffline_write();
			indOffline_read[i] = individualTiming[i].groupOffline_read();
			indAccess[i] = individualTiming[i].groupAccess();
			indAccess_write[i] = individualTiming[i].groupAccess_write();
			indAccess_read[i] = individualTiming[i].groupAccess_read();
			indPE[i] = individualTiming[i].groupPE();
			indPE_write[i] = individualTiming[i].groupPE_write();
			indPE_read[i] = individualTiming[i].groupPE_read();
		}
		
		StopWatch varOffline = getSTD(avgOffline, indOffline);
		StopWatch varOffline_write = getSTD(avgOffline_write, indOffline_write);
		StopWatch varOffline_read = getSTD(avgOffline_read, indOffline_read);
		StopWatch varAccess = getSTD(avgAccess, indAccess);
		StopWatch varAccess_write = getSTD(avgAccess_write, indAccess_write);
		StopWatch varAccess_read = getSTD(avgAccess_read, indAccess_read);
		StopWatch varPE = getSTD(avgPE, indPE);
		StopWatch varPE_write = getSTD(avgPE_write, indPE_write);
		StopWatch varPE_read = getSTD(avgPE_read, indPE_read);
		
		System.out.println("\n######### AVERAGE AND STD DEVIATION SECTION ###########\n");
		System.out.println(avgOffline.toNumber());
		System.out.println(avgOffline_write.toNumber());
		System.out.println(avgOffline_read.toNumber());
		System.out.println();
		System.out.println(varOffline.toNumber());
		System.out.println(varOffline_write.toNumber());
		System.out.println(varOffline_read.toNumber());
		System.out.println();
		
		System.out.println(avgAccess.toNumber());
		System.out.println(avgAccess_write.toNumber());
		System.out.println(avgAccess_read.toNumber());
		System.out.println();
		System.out.println(varAccess.toNumber());
		System.out.println(varAccess_write.toNumber());
		System.out.println(varAccess_read.toNumber());
		System.out.println();
		
		System.out.println(avgPE.toNumber());
		System.out.println(avgPE_write.toNumber());
		System.out.println(avgPE_read.toNumber());
		System.out.println();		
		System.out.println(varPE.toNumber());
		System.out.println(varPE_write.toNumber());
		System.out.println(varPE_read.toNumber());
		System.out.println();
		
		System.out.println("\n######### BANDWIDTH SECTION ###########\n");
		int peBW = 0;
		peBW += con1.bandwidth[PID.ppt].bandwidth;
		peBW += con1.bandwidth[PID.reshuffle].bandwidth;
		peBW += con1.bandwidth[PID.eviction].bandwidth;
		peBW += con1.bandwidth[PID.encrypt].bandwidth;
		peBW += con2.bandwidth[PID.ppt].bandwidth;
		peBW += con2.bandwidth[PID.reshuffle].bandwidth;
		peBW += con2.bandwidth[PID.eviction].bandwidth;
		peBW += con2.bandwidth[PID.encrypt].bandwidth;
		int accessBW = con1.bandwidth[PID.access].bandwidth + con2.bandwidth[PID.access].bandwidth;
		int gcfBW = con1.bandwidth[PID.gcf].bandwidth + con2.bandwidth[PID.gcf].bandwidth;
		int precomputationBW = con1.bandwidth[PID.pre].bandwidth + con2.bandwidth[PID.pre].bandwidth;
		System.out.println(precomputationBW * 8);
		System.out.println(accessBW * 8);
		System.out.println(peBW * 8);
		System.out.println(gcfBW * 8);
		System.out.println();
		
		System.out.println("\n######### WHOLE EXECUTION TIMING SECTION ###########\n");
		System.out.println(wholeExecution.afterConversion());
		System.out.println();
		

		int t = ForestMetadata.getTau();
		int n = ForestMetadata.getLastNBits();
		int w = ForestMetadata.getBucketDepth();
		int d = ForestMetadata.getDataSize();			
			
		try {
			wholeTiming.writeToFile("stats/timing-" + party + "-t" + t + "n" + n
					+ "w" + w + "d" + d);
			con1.writeBandwidthToFile("stats/" + party + "-bandwidth-1" + "-t"
					+ t + "n" + n + "w" + w + "d" + d);
			con2.writeBandwidthToFile("stats/" + party + "-bandwidth-2" + "-t"
					+ t + "n" + n + "w" + w + "d" + d);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public StopWatch getSTD(StopWatch avg, StopWatch[] ind) {
		int n = ind.length;
		StopWatch var = new StopWatch();
		long sumWall = 0L;
		long sumCPU = 0L;
		for (int i=0; i<n; i++) {
			sumWall += (long) Math.pow(avg.elapsedWallClockTime-ind[i].elapsedWallClockTime, 2);
			sumCPU += (long) Math.pow(avg.elapsedCPUTime-ind[i].elapsedCPUTime, 2);
		}
		var.elapsedWallClockTime = (long) Math.sqrt(sumWall / n);
		var.elapsedCPUTime = (long) Math.sqrt(sumCPU / n);
		return var;
	}

}
