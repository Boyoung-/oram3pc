package sprout.oram.operations;

import java.math.BigInteger;

import org.apache.commons.lang3.NotImplementedException;

import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.util.Util;

public abstract class TreeOperation<T extends Object, V> extends Operation {
  
  int tau;                               // tau in the writeup
  int twotaupow;                         // 2^tau
  int h;                                 // # trees
  int w;                                 // # tuples in each bucket
  int expen;                             // # buckets in each leaf
  //ForestMetadata metadata;
  
  static boolean print_out = false;
  
  
  public TreeOperation(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  TreeOperation(Communication con1, Communication con2, ForestMetadata metadata) {
    super(con1, con2);
    initializeMetadata(metadata);
  }
  
  private void initializeMetadata(ForestMetadata metadata) {
    if (metadata != null) {
      // parameters
      tau       = metadata.getTau();    
      twotaupow     = metadata.getTwoTauPow();              
      h       = metadata.getLevels()-1;           
      w         = metadata.getBucketDepth();    
      expen     = metadata.getLeafExpansion();    
      this.metadata = metadata;
    }
  }

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
    i         = OT.getTreeIndex();          // tree index in the writeup
    d_i       = metadata.getLBits(i);                // # levels in this tree (excluding the root level)
    if (i == h)
      d_ip1     = metadata.getABits(i) / twotaupow;
    else
      d_ip1     = metadata.getLBits(i+1); // TODO: Compute this some how before hand
    ln        = metadata.getNBits(i);              // # N bits
    ll        = d_i;                // # L bits
    ld        = metadata.getABits(i);        // # data bits
    tupleBitLength  = metadata.getTupleBits(i);         // # tuple size (bits)
    l       = metadata.getBucketTupleBits(i);           // # bucket tuples' size (bits)
    n       = w * (d_i + expen);          // # tuples in one path
    if (i == 0) 
      n         = 1;
  }
  
  public T execute(Party party, String Li, BigInteger k, Tree OT, V extraArgs) {
    loadTreeSpecificParameters(OT);
    
    switch (party) {
    case Charlie:
      return executeCharlieSubTree(con1, con2, Li, OT, extraArgs);
    case Debbie:
      return executeDebbieSubTree(con1, con2, k, OT, extraArgs);
    case Eddie:
      return executeEddieSubTree(con1, con2, OT, extraArgs);
    }
    return null;
  }
  
  
  /*
   * This is mostly just testing code and may need to change for the purpose of an actual execution
   */
  @Override
  public void run(Party party, Forest forest) throws ForestException {
    initializeMetadata(forest.getMetadata());
    
    // i = 0 case
    BigInteger k = OPRFHelper.getOPRF(party).getK();
    T out = execute(party, "", k, forest.getInitialORAM(), null, prepareArgs(party));
    if (print_out && out!=null) System.out.println("Output i=0 : " + out.toString());
    else System.out.println("Finished round 0");
    for (int treeLevel = forest.getNumberOfTrees()-1; treeLevel >= 0; treeLevel--) {
      Tree OT = forest.getTree(treeLevel);
      this.loadTreeSpecificParameters(OT);
    
      String Li = Util.addZero(new BigInteger(ll, rnd).toString(2), ll);    
      
      // TODO: Print out here too
      out = execute(party, Li, k, forest.getInitialORAM(), OT, prepareArgs(party));
      if (print_out && out!=null) System.out.println("Output i=" + i + " : " + out.toString());
      else System.out.println("Finished round " + i);
    }
    
    // Synchronization. This ensures we don't exit early
    // This should not be timed
    con1.write("end");
    con2.write("end");
    con1.readString();
    con2.readString();
  }
  
  public abstract T executeCharlieSubTree(Communication debbie, Communication eddie, String Li, TreeZero OT_0, Tree OT, V extraArgs);
  public abstract T executeDebbieSubTree(Communication charlie, Communication eddie, BigInteger k, TreeZero OT_0, Tree OT, V extraArgs);
  public abstract T executeEddieSubTree(Communication charlie, Communication debbie, TreeZero OT_0, Tree OT, V extraArgs);
  
  public V prepareArgs() {
    return prepareArgs(null);
  }
  
  public V prepareArgs(Party party) {
    if (party == null) {
      throw new NotImplementedException("Must overide prepareArgs() or prepareArgs(Party)");
    }
    return prepareArgs();
  }
  // TODO: Add timing information
  
}
