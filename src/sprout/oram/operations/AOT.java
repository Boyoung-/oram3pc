package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.crypto.AES_PRF;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.oram.PreData;

public class AOT extends Operation {
	public AOT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public BigInteger executeCharlie(Communication D, Communication E, int j) {
		// protocol
		// step 1
		int alpha = E.readInt();
		BigInteger[] m_p = E.readBigIntegerArray();

		// step 2
		int N = m_p.length;
		int j_p = (j + alpha) / N;
		D.write(j_p);

		// step 3
		BigInteger c = D.readBigInteger();
		BigInteger m_j = c.xor(m_p[j]);

		return m_j;
	}

	public void executeDebbie(Communication C, Communication E, int i) {
		// protocol
		// step 2
		int j_p = C.readInt();

		// step 3
		int l = ForestMetadata.getABits(i);
		BigInteger c = null;
		try {
			AES_PRF F = new AES_PRF(l);
			F.init(PreData.aot_k[i]);
			c = new BigInteger(1, F.compute(BigInteger.valueOf(j_p)
					.toByteArray()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		C.write(c);
	}

	public void executeEddie(Communication C, Communication D, int i, BigInteger[] m) {
		// protocol
		// step 1
		int l = ForestMetadata.getABits(i);
		int N = m.length;
		BigInteger[] m_p = new BigInteger[N];
		int alpha = SR.rand.nextInt(N);

		try {
			AES_PRF F = new AES_PRF(l);
			F.init(PreData.aot_k[i]);
			for (int t = 0; t < N; t++) {
				m_p[t] = new BigInteger(1, F.compute(BigInteger.valueOf(
						(t + alpha) / N).toByteArray())).xor(m[t]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		C.write(alpha);
		C.write(m_p);
	}

	// for testing correctness
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing AOT  #####");

		if (party == Party.Eddie) {
			int levels = ForestMetadata.getLevels();
			int i = SR.rand.nextInt(levels - 1) + 1;
			int l = ForestMetadata.getABits(i);
			int N = SR.rand.nextInt(50) + 50; // 50-99
			int j = SR.rand.nextInt(N);
			PreData.aot_k = new byte[levels][16];
			BigInteger[] m = new BigInteger[N];
			SR.rand.nextBytes(PreData.aot_k[i]);
			for (int o = 0; o < N; o++)
				m[o] = new BigInteger(l, SR.rand);

			con2.write(i);
			con2.write(PreData.aot_k[i]);
			con1.write(j);

			executeEddie(con1, con2, i, m);

			BigInteger m_j = con1.readBigInteger();
			if (m[j].compareTo(m_j) == 0)
				System.out.println("AOT test passed: m[j]=" + m[j]);
			else
				System.out.println("AOT test failed: m[j]=" + m[j] + ", m_j=" + m_j);
		} else if (party == Party.Debbie) {
			int levels = ForestMetadata.getLevels();
			int i = con2.readInt();
			PreData.aot_k = new byte[levels][];
			PreData.aot_k[i] = con2.read();

			executeDebbie(con1, con2, i);
		} else if (party == Party.Charlie) {
			int j = con2.readInt();
					
			BigInteger m_j = executeCharlie(con1, con2, j);
			con2.write(m_j);
		}

		System.out.println("#####  Testing AOT Finished  #####");
	}
}
