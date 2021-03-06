package sprout.oram.operations;

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
	 * For Charlie con1 = debbie con2 = eddie
	 * 
	 * For Debbie con1 = charlie con2 = eddie
	 * 
	 * For Eddie con1 = charlie con2 = debbie
	 */
	public Operation(Communication con1, Communication con2) {
		this.con1 = con1;
		this.con2 = con2;
	}

	private static final boolean ENSURE_SANITY = true;

	public boolean ifSanityCheck() {
		return ENSURE_SANITY;
	}

	// Utility function will test for synchrony between the parties.
	public void sanityCheck() {
		if (ENSURE_SANITY) {
			
			// System.out.println("Sanity check");
			con1.write("sanity");
			con2.write("sanity");

			if (!con1.readString().equals("sanity")) {
				System.out.println("Sanity check failed for con1");
			}
			if (!con2.readString().equals("sanity")) {
				System.out.println("Sanity check failed for con2");
			}
		}
	}

	// Even though many operations don't rely on the existance of a forest, we
	// include it here to have a unifying api
	public void run(Party party) throws ForestException {
		run(party, "config/newConfig.yaml", "files/forest.bin", false);
	}

	public void run(Party party, String configFile, String dbFile)
			throws ForestException {
		run(party, configFile, dbFile, false);
	}

	public void run(Party party, String configFile, String dbFile, boolean build)
			throws ForestException {

		Forest forest = null;
		if (party == Party.Eddie) {
			if (build)
				try {
					forest = new Forest("init");
				} catch (Exception e) {
					e.printStackTrace();
				}
			else {
				try {
					if (!Forest.loadPathCheat())
						forest = new Forest("restore");
					else
						forest = new Forest("loadpathcheat");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (party == Party.Debbie) {
			try {
				if (!Forest.loadPathCheat())
					forest = new Forest("restore",
							ForestMetadata.getDefaultForestNames()[1], null);
				else
					forest = new Forest("loadpathcheat",
							ForestMetadata.getDefaultPathNames()[1], null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		run(party, forest);
	}

	/*
	 * This is mostly just testing code and may need to change for the purpose
	 * of an actual execution
	 */
	public abstract void run(Party party, Forest forest) throws ForestException;
}
