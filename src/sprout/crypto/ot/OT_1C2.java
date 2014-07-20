package sprout.crypto.ot;

import sprout.communication.Message;
import sprout.crypto.CryptoException;
import sprout.crypto.SecureRandom;

import org.bouncycastle.pqc.math.linearalgebra.*;

/**
 * This class implements the steps of a (1,2) OT 
 */
public class OT_1C2
{
	// HelperInit((n,2)
	// 1) choose three random bits, x0<-n, x1<-n, a<-1
	// 2) send x0,x1 to S, a ;;;;  xa to R
	public Message HelperInit(int n)
	{
		Message data = new Message();
		
		int nBytes = (int)Math.ceil(((n + 7) / 8));
		byte[] x0 = SecureRandom.generateRandomBytes(nBytes);
		data.arrayMap.put("x0", x0);
		byte[] x1 = SecureRandom.generateRandomBytes(nBytes);
		data.arrayMap.put("x1", x1);
		byte a = SecureRandom.generateRandomBit();
		data.scalarMap.put("a", a);
		
		return data;
	}
	
	// ReceiverXor(b):
	// 1) read a, xa from H
	// 2) compute c = a XOR b
	// 3) send c to S
	public Message ReceiverXor(byte b, byte a, byte[] xa)
	{
		Message data = new Message();
		byte c = (byte) ((a ^ b) & 0x01);
		data.scalarMap.put("c", c);
		return data;
	}
	
	// SenderPrepareAndSend(m0, m1):
	// 1) read x0, x1 from H
	// 2) read c from R
	// 3) compute z0 = x_c XOR m0, z1 = x_c XOR m1
	// 4) Send z0/z1 to R
	public Message SenderPrepare(byte[] m0, byte[] m1, byte[] x0, byte[] x1, byte c) throws CryptoException
	{
		if (m0.length != m1.length || m0.length != x0.length || m0.length != x1.length)
		{
			throw new CryptoException("SenderPrepare OT input lengths do not match: " + m0.length + "," + m1.length + "," + x0.length + "," + x1.length);
		}
		
		Message data = new Message();
		if (c == 0)
		{
			byte[] z0 = ByteUtils.xor(m0, x0);
			byte[] z1 = ByteUtils.xor(m1, x0);
			data.arrayMap.put("z0", z0);
			data.arrayMap.put("z1", z1);
		}
		else
		{
			byte[] z0 = ByteUtils.xor(m0, x1);
			byte[] z1 = ByteUtils.xor(m1, x1);
			data.arrayMap.put("z0", z0);
			data.arrayMap.put("z1", z1);
		} 
		
		return data;
	}
	
	// ReceiverChoose(b):
	// 1) read z0/z1 from S
	// 2) compute z = z_b XOR x_a
	public byte[] ReceiverChoose(byte[] z0, byte[] z1, byte[] xa, byte b) throws CryptoException
	{
		if (z0.length != z1.length || z0.length != xa.length)
		{
			throw new CryptoException("ReceiverChoose OT input lengths do not match: " + z0.length + "," + z1.length + "," + xa.length);
		}
		
		if (b == 0)
		{
			return ByteUtils.xor(z0, xa);
		}
		else
		{
			return ByteUtils.xor(z1, xa);
		}
	}
}
