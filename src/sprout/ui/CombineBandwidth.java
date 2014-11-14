package sprout.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import sprout.oram.PID;
import sprout.util.Bandwidth;

public class CombineBandwidth
{	
	private static Bandwidth[] readFromFile(String filename) throws IOException {
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
	
	private static Bandwidth[] add(Bandwidth[] a, Bandwidth[] b) {
		Bandwidth[] c = new Bandwidth[a.length];
		for (int i=0; i<a.length; i++)
			c[i] = a[i].add(b[i]);
		return c;
	}
	
	private static void print(Bandwidth[] a) {
		for (int i=0; i<a.length; i++)
			System.out.println(a[i]);
		System.out.println();
	}
	
	private static void printCSV(Bandwidth[] a) {
		System.out.println(a[PID.decrypt].bandwidth*8 + 
				"," + a[PID.pet].bandwidth*8 + 
				"," + a[PID.aot].bandwidth*8 +
				"," + a[PID.access].bandwidth*8 +
				"," + (a[PID.ppt].bandwidth+a[PID.reshuffle].bandwidth)*8 +
				"," + a[PID.gcf].bandwidth*8 +
				"," + a[PID.ssot].bandwidth*8 +
				"," + a[PID.encrypt].bandwidth*8 +
				"," + a[PID.eviction].bandwidth*8);
	}
	
	public static void main(String[] args) throws Exception
	{
		Bandwidth[] a = readFromFile("files/charlie-bandwidth-1");
		Bandwidth[] b = readFromFile("files/charlie-bandwidth-2");
		a = add(a, b);
		b = readFromFile("files/debbie-bandwidth-1");
		a = add(a, b);
		b = readFromFile("files/debbie-bandwidth-2");
		a = add(a, b);
		b = readFromFile("files/eddie-bandwidth-1");
		a = add(a, b);
		b = readFromFile("files/eddie-bandwidth-2");
		a = add(a, b);
		
		printCSV(a);
	}

}