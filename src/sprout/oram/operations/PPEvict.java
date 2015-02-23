package sprout.oram.operations;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import sprout.communication.Communication;
import sprout.oram.Bucket;
import sprout.oram.BucketException;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.oram.Tree;
import sprout.oram.TreeException;
import sprout.util.Util;

public class PPEvict extends Thread {
	public static Communication[] threadCon1;
	public static Communication[] threadCon2;

	private Party p;
	private AOutput AOut;
	private Tree OT;
	private BigInteger[] extraArgs;
	private int currTree;

	public PPEvict(Party p, AOutput AOut, Tree OT, BigInteger[] extraArgs,
			int currTree) {
		this.p = p;
		this.AOut = AOut;
		this.OT = OT;
		this.extraArgs = extraArgs;
		// this.con1 = con1;
		// this.con2 = con2;
		this.currTree = currTree;
		// this.ppt = ppt;
		// this.rs = rs;
		// this.evict = evict;
		// this.ep = ep;
	}

	public void run() {
		if (p == Party.Charlie)
			runCharlie();
		else if (p == Party.Debbie)
			runDebbie();
		else if (p == Party.Eddie)
			runEddie();
	}

	private void runCharlie() {
		BigInteger Li = extraArgs[0];
		BigInteger Nip1 = extraArgs[1];
		
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
		PostProcessT ppt = new PostProcessT(threadCon1[currTree],
				threadCon2[currTree]);
		ppt.loadTreeSpecificParameters(currTree);

		// sanityCheck();
		BigInteger secretC_Ti_p = ppt.executeCharlieSubTree(
				threadCon1[currTree], threadCon2[currTree],
				new BigInteger[] { Li, secretC_Ti, secretC_Li_p,
						secretC_Lip1_p, Lip1, Nip1_pr });

		// Reshuffle
		BigInteger secretC_P_p = AOut.secretC_P_p;
		Reshuffle rs = new Reshuffle(threadCon1[currTree],
				threadCon2[currTree]);
		rs.loadTreeSpecificParameters(currTree);
		List<Integer> tmp = null;
		BigInteger secretC_pi_P = rs.executeCharlieSubTree(
				threadCon1[currTree], threadCon2[currTree],
				Pair.of(secretC_P_p, tmp));

		// Eviction
		Eviction evict = new Eviction(threadCon1[currTree],
				threadCon2[currTree]);
		evict.loadTreeSpecificParameters(currTree);
		BigInteger secretC_P_pp = evict.executeCharlieSubTree(
				threadCon1[currTree], threadCon2[currTree],
				new BigInteger[] { secretC_pi_P, secretC_Ti_p });
		if (currTree == 0)
			secretC_P_pp = secretC_Ti_p;

		// EncryptPath
		EncryptPath ep = new EncryptPath(threadCon1[currTree],
				threadCon2[currTree]);
		ep.loadTreeSpecificParameters(currTree);
		ep.executeCharlieSubTree(threadCon1[currTree],
				threadCon2[currTree], secretC_P_pp);
	}

	private void runDebbie() {
		BigInteger k = extraArgs[0];
		
		// PostProcessT
		PostProcessT ppt = new PostProcessT(threadCon1[currTree],
				threadCon2[currTree]);
		ppt.loadTreeSpecificParameters(currTree);

		// sanityCheck();
		ppt.executeDebbieSubTree(threadCon1[currTree], threadCon2[currTree],
				new BigInteger[] {});

		// Reshuffle
		Reshuffle rs = new Reshuffle(threadCon1[currTree], threadCon2[currTree]);
		rs.loadTreeSpecificParameters(currTree);
		BigInteger tmp = null;
		List<Integer> tmp2 = null;
		rs.executeDebbieSubTree(threadCon1[currTree], threadCon2[currTree],
				Pair.of(tmp, tmp2));

		// Eviction
		Eviction evict = new Eviction(threadCon1[currTree],
				threadCon2[currTree]);
		evict.loadTreeSpecificParameters(currTree);
		evict.executeDebbieSubTree(threadCon1[currTree], threadCon2[currTree],
				new BigInteger[] {});

		// EncryptPath
		EncryptPath ep = new EncryptPath(threadCon1[currTree],
				threadCon2[currTree]);
		ep.loadTreeSpecificParameters(currTree);
		ep.executeDebbieSubTree(threadCon1[currTree], threadCon2[currTree],
				k);
	}

	private void runEddie() {
		BigInteger Li = extraArgs[0];
		
		// PostProcessT
		BigInteger secretE_Ti = AOut.secretE_Ti;
		BigInteger secretE_Li_p = PreData.ppt_sE_Li_p[currTree];
		BigInteger secretE_Lip1_p = null;
		if (currTree < ForestMetadata.getLevels() - 1)
			secretE_Lip1_p = PreData.ppt_sE_Li_p[currTree + 1];
		PostProcessT ppt = new PostProcessT(threadCon1[currTree],
				threadCon2[currTree]);
		ppt.loadTreeSpecificParameters(currTree);

		// sanityCheck();
		BigInteger secretE_Ti_p = ppt.executeEddieSubTree(threadCon1[currTree],
				threadCon2[currTree], new BigInteger[] { secretE_Ti,
						secretE_Li_p, secretE_Lip1_p });

		// Reshuffle
		BigInteger secretE_P_p = AOut.secretE_P_p;
		List<Integer> tmp = null;
		Reshuffle rs = new Reshuffle(threadCon1[currTree], threadCon2[currTree]);
		rs.loadTreeSpecificParameters(currTree);
		BigInteger secretE_pi_P = rs.executeEddieSubTree(threadCon1[currTree],
				threadCon2[currTree], Pair.of(secretE_P_p, tmp));

		// Eviction
		Eviction evict = new Eviction(threadCon1[currTree],
				threadCon2[currTree]);
		evict.loadTreeSpecificParameters(currTree);
		BigInteger secretE_P_pp = evict.executeEddieSubTree(
				threadCon1[currTree], threadCon2[currTree], new BigInteger[] {
						secretE_pi_P, secretE_Ti_p, Li });
		if (currTree == 0)
			secretE_P_pp = secretE_Ti_p;

		// EncryptPath
		EncryptPath ep = new EncryptPath(threadCon1[currTree],
				threadCon2[currTree]);
		ep.loadTreeSpecificParameters(currTree);
		EPath EPOut = ep.executeEddieSubTree(threadCon1[currTree],
				threadCon2[currTree], secretE_P_pp);

		// put encrypted path back to tree
		Bucket[] buckets = new Bucket[EPOut.x.length];
		// timing.stopwatch[PID.encrypt][TID.online].start();
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
		// timing.stopwatch[PID.encrypt][TID.online].stop();
	}

	public static void main(String args[]) {
		/*
		 * PPEvict thread = new PPEvict(1); thread.start();
		 * System.out.println("should print first"); try { Thread.sleep(2000);
		 * thread.join(); } catch (InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 * System.out.println("all done");
		 */
	}
}
