package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.oram.ForestMetadata;
import sprout.oram.Tree;
import sprout.oram.Forest.TreeZero;
import sprout.ui.CryptoParam;
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
    BigInteger[] sigma_x = new BigInteger[d_i+expen];
    for (int j=0; j<d_i+expen; j++)
      sigma_x[j] = eddie.readBigInteger();
    
    // step 4
    // party C and D run OPRF on C's input sigma_x and D's input k
    // TODO: add OPRF
    // PRG is used here instead of OPRF for testing purpose
    // SKY: it seems like a PRG of a PRF is used here instead of an OPRF. Isn't sigma_x^k a PRF??
    //   This is only an issue because the rest of tEPath Pbar = he code depends on an l length string as the secret, where as i'm not sure
    //   that the OPRF will produce this. Because of this I've left the PRG in place
    String secretC_P = "";
    
    BigInteger k = debbie.readBigInteger();
    
    for (int j=0; j<d_i+expen; j++) {
      PRG G;
      try {
        G = new PRG(l);
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        return null;
      } // non-fresh generated SecureRandom cannot guarantee determinism... (why???) Sky: Did you seed it explicitly?
      secretC_P += G.generateBitString(l, sigma_x[j].modPow(k, CryptoParam.p));
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
    
    // TODO: add OPRF
    // For now PRG is used here instead
    charlie.write(k);
    
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
    BigInteger[] x = Pbar.x.clone();
    String[] Bbar = Pbar.Bbar.clone();
    BigInteger[] sigma_x = Util.permute(x, sigma);
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
    //n = n/w;
    
    // For now to be extra sure load parameters as originally written
    // I don't think this is needed, but leave this here until we have it working
    i         = h - treeLevel;
    d_i       = OT.getNumLevels();
    d_ip1       = -1;
    if (i == h)
      d_ip1     = OT.getDBytes() * 8 / twotaupow;
    else
      d_ip1     = metadata.getTupleBitsL(treeLevel-1);
    ln        = i * tau;          
    ll        = d_i;            
    ld        = twotaupow * d_ip1;          
    tupleBitLength  = 1 + ln + ll + ld;
    l       = tupleBitLength * w;  
    n       = d_i + expen;
  }
  @Override
  public EPath prepareArgs() {
    if (i==0)
      return null;
    return new EPath(n, l);
  }
}
