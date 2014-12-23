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
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.oram.TreeException;
import sprout.util.Timing;
import sprout.util.Util;

public class Retrieve extends Operation {
	
	private int currTree;
	private BigInteger[] sC_Li_p;
	private BigInteger[] sE_Li_p;
	
  public Retrieve(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  private void precomputation() {
	  int levels = ForestMetadata.getLevels();
	  sC_Li_p = new BigInteger[levels];
	  sE_Li_p = new BigInteger[levels];
	  for (int i=0; i<levels; i++) {
		  int lBits = ForestMetadata.getLBits(i);
		  sC_Li_p[i] = new BigInteger(lBits, SR.rand);
		  sE_Li_p[i] = new BigInteger(lBits, SR.rand);
	  }
  }

  public BigInteger[] executeCharlie(Communication debbie, Communication eddie, String Li, String Nip1) {
	  // Access
	  Access access = new Access(debbie, eddie);
	  access.loadTreeSpecificParameters(currTree);
	  timing.access.start();
	  AOutput AOut = access.executeCharlieSubTree(debbie, eddie, Li, null, Nip1);
	  timing.access.stop();
	  BigInteger[] output = new BigInteger[]{AOut.Lip1, AOut.secretC_Ti};
	  
	  // PostProcessT
	  BigInteger secretC_Ti = AOut.secretC_Ti;
	  BigInteger secretC_Li_p = sC_Li_p[currTree];
	  BigInteger secretC_Lip1_p = null;
	  if (currTree < ForestMetadata.getLevels()-1)
		  secretC_Lip1_p = sC_Li_p[currTree+1];
	  BigInteger Lip1 = AOut.Lip1;
	  BigInteger Nip1_pr = BigInteger.ZERO;
	  if (Nip1.substring(ForestMetadata.getNBits(currTree)).length() > 0)
		  Nip1_pr = new BigInteger(Nip1.substring(ForestMetadata.getNBits(currTree)), 2);
	  PostProcessT ppt = new PostProcessT(debbie, eddie);
	  ppt.loadTreeSpecificParameters(currTree);
	  timing.post.start();
	  BigInteger secretC_Ti_p = ppt.executeCharlieSubTree(debbie, eddie, Li, null, new BigInteger[]{secretC_Ti, secretC_Li_p, secretC_Lip1_p, Lip1, Nip1_pr});
	  timing.post.stop();
	  
	  // Reshuffle
	  BigInteger secretC_P_p = AOut.secretC_P_p;
	  Reshuffle rs = new Reshuffle(debbie, eddie);
	  rs.loadTreeSpecificParameters(currTree);
	  List<Integer> tmp = null;
	  timing.reshuffle.start();
	  String secretC_pi_P = rs.executeCharlieSubTree(debbie, eddie, null, null, Pair.of(secretC_P_p, tmp));
	  timing.reshuffle.stop();
	  
	  // Eviction
	  Eviction evict = new Eviction(debbie, eddie);
	  evict.loadTreeSpecificParameters(currTree);
	  timing.eviction.start();
	  BigInteger secretC_P_pp = evict.executeCharlieSubTree(debbie, eddie, null, null, new BigInteger[]{secretC_pi_P.equals("")?null:new BigInteger(secretC_pi_P, 2), secretC_Ti_p});
	  timing.eviction.stop();
	  if (currTree == 0)
		  secretC_P_pp = secretC_Ti_p;
	  
	  // EncryptPath
	  EncryptPath ep = new EncryptPath(debbie, eddie);
	  ep.loadTreeSpecificParameters(currTree);
	  timing.encrypt.start();
	  ep.executeCharlieSubTree(debbie, eddie, null, null, secretC_P_pp);
	  timing.encrypt.stop();
	  
	  return output;
  }

  public void executeDebbie(Communication charlie, Communication eddie, BigInteger k) {
	  // Access
	  Access access = new Access(charlie, eddie);
	  access.loadTreeSpecificParameters(currTree);
	  timing.access.start();
	  access.executeDebbieSubTree(charlie, eddie, k, null, null);
	  timing.access.stop();
	  //System.out.println(timing.access);
	  
	  // PostProcessT
	  PostProcessT ppt = new PostProcessT(charlie, eddie);
	  ppt.loadTreeSpecificParameters(currTree);
	  timing.post.start();
	  ppt.executeDebbieSubTree(charlie, eddie, null, null, null);
	  timing.post.stop();
	  
	  // Reshuffle
	  List<Integer> pi = eddie.readListInt();
	  Reshuffle rs = new Reshuffle(charlie, eddie);
	  rs.loadTreeSpecificParameters(currTree);
	  BigInteger tmp = null;
	  timing.reshuffle.start();
	  rs.executeDebbieSubTree(charlie, eddie, null, null, Pair.of(tmp, pi));
	  timing.reshuffle.stop();
	  
	  // Eviction
	  Eviction evict = new Eviction(charlie, eddie);
	  evict.loadTreeSpecificParameters(currTree);
	  timing.eviction.start();
	  evict.executeDebbieSubTree(charlie, eddie, null, null, null);
	  timing.eviction.stop();
	  
	  // EncryptPath
	  EncryptPath ep = new EncryptPath(charlie, eddie);
	  ep.loadTreeSpecificParameters(currTree);
	  timing.encrypt.start();
	  ep.executeDebbieSubTree(charlie, eddie, k, null, null);
	  timing.encrypt.stop();
  }

  public void executeEddie(Communication charlie, Communication debbie, Tree OT, String Li) {
	  // Access
	  Access access = new Access(charlie, debbie);
	  access.loadTreeSpecificParameters(currTree);
	  timing.access.start();
	  AOutput AOut = access.executeEddieSubTree(charlie, debbie, OT, null);
	  timing.access.stop();
	  
	  // PostProcessT
	  BigInteger secretE_Ti = AOut.secretE_Ti;
	  BigInteger secretE_Li_p = sE_Li_p[currTree];
	  BigInteger secretE_Lip1_p = null;
	  if (currTree < ForestMetadata.getLevels()-1)
		  secretE_Lip1_p = sE_Li_p[currTree+1];
	  PostProcessT ppt = new PostProcessT(charlie, debbie);
	  ppt.loadTreeSpecificParameters(currTree);
	  timing.post.start();
	  BigInteger secretE_Ti_p = ppt.executeEddieSubTree(charlie, debbie, null, new BigInteger[]{secretE_Ti, secretE_Li_p, secretE_Lip1_p});
	  timing.post.stop();
	  
	  // Reshuffle
	  BigInteger secretE_P_p = AOut.secretE_P_p;
	  List<Integer> pi = Util.getInversePermutation(AOut.p);
	  debbie.write(pi); // make sure D gets this pi  // TODO: move this send into Reshuffle pre-computation?
	  Reshuffle rs = new Reshuffle(charlie, debbie);
	  rs.loadTreeSpecificParameters(currTree);
	  timing.reshuffle.start();
	  String secretE_pi_P = rs.executeEddieSubTree(charlie, debbie, null, Pair.of(secretE_P_p, pi));
	  timing.reshuffle.stop();
	  
	  // Eviction
	  Eviction evict = new Eviction(charlie, debbie);
	  evict.loadTreeSpecificParameters(currTree);
	  timing.eviction.start();
	  BigInteger secretE_P_pp = evict.executeEddieSubTree(charlie, debbie, null, new BigInteger[]{secretE_pi_P.equals("")?null:new BigInteger(secretE_pi_P, 2), secretE_Ti_p, Li.equals("")?null:new BigInteger(Li, 2)});
	  timing.eviction.stop();
	  if (currTree == 0)
		  secretE_P_pp = secretE_Ti_p;

	  // EncryptPath
	  EncryptPath ep = new EncryptPath(charlie, debbie);
	  ep.loadTreeSpecificParameters(currTree);
	  timing.encrypt.start();
	  EPath EPOut = ep.executeEddieSubTree(charlie, debbie, null, secretE_P_pp);
	  timing.encrypt.stop();
	  
	  // put encrypted path back to tree
	  Bucket[] buckets = new Bucket[EPOut.x.length];
	  for (int j=0; j<EPOut.x.length; j++) {
		  try {
			buckets[j] = new Bucket(currTree, EPOut.x[j].getEncoded(), Util.rmSignBit(EPOut.Bbar[j].toByteArray()));
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
	  if (ifSanityCheck())
		  System.out.println("Sanity check enabled");
	  
	  long numInsert = Math.min(ForestMetadata.getNumInsert(), ForestMetadata.getAddressSpace());
	  if (numInsert == 0L)
		  return;
	  
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
	  
	  int records = 6;     // how many random records we want to test retrieval
	  int retrievals = 5;  // for each record, how many repeated retrievals we want to do
	  
	  for (int test=0; test<records; test++) { 
		  String N = null;
		  long expected = 0;
		  if (party == Party.Charlie) {
			  if (numInsert == -1)
				  N = Util.addZero(new BigInteger(lastNBits, SR.rand).toString(2), lastNBits);
			  else
				  N = Util.addZero(Util.nextBigInteger(BigInteger.valueOf(numInsert)).toString(2), lastNBits);
			  expected = new BigInteger(N, 2).intValue();
		  }
		  
		  for (long exec=0; exec<retrievals; exec++) {
			  String Li = "";
			  if (party == Party.Charlie)
				  System.out.println(test + ": stored record is: " + expected + " (N=" + N + ")");
			  System.out.println("Execution cycle: " + exec);
			  
			  precomputation(); // TODO: not every party needs to do all of them
			  
			  for (int i=0; i<= h; i++) {
				  currTree = i;
				  
			    switch (party) {
			    case Charlie: 
			    	String Ni = N;
			    	if (i < h-1) 
			    		Ni = N.substring(0, (i+1)*tau);
			    	System.out.println("i=" + i + ", Li=" + Li);
			    	con2.write(Li);
			    	BigInteger[] outC = executeCharlie(con1, con2, Li, Ni);
			    	int d_ip1;
			    	if (i == h)
			    	      d_ip1     	= ForestMetadata.getABits(i) / ForestMetadata.getTwoTauPow();
			    	    else
			    	      d_ip1     	= ForestMetadata.getLBits(i+1); 
			    	Li = "";
			    	if (outC[0] != null)
			    		Li = Util.addZero(outC[0].toString(2), d_ip1);
			    	if (i == h) {
			    		//String D = outC[1].substring(outC[1].length()-ForestMetadata.getDataSize()*8);
			    		String outC_1 = Util.addZero(outC[1].toString(2), ForestMetadata.getTupleBits(i));
			    		String D = outC_1.substring(outC_1.length()-ForestMetadata.getDataSize()*8);
			    		
			    		int record = new BigInteger(D, 2).intValue();
			    		System.out.println("Retrieved record is: " + new BigInteger(D, 2));
			    		System.out.println("Is record correct: " + (record==expected?"YES":"NO!!") + "\n");
			    		if (record != expected)
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
			    	Li = con1.readString();
			    	executeEddie(con1, con2, forest.getTree(currTree), Li);
			      break;
			    }
			  }
			  
			  // only need to count bandwidth once
			  con1.bandWidthSwitch = false;
			  con2.bandWidthSwitch = false;
		  }
		  
		  if (test == 0 && records > 1)
			  timing.init(); // abandon the timing of the first several retrievals
	  }
	  
	  // average timing
	  int cycles = 0;
	  if (records > 1)
		cycles = (records-1) * retrievals; // first round is abandoned
	  else if (records == 1)
		  cycles = retrievals;
	  else
		  return;
		  
	try {
		switch (party) {
		case Charlie:
			break;
		case Debbie:
			timing.gcf_online = timing.gcf_online.subtract(timing.gcf_offline_read);
			break;
		case Eddie:
			timing.gcf_online = timing.gcf_online.subtract(timing.gcf_offline_write);
			break;
		}

		int t = ForestMetadata.getTau();
		int n = ForestMetadata.getLastNBits();
		int w = ForestMetadata.getBucketDepth();
		int d = ForestMetadata.getDataSize();
		
		timing.divide(cycles);
		timing.writeToFile("stats/timing-" + party + "-t" + t + "n" + n + "w" + w + "d" + d);
		con1.writeBandwidthToFile("stats/" + party + "-bandwidth-1" + "-t" + t + "n" + n + "w" + w + "d" + d);
		con2.writeBandwidthToFile("stats/" + party + "-bandwidth-2" + "-t" + t + "n" + n + "w" + w + "d" + d);
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
  
}
