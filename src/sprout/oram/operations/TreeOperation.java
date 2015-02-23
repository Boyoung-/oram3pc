package sprout.oram.operations;

import org.apache.commons.lang3.NotImplementedException;

import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.util.Timing;

public abstract class TreeOperation<T extends Object, V> extends Operation {

	int tau; // tau in the writeup
	int twotaupow; // 2^tau
	int levels; // # trees
	int h; // # trees - 1
	int w; // # tuples in each bucket
	int expen; // # buckets in each leaf

	static boolean print_out = true;

	public TreeOperation(Communication con1, Communication con2) {
		super(con1, con2);
		initializeMetadata();
	}

	private void initializeMetadata() {
		tau = ForestMetadata.getTau();
		twotaupow = ForestMetadata.getTwoTauPow();
		levels = ForestMetadata.getLevels();
		h = levels - 1;
		w = ForestMetadata.getBucketDepth();
		expen = ForestMetadata.getLeafExpansion();
	}

	int i; // tree index in the writeup
	int d_i; // # levels in this tree (excluding the root level)
	int d_ip1; // # levels in the next tree (excluding the root level)
	int nBits;
	int lBits;
	int aBits;
	int tupleBits;
	int bucketBits;
	int pathBuckets;
	int pathTuples;

	public void loadTreeSpecificParameters(int index) {
		// TODO: Do these need to be accessed by all parties? If not we should
		// separate them out.
		i = index; // tree index in the writeup
		d_i = ForestMetadata.getLBits(i); // # levels in this tree (excluding
											// the root level)
		if (i == h)
			d_ip1 = ForestMetadata.getABits(i) / twotaupow;
		else
			d_ip1 = ForestMetadata.getLBits(i + 1);
		nBits = ForestMetadata.getNBits(i); // # N bits
		lBits = d_i; // # L bits
		aBits = ForestMetadata.getABits(i); // # data bits
		tupleBits = ForestMetadata.getTupleBits(i); // # tuple size (bits)
		bucketBits = ForestMetadata.getBucketTupleBits(i); // # bucket tuples'
															// size (bits)
		if (i == 0) {
			pathBuckets = 1;
			pathTuples = 1;
		} else {
			pathBuckets = d_i + expen;
			pathTuples = pathBuckets * w;
		}
	}

	/*
	 * public T execute(Party party, BigInteger Li, BigInteger k, Tree OT, V
	 * extraArgs) { loadTreeSpecificParameters(i);
	 * 
	 * // TODO: remove unnecessary args switch (party) { case Charlie: return
	 * executeCharlieSubTree(con1, con2, Li, OT, extraArgs); case Debbie: return
	 * executeDebbieSubTree(con1, con2, k, OT, extraArgs); case Eddie: return
	 * executeEddieSubTree(con1, con2, OT, extraArgs); } return null; }
	 */
	/*
	public T execut(Party party, V args) {
		loadTreeSpecificParameters(i);

		// TODO: remove unnecessary args
		switch (party) {
		case Charlie:
			return executeCharlieSubTree(con1, con2, args);
		case Debbie:
			return executeDebbieSubTree(con1, con2, args);
		case Eddie:
			return executeEddieSubTree(con1, con2, args);
		}
		return null;
	}
	*/

	/*
	 * This is mostly just testing code and may need to change for the purpose
	 * of an actual execution
	 */

	@Override
	public void run(Party party, Forest forest) throws ForestException {
		/*
		 * initializeMetadata();
		 * 
		 * BigInteger k = OPRFHelper.getOPRF(party).getK(); for (int i = 0; i <=
		 * h; i++) { Tree OT = null; if (forest != null) OT = forest.getTree(i);
		 * this.loadTreeSpecificParameters(i); BigInteger Li = new
		 * BigInteger(lBits, SR.rand);
		 * 
		 * T out = execute(party, prepareArgs(party)); if (print_out && out !=
		 * null) System.out.println("Output i=" + i + " : \n" + out.toString());
		 * else System.out.println("Finished round " + i); }
		 */
	}

	/*
	 * public abstract T executeCharlieSubTree(Communication debbie,
	 * Communication eddie, BigInteger Li, Tree OT, V extraArgs);
	 * 
	 * public abstract T executeDebbieSubTree(Communication charlie,
	 * Communication eddie, BigInteger k, Tree OT, V extraArgs);
	 * 
	 * public abstract T executeEddieSubTree(Communication charlie,
	 * Communication debbie, Tree OT, V extraArgs);
	 */
	public abstract T executeCharlieSubTree(Communication debbie,
			Communication eddie, Timing localTiming, V args);

	public abstract T executeDebbieSubTree(Communication charlie,
			Communication eddie, Timing localTiming, V args);

	public abstract T executeEddieSubTree(Communication charlie,
			Communication debbie, Timing localTiming, V args);

	public V prepareArgs() {
		return prepareArgs(null);
	}

	public V prepareArgs(Party party) {
		if (party == null) {
			throw new NotImplementedException(
					"Must overide prepareArgs() or prepareArgs(Party)");
		}
		return prepareArgs();
	}
	// TODO: Add timing information

}
