package sprout.oram.operations;

import java.io.File;
import java.io.FileNotFoundException;

import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.util.Timing;

public abstract class Operation {  
  Communication con1, con2;
  
  public static Timing timing;
  
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
   * con1 = charlie
   * con2 = debbie
   */
  public Operation(Communication con1, Communication con2) {
    this.con1 = con1;
    this.con2 = con2;
  }
  
  private static final boolean ENSURE_SANITY = false;
  // Utility function will test for synchrony between the parties.
  public void sanityCheck() {
    if (ENSURE_SANITY) {
    	con1.countBandwidth = false;
    	con2.countBandwidth = false;
    	
      System.out.println("Performing sanity check");
      con1.write("sanity");
      con2.write("sanity");
      
      if (!con1.readString().equals("sanity")) {
        System.out.println("Sanity check failed for con1");
      } if (!con2.readString().equals("sanity")) {
        System.out.println("Sanity check failed for con2");
      }
      
      System.out.println("Sanity check finished");
      
      con1.countBandwidth = true;
      con2.countBandwidth = true;
    }
  }
  
  // Even though many operations don't rely on the existance of a forest, we include it here to have a 
  //  unifying api
  public void run(Party party) throws ForestException {
    run(party, "config/newConfig.yaml", "files/forest.bin", false);
  }
  public void run(Party party, String configFile, String dbFile) throws ForestException {
    run(party, configFile, dbFile, false);
  }
  public void run(Party party, String configFile, String dbFile, boolean build) throws ForestException {
	  /*
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
    */
	  
	if (!build && !(new File(dbFile)).exists()) {
	      throw new IllegalArgumentException("DB file does not exist " + dbFile);
	    }
    
	try {
		ForestMetadata.setup(configFile);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
    Forest forest = null;
    if (party == Party.Eddie) {
		if (build)
			try {
				forest = new Forest();
			} catch (Exception e) {
				e.printStackTrace();
			}
		else { 
			try {
				forest = new Forest(dbFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
    
    run(party, forest);
  }
  
  /*
   * This is mostly just testing code and may need to change for the purpose of an actual execution
   */
  public abstract void run(Party party, Forest forest) throws ForestException;
}
