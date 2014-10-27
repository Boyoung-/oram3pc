package sprout.oram.operations;

import java.math.BigInteger;

import org.apache.commons.lang3.tuple.Pair;

import sprout.communication.Communication;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.util.Util;

// This is what eviction might look like
// TODO: Is this recursive similar to access? If so we can use TreeOperation as is

public class Eviction extends TreeOperation<String, Pair<String, String>> {

  public Eviction(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  /*
  public Eviction(Communication con1, Communication con2, ForestMetadata meta) {
    super(con1, con2, meta);
  }
  */

  @Override
  public String executeCharlieSubTree(Communication debbie,
      Communication eddie, String Li, Tree OT, Pair<String, String> extraArgs) {
    String sC_P_p = extraArgs.getLeft();
    String sC_T_p = extraArgs.getRight();
    return null;
  }

  @Override
  public String executeDebbieSubTree(Communication charlie,
      Communication eddie, BigInteger k, Tree OT,
      Pair<String, String> extraArgs) {
    String Li = extraArgs.getLeft();
    
    return null;
  }

  @Override
  public String executeEddieSubTree(Communication charlie,
      Communication debbie, Tree OT, Pair<String, String> extraArgs) {
    String sE_P_p = extraArgs.getLeft();
    String sE_T_p = extraArgs.getRight();
    return null;
  }

  @Override
  public Pair<String, String> prepareArgs(Party party) {
    // Randomly generate a secret
    if (party == Party.Debbie) {
      String Li = Util.addZero(new BigInteger(ll, rnd).toString(2), ll);
      return Pair.of(Li, null);
    } else {
      String s_P_p   = Util.addZero(new BigInteger(n*bucketSize, rnd).toString(2), n*bucketSize);
      String s_T_p   = Util.addZero(new BigInteger(tupleBitLength, rnd).toString(2), tupleBitLength);
      
      return Pair.of(s_P_p, s_T_p);
    }
  }
  
 //Temporarily redefine parameters for testing
 int bucketSize; // TODO: This is l everywhere else we should redefine here
 @Override
 public void loadTreeSpecificParameters(int index) {
   super.loadTreeSpecificParameters(index);
   
   i         = 2;
   d_i       = 4;
   d_ip1       = 7;
   tau       = 3;
   twotaupow     = (int) Math.pow(2, tau);
   ln        = i * tau;          
   ll        = d_i;            
   ld        = twotaupow * d_ip1;          
   tupleBitLength  = 1 + ln + ll + ld;
   w       = 4;
   l    = tupleBitLength * w; 
   n       = d_i + 4;
   
   bucketSize = l;
 }

}
