package sprout.ui;

import java.io.IOException;

import sprout.crypto.oprf.OPRF;

public class generateOPRFKeys {

  public static String publicFilename = "keys/publicKey";
  public static String privateFilename = "keys/privateKey";
  
  public static void main(String[] args)
  {
    OPRF serverOprf = new OPRF();
    OPRF clientOprf = new OPRF(serverOprf.getPK());
    
    try {
      serverOprf.save(publicFilename);
      clientOprf.save(privateFilename);
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
