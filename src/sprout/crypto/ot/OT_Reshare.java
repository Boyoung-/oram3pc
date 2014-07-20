package sprout.crypto.ot;

import sprout.communication.Message;
import sprout.crypto.CryptoException;
import sprout.crypto.SecureRandom;

import org.bouncycastle.pqc.math.linearalgebra.*;



public class OT_Reshare {
	
	// maybe it should be final
	private int numBytes = 4;
			
	// constructor, set bytes
			
			
	public OT_Reshare(int nBytes)
	{
		numBytes = nBytes;
	}
			
	public void setNumBytes(int n)
	{
		numBytes = n;
				
		return;
	}
	
	private void BlindAUTestMessages(byte[] a, byte[] u) throws CryptoException
	{
		// blah blah test all... throw execption
		//throw new CryptoException("SenderPrepare OT input lengths do not match: ");
	}
	
	// Init(a, u):
	// 1) sample blinding delta, compute new blinded messages

	public Message BlindAU(byte[] a, byte[] u) throws CryptoException
	{
		try
		{
			BlindAUTestMessages(a, u);
		}
		catch(CryptoException E)
		{
			throw new CryptoException(E);
		}
		
		Message data = new Message();
		byte[] delta;
		byte[] aBlinded;
		byte[] uBlinded;
		
		delta = SecureRandom.generateRandomBytes(numBytes);
		data.arrayMap.put("delta", delta);

		aBlinded = ByteUtils.xor(a, delta);
		data.arrayMap.put("aBlinded", aBlinded);	
		uBlinded = ByteUtils.xor(u, delta);
		data.arrayMap.put("uBlinded", uBlinded);	
		
		return data;
	}
	
	private void reshareMTestMessages(byte[] m, byte[] delta) throws CryptoException
	{
		// blah blah test all... throw execption
		//throw new CryptoException("SenderPrepare OT input lengths do not match: ");
	}
	
	
	// Apply blinding factor delta on m, to get a valid resharing.
	
	public byte[] reshareM(byte[] m, byte[] delta) throws CryptoException
	{
		try
		{
			reshareMTestMessages(m, delta);
		}
		catch(CryptoException E)
		{
			throw new CryptoException(E);
		}
		
		
		return ByteUtils.xor(m, delta);
	}

}
