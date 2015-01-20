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

	// AOT
	public static byte[][][] aot_k;

	// PPT
	public static BigInteger[] ppt_sC_Li_p;
	public static BigInteger[] ppt_sE_Li_p;

	// Reshuffle
	public static byte[][] reshuffle_s1;
	public static byte[][] reshuffle_s2;
	public static byte[][] reshuffle_p1;
	public static byte[][] reshuffle_p2;
	public static BigInteger[][] reshuffle_a;
	public static List<Integer>[] reshuffle_pi;
}
