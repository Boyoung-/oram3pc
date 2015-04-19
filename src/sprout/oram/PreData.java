package sprout.oram;

import java.math.BigInteger;
import java.util.List;

import org.bouncycastle.math.ec.ECPoint;

import YaoGC.Circuit;

// TODO: clean unused stuff

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
	
	// Reshuffle
	public static BigInteger[] reshuffle_p;
	public static BigInteger[] reshuffle_r;
	public static BigInteger[][] reshuffle_a_p;
	public static List<Integer>[] reshuffle_pi;
	
	
	
	
	
	
	////// OLD STUFF /////////
	
	

	// PET
	public static BigInteger[][] pet_alpha;
	public static byte[][] pet_k;

	// AOT
	public static byte[][] aot_k;
	
	// AOTSS
	public static byte[][] aotss_k;

	// PPT
	public static BigInteger[] ppt_sC_Li_p;
	public static BigInteger[] ppt_sE_Li_p;

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
