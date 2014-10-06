package sprout.oram.operations;

import java.math.BigInteger;
import java.security.SecureRandom;

import sprout.communication.Communication;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.oram.Forest.TreeZero;

public abstract class Operation<T, V> {
  
  static SecureRandom rnd = new SecureRandom();
  
  int tau;                               // tau in the writeup
  int twotaupow;                         // 2^tau
  int h;                                 // # trees
  int w;                                 // # tuples in each bucket
  int expen;                             // # buckets in each leaf
  ForestMetadata metadata;
  
  Communication con1, con2;
  
  /*
   * Connections are alphabetized so:
   * 
   * For Charlie
   * con1 = debbie
   * con2 = eddie
   * 
   * For Debbie
   * con1 = charlie
   * con2 = eddie
   * 
   * For Eddie
   * con1 = debbie
   * con2 = charlie
   */
  
  Operation(Communication con1, Communication con2, ForestMetadata metadata) {
    if (metadata != null) {
      // parameters
      tau       = metadata.getTauExponent();    
      twotaupow     = metadata.getTau();              
      h       = metadata.getLevels();           
      w         = metadata.getBucketDepth();    
      expen     = metadata.getLeafExpansion();    
      this.metadata = metadata;
    }
    this.con1 = con1;
    this.con2 = con2;
  }
  
  int treeLevel;                      
  int i;                            // tree index in the writeup
  int d_i;                          // # levels in this tree (excluding the root level)
  int d_ip1;                        // # levels in the next tree (excluding the root level)
  int ln;                           // # N bits
  int ll;                           // # L bits
  int ld;                           // # data bits
  int tupleBitLength;               // # tuple size (bits)
  int l;                            // # bucket size (bits)
  int n;                            // # tuples in one path
  
  public void loadTreeSpecificParameters(Tree OT) {
    // TODO: Do these need to be accessed by all parties? If not we should separate them out.
    if (OT != null)                     // we are dealing with the initial tree
      treeLevel   = OT.getTreeLevel();        
    else
      treeLevel   = h;                // tree index in ORAM forest
    i         = h - treeLevel;          // tree index in the writeup
    d_i       = 0;                // # levels in this tree (excluding the root level)
    if (i > 0)
      d_i       = OT.getNumLevels();
    if (i == h)
      d_ip1     = OT.getDBytes() * 8 / twotaupow;
    else
      d_ip1     = metadata.getTupleBitsL(treeLevel-1); // TODO: Compute this some how before hand
    ln        = i * tau;              // # N bits
    ll        = d_i;                // # L bits
    ld        = twotaupow * d_ip1;        // # data bits
    tupleBitLength  = 1 + ln + ll + ld;         // # tuple size (bits)
    l       = tupleBitLength * w;           // # bucket size (bits)
    n       = w * (d_i + expen);          // # tuples in one path
    if (i == 0) {
      tupleBitLength  = ld;
      l         = ld;
      n         = 1;
    }
  }
  
  public T execute(Party party, String Li, String Nip1, BigInteger k, TreeZero OT_0, Tree OT, V extraArgs) {
    loadTreeSpecificParameters(OT);
    
    switch (party) {
    case Charlie:
      return executeCharlieSubTree(con1, con2, Li, OT_0, OT, extraArgs);
    case Debbie:
      return executeDebbieSubTree(con1, con2, k, OT_0, OT, extraArgs);
    case Eddie:
      return executeEddieSubTree(con1, con2, OT_0, OT, extraArgs);
    }
    return null;
  }
  
  public abstract T executeCharlieSubTree(Communication debbie, Communication eddie, String Li, TreeZero OT_0, Tree OT, V extraArgs);
  public abstract T executeDebbieSubTree(Communication charlie, Communication eddie,BigInteger k, TreeZero OT_0, Tree OT, V extraArgs);
  public abstract T executeEddieSubTree(Communication charlie, Communication debbie, TreeZero OT_0, Tree OT, V extraArgs);
  
  // TODO: Add timing information
//TODO: Something of this form which actually performs a complete step
 //public void run(Party party) {
 //  
 //}
  
}
