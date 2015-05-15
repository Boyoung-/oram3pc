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
		reset();
	}

	public Timing(Timing t) {
		stopwatch = new StopWatch[PID.size][TID.size];
		for (int i = 0; i < PID.size; i++)
			for (int j = 0; j < TID.size; j++)
				stopwatch[i][j] = new StopWatch(t.stopwatch[i][j]);
	}

	public void reset() {
		stopwatch = new StopWatch[PID.size][TID.size];
		for (int i = 0; i < PID.size; i++)
			for (int j = 0; j < TID.size; j++)
				stopwatch[i][j] = new StopWatch(PID.names[i] + "_"
						+ TID.names[j]);
	}

	public void writeToFile(String filename) throws IOException {
		FileOutputStream fout = new FileOutputStream(filename);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(fout);
			for (int i = 0; i < PID.size; i++)
				for (int j = 0; j < TID.size; j++)
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
			for (int i = 0; i < PID.size; i++)
				for (int j = 0; j < TID.size; j++)
					stopwatch[i][j] = (StopWatch) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException("File contains invalid structure.", e);
		} finally {
			if (ois != null)
				ois.close();
		}
	}

	// TODO: change return types to be the same
	public Timing add(Timing t) {
		Timing out = new Timing();
		// out.init();
		for (int i = 0; i < PID.size; i++)
			for (int j = 0; j < TID.size; j++)
				out.stopwatch[i][j] = stopwatch[i][j].add(t.stopwatch[i][j]);
		return out;
	}

	public void divide(int n) {
		for (int i = 0; i < PID.size; i++)
			for (int j = 0; j < TID.size; j++)
				stopwatch[i][j].divide(n);
	}

	public StopWatch groupOffline() {
		StopWatch sw = new StopWatch();
		for (int i = 0; i < PID.size; i++)
			sw = sw.add(stopwatch[i][3]);
		return sw;
	}

	public StopWatch groupOffline_write() {
		StopWatch sw = new StopWatch();
		for (int i = 0; i < PID.size; i++)
			sw = sw.add(stopwatch[i][4]);
		return sw;
	}

	public StopWatch groupOffline_read() {
		StopWatch sw = new StopWatch();
		for (int i = 0; i < PID.size; i++)
			sw = sw.add(stopwatch[i][5]);
		return sw;
	}

	public StopWatch groupAccess() {
		StopWatch sw = new StopWatch();
		sw = sw.add(stopwatch[0][0]);
		sw = sw.add(stopwatch[1][0]);
		sw = sw.add(stopwatch[4][0]);
		sw = sw.add(stopwatch[7][0]);
		sw = sw.add(stopwatch[9][0]);
		return sw;
	}

	public StopWatch groupAccess_write() {
		StopWatch sw = new StopWatch();
		sw = sw.add(stopwatch[0][1]);
		sw = sw.add(stopwatch[1][1]);
		sw = sw.add(stopwatch[4][1]);
		sw = sw.add(stopwatch[7][1]);
		sw = sw.add(stopwatch[9][1]);
		return sw;
	}

	public StopWatch groupAccess_read() {
		StopWatch sw = new StopWatch();
		sw = sw.add(stopwatch[0][2]);
		sw = sw.add(stopwatch[1][2]);
		sw = sw.add(stopwatch[4][2]);
		sw = sw.add(stopwatch[7][2]);
		sw = sw.add(stopwatch[9][2]);
		return sw;
	}

	public StopWatch groupPE() {
		StopWatch sw = new StopWatch();
		sw = sw.add(stopwatch[2][0]);
		sw = sw.add(stopwatch[3][0]);
		sw = sw.add(stopwatch[5][0]);
		sw = sw.add(stopwatch[6][0]);
		sw = sw.add(stopwatch[8][0]);
		sw = sw.add(stopwatch[10][0]);
		sw = sw.add(stopwatch[11][0]);
		return sw;
	}

	public StopWatch groupPE_write() {
		StopWatch sw = new StopWatch();
		sw = sw.add(stopwatch[2][1]);
		sw = sw.add(stopwatch[3][1]);
		sw = sw.add(stopwatch[5][1]);
		sw = sw.add(stopwatch[6][1]);
		sw = sw.add(stopwatch[8][1]);
		sw = sw.add(stopwatch[10][1]);
		sw = sw.add(stopwatch[11][1]);
		return sw;
	}

	public StopWatch groupPE_read() {
		StopWatch sw = new StopWatch();
		sw = sw.add(stopwatch[2][2]);
		sw = sw.add(stopwatch[3][2]);
		sw = sw.add(stopwatch[5][2]);
		sw = sw.add(stopwatch[6][2]);
		sw = sw.add(stopwatch[8][2]);
		sw = sw.add(stopwatch[10][2]);
		sw = sw.add(stopwatch[11][2]);
		return sw;
	}

	/*
	 * @Override public String toString() { String out = access + "\n" +
	 * access_online + "\n" + access_write + "\n" + access_read + "\n\n" +
	 * decrypt + "\n" + decrypt_online + "\n" + decrypt_write + "\n" +
	 * decrypt_read + "\n\n" + oprf + "\n" + oprf_online + "\n" + oprf_write +
	 * "\n" + oprf_read + "\n\n" + pet + "\n" + pet_online + "\n" + pet_write +
	 * "\n" + pet_read + "\n\n" + aot + "\n" + aot_online + "\n" + aot_write +
	 * "\n" + aot_read + "\n\n" + post + "\n" + post_online + "\n" + post_write
	 * + "\n" + post_read + "\n\n" + reshuffle + "\n" + reshuffle_online + "\n"
	 * + reshuffle_write + "\n" + reshuffle_read + "\n" + reshuffle_offline +
	 * "\n" + reshuffle_offline_write + "\n" + reshuffle_offline_read + "\n\n" +
	 * eviction + "\n" + eviction_online + "\n" + eviction_write + "\n" +
	 * eviction_read + "\n\n" + gcf + "\n" + gcf_online + "\n" + gcf_write +
	 * "\n" + gcf_read + "\n" + gcf_offline + "\n" + gcf_offline_write + "\n" +
	 * gcf_offline_read + "\n\n" + ssot + "\n" + ssot_online + "\n" + ssot_write
	 * + "\n" + ssot_read + "\n\n" + iot + "\n" + iot_online + "\n" + iot_write
	 * + "\n" + iot_read + "\n\n" + encrypt + "\n" + encrypt_online + "\n" +
	 * encrypt_write + "\n" + encrypt_read; return out; }
	 */

	public String afterConversion() {
		String csv = "";
		for (int i = 0; i < PID.size; i++) {
			for (int j = 0; j < TID.size; j++)
				csv += stopwatch[i][j].toMS() + "\n";
			csv += "\n";
		}
		return "\n" + csv;
	}

	public String toTab() {
		String out = "";
		for (int i = 0; i < PID.size; i++) {
			for (int j = 0; j < TID.size; j++)
				out += stopwatch[i][j].toTab() + "\n";
		}
		return out;
	}

	public String toCSV() {
		String csv = "";
		for (int i = 0; i < PID.size; i++) {
			for (int j = 0; j < TID.size; j++)
				csv += stopwatch[i][j].toCSV() + "\n";
			csv += "\n";
		}
		return "\n" + csv;
	}
}
