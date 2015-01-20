package sprout.oram;

import java.math.BigInteger;
import java.util.List;

import org.bouncycastle.math.ec.ECPoint;

import sprout.crypto.oprf.OPRF;

public class PreData {
	// DecryptPath
	public static List<Integer>[] decrypt_sigma;
	
	// OPRF
	public static OPRF oprf_oprf;
	public static ECPoint[][][] oprf_gy;
	
	// PET
	public static BigInteger[][] pet_alpha;
	public static BigInteger[][] pet_beta;
	public static BigInteger[][] pet_tau;
	public static BigInteger[][] pet_r;
	public static BigInteger[][] pet_gamma;
	public static BigInteger[][] pet_delta;
}
