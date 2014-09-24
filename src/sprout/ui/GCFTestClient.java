package sprout.ui;

import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;

import Program.ORAMTrialCommon;
import Program.ProgCommon;
import YaoGC.*;

public class GCFTestClient
{
	public static String serverIPname = "localhost";             // server IP name
    private final static int    serverPort   = 12346;                   // server port number
    private static Socket       sock         = null;                    // Socket object for communicating
	
	static SecureRandom rnd = new SecureRandom();
	
	public static void executeGCFClient() throws Exception {	    
		sock = new java.net.Socket(serverIPname, serverPort);          // create socket and connect
		ProgCommon.oos  = new java.io.ObjectOutputStream(sock.getOutputStream());  
		ProgCommon.ois  = new java.io.ObjectInputStream(sock.getInputStream());
		
		int[] msg = (int[]) ORAMTrialCommon.ois.readObject();
		Circuit.isForGarbling = false;
		Circuit gc_C = null;
		if (msg[0] == 0)
			gc_C = new F2ET_Wplus2_Wplus2(msg[1], msg[2], msg[3]);
		else
			gc_C = new F2FT_2Wplus2_Wplus2(msg[1], msg[2], msg[3]);
		Circuit.setIOStream(ProgCommon.ois, ProgCommon.oos);
		gc_C.build();
		
		BigInteger[] K_C = (BigInteger[]) ORAMTrialCommon.ois.readObject();
		
		State in_C = State.fromLabels(K_C);
		State out_C = gc_C.startExecuting(in_C);
		
		ORAMTrialCommon.oos.writeObject(out_C.toLabels());
		ORAMTrialCommon.oos.flush();
		
		// close everything
		ProgCommon.oos.close();                                                   
		ProgCommon.ois.close();
		sock.close();
	}
	
	public static void main(String[] args) throws Exception {
		executeGCFClient();
	}

}
