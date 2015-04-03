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

public class PET extends Operation {

	public PET(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public int executeCharlie(Communication debbie, Communication eddie) {
		// Protocol
		// step 1
		BigInteger[] v = eddie.readBigIntegerArray();
		// step 2
		BigInteger[] w = debbie.readBigIntegerArray();
		
		// step 3
		int j;
		for (j = 0; j < v.length; j++) {
			if (v[j].compareTo(w[j]) == 0)
				break;
		}

		if (j == v.length)
			return -1; // error
		return j;
	}

	public void executeDebbie(Communication charlie, Communication eddie,
			int i, BigInteger[] c) {
		// protocol
		// step 2
		int m = 1 + ForestMetadata.getNBits(i);
		AES_PRF prf = null;
		try {
			prf = new AES_PRF(m);
			prf.init(PreData.pet_k[i]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		BigInteger[] w = new BigInteger[c.length];
		for (int j = 0; j < c.length; j++)
			try {
				w[j] = new BigInteger(1, prf.compute(PreData.pet_alpha[i][j].xor(c[j]).toByteArray()));
			} catch (Exception e) {
				e.printStackTrace();
			}

		charlie.write(w);
	}

	public void executeEddie(Communication charlie, Communication debbie,
			int i, BigInteger[] b) {
		// protocol
		// step 1
		int m = 1 + ForestMetadata.getNBits(i);
		AES_PRF prf = null;
		try {
			prf = new AES_PRF(m);
			prf.init(PreData.pet_k[i]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		BigInteger[] v = new BigInteger[b.length];
		for (int j = 0; j < b.length; j++)
			try {
				v[j] = new BigInteger(1, prf.compute(PreData.pet_alpha[i][j].xor(b[j]).toByteArray()));
			} catch (Exception e) {
				e.printStackTrace();
			}

		charlie.write(v);
	}

	// for testing correctness
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing PET  #####");
		
		if (party == Party.Eddie) {
			int levels = ForestMetadata.getLevels();
			int i = SR.rand.nextInt(levels - 1) + 1;
			int m = 1 + ForestMetadata.getNBits(i);
			int n = SR.rand.nextInt(50) + 50; // 50-99
			int j = SR.rand.nextInt(n);
			PreData.pet_k = new byte[levels][16];
			PreData.pet_alpha = new BigInteger[levels][n];
			BigInteger[] b = new BigInteger[n];
			BigInteger[] c = new BigInteger[n];
			SR.rand.nextBytes(PreData.pet_k[i]);
			for (int o = 0; o < n; o++) {
				PreData.pet_alpha[i][o] = new BigInteger(m, SR.rand);
				b[o] = new BigInteger(m, SR.rand);
				c[o] = new BigInteger(m, SR.rand);
				while(c[o].compareTo(b[o]) == 0)
					c[o] = new BigInteger(m, SR.rand);
			}
			c[j] = b[j];
			
			con2.write(i);
			con2.write(PreData.pet_k[i]);
			con2.write(PreData.pet_alpha[i]);
			con2.write(c);
			
			executeEddie(con1, con2, i, b);
			
			int out_j = con1.readInt();
			if (j == out_j)
				System.out.println("PET test passed: j=" + j); 
			else
				System.out.println("PET test failed: j=" + j + ", out_j=" + out_j);
		}
		else if (party == Party.Debbie) {
			int levels = ForestMetadata.getLevels();
			int i = con2.readInt();
			PreData.pet_k = new byte[levels][];
			PreData.pet_k[i] = con2.read();
			PreData.pet_alpha = new BigInteger[levels][];
			PreData.pet_alpha[i] = con2.readBigIntegerArray();
			BigInteger[] c = con2.readBigIntegerArray();
			
			executeDebbie(con1, con2, i, c);
		}
		else if (party == Party.Charlie) {
			int out_j = executeCharlie(con1, con2);
			con2.write(out_j);
		}
		
		System.out.println("#####  Testing PET Finished  #####");
	}
}
