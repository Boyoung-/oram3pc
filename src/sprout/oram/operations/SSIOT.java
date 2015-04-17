package sprout.oram.operations;

import java.math.BigInteger;

import org.apache.commons.lang3.tuple.Pair;

import sprout.communication.Communication;
import sprout.crypto.AES_PRF;
import sprout.crypto.PRG;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.oram.PreData;

public class SSIOT extends Operation {
	public SSIOT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public Pair<Integer, BigInteger> executeCharlie(Communication D, Communication E, int i, int N, int l, int tau) {
		// protocol
		// step 1
		BigInteger[] e = E.readBigIntegerArray();
		BigInteger[] v = E.readBigIntegerArray();

		// step 2
		BigInteger p = D.readBigInteger();
		BigInteger w = D.readBigInteger();

		// step 3
		PRG G = new PRG(l);
		
		for (int j=0; j<N; j++) {
			if (v[j].compareTo(w) == 0) {
				BigInteger m = e[j].xor(new BigInteger(1, G.compute(p.toByteArray())));
				return Pair.of(j, m);
			}
		}

		// error
		return null;
	}

	public void executeDebbie(Communication C, Communication E, int i, int N, int l, int tau, BigInteger j_D) {
		// protocol
		// step 2
		int diffBits = SR.kBits - tau;
		AES_PRF F_k = new AES_PRF(SR.kBits);
		AES_PRF F_k_p = new AES_PRF(SR.kBits);
		F_k.init(PreData.ssiot_k[i]);
		F_k_p.init(PreData.ssiot_k_p[i]);
		
		BigInteger y = PreData.ssiot_r[i].xor(j_D.shiftLeft(diffBits));
		BigInteger p = new BigInteger(1, F_k.compute(y.toByteArray()));
		BigInteger w = new BigInteger(1, F_k_p.compute(y.toByteArray()));
		
		C.write(p);
		C.write(w);
	}

	public void executeEddie(Communication C, Communication D, int i, int N, int l, int tau, BigInteger[] m, BigInteger j_E) {
		// protocol
		// step 1
		int diffBits = SR.kBits - tau;
		BigInteger[] x = new BigInteger[N];
		BigInteger[] e = new BigInteger[N];
		BigInteger[] v = new BigInteger[N];
		AES_PRF F_k = new AES_PRF(SR.kBits);
		AES_PRF F_k_p = new AES_PRF(SR.kBits);
		PRG G = new PRG(l);
		F_k.init(PreData.ssiot_k[i]);
		F_k_p.init(PreData.ssiot_k_p[i]);
		
		for (int t=0; t<N; t++) {
			x[t] = PreData.ssiot_r[i].xor(j_E.xor(BigInteger.valueOf(t)).shiftLeft(diffBits));
			e[t] = new BigInteger(1, G.compute(F_k.compute(x[t].toByteArray()))).xor(m[t]);
			v[t] = new BigInteger(1, F_k_p.compute(x[t].toByteArray()));
		}
		
		C.write(e);
		C.write(v);
	}

	// for testing correctness
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing SSIOT  #####");
		
		if (party == Party.Eddie) {
			int levels = ForestMetadata.getLevels();
			int i = SR.rand.nextInt(levels - 1) + 1;
			int tau = 8;
			int N = (int) Math.pow(2, tau);
			int l = SR.rand.nextInt(50) + 50;
			int j = SR.rand.nextInt(N);
			BigInteger j_E = new BigInteger(tau, SR.rand);
			BigInteger j_D = BigInteger.valueOf(j).xor(j_E);
			
			PreData.ssiot_k = new byte[levels][16];
			PreData.ssiot_k_p = new byte[levels][16];
			PreData.ssiot_r = new BigInteger[levels];
			BigInteger[] m = new BigInteger[N];
			
			SR.rand.nextBytes(PreData.ssiot_k[i]);
			SR.rand.nextBytes(PreData.ssiot_k_p[i]);
			PreData.ssiot_r[i] = new BigInteger(SR.kBits, SR.rand);
			for (int o = 0; o < N; o++) {
				m[o] = new BigInteger(l, SR.rand);
			}
			
			con1.write(i);
			con1.write(N);
			con1.write(l);
			con1.write(tau);
			
			con2.write(i);
			con2.write(N);
			con2.write(l);
			con2.write(tau);
			con2.write(PreData.ssiot_k[i]);
			con2.write(PreData.ssiot_k_p[i]);
			con2.write(PreData.ssiot_r[i]);
			con2.write(j_D);
			
			executeEddie(con1, con2, i, N, l, tau, m, j_E);
			
			int output_j = con1.readInt();
			BigInteger output_m = con1.readBigInteger();
			
			if (j == output_j && m[j].compareTo(output_m) == 0) {
				System.out.println("SSIOT test passed:");
			}
			else {
				System.out.println("SSIOT test failed:");
			}
			System.out.println("j=" + j + ", output_j=" + output_j);
			System.out.println("m[j]=" + m[j] + ", output_m=" + output_m);
		}
		else if (party == Party.Debbie) {			
			int i = con2.readInt();
			int N = con2.readInt();
			int l = con2.readInt();
			int tau = con2.readInt();

			int levels = ForestMetadata.getLevels();
			PreData.ssiot_k = new byte[levels][];
			PreData.ssiot_k_p = new byte[levels][];
			PreData.ssiot_r = new BigInteger[levels];

			PreData.ssiot_k[i] = con2.read();
			PreData.ssiot_k_p[i] = con2.read();
			PreData.ssiot_r[i] = con2.readBigInteger();
			BigInteger j_D = con2.readBigInteger();
			
			executeDebbie(con1, con2, i, N, l, tau, j_D);
		}
		else if (party == Party.Charlie) {			
			int i = con2.readInt();
			int N = con2.readInt();
			int l = con2.readInt();
			int tau = con2.readInt();
			
			Pair<Integer, BigInteger> output = executeCharlie(con1, con2, i, N, l, tau);
			int j = output.getLeft();
			BigInteger m = output.getRight();
			
			con2.write(j);
			con2.write(m);
		}

		System.out.println("#####  Testing SSIOT Finished  #####");
	}
}
