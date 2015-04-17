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

public class SSCOT extends Operation {
	public SSCOT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public Pair<Integer, BigInteger> executeCharlie(Communication D, Communication E, int i, int N, int l, int l_p) {
		// protocol
		// step 1
		BigInteger[] e = E.readBigIntegerArray();
		BigInteger[] v = E.readBigIntegerArray();

		// step 2
		BigInteger[] p = D.readBigIntegerArray();
		BigInteger[] w = D.readBigIntegerArray();

		// step 3
		PRG G = new PRG(l);
		
		for (int t=0; t<N; t++) {
			if (v[t].compareTo(w[t]) == 0) {
				BigInteger m_t = e[t].xor(new BigInteger(1, G.compute(p[t].toByteArray())));
				return Pair.of(t, m_t);
			}
		}

		// error
		return null;
	}

	public void executeDebbie(Communication C, Communication E, int i, int N, int l, int l_p, BigInteger[] b) {
		// protocol
		// step 2
		int diffBits = SR.kBits - l_p;
		BigInteger[] y = new BigInteger[N];
		BigInteger[] p = new BigInteger[N];
		BigInteger[] w = new BigInteger[N];
		AES_PRF F_k = new AES_PRF(SR.kBits);
		AES_PRF F_k_p = new AES_PRF(SR.kBits);
		F_k.init(PreData.sscot_k[i]);
		F_k_p.init(PreData.sscot_k_p[i]);
		
		for (int t=0; t<N; t++) {
			y[t] = PreData.sscot_r[i][t].xor(b[t].shiftLeft(diffBits));
			p[t] = new BigInteger(1, F_k.compute(y[t].toByteArray()));
			w[t] = new BigInteger(1, F_k_p.compute(y[t].toByteArray()));
		}
		
		C.write(p);
		C.write(w);
	}

	public void executeEddie(Communication C, Communication D, int i, int N, int l, int l_p, BigInteger[] m, BigInteger[] a) {
		// protocol
		// step 1
		int diffBits = SR.kBits - l_p;
		BigInteger[] x = new BigInteger[N];
		BigInteger[] e = new BigInteger[N];
		BigInteger[] v = new BigInteger[N];
		AES_PRF F_k = new AES_PRF(SR.kBits);
		AES_PRF F_k_p = new AES_PRF(SR.kBits);
		PRG G = new PRG(l);
		F_k.init(PreData.sscot_k[i]);
		F_k_p.init(PreData.sscot_k_p[i]);
		
		for (int t=0; t<N; t++) {
			x[t] = PreData.sscot_r[i][t].xor(a[t].shiftLeft(diffBits));
			e[t] = new BigInteger(1, G.compute(F_k.compute(x[t].toByteArray()))).xor(m[t]);
			v[t] = new BigInteger(1, F_k_p.compute(x[t].toByteArray()));
		}
		
		C.write(e);
		C.write(v);
	}

	// for testing correctness
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing SSCOT  #####");
		
		if (party == Party.Eddie) {
			int levels = ForestMetadata.getLevels();
			int i = SR.rand.nextInt(levels - 1) + 1;
			int N = SR.rand.nextInt(50) + 150; // 150-199
			int l = ForestMetadata.getTupleBits(i);
			int l_p = 1 + ForestMetadata.getNBits(i);
			int t = SR.rand.nextInt(N);
			
			PreData.sscot_k = new byte[levels][16];
			PreData.sscot_k_p = new byte[levels][16];
			PreData.sscot_r = new BigInteger[levels][N];
			BigInteger[] a = new BigInteger[N];
			BigInteger[] b = new BigInteger[N];
			BigInteger[] m = new BigInteger[N];
			
			SR.rand.nextBytes(PreData.sscot_k[i]);
			SR.rand.nextBytes(PreData.sscot_k_p[i]);
			for (int o = 0; o < N; o++) {
				PreData.sscot_r[i][o] = new BigInteger(SR.kBits, SR.rand);
				a[o] = new BigInteger(l_p, SR.rand);
				b[o] = new BigInteger(l_p, SR.rand);
				while(a[o].compareTo(b[o]) == 0)
					b[o] = new BigInteger(l_p, SR.rand);
				m[o] = new BigInteger(l, SR.rand);
			}
			a[t] = b[t];
			
			con1.write(i);
			con1.write(N);
			con1.write(l);
			con1.write(l_p);
			
			con2.write(i);
			con2.write(N);
			con2.write(l);
			con2.write(l_p);
			con2.write(PreData.sscot_k[i]);
			con2.write(PreData.sscot_k_p[i]);
			con2.write(PreData.sscot_r[i]);
			con2.write(b);
			
			executeEddie(con1, con2, i, N, l, l_p, m, a);
			
			int output_t = con1.readInt();
			BigInteger m_t = con1.readBigInteger();
			
			if (t == output_t && m[t].compareTo(m_t) == 0) {
				System.out.println("SSCOT test passed:");
			}
			else {
				System.out.println("SSCOT test failed:");
			}
			System.out.println("t=" + t + ", output_t=" + output_t);
			System.out.println("m[t]=" + m[t] + ", m_t=" + m_t);
		}
		else if (party == Party.Debbie) {			
			int i = con2.readInt();
			int N = con2.readInt();
			int l = con2.readInt();
			int l_p = con2.readInt();

			int levels = ForestMetadata.getLevels();
			PreData.sscot_k = new byte[levels][];
			PreData.sscot_k_p = new byte[levels][];
			PreData.sscot_r = new BigInteger[levels][];

			PreData.sscot_k[i] = con2.read();
			PreData.sscot_k_p[i] = con2.read();
			PreData.sscot_r[i] = con2.readBigIntegerArray();
			BigInteger[] b = con2.readBigIntegerArray();
			
			executeDebbie(con1, con2, i, N, l, l_p, b);
		}
		else if (party == Party.Charlie) {			
			int i = con2.readInt();
			int N = con2.readInt();
			int l = con2.readInt();
			int l_p = con2.readInt();
			
			Pair<Integer, BigInteger> output = executeCharlie(con1, con2, i, N, l, l_p);
			int t = output.getLeft();
			BigInteger m_t = output.getRight();
			
			con2.write(t);
			con2.write(m_t);
		}

		System.out.println("#####  Testing SSCOT Finished  #####");
	}
}
