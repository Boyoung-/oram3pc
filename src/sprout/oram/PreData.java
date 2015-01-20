package sprout.oram;

import java.util.List;

import org.bouncycastle.math.ec.ECPoint;

import sprout.crypto.oprf.OPRF;

public class PreData {
	// DecryptPath
	public static List<Integer>[] decrypt_sigma;
	
	// OPRF
	public static OPRF oprf_oprf;
	public static ECPoint[][][] oprf_gy;
}
