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

//TODO: fix indenting in fastergc

public class Retrieve extends Operation {

	//private StopWatch indParallelPE = new StopWatch("Individual Paralleled PP + Evict");

	public Retrieve(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public Pair<BigInteger[], PPEvict> executeCharlie(Communication debbie,
			Communication eddie, int i, BigInteger Li, BigInteger sC_Nip1) {
		// Access
		Access access = new Access(debbie, eddie);
		access.loadTreeSpecificParameters(i);
		AOutput AOut = access.executeCharlieSubTree(debbie, eddie, null,
				new BigInteger[] { Li, sC_Nip1 }, null);
		BigInteger[] output = new BigInteger[] { AOut.Lip1, AOut.data };

		PostProcessT ppt = new PostProcessT(debbie, eddie);
		ppt.loadTreeSpecificParameters(i);
		BigInteger sC_Ti_p = ppt.executeCharlieSubTree(debbie, eddie, null, new BigInteger[]{AOut.sC_Ti, Li, AOut.Lip1, AOut.j_2}, timing);

		Reshuffle res = new Reshuffle(debbie, eddie);
		res.loadTreeSpecificParameters(i);
		BigInteger sC_pi_P = res.executeCharlieSubTree(debbie, eddie, null, AOut.sC_sig_P_p, timing);

		Eviction evict = new Eviction(debbie, eddie);
		evict.loadTreeSpecificParameters(i);
		evict.executeCharlieSubTree(debbie, eddie, null, new BigInteger[]{sC_pi_P, sC_Ti_p}, timing);
		
		// PP+Evict
		//Timing localTiming = new Timing();
		//PPEvict thread = new PPEvict(Party.Charlie, AOut, null, new BigInteger[] { Li, Nip1 }, currTree, localTiming);
		//if (currTree == 0)
			//indParallelPE.start();
		//thread.start();
		
		//return Pair.of(output, thread);
		return Pair.of(output, null);
	}

	public PPEvict executeDebbie(Communication charlie, Communication eddie, int i, Tree sD_OT) {
		// Access
		Access access = new Access(charlie, eddie);
		access.loadTreeSpecificParameters(i);
		access.executeDebbieSubTree(charlie, eddie, sD_OT, null, null);

		PostProcessT ppt = new PostProcessT(charlie, eddie);
		ppt.loadTreeSpecificParameters(i);
		ppt.executeDebbieSubTree(charlie, eddie, null, null, timing);

		Reshuffle res = new Reshuffle(charlie, eddie);
		res.loadTreeSpecificParameters(i);
		res.executeDebbieSubTree(charlie, eddie, null, null, timing);

		Eviction evict = new Eviction(charlie, eddie);
		evict.loadTreeSpecificParameters(i);
		evict.executeDebbieSubTree(charlie, eddie, sD_OT, null, timing);
		
		// PP+Evictv
		//Timing localTiming = new Timing();
		//PPEvict thread = new PPEvict(Party.Debbie, null, null, new BigInteger[] { k }, currTree, localTiming);
		//if (currTree == 0)
			//indParallelPE.start();
		//thread.start();
		
		//return thread;
		return null;
	}

	public PPEvict executeEddie(Communication charlie, Communication debbie, int i, Tree sE_OT, BigInteger sE_Nip1) {
		// Access
		Access access = new Access(charlie, debbie);
		access.loadTreeSpecificParameters(i);
		AOutput AOut = access.executeEddieSubTree(charlie, debbie, sE_OT, new BigInteger[] {sE_Nip1}, null);

		PostProcessT ppt = new PostProcessT(charlie, debbie);
		ppt.loadTreeSpecificParameters(i);
		BigInteger sE_Ti_p = ppt.executeEddieSubTree(charlie, debbie, null, new BigInteger[]{AOut.sE_Ti}, timing);

		Reshuffle res = new Reshuffle(charlie, debbie);
		res.loadTreeSpecificParameters(i);
		BigInteger sE_pi_P = res.executeEddieSubTree(charlie, debbie, null, AOut.sE_sig_P_p, timing);

		Eviction evict = new Eviction(charlie, debbie);
		evict.loadTreeSpecificParameters(i);
		evict.executeEddieSubTree(charlie, debbie, sE_OT, new BigInteger[]{sE_pi_P, sE_Ti_p}, timing);
		
				
		// PP+Evict
		//Timing localTiming = new Timing();
		//PPEvict thread = new PPEvict(Party.Eddie, AOut, OT, new BigInteger[] { Li }, currTree, localTiming);
		//if (currTree == 0)
			//indParallelPE.start();
		//thread.start();
		
		//return thread;
		return null;
	}

	@Override
	public void run(Party party, Forest forest) throws ForestException {
		int records = 1; // how many random records we want to test retrieval
		int retrievals = 100; // for each record, how many repeated retrievals we
							// want to do
		/*
		if (records < 2) {
			System.err.println("Number of records must be at least 2 for average timing");
			return;
		}
		else if (retrievals < 1) {
			System.err.println("Number of retrievals must be at least 1");
			return;
		}
		*/
		
		long numInsert = Math.min(ForestMetadata.getNumInsert(),
				ForestMetadata.getAddressSpace());
		if (numInsert == 0L) {
			System.err.println("No record in the forest");
			return;
		}
		
		//int cycles = (records - 1) * retrievals; // first round timing is abandoned	
		int numTrees = ForestMetadata.getLevels();
		int h = numTrees - 1;
		int tau = ForestMetadata.getTau();
		int lastNBits = ForestMetadata.getLastNBits();
		int shiftN = lastNBits % tau;
		if (shiftN == 0)
			shiftN = tau;	

		// timing stuff
		timing = new Timing();
		
		/*
		Timing[] individualTiming = new Timing[cycles];
		Timing wholeTiming = new Timing();
		
		StopWatch wholeExecution = new StopWatch("Whole Execution");
		StopWatch avgParallelPE = new StopWatch("Average Paralleled PP + Evict");
		*/
		
		// threads init
		PPEvict[] threads = new PPEvict[numTrees];
		
		// turn on bandwidth measurement
		//con1.bandWidthSwitch = true;
		//con2.bandWidthSwitch = true;

		// sync TODO: check where?
		if (ifSanityCheck())
			System.out.println("Sanity check enabled\n");

		StopWatch bp_whole = new StopWatch("ballpark_whole");
		StopWatch bp_online = new StopWatch("ballpark_online");
		
		////////////////////////////////////////////
		////////   main execution starts   /////////
		////////////////////////////////////////////
		
		for (int rec = 0; rec < records; rec++) {
			// retrieve a record by picking a random N
			BigInteger N = null;
			BigInteger sC_N = null;
			BigInteger sE_N = null;
			if (party == Party.Charlie) {
				if (numInsert == -1) {
					N = new BigInteger(lastNBits, SR.rand);
					sC_N = new BigInteger(lastNBits, SR.rand);
				}
				else {
					N = Util.nextBigInteger(BigInteger.valueOf(numInsert));
					sC_N = Util.nextBigInteger(BigInteger.valueOf(numInsert));
				}
				
				//debug
				//N = BigInteger.valueOf(3);
				
				sE_N = N.xor(sC_N);
				con2.write(sE_N);
			}
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
				
				// sync so online protocols for all parties start at the same time
				sanityCheck();
				
				bp_online.start();

				// for each retrieval, execute protocols on each tree
				BigInteger Li = null;
				for (int i = 0; i < numTrees; i++) {
					switch (party) {
					case Charlie:
						BigInteger sC_Ni;
						if (i < h - 1) {
							sC_Ni = Util.getSubBits(sC_N, lastNBits - (i + 1) * tau,
									lastNBits);
						} else 
							sC_Ni = sC_N;

						System.out.println("i="
								+ i
								+ ", Li="
								+ (Li == null ? "" : Util.addZero(
										Li.toString(2),
										ForestMetadata.getLBits(i))));
						//con2.write(Li);
						Pair<BigInteger[], PPEvict> outPair = executeCharlie(con1, con2, i, Li, sC_Ni);
						BigInteger[] outC = outPair.getLeft();
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
						threads[i] = outPair.getRight();
						break;
					case Debbie:
						threads[i] = executeDebbie(con1, con2, i, forest.getTree(i));
						break;
					case Eddie:
						//Li = con1.readBigInteger();
						BigInteger sE_Ni;
						if (i < h - 1) {
							sE_Ni = Util.getSubBits(sE_N, lastNBits - (i + 1) * tau, lastNBits);
						} else 
							sE_Ni = sE_N;
						threads[i] = executeEddie(con1, con2, i, forest.getTree(i), sE_Ni);
						break;
					}
				}
				
				// wait for all threads to terminate
				// so timing data can be gathered
				/*
				for (int i = 0; i < numTrees; i++)
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					*/
				//indParallelPE.stop();
				
				// get individual timing
				/*
				if (rec > 0) {
					for (int i = 0; i < numTrees; i++)
						timing = timing.add(threads[i].getTiming());
					individualTiming[(int) ((rec-1)*retrievals+retri)] = new Timing(timing);
					individualTiming[(int) ((rec-1)*retrievals+retri)].divide(1000000);
					wholeTiming = wholeTiming.add(timing);
					avgParallelPE = avgParallelPE.add(indParallelPE);
				}
				*/
				//indParallelPE.reset();

				// only need to count bandwidth once
				con1.bandWidthSwitch = false;
				con2.bandWidthSwitch = false;
				
				bp_online.stop();
				bp_whole.stop();
				
				if (retri == (retrievals / 2) -1 ) {
					bp_online.reset();
					bp_whole.reset();
					timing.reset();
				}
			}

			// abandon the timing of the first several retrievals
			// assert records > 1
			if (rec == 0) {
				;//wholeExecution.start();
				//bp_online.reset();
				//bp_whole.reset();
			}
		}
		
		System.out.println(bp_whole.toTab());
		System.out.println(bp_online.toTab());
		
		System.out.println("-------------------------");
		System.out.println(timing.toTab());
		
		/*
		wholeExecution.stop();
		wholeTiming.divide(cycles);
		Timing avgTiming = new Timing(wholeTiming);
		avgTiming.divide(1000000);
		avgParallelPE.divide(cycles);
		
		
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
		
		System.out.println("\n######### PARALLEL PP+EVICT TIMING SECTION ###########\n");
		System.out.println(avgParallelPE.elapsedWallClockTime / StopWatch.convert);
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
		*/
	}
	
	/*
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
	 */
}
