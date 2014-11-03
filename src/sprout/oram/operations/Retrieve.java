package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.Party;
import sprout.oram.Tree;

public class Retrieve extends Operation {
	
	private int currTree;

  public Retrieve(Communication con1, Communication con2) {
    super(con1, con2);
  }

  public Integer executeCharlie(Communication debbie, Communication eddie, String Li, String Nip1) {
	  Access access = new Access();
	  access.loadTreeSpecificParameters(currTree);
	  AOutput AOut = access.executeCharlieSubTree(debbie, eddie, Li, null, Nip1);
	  //System.out.println("Lip1: " + AOut.Lip1);
	  //System.out.println("secretC_Ti: " + AOut.secretC_Ti);
	  return 0;
  }

  public Integer executeDebbie(Communication charlie, Communication eddie, BigInteger k) {
	  Access access = new Access();
	  access.loadTreeSpecificParameters(currTree);
	  access.executeDebbieSubTree(charlie, eddie, k, null, null);
	  return 0;
  }

  public Integer executeEddie(Communication charlie, Communication debbie, Tree OT) {
	  Access access = new Access();
	  access.loadTreeSpecificParameters(currTree);
	  AOutput AOut = access.executeEddieSubTree(charlie, debbie, OT, null);
	  //System.out.println("secretE_Ti: " + AOut.secretE_Ti);
	  return 0;
  }
  
  @Override
  public void run(Party party, Forest forest) throws ForestException {
	  currTree = 2;
	  
    switch (party) {
    case Charlie: 
    	executeCharlie(con1, con2, "0011", "001001");
      break;
    case Debbie: 
    	executeDebbie(con1, con2, null);
      break;
    case Eddie: 
    	executeEddie(con1, con2, forest.getTree(currTree));
      break;
    }
  }
  
}
