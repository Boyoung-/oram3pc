package sprout.oram;

import java.math.BigInteger;
import java.util.List;

import YaoGC.Circuit;

public class PreData {
	// SSCOT
	public static byte[][] sscot_k;
	public static byte[][] sscot_k_p;
	public static BigInteger[][] sscot_r;

	// SSIOT
	public static byte[][] ssiot_k;
	public static byte[][] ssiot_k_p;
	public static BigInteger[] ssiot_r;

	// Access
	public static List<Integer>[] access_sigma;
	public static BigInteger[] access_p;
	public static BigInteger[] access_Li;

	// Reshuffle
	public static BigInteger[] reshuffle_p;
	public static BigInteger[] reshuffle_r;
	public static BigInteger[][] reshuffle_a_p;
	public static List<Integer>[] reshuffle_pi;

	// PPT
	public static BigInteger[] ppt_sC_Li_p;
	public static BigInteger[] ppt_sE_Li_p;
	public static BigInteger[] ppt_sC_Lip1_p;
	public static BigInteger[] ppt_sE_Lip1_p;
	public static BigInteger[][] ppt_r;
	public static BigInteger[][] ppt_r_p;
	public static int[] ppt_alpha;

	// XOT
	public static List<Integer>[][] xot_pi;
	public static List<Integer>[][] xot_pi_ivs;
	public static BigInteger[][][] xot_r;

	// SSXOT
	public static BigInteger[][] ssxot_delta;

	// GCF
	public static Circuit[][] gcf_gc_D;
	public static Circuit[][] gcf_gc_E;
	public static BigInteger[][][][] gcf_lbs;

	// Eviction
	public static BigInteger[] evict_upxi;
}
