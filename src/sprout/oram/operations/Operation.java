package sprout.oram.operations;

import java.io.File;
import java.security.SecureRandom;

import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.Party;

public abstract class Operation {
  static SecureRandom rnd = new SecureRandom();
  
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
  public Operation(Communication con1, Communication con2) {
    this.con1 = con1;
    this.con2 = con2;
  }
  
  // Even though many operations don't rely on the existance of a forest, we include it here to have a 
  //  unifying api
  public void run(Party party) throws ForestException {
    run(party, "config/smallConfig.yaml", "config/smallData.txt", "db.bin", false);
  }
  public void run(Party party, String configFile, String dbFile) throws ForestException {
    run(party, configFile, dbFile, null, false);
  }
  public void run(Party party, String configFile, String dbFile, String dataFile, boolean build) throws ForestException {
    if (build && (dataFile == null || !(new File(dataFile)).exists())) {
      throw new IllegalArgumentException("Must supply a data file to build the database");
    } else if (!build && !(new File(dbFile)).exists()) {
      throw new IllegalArgumentException("DB file does not exist " + dbFile);
    }
    
    Forest forest = new Forest();
    try {
      if (build) {
        System.out.println("Creating forest at " + dbFile + " with parameters " + configFile);
        forest.buildFromFile(configFile, dataFile, dbFile);
        System.out.println("Forest built.\n");
      } else {
        System.out.println("Opening forest stored at " + configFile + " : " + dbFile);
        forest.loadFile(configFile, dbFile);
        System.out.println("Forest loaded.\n");
      }
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    
    run(party, forest);
  }
  
  /*
   * This is mostly just testing code and may need to change for the purpose of an actual execution
   */
  public abstract void run(Party party, Forest forest) throws ForestException;
}
