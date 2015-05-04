package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.util.Timing;

public class PPEvict extends Thread {
	public static Communication[] threadCon1;
	public static Communication[] threadCon2;

	private Party p;
	private Tree OT;
	private BigInteger[] extraArgs;
	private int currTree;
	private Timing localTiming;

	public PPEvict(Party p, int currTree, Tree OT, Timing localTiming, BigInteger[] extraArgs) {
		this.p = p;
		this.OT = OT;
		this.localTiming = localTiming;
		this.extraArgs = extraArgs;
		this.currTree = currTree;
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
		BigInteger AOut_sC_Ti = extraArgs[0];
		BigInteger Li = extraArgs[1];
		BigInteger AOut_Lip1 = extraArgs[2];
		BigInteger AOut_j_2 = extraArgs[3];
		BigInteger AOut_sC_sig_P_p = extraArgs[4];
		
		PostProcessT ppt = new PostProcessT(threadCon1[currTree], threadCon2[currTree]);
		ppt.loadTreeSpecificParameters(currTree);
		BigInteger sC_Ti_p = ppt.executeCharlieSubTree(threadCon1[currTree], threadCon2[currTree], null, new BigInteger[]{AOut_sC_Ti, Li, AOut_Lip1, AOut_j_2}, localTiming);

		Reshuffle res = new Reshuffle(threadCon1[currTree], threadCon2[currTree]);
		res.loadTreeSpecificParameters(currTree);
		BigInteger sC_pi_P = res.executeCharlieSubTree(threadCon1[currTree], threadCon2[currTree], null, AOut_sC_sig_P_p, localTiming);

		Eviction evict = new Eviction(threadCon1[currTree], threadCon2[currTree]);
		evict.loadTreeSpecificParameters(currTree);
		evict.executeCharlieSubTree(threadCon1[currTree], threadCon2[currTree], null, new BigInteger[]{sC_pi_P, sC_Ti_p}, localTiming);
	}
	

	private void runDebbie() {
		PostProcessT ppt = new PostProcessT(threadCon1[currTree], threadCon2[currTree]);
		ppt.loadTreeSpecificParameters(currTree);
		ppt.executeDebbieSubTree(threadCon1[currTree], threadCon2[currTree], null, null, localTiming);

		Reshuffle res = new Reshuffle(threadCon1[currTree], threadCon2[currTree]);
		res.loadTreeSpecificParameters(currTree);
		res.executeDebbieSubTree(threadCon1[currTree], threadCon2[currTree], null, null, localTiming);

		Eviction evict = new Eviction(threadCon1[currTree], threadCon2[currTree]);
		evict.loadTreeSpecificParameters(currTree);
		evict.executeDebbieSubTree(threadCon1[currTree], threadCon2[currTree], OT, null, localTiming);
	}

	private void runEddie() {
		BigInteger AOut_sE_Ti = extraArgs[0];
		BigInteger AOut_sE_sig_P_p = extraArgs[1];
		
		PostProcessT ppt = new PostProcessT(threadCon1[currTree], threadCon2[currTree]);
		ppt.loadTreeSpecificParameters(currTree);
		BigInteger sE_Ti_p = ppt.executeEddieSubTree(threadCon1[currTree], threadCon2[currTree], null, new BigInteger[]{AOut_sE_Ti}, localTiming);

		Reshuffle res = new Reshuffle(threadCon1[currTree], threadCon2[currTree]);
		res.loadTreeSpecificParameters(currTree);
		BigInteger sE_pi_P = res.executeEddieSubTree(threadCon1[currTree], threadCon2[currTree], null, AOut_sE_sig_P_p, localTiming);

		Eviction evict = new Eviction(threadCon1[currTree], threadCon2[currTree]);
		evict.loadTreeSpecificParameters(currTree);
		evict.executeEddieSubTree(threadCon1[currTree], threadCon2[currTree], OT, new BigInteger[]{sE_pi_P, sE_Ti_p}, localTiming);
	}
	
	public Timing getTiming() {
		return localTiming;
	}
/*
	public static void main(String args[]) {
		
		 PPEvict thread = new PPEvict(1); thread.start();
		 System.out.println("should print first"); try { Thread.sleep(2000);
		 thread.join(); } catch (InterruptedException e) { // TODO
		 Auto-generated catch block e.printStackTrace(); }
		 System.out.println("all done");
		 
	}
*/
}
