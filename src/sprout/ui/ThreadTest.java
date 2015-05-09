package sprout.ui;

import java.io.IOException;
import java.io.RandomAccessFile;

class RAFThread extends Thread {
	
	byte[] content;

	public RAFThread(byte[] content) {
		this.content = content;
	}

	public void run() {
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile("c:/test/test.txt", "rw");
	        raf.seek(0);
	        raf.write(content, 0, content.length);
	        raf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

public class ThreadTest extends Thread {
	public static void main(String args[]) {
		byte[] in1 = new byte[50000000];
		for (int i=0; i<in1.length; i++)
			in1[i] = 66;
		byte[] in2 = new byte[in1.length];
		RAFThread raft = new RAFThread(in1);
		RAFThread raft2 = new RAFThread(in2);
		System.out.println("start");
		raft.run();
		raft2.run();
		try {
			raft.join();
			raft2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done");
	}
}
