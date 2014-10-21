package sprout.ui;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;

import sprout.util.Util;
import Program.ORAMTrialCommon;
import Program.ProgCommon;
import YaoGC.*;

public class GCFTestServer
{
	public final static  int         serverPort   = 12345;             // server port number
    static ServerSocket       sock         = null;              // original server socket
    static Socket             clientSocket = null;              // socket created by accept
	
	static SecureRandom rnd = new SecureRandom();
	
	public static String executeGCF(String sC, String sE, String circuit) throws Exception {	    
	    sock = new ServerSocket(serverPort);            // create socket and bind to port
		System.out.println("waiting for client to connect");
		clientSocket = sock.accept();                   // wait for client to connect
		System.out.println("client has connected");

		CountingOutputStream cos = new CountingOutputStream(clientSocket.getOutputStream());
		CountingInputStream  cis = new CountingInputStream(clientSocket.getInputStream());
		
		ProgCommon.oos = new ObjectOutputStream(cos);
		ProgCommon.ois = new ObjectInputStream(cis);
		
		//int labelBitLength = 80;
		int n = sC.length();
		String input = Util.addZero(new BigInteger(sC, 2).xor(new BigInteger(sE, 2)).toString(2), n);
		
		// pre-computed
		int w = n - 2;
		if (circuit.equals("F2FT"))
			w /= 2;
		int tmp1 = rnd.nextInt(w) + 1;
		int tmp2 = rnd.nextInt(w) + 1;
		int s1 = Math.min(tmp1, tmp2);
		int s2 = Math.max(tmp1, tmp2);
		System.out.println("w:\t" + w);
		System.out.println("sigma:\t" + s1 + " " + s2);
		int[] msg = new int[]{circuit.equals("F2ET")?0:1, w};
		ORAMTrialCommon.oos.writeObject(msg);
		
		Circuit gc_S = null;
		Circuit.isForGarbling = true;
		if (circuit.equals("F2ET"))
			gc_S = new F2ET_Wplus2_Wplus2(w, s1, s2);
		else
			gc_S = new F2FT_2Wplus2_Wplus2(w, s1, s2);
		Circuit.setIOStream(ProgCommon.ois, ProgCommon.oos);
		gc_S.build();
		
		BigInteger[][] lbs = new BigInteger[n][2];
		for (int i = 0; i < n; i++) {
		    BigInteger glb0 = new BigInteger(Wire.labelBitLength, rnd);
		    BigInteger glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
		    lbs[i][0] = glb0;
		    lbs[i][1] = glb1;
		}
		
		// protocol
		// step 1
		BigInteger[][] A = new BigInteger[n][2];
		for (int i=0; i<n; i++) {
			int alpha = Character.getNumericValue(sE.charAt(i));
			A[i][0] = lbs[i][alpha];
			A[i][1] = lbs[i][1-alpha];
		}
		
		// step 2
		BigInteger[] K_S = new BigInteger[n];
		BigInteger[] K_C = new BigInteger[n];
		for (int i=0; i<n; i++) {
			int beta = Character.getNumericValue(sC.charAt(i));
			K_C[i] = A[i][beta];
			K_S[i] = lbs[i][0];
		}
		
		ORAMTrialCommon.oos.writeObject(K_C);
		ORAMTrialCommon.oos.flush();
		
		// step 3
		State in_S = State.fromLabels(K_S);
		State out_S = gc_S.startExecuting(in_S);		
		BigInteger[] outLabels = (BigInteger[]) ORAMTrialCommon.ois.readObject();
		State outputState = State.fromLabels(out_S.toLabels());
		
		// close everything
		ProgCommon.oos.close();                          
		ProgCommon.ois.close();
		clientSocket.close();
		sock.close();
		
		// interpret results		
		System.out.println("input:\t" + input);
		
		//String output = gc_S.interpretOutputELabels(outLbs).toString(2);
		BigInteger output = BigInteger.ZERO;
		for (int i = 0; i < outLabels.length; i++) {
		    if (outputState.wires[i].value != Wire.UNKNOWN_SIG) {
			if (outputState.wires[i].value == 1)
			    output = output.setBit(i);
			continue;
		    }
		    else if (outLabels[i].equals(outputState.wires[i].invd ? 
						 outputState.wires[i].lbl :
						 outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)))) {
			    output = output.setBit(i);
		    }
		    else if (!outLabels[i].equals(outputState.wires[i].invd ? 
						  outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)) :
						  outputState.wires[i].lbl)) 
			throw new Exception("Bad label encountered: i = " + i + "\t" +
					    outLabels[i] + " != (" + 
					    outputState.wires[i].lbl + ", " +
					    outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)) + ")");
		}
		
		String out = output.toString(2);
		if (circuit.equals("F2ET"))
			out = Util.addZero(out, n);
		else
			out = Util.addZero(out, w+2);
		System.out.println("output:\t" + out);
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		int n = 18;
		String circuit = "F2ET";
		String sC = "00" + Util.addZero(new BigInteger(n-2, rnd).toString(2), n-2);
		//String circuit = "F2FT";
		//String sC = "0011111111" + Util.addZero(new BigInteger(n-10, rnd).toString(2), n-10);
		String sE = Util.addZero("", n);
		executeGCF(sC, sE, circuit);
	}

}
