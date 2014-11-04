package sprout.oram.operations;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import sprout.communication.Communication;
import sprout.oram.Bucket;
import sprout.oram.BucketException;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.oram.TreeException;
import sprout.oram.TupleException;
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
		  sC_Li_p[i] = Util.addZero(new BigInteger(lBits, rnd).toString(2), lBits);
		  sE_Li_p[i] = Util.addZero(new BigInteger(lBits, rnd).toString(2), lBits);
	  }
  }

  public String[] executeCharlie(Communication debbie, Communication eddie, String Li, String Nip1) {
	  // Access
	  Access access = new Access();
	  access.loadTreeSpecificParameters(currTree);
	  AOutput AOut = access.executeCharlieSubTree(debbie, eddie, Li, null, Nip1);
	  String[] output = new String[]{AOut.Lip1, AOut.secretC_Ti};
	  
	  // PostProcessT
	  String secretC_Ti = AOut.secretC_Ti;
	  String secretC_Li_p = sC_Li_p[currTree];
	  String secretC_Lip1_p = null;
	  if (currTree < ForestMetadata.getLevels()-1)
		  secretC_Lip1_p = sC_Li_p[currTree+1];
	  String Lip1 = AOut.Lip1;
	  String Nip1_pr = Nip1.substring(ForestMetadata.getNBits(currTree));
	  PostProcessT ppt = new PostProcessT();
	  ppt.loadTreeSpecificParameters(currTree);
	  String secretC_Ti_p = ppt.executeCharlieSubTree(debbie, eddie, Li, null, new String[]{secretC_Ti, secretC_Li_p, secretC_Lip1_p, Lip1, Nip1_pr});
	  
	  // Reshuffle
	  String secretC_P_p = AOut.secretC_P_p;
	  Reshuffle rs = new Reshuffle();
	  rs.loadTreeSpecificParameters(currTree);
	  List<Integer> tmp = null;
	  String secretC_pi_P = rs.executeCharlieSubTree(debbie, eddie, null, null, Pair.of(secretC_P_p, tmp));
	  
	  // Eviction
	  Eviction evict = new Eviction();
	  evict.loadTreeSpecificParameters(currTree);
	  String secretC_P_pp = evict.executeCharlieSubTree(debbie, eddie, null, null, new String[]{secretC_pi_P, secretC_Ti_p});
	  if (currTree == 0)
		  secretC_P_pp = secretC_Ti_p;
	  
	  // EncryptPath
	  EncryptPath ep = new EncryptPath();
	  ep.loadTreeSpecificParameters(currTree);
	  ep.executeCharlieSubTree(debbie, eddie, null, null, secretC_P_pp);
	  
	  return output;
  }

  public void executeDebbie(Communication charlie, Communication eddie, BigInteger k) {
	  // Access
	  Access access = new Access();
	  access.loadTreeSpecificParameters(currTree);
	  access.executeDebbieSubTree(charlie, eddie, k, null, null);
	  
	  // PostProcessT
	  PostProcessT ppt = new PostProcessT();
	  ppt.loadTreeSpecificParameters(currTree);
	  ppt.executeDebbieSubTree(charlie, eddie, null, null, null);
	  
	  // Reshuffle
	  List<Integer> pi = eddie.readListInt();
	  Reshuffle rs = new Reshuffle();
	  rs.loadTreeSpecificParameters(currTree);
	  String tmp = null;
	  rs.executeDebbieSubTree(charlie, eddie, null, null, Pair.of(tmp, pi));
	  
	  // Eviction
	  Eviction evict = new Eviction();
	  evict.loadTreeSpecificParameters(currTree);
	  evict.executeDebbieSubTree(charlie, eddie, null, null, null);
	  
	  // EncryptPath
	  EncryptPath ep = new EncryptPath();
	  ep.loadTreeSpecificParameters(currTree);
	  ep.executeDebbieSubTree(charlie, eddie, k, null, null);
  }

  public void executeEddie(Communication charlie, Communication debbie, Tree OT, String Li) {
	  // Access
	  Access access = new Access();
	  access.loadTreeSpecificParameters(currTree);
	  AOutput AOut = access.executeEddieSubTree(charlie, debbie, OT, null);
	  
	  // PostProcessT
	  String secretE_Ti = AOut.secretE_Ti;
	  String secretE_Li_p = sE_Li_p[currTree];
	  String secretE_Lip1_p = null;
	  if (currTree < ForestMetadata.getLevels()-1)
		  secretE_Lip1_p = sE_Li_p[currTree+1];
	  PostProcessT ppt = new PostProcessT();
	  ppt.loadTreeSpecificParameters(currTree);
	  String secretE_Ti_p = ppt.executeEddieSubTree(charlie, debbie, null, new String[]{secretE_Ti, secretE_Li_p, secretE_Lip1_p});
	  
	  // Reshuffle
	  String secretE_P_p = AOut.secretE_P_p;
	  List<Integer> pi = Util.getInversePermutation(AOut.p);
	  debbie.write(pi); // make sure D gets this pi
	  Reshuffle rs = new Reshuffle();
	  rs.loadTreeSpecificParameters(currTree);
	  String secretE_pi_P = rs.executeEddieSubTree(charlie, debbie, null, Pair.of(secretE_P_p, pi));
	  
	  // Eviction
	  Eviction evict = new Eviction();
	  evict.loadTreeSpecificParameters(currTree);
	  String secretE_P_pp = evict.executeEddieSubTree(charlie, debbie, null, new String[]{secretE_pi_P, secretE_Ti_p, Li});
	  if (currTree == 0)
		  secretE_P_pp = secretE_Ti_p;

	  // EncryptPath
	  EncryptPath ep = new EncryptPath();
	  ep.loadTreeSpecificParameters(currTree);
	  EPath EPOut = ep.executeEddieSubTree(charlie, debbie, null, secretE_P_pp);
	  
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
	  int h = ForestMetadata.getLevels() - 1;
	  int tau = ForestMetadata.getTau();
	  
	  for (int test=0; test<10; test++) { // test 10 random records
		  String N = Util.addZero(new BigInteger(h*tau, rnd).toString(2), h*tau);
		  int expected = new BigInteger(N, 2).intValue();
		  for (int exec=0; exec<20; exec++) { // for each record, test retrieval 20 times
			  String Li = "";
			  System.out.println("Stored record is: " + expected);
			  System.out.println("Execution cycle: " + exec);
			  precomputation();
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
			    		System.out.println("Is record correct: " + (record == expected) + "\n");
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
		  }
	  }
  }
  
}
