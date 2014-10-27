package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bouncycastle.math.ec.ECPoint;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.oprf.Message;
import sprout.crypto.oprf.OPRF;
import sprout.oram.Bucket;
import sprout.oram.BucketException;
import sprout.oram.Tree;
import sprout.oram.TreeException;
import sprout.util.Util;

public class DecryptPath extends TreeOperation<DPOutput, EPath>{
	
	SecureRandom rnd = new SecureRandom();
  
  // Should only be used for reusing operations
  DecryptPath() {
    super(null, null);
  }

  public DecryptPath(Communication con1, Communication con2) {
    super(con1, con2);
  }

  /*
  public DecryptPath(Communication con1, Communication con2, ForestMetadata metadata) {
    super(con1, con2, metadata);
  }
  */

  @Override
  public DPOutput executeCharlieSubTree(Communication debbie, Communication eddie,
                                        String Li, Tree none1, EPath none2) {
    // protocol
    // step 1
    // party C
    // C sends Li to E
    eddie.write(Li);
    
    
    // step 3   
    // party C
    // E sends sigma_x to C
    ECPoint[] sigma_x = eddie.readECPointArray();
    
    // step 4
    // party C and D run OPRF on C's input sigma_x and D's input k
    OPRF oprf = OPRFHelper.getOPRF();
    String[] secretC_P = new String[sigma_x.length];
    for (int j=0; j<sigma_x.length; j++) {
      // This oprf should possibly be evaulated in as an Operation
      // For an easier description of the flow look at OPRFTest.java
      // TODO: May want a different encoding here we leave this until OPRF changes
      Message msg1 = oprf.prepare(sigma_x[j]);
      debbie.write(new Message(msg1.getV()));
      
      Message msg2 = debbie.readMessage();
      msg2.setW(msg1.getW());
      Message res = oprf.deblind(msg2);

      PRG G;
      try {
        G = new PRG(l); // TODO: solve this non-deterministic problem
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        return null;
      }
      secretC_P[j] = G.generateBitString(l, res.getResult());
    }
    // C outputs secretC_P
    
    return new DPOutput(secretC_P, null, null);
  }
  
  @Override
  public DPOutput executeDebbieSubTree(Communication charlie, Communication eddie,
                                       BigInteger k, Tree none1, EPath none2) {
    OPRF oprf = OPRFHelper.getOPRF(false);
    
    int length = 1;
    if (i > 0)
    	length = d_i+expen;
    for (int j=0; j < length; j++) {
      Message msg = charlie.readMessage();
      msg = oprf.evaluate(msg); // TODO: make use of the k
      charlie.write(msg);
    }
    
    // D outputs nothing
    DPOutput out = new DPOutput();
    return out;
  }
  
  @Override
  public DPOutput executeEddieSubTree(Communication charlie, Communication debbie,
                                      Tree OT, EPath none1) {	  
    // protocol
    // step 1
    // party C
    // C sends Li to E
    String Li = charlie.readString();    
    
    // step 2
    // party E
    // E retrieves encrypted path Pbar using Li
    Bucket[] Pbar = null;
	try {
		Pbar = OT.getBucketsOnPath(Li);
	} catch (TreeException e) {
		e.printStackTrace();
	} catch (BucketException e) {
		e.printStackTrace();
	}
    
    // step 3   
    // party E
    // E sends sigma_x to C
    List<Integer> sigma = new ArrayList<Integer>();   
    for (int j=0; j<Pbar.length; j++)
      sigma.add(j);
    Collections.shuffle(sigma, rnd); 
    
    ECPoint[] x = new ECPoint[Pbar.length];
    String[] Bbar = new String[Pbar.length];
    for (int j=0; j<Pbar.length; j++) {
    	x[j] = Util.byteArrayToECPoint(Pbar[j].getNonce());
    	Bbar[j] = Util.addZero(new BigInteger(1, Pbar[j].getByteTuples()).toString(2), l);
    }
    ECPoint[] sigma_x = Util.permute(x, sigma);
    String[] secretE_P = Util.permute(Bbar, sigma);
    
    charlie.write(sigma_x);
    // E outputs sigma and secretE_P
    
    return new DPOutput(null, secretE_P, sigma);
  }
  
  // Temporarily redefine n for decrpyt
  // We probably want to eventually unify the meaning of n
  @Override
  public void loadTreeSpecificParameters(Tree OT) {
    super.loadTreeSpecificParameters(OT);
    n = n/w;
  }
  @Override
  public EPath prepareArgs() {
	  return null;
	  /*
    if (i==0)
      return null;
    return new EPath(n, l);
    */
  }
}
