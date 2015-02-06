package sprout.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import sprout.oram.PID;
import sprout.oram.TID;

public class Timing {
	public StopWatch[][] stopwatch = null;

	public Timing() {
	}

	public void init() {
		stopwatch = new StopWatch[PID.size][TID.size];
		for (int i=0; i<PID.size; i++)
			for (int j=0; j<TID.size; j++)
				stopwatch[i][j] = new StopWatch(PID.names[i] + "_" + TID.names[j]);
	}

	public void writeToFile(String filename) throws IOException {
		FileOutputStream fout = new FileOutputStream(filename);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(fout);
			for (int i=0; i<PID.size; i++)
				for (int j=0; j<TID.size; j++)
					oos.writeObject(stopwatch[i][j]);
		} finally {
			if (oos != null)
				oos.close();
		}
	}

	public void readFromFile(String filename) throws IOException {
		if (stopwatch == null)
			stopwatch = new StopWatch[PID.size][TID.size];
		
		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(fin);
			for (int i=0; i<PID.size; i++)
				for (int j=0; j<TID.size; j++)
					stopwatch[i][j] = (StopWatch) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException("File contains invalid structure.", e);
		} finally {
			if (ois != null)
				ois.close();
		}
	}

	public Timing add(Timing t) {
		Timing out = new Timing();
		for (int i=0; i<PID.size; i++)
			for (int j=0; j<TID.size; j++)
				out.stopwatch[i][j] = stopwatch[i][j].add(t.stopwatch[i][j]);
		return out;
	}

	public void divide(int n) {
		for (int i=0; i<PID.size; i++)
			for (int j=0; j<TID.size; j++)
				stopwatch[i][j].divide(n);
	}

	/*
	@Override
	public String toString() {
		String out = access + "\n" + access_online + "\n" + access_write + "\n"
				+ access_read + "\n\n" + decrypt + "\n" + decrypt_online + "\n"
				+ decrypt_write + "\n" + decrypt_read + "\n\n" + oprf + "\n"
				+ oprf_online + "\n" + oprf_write + "\n" + oprf_read + "\n\n"
				+ pet + "\n" + pet_online + "\n" + pet_write + "\n" + pet_read
				+ "\n\n" + aot + "\n" + aot_online + "\n" + aot_write + "\n"
				+ aot_read + "\n\n" + post + "\n" + post_online + "\n"
				+ post_write + "\n" + post_read + "\n\n" + reshuffle + "\n"
				+ reshuffle_online + "\n" + reshuffle_write + "\n"
				+ reshuffle_read + "\n" + reshuffle_offline + "\n"
				+ reshuffle_offline_write + "\n" + reshuffle_offline_read
				+ "\n\n" + eviction + "\n" + eviction_online + "\n"
				+ eviction_write + "\n" + eviction_read + "\n\n" + gcf + "\n"
				+ gcf_online + "\n" + gcf_write + "\n" + gcf_read + "\n"
				+ gcf_offline + "\n" + gcf_offline_write + "\n"
				+ gcf_offline_read + "\n\n" + ssot + "\n" + ssot_online + "\n"
				+ ssot_write + "\n" + ssot_read + "\n\n" + iot + "\n"
				+ iot_online + "\n" + iot_write + "\n" + iot_read + "\n\n"
				+ encrypt + "\n" + encrypt_online + "\n" + encrypt_write + "\n"
				+ encrypt_read;
		return out;
	}
	*/

	public String toCSV() {
		String csv = "";
		for (int i=0; i<PID.size; i++) {
			for (int j=0; j<TID.size; j++)
				csv += stopwatch[i][j].toCSV() + "\n";
			csv += "\n";
		}
		return "\n" + csv;
	}
}
