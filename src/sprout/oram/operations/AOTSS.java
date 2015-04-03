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

public class AOTSS extends Operation {
	public AOTSS(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public BigInteger executeCharlie(Communication D, Communication E, BigInteger r) {
		// protocol
		// step 1
		BigInteger alpha = E.readBigInteger();
		BigInteger[] m_p = E.readBigIntegerArray();

		// step 2
		BigInteger j_p = r.xor(alpha);
		D.write(j_p);

		// step 3
		BigInteger c = D.readBigInteger();
		BigInteger m_j = c.xor(m_p[j_p.intValue()]);

		return m_j;
	}

	public void executeDebbie(Communication C, Communication E, int i, int d_ip1) {
		// protocol
		// step 2
		BigInteger j_p = C.readBigInteger();

		// step 3
		BigInteger c = null;
		try {
			AES_PRF F = new AES_PRF(d_ip1);
			F.init(PreData.aotss_k[i]);
			c = new BigInteger(1, F.compute(j_p.toByteArray()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		C.write(c);
	}

	public void executeEddie(Communication C, Communication D, int i,
			int d_ip1, BigInteger[] m, BigInteger s) {
		// protocol
		// step 1
		int N = m.length;
		int tau = ForestMetadata.getTau();
		BigInteger[] m_p = new BigInteger[N];
		BigInteger alpha = new BigInteger(tau, SR.rand);

		try {
			AES_PRF F = new AES_PRF(d_ip1);
			F.init(PreData.aotss_k[i]);
			for (int t = 0; t < N; t++) {
				BigInteger bigint_t = BigInteger.valueOf(t);
				m_p[t] = new BigInteger(1, F.compute(bigint_t.toByteArray()))
						.xor(m[bigint_t.xor(s).xor(alpha).intValue()]);
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
		System.out.println("#####  Testing AOTSS  #####");

		if (party == Party.Eddie) {
			int levels = ForestMetadata.getLevels();
			int i = SR.rand.nextInt(levels - 1) + 1;
			int tau = ForestMetadata.getTau();
			int N = ForestMetadata.getTwoTauPow(); // 2^tau
			int l; // d_ip1
			if (i == levels-1)
				l = ForestMetadata.getABits(i) / N;
			else
				l = ForestMetadata.getLBits(i + 1);
			int j = SR.rand.nextInt(N);
			BigInteger s = new BigInteger(tau, SR.rand);
			BigInteger r = BigInteger.valueOf(j).xor(s);
			PreData.aotss_k = new byte[levels][16];
			BigInteger[] m = new BigInteger[N];
			SR.rand.nextBytes(PreData.aotss_k[i]);
			for (int o = 0; o < N; o++)
				m[o] = new BigInteger(l, SR.rand);

			con2.write(i);
			con2.write(PreData.aotss_k[i]);
			con1.write(r);

			executeEddie(con1, con2, i, l, m, s);

			BigInteger m_j = con1.readBigInteger();
			if (m[j].compareTo(m_j) == 0)
				System.out.println("AOTSS test passed: m[j]=" + m[j]);
			else
				System.out.println("AOTSS test failed: m[j]=" + m[j] + ", m_j="
						+ m_j);
		} else if (party == Party.Debbie) {
			int levels = ForestMetadata.getLevels();
			int i = con2.readInt();
			PreData.aotss_k = new byte[levels][];
			PreData.aotss_k[i] = con2.read();
			int N = ForestMetadata.getTwoTauPow(); // 2^tau
			int l; // d_ip1
			if (i == levels-1)
				l = ForestMetadata.getABits(i) / N;
			else
				l = ForestMetadata.getLBits(i + 1);

			executeDebbie(con1, con2, i, l);
		} else if (party == Party.Charlie) {
			BigInteger r = con2.readBigInteger();

			BigInteger m_j = executeCharlie(con1, con2, r);
			con2.write(m_j);
		}

		System.out.println("#####  Testing AOTSS Finished  #####");
	}
}
