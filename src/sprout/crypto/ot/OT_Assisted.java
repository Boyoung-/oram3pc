package sprout.crypto.ot;

import sprout.communication.Message;
import sprout.crypto.CryptoException;
import sprout.crypto.SecureRandom;

import org.bouncycastle.pqc.math.linearalgebra.*;


public class OT_Assisted {
	
	// actually maybe both should be final
	private int numBytes = 4;
	private final int numOfMessages; // default it 2.
	
	// constructors, set messages and bytes
	public OT_Assisted()
	{
		numOfMessages = 2;
	}
	
	public OT_Assisted(int nMessages)
	{
		numOfMessages = nMessages;
	}
	
	public OT_Assisted(int nMessages, int nBytes)
	{
		numOfMessages = nMessages;
		numBytes = nBytes;
	}
	
	public void setNumBytes(int n)
	{
		numBytes = n;
		
		return;
	}
	
	//public void setNumOfMessages(int n)
	//{
	//	numOfMessages = n;
	//	
	//	return;
	//}
	
	
	// HelperInit()
	// 1) choose random bytes, xi<-n  for the number of messages, 
	//    choose random integer a<-[0,number of messages]
	// 2) send xi's to S, send a and  xa to R
	public Message HelperInit()
	{
		Message data = new Message();
		byte[] x;

		
		for(int j = 0; j < numOfMessages; j++)
		{
			x = SecureRandom.generateRandomBytes(numBytes);
			data.arrayMap.put("x"+j, x);	
		}	
		int a = SecureRandom.generateRandomInt(0, numOfMessages-1);
		data.intMap.put("a", a);
		
		return data;
	}
	
	// ReceiverXor(i):
	// 1) read a, xa from H
	// 2) compute c = a - i modulo numMessages
	// 3) send c to S
	public Message ReceiverCompC(int i, int a, byte[] xa)
	{
		Message data = new Message();
		int c = (a -i)%numOfMessages;
		data.intMap.put("c", c);
		return data;
	}
	

	private void senderPrepareTestMessagesAndBlidings(byte[][] m, byte[][] x) throws CryptoException
	{
		// blah blah test all... throw execption
		//throw new CryptoException("SenderPrepare OT input lengths do not match: ");
		
	}
	
	// SenderPrepareAndSend(mi's):
	// 1) read xi's from H
	// 2) read c from R
	// 3) compute zi = x_(c+i) XOR zi
	// 4) Send zi's to R
	public Message SenderPrepare(byte[][] m, byte[][] x, int c) throws CryptoException
	{
		try
		{
			senderPrepareTestMessagesAndBlidings(m, x);
		}
		catch(CryptoException E)
		{
			throw new CryptoException(E);
		}
		
		Message data = new Message();
		byte[] z;
		
		for(int j = 0; j < numOfMessages; j++)
		{
			z = ByteUtils.xor(m[j], x[(c+j)%numOfMessages]);
			data.arrayMap.put("z"+j, z);	
		}	
		
		return data;
	}
	
	
	private void receiverChooseTestZAndXA(byte[][] z, byte[] xa) throws CryptoException
	{
		// blah blah test all... throw execption
		//throw new CryptoException("SenderPrepare OT input lengths do not match: ");
		
	}
	
	// ReceiverChoose(i):
	// 1) read zi's from S
	// 2) compute z = z_i XOR x_a
	public byte[] ReceiverChoose(byte[][] z, byte[] xa, int i) throws CryptoException
	{
		try
		{
			receiverChooseTestZAndXA(z, xa);
		}
		catch(CryptoException E)
		{
			throw new CryptoException(E);
		}
		
		return ByteUtils.xor(z[i], xa);

	}
		
}
