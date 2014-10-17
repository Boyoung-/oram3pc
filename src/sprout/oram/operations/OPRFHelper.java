package sprout.oram.operations;

import java.io.IOException;

import sprout.crypto.oprf.OPRF;
import sprout.oram.Party;
import sprout.ui.generateOPRFKeys;

// TODO: Switch from using generateOPRFKEys filenames to ones specified at runtime (or a config file)
public class OPRFHelper {
  private static OPRF oprf= null;
  
  /**
   * Return the memoized oprf or a new party appropriate oprf.
   * @param party
   * @return
   */
  public static OPRF getOPRF(Party party) {
    switch (party) {
    case Charlie:
      return getOPRF(generateOPRFKeys.privateFilename);
    case Debbie:
      return getOPRF(generateOPRFKeys.publicFilename);
    case Eddie:
      return getOPRF(generateOPRFKeys.privateFilename);
    }
    
    return null;
  }
  
  /**
   * Return the memoized oprf or a new  oprf.
   * @param pub If true return a oprf without the private key.
   * @return
   */
  public static OPRF getOPRF(boolean pub) {
    if (pub) {
      return getOPRF(generateOPRFKeys.publicFilename);
    } else {
      return getOPRF(generateOPRFKeys.privateFilename);
    }
  }
  
  public static OPRF getOPRF() {
    return getOPRF(true);
  }
  
  /**
   * Return the memoized oprf or load from filename
   * @param filename
   * @return
   */
  public static OPRF getOPRF(String filename) {
    if (oprf == null) {
      try {
        oprf = new OPRF(filename);
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    }
  
    return oprf; 
  }
  
  public static OPRF setOPRF(OPRF _oprf) {
    oprf = _oprf;
    return oprf;
  }
}
