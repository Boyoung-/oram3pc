package sprout.oram.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bouncycastle.math.ec.ECPoint;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.PID;
import sprout.oram.PreData;
import sprout.oram.TID;

public class Precomputation extends TreeOperation<Object, Object> {

	public Precomputation(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public Object executeCharlieSubTree(Communication debbie,
			Communication eddie, Object unused) {
		// DecryptPath
		
		// OPRF
		PreData.oprf_oprf = OPRFHelper.getOPRF();
		PreData.oprf_gy = new ECPoint[levels][][];
		
		
		for (int index = 0; index < levels; index++) {
			loadTreeSpecificParameters(index);
			
			// DecryptPath
			
			// OPRF
			timing.stopwatch[PID.oprf][TID.offline].start();
			PreData.oprf_gy[i] = PreData.oprf_oprf.preparePairs(pathBuckets);
			timing.stopwatch[PID.oprf][TID.offline].stop();
			
		}
			
		return null;
	}

	@Override
	public Object executeDebbieSubTree(Communication charlie,
			Communication eddie, Object unused) {
		// DecryptPath
		
		// OPRF
		
		
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);
			
			// DecryptPath
			
			// OPRF
			
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object executeEddieSubTree(Communication charlie,
			Communication debbie, Object unused) {
		// DecryptPath
		PreData.decrypt_sigma = (List<Integer>[]) new List[levels];
		
		// OPRF
		
		
		
		for (int index = 0; index <= h; index++) {
			loadTreeSpecificParameters(index);
			
			// DecryptPath
			timing.stopwatch[PID.decrypt][TID.offline].start();
			PreData.decrypt_sigma[i] = new ArrayList<Integer>();
			for (int j = 0; j < pathBuckets; j++)
				PreData.decrypt_sigma[i].add(j);
			Collections.shuffle(PreData.decrypt_sigma[i], SR.rand);
			timing.stopwatch[PID.decrypt][TID.offline].stop();
			
			// OPRF
			
		}
		
		return null;
	}
}
