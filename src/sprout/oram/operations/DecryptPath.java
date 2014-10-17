package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bouncycastle.math.ec.ECPoint;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.oprf.Message;
import sprout.crypto.oprf.OPRF;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.oram.Forest.TreeZero;
import sprout.util.Util;

public class DecryptPath extends TreeOperation<DPOutput, EPath>{
  
  // Should only be used for reusing operations
  DecryptPath(ForestMetadata metadata) {
    super(null, null, metadata);
  }

  public DecryptPath(Communication con1, Communication con2) {
    super(con1, con2);
  }

  public DecryptPath(Communication con1, Communication con2, ForestMetadata metadata) {
    super(con1, con2, metadata);
  }

  @Override
  public DPOutput executeCharlieSubTree(Communication debbie, Communication eddie,
                                        String Li, TreeZero OT_0, Tree OT, EPath Pbar) {
    // i = 0 case
    // TODO: encrypt data when generating the forest, then modify the code here
    // right now the nonce are all 0s
    if (i == 0) {
      String secretC_P = Util.addZero(OT_0.nonce.toString(2), ld);
      return new DPOutput(secretC_P, null, null);
    }
    
    // protocol
    // step 1
    // party C
    // C sends Li to E
    eddie.write(Li);
    
    // step 3   
    // party C
    // E sends sigma_x to C
    ECPoint[] sigma_x = new ECPoint[d_i+expen];
    for (int j=0; j<d_i+expen; j++)
      sigma_x[j] = eddie.readECPoint();
    
    // step 4
    // party C and D run OPRF on C's input sigma_x and D's input k
    OPRF oprf = OPRFHelper.getOPRF();
    PRG G;
    try {
      G = new PRG(l);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
    
    String secretC_P = "";
    for (int j=0; j<d_i+expen; j++) {
      // This oprf should possibly be evaulated in as an Operation
      // For an easier description of the flow look at OPRFTest.java
      // TODO: May want a different encoding here we leave this until OPRF changes
      Message msg1 = oprf.prepare(sigma_x[j]);
      debbie.write(new Message(msg1.getV()));
      
      Message msg2 = debbie.readMessage();
      msg2.setW(msg1.getW());
      Message res = oprf.deblind(msg2);
      
      secretC_P += G.generateBitString(l, res.getResult());
    }
    // C outputs secretC_P
    
    return new DPOutput(secretC_P, null, null);
  }
  
  @Override
  public DPOutput executeDebbieSubTree(Communication charlie, Communication eddie,
                                       BigInteger k, TreeZero OT_0, Tree OT, EPath Pbar) {
    // i = 0 case
    DPOutput out = new DPOutput();
    if (i == 0) {
      return out;
    }
    
    OPRF oprf = OPRFHelper.getOPRF(false);
    
    for (int j=0; j < d_i+expen; j++) {
      Message msg = charlie.readMessage();
      msg = oprf.evaluate(msg);
      charlie.write(msg);
    }
    
    // D outputs nothing
    return out;
  }
  
  @Override
  public DPOutput executeEddieSubTree(Communication charlie, Communication debbie,
                                      TreeZero OT_0, Tree OT, EPath Pbar) {
 // i = 0 case
    // TODO: encrypt data when generating the forest, then modify the code here
    // right now the nonce are all 0s
    List<Integer> sigma = new ArrayList<Integer>();   
    if (i == 0) {
      String secretE_P = Util.byteArraytoKaryString(OT_0.initialEntry, 2, ld);
      sigma.add(0);
      return new DPOutput(null, secretE_P, sigma);
    }
    
    // protocol
    // step 1
    // party C
    // C sends Li to E
    String Li = charlie.readString();
    
    
    // step 2
    // party E
    // E retrieves encrypted path Pbar using Li
    // TODO: encrypt data when building the forest, so we can retrieve path using Li
    // SKY: Does this retrieval take any work? If so maybe we can mock it?
    
    // step 3   
    // party E
    // E sends sigma_x to C
    for (int j=0; j<d_i+expen; j++)
      sigma.add(j);
    Collections.shuffle(sigma); // random permutation // TODO: This may  not be random enough
    ECPoint[] x = Pbar.x.clone();
    String[] Bbar = Pbar.Bbar.clone();
    ECPoint[] sigma_x = Util.permute(x, sigma);
    String[] secretE_P_arr = Util.permute(Bbar, sigma);
    String secretE_P = "";
    for (int j=0; j<d_i+expen; j++)
      secretE_P += secretE_P_arr[j];
    
    for (int j=0; j<d_i+expen; j++)
      charlie.write(sigma_x[j]);
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
    if (i==0)
      return null;
    return new EPath(n, l);
  }
}
