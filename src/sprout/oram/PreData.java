package sprout.oram;

import java.math.BigInteger;
import java.util.List;

import org.bouncycastle.math.ec.ECPoint;

import YaoGC.Circuit;
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

	// GCF
	public static Circuit[][] gcf_gc_D;
	public static Circuit[][] gcf_gc_E;
	public static BigInteger[][][][] gcf_lbs;

	// IOT
	public static List<Integer>[][] iot_pi;
	public static List<Integer>[][] iot_pi_ivs;
	public static BigInteger[][][] iot_r;
	public static byte[][][] iot_s;

	// Encrypt
	public static byte[][] encrypt_s;
	public static ECPoint[][] encrypt_x;
	public static BigInteger[][] encrypt_c;
	public static BigInteger[][] encrypt_a;
}
