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
	private String[] sC_Li_p;
	private String[] sE_Li_p;
	
  public Retrieve(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  private void precomputation() {
	  int levels = ForestMetadata.getLevels();
	  sC_Li_p = new String[levels];
	  sE_Li_p = new String[levels];
	  for (int i=0; i<levels; i++) {
		  int lBits = ForestMetadata.getLBits(i);
		  sC_Li_p[i] = Util.addZero(new BigInteger(lBits, SR.rand).toString(2), lBits);
		  sE_Li_p[i] = Util.addZero(new BigInteger(lBits, SR.rand).toString(2), lBits);
	  }
  }

  public String[] executeCharlie(Communication debbie, Communication eddie, String Li, String Nip1) {
	  // Access
	  Access access = new Access(debbie, eddie);
	  access.loadTreeSpecificParameters(currTree);
	  timing.access.start();
	  AOutput AOut = access.executeCharlieSubTree(debbie, eddie, Li, null, Nip1);
	  timing.access.stop();
	  //System.out.println(timing.access);
	  String[] output = new String[]{AOut.Lip1, AOut.secretC_Ti};
	  
	  // PostProcessT
	  String secretC_Ti = AOut.secretC_Ti;
	  String secretC_Li_p = sC_Li_p[currTree];
	  String secretC_Lip1_p = null;
	  if (currTree < ForestMetadata.getLevels()-1)
		  secretC_Lip1_p = sC_Li_p[currTree+1];
	  String Lip1 = AOut.Lip1;
	  String Nip1_pr = Nip1.substring(ForestMetadata.getNBits(currTree));
	  PostProcessT ppt = new PostProcessT(debbie, eddie);
	  ppt.loadTreeSpecificParameters(currTree);
	  timing.post.start();
	  String secretC_Ti_p = ppt.executeCharlieSubTree(debbie, eddie, Li, null, new String[]{secretC_Ti, secretC_Li_p, secretC_Lip1_p, Lip1, Nip1_pr});
	  timing.post.stop();
	  
	  // Reshuffle
	  String secretC_P_p = AOut.secretC_P_p;
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
	  String secretC_P_pp = evict.executeCharlieSubTree(debbie, eddie, null, null, new String[]{secretC_pi_P, secretC_Ti_p});
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
	  String tmp = null;
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
	  //System.out.println(timing.access);
	  
	  // PostProcessT
	  String secretE_Ti = AOut.secretE_Ti;
	  String secretE_Li_p = sE_Li_p[currTree];
	  String secretE_Lip1_p = null;
	  if (currTree < ForestMetadata.getLevels()-1)
		  secretE_Lip1_p = sE_Li_p[currTree+1];
	  PostProcessT ppt = new PostProcessT(charlie, debbie);
	  ppt.loadTreeSpecificParameters(currTree);
	  timing.post.start();
	  String secretE_Ti_p = ppt.executeEddieSubTree(charlie, debbie, null, new String[]{secretE_Ti, secretE_Li_p, secretE_Lip1_p});
	  timing.post.stop();
	  
	  // Reshuffle
	  String secretE_P_p = AOut.secretE_P_p;
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
	  String secretE_P_pp = evict.executeEddieSubTree(charlie, debbie, null, new String[]{secretE_pi_P, secretE_Ti_p, Li});
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
	  
	  int records = 4;     // how many random records we want to test retrieval
	  int retrievals = 3;  // for each record, how many repeated retrievals we want to do
	  
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
				  System.out.println("Stored record is: " + expected + " (N=" + N + ")");
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
			    	String[] outC = executeCharlie(con1, con2, Li, Ni);
			    	Li = outC[0];
			    	if (i == h) {
			    		String D = outC[1].substring(outC[1].length()-ForestMetadata.getDataSize()*8);
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
			timing.divide(cycles);
			timing.writeToFile("files/timing-charlie");
			break;
		case Debbie:
			timing.gcf_online = timing.gcf_online.subtract(timing.gtt_read);
			timing.gcf_read = timing.gcf_read.add(timing.gtt_read);
			timing.divide(cycles);
			timing.writeToFile("files/timing-debbie");
			break;
		case Eddie:
			timing.gcf_online = timing.gcf_online.subtract(timing.gtt_write);
			timing.gcf_write = timing.gcf_write.add(timing.gtt_write);
			timing.divide(cycles);
			timing.writeToFile("files/timing-eddie");
			break;
		}
		
		con1.writeBandwidthToFile("files/" + party + "-bandwidth-1");
		con2.writeBandwidthToFile("files/" + party + "-bandwidth-2");
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
  
}
