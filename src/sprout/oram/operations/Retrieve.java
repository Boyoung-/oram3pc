package sprout.oram.operations;

import java.math.BigInteger;
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
import sprout.util.Util;

public class Retrieve extends Operation {
	
	private int currTree;
	
  public Retrieve(Communication con1, Communication con2) {
    super(con1, con2);
  }

  public Integer executeCharlie(Communication debbie, Communication eddie, String Li, String Nip1) {
	  // Access
	  Access access = new Access();
	  access.loadTreeSpecificParameters(currTree);
	  AOutput AOut = access.executeCharlieSubTree(debbie, eddie, Li, null, Nip1);
	  //System.out.println("Lip1: " + AOut.Lip1);
	  //System.out.println("secretC_Ti: " + AOut.secretC_Ti);
	  
	  // PostProcessT
	  // TODO: add precomputation
	  String secretC_Ti = AOut.secretC_Ti;
	  String secretC_Li_p = Util.addZero(new BigInteger(ForestMetadata.getLBits(currTree), rnd).toString(2), ForestMetadata.getLBits(currTree));
	  String secretC_Lip1_p = null;
	  if (currTree < ForestMetadata.getLevels()-1)
		  secretC_Lip1_p = Util.addZero(new BigInteger(ForestMetadata.getLBits(currTree+1), rnd).toString(2), ForestMetadata.getLBits(currTree+1));
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
	  
	  return 0;
  }

  public Integer executeDebbie(Communication charlie, Communication eddie, BigInteger k) {
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
	  
	  return 0;
  }

  public Integer executeEddie(Communication charlie, Communication debbie, Tree OT, String Li) {
	  // Access
	  Access access = new Access();
	  access.loadTreeSpecificParameters(currTree);
	  AOutput AOut = access.executeEddieSubTree(charlie, debbie, OT, null);
	  //System.out.println("secretE_Ti: " + AOut.secretE_Ti);
	  
	  // PostProcessT
	  // TODO: add precomputation
	  String secretE_Ti = AOut.secretE_Ti;
	  String secretE_Li_p = Util.addZero(new BigInteger(ForestMetadata.getLBits(currTree), rnd).toString(2), ForestMetadata.getLBits(currTree));
	  String secretE_Lip1_p = null;
	  if (currTree < ForestMetadata.getLevels()-1)
		  secretE_Lip1_p = Util.addZero(new BigInteger(ForestMetadata.getLBits(currTree+1), rnd).toString(2), ForestMetadata.getLBits(currTree+1));
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
	  
	  // put EPOut back to tree
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
	  
	  return 0;
  }
  
  @Override
  public void run(Party party, Forest forest) throws ForestException {
	  currTree = 2;
	  
    switch (party) {
    case Charlie: 
    	if (currTree == 0)
    		executeCharlie(con1, con2, "", "001");
    	else if (currTree == 1)
    		executeCharlie(con1, con2, "0", "001001");
    	else
    		executeCharlie(con1, con2, "0011", "001001");
      break;
    case Debbie: 
    	executeDebbie(con1, con2, null);
      break;
    case Eddie: 
    	if (currTree == 0)
    		executeEddie(con1, con2, forest.getTree(currTree), "");
    	else if (currTree == 1)
    		executeEddie(con1, con2, forest.getTree(currTree), "0");
    	else
    		executeEddie(con1, con2, forest.getTree(currTree), "0011");
      break;
    }
  }
  
}
