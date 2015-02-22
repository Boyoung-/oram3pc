package sprout.oram.operations;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import sprout.communication.Communication;
import sprout.oram.Party;
import sprout.oram.Tree;

public class PPEvict extends Thread {
	public static Communication[] threadCon1;
	public static Communication[] threadCon2;	
	
	private Party p;
	private AOutput AOut;
	private Tree OT;
	private BigInteger[] extraArgs;
	//private Communication con1;
	//private Communication con2;
	private int currTree;

	// private PostProcessT ppt;
	// private Reshuffle rs;
	// private Eviction evict;
	// private EncryptPath ep;

	public PPEvict(Party p, AOutput AOut, Tree OT, BigInteger[] extraArgs, int currTree) {
		this.p = p;
		this.AOut = AOut;
		this.OT = OT;
		this.extraArgs = extraArgs;
		//this.con1 = con1;
		//this.con2 = con2;
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

	}

	private void runDebbie() {
		// PostProcessT
		PostProcessT ppt = new PostProcessT(threadCon1[currTree], threadCon2[currTree]);
		ppt.loadTreeSpecificParameters(currTree);

		// sanityCheck();
		ppt.executeDebbieSubTree(threadCon1[currTree], threadCon2[currTree], new BigInteger[] {});

		// Reshuffle
		Reshuffle rs = new Reshuffle(threadCon1[currTree], threadCon2[currTree]);
		rs.loadTreeSpecificParameters(currTree);
		BigInteger tmp = null;
		List<Integer> tmp2 = null;
		rs.executeDebbieSubTree(threadCon1[currTree], threadCon2[currTree], Pair.of(tmp, tmp2));

		// Eviction
		Eviction evict = new Eviction(threadCon1[currTree], threadCon2[currTree]);
		evict.loadTreeSpecificParameters(currTree);
		evict.executeDebbieSubTree(threadCon1[currTree], threadCon2[currTree], new BigInteger[] {});

		// EncryptPath
		EncryptPath ep = new EncryptPath(threadCon1[currTree], threadCon2[currTree]);
		ep.loadTreeSpecificParameters(currTree);
		ep.executeDebbieSubTree(threadCon1[currTree], threadCon2[currTree], extraArgs[0]);
	}

	private void runEddie() {

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
