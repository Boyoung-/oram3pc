package sprout.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import sprout.oram.PID;
import sprout.util.Bandwidth;

public class CombineBandwidth
{	
	private Bandwidth[] readFromFile(String filename) throws IOException {
		Bandwidth[] out = new Bandwidth[PID.size];
		
		FileInputStream fin = new FileInputStream(filename);
	    ObjectInputStream ois = null;
	    try {
	      ois = new ObjectInputStream(fin);
	      for (int i=0; i<PID.size; i++)
	    	  out[i] = (Bandwidth) ois.readObject();
	      
	    } catch (ClassNotFoundException e1) {
	      throw new IOException("File contains invalid structure.", e1);
	    } finally {
	      if (ois != null)
	        ois.close();
	    }
	    
	    return out;
	}
	
	public static void main(String[] args) throws Exception
	{
		Bandwidth[] c = new Bandwidth[PID.size];
		Bandwidth[] d = new Bandwidth[PID.size];
		Bandwidth[] e = new Bandwidth[PID.size];
		
		
	}

}
