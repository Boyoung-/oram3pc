package sprout.crypto.ot;

import sprout.communication.Message;
import sprout.crypto.CryptoException;
import sprout.crypto.SecureRandom;

import org.bouncycastle.pqc.math.linearalgebra.*;

public class OT_3P {
		// maybe it should be final
		private int numBytes = 4;
		
		// constructor, set bytes
		
		
		public OT_3P(int nBytes)
		{
			numBytes = nBytes;
		}
		
		public void setNumBytes(int n)
		{
			numBytes = n;
			
			return;
		}
		
		
		// DecideInit(bit b)
		// 1) choose random bytes, x0, x1  for the number of messages, 
		//    choose random bit sigma <-[0,1]
		// 2) send x0,x1,sigma to S, 
		// 3) send delta = sigma XOR b x_a to R
		public Message DecideInit(int b) throws CryptoException 
		{
			if (b != 0 || b != 1)
			{
				throw new CryptoException("HelperInit OT_3P input b should be 0 or 1, it is "+b+" instead.");
			}
			
			Message data = new Message();
			byte[] x0;
			byte[] x1;

			// inputs for receiver
			//for(int j = 0; j < 2; j++)
			//{
			//	x[j] = SecureRandom.generateRandomBytes(numBytes);
			//	data.arrayMap.put("x"+j, x[j]);	
			//}
			x0 = SecureRandom.generateRandomBytes(numBytes);
			data.arrayMap.put("x0", x0);
			x1 = SecureRandom.generateRandomBytes(numBytes);
			data.arrayMap.put("x1", x1);
			int s = SecureRandom.generateRandomInt(0, 1);
			data.intMap.put("sigma", s);
			
			//inputs for
			data.intMap.put("delta", (s+b)%2);
			// data.arrayMap.put("x_a", x[(s+b)%2]);
			if ( 0 == (s+b)%2 )
			{
				data.arrayMap.put("x_a", x0);
			}
			else
			{
				data.arrayMap.put("x_a", x1);
			}
			
			return data;
		}
		

		private void senderPrepareTestMessagesAndBlidings(byte[][] m, byte[][] x) throws CryptoException
		{
			// blah blah test all... throw execption
			//throw new CryptoException("SenderPrepare OT input lengths do not match: ");
		}
		
		// SenderPrepare(mi's):
		// 1) read xi's and s from D
		// 2) compute z0 = x_(s) XOR m0, z1 = x_(!s) XOR m1 
		// 4) Send zi's to R
		public Message SenderPrepare(byte[][] m, byte[][] x, int s) throws CryptoException
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
			
			for(int j = 0; j < 2; j++)
			{
				z = ByteUtils.xor(m[j], x[(s+j)%2]);
				data.arrayMap.put("z"+j, z);	
			}	
			
			return data;
		}
		
		
		private void receiverChooseTestZAndXA(byte[][] z, byte[] xa) throws CryptoException
		{
			// blah blah test all... throw execption
			//throw new CryptoException("SenderPrepare OT input lengths do not match: ");
			
		}
		
		// ReceiverChoose(delta):
		// 1) read zi's from S
		// 2) compute z = z_delta XOR x_a
		public byte[] ReceiverChoose(byte[][] z, byte[] xa, int delta) throws CryptoException
		{
			try
			{
				receiverChooseTestZAndXA(z, xa);
			}
			catch(CryptoException E)
			{
				throw new CryptoException(E);
			}
			
			return ByteUtils.xor(z[delta], xa);

		}

}
