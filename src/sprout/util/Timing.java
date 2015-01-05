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
	
	/*
	public StopWatch access;
	public StopWatch access_online;
	public StopWatch access_write;
	public StopWatch access_read;

	public StopWatch decrypt;
	public StopWatch decrypt_online;
	public StopWatch decrypt_write;
	public StopWatch decrypt_read;

	public StopWatch oprf;
	public StopWatch oprf_online;
	public StopWatch oprf_write;
	public StopWatch oprf_read;

	public StopWatch pet;
	public StopWatch pet_online;
	public StopWatch pet_write;
	public StopWatch pet_read;

	public StopWatch aot;
	public StopWatch aot_online;
	public StopWatch aot_write;
	public StopWatch aot_read;

	public StopWatch post;
	public StopWatch post_online;
	public StopWatch post_write;
	public StopWatch post_read;

	public StopWatch reshuffle;
	public StopWatch reshuffle_online;
	public StopWatch reshuffle_write;
	public StopWatch reshuffle_read;
	public StopWatch reshuffle_offline;
	public StopWatch reshuffle_offline_write;
	public StopWatch reshuffle_offline_read;

	public StopWatch eviction;
	public StopWatch eviction_online;
	public StopWatch eviction_write;
	public StopWatch eviction_read;

	public StopWatch gcf;
	public StopWatch gcf_online;
	public StopWatch gcf_write;
	public StopWatch gcf_read;
	public StopWatch gcf_offline;
	public StopWatch gcf_offline_write;
	public StopWatch gcf_offline_read;

	// public StopWatch gtt_write;
	// public StopWatch gtt_read;

	public StopWatch ssot;
	public StopWatch ssot_online;
	public StopWatch ssot_write;
	public StopWatch ssot_read;

	public StopWatch iot;
	public StopWatch iot_online;
	public StopWatch iot_write;
	public StopWatch iot_read;

	public StopWatch encrypt;
	public StopWatch encrypt_online;
	public StopWatch encrypt_write;
	public StopWatch encrypt_read;
	*/

	public Timing() {
	}

	public void init() {
		/*
		access = new StopWatch("access");
		access_online = new StopWatch("access_online");
		access_write = new StopWatch("access_write");
		access_read = new StopWatch("access_read");

		decrypt = new StopWatch("decrypt");
		decrypt_online = new StopWatch("decrypt_online");
		decrypt_write = new StopWatch("decrypt_write");
		decrypt_read = new StopWatch("decrypt_read");

		oprf = new StopWatch("oprf");
		oprf_online = new StopWatch("oprf_online");
		oprf_write = new StopWatch("oprf_write");
		oprf_read = new StopWatch("oprf_read");

		pet = new StopWatch("pet");
		pet_online = new StopWatch("pet_online");
		pet_write = new StopWatch("pet_write");
		pet_read = new StopWatch("pet_read");

		aot = new StopWatch("aot");
		aot_online = new StopWatch("aot_online");
		aot_write = new StopWatch("aot_write");
		aot_read = new StopWatch("aot_read");

		post = new StopWatch("post");
		post_online = new StopWatch("post_online");
		post_write = new StopWatch("post_write");
		post_read = new StopWatch("post_read");

		reshuffle = new StopWatch("reshuffle");
		reshuffle_online = new StopWatch("reshuffle_online");
		reshuffle_write = new StopWatch("reshuffle_write");
		reshuffle_read = new StopWatch("reshuffle_read");
		reshuffle_offline = new StopWatch("reshuffle_offline");
		reshuffle_offline_write = new StopWatch("reshuffle_offline_write");
		reshuffle_offline_read = new StopWatch("reshuffle_offline_read");

		eviction = new StopWatch("eviction");
		eviction_online = new StopWatch("eviction_online");
		eviction_write = new StopWatch("eviction_write");
		eviction_read = new StopWatch("eviction_read");

		gcf = new StopWatch("gcf");
		gcf_online = new StopWatch("gcf_online");
		gcf_write = new StopWatch("gcf_write");
		gcf_read = new StopWatch("gcf_read");
		gcf_offline = new StopWatch("gcf_offline");
		gcf_offline_write = new StopWatch("gcf_offline_write");
		gcf_offline_read = new StopWatch("gcf_offline_read");

		ssot = new StopWatch("ssot");
		ssot_online = new StopWatch("ssot_online");
		ssot_write = new StopWatch("ssot_write");
		ssot_read = new StopWatch("ssot_read");

		iot = new StopWatch("iot");
		iot_online = new StopWatch("iot_online");
		iot_write = new StopWatch("iot_write");
		iot_read = new StopWatch("iot_read");

		encrypt = new StopWatch("encrypt");
		encrypt_online = new StopWatch("encrypt_online");
		encrypt_write = new StopWatch("encrypt_write");
		encrypt_read = new StopWatch("encrypt_read");

		// special use
		// gtt_write = new StopWatch("gtt_write");
		// gtt_read = new StopWatch("gtt_read");
		 */

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
					
			/*
			oos.writeObject(access);
			oos.writeObject(access_online);
			oos.writeObject(access_write);
			oos.writeObject(access_read);

			oos.writeObject(decrypt);
			oos.writeObject(decrypt_online);
			oos.writeObject(decrypt_write);
			oos.writeObject(decrypt_read);

			oos.writeObject(oprf);
			oos.writeObject(oprf_online);
			oos.writeObject(oprf_write);
			oos.writeObject(oprf_read);

			oos.writeObject(pet);
			oos.writeObject(pet_online);
			oos.writeObject(pet_write);
			oos.writeObject(pet_read);

			oos.writeObject(aot);
			oos.writeObject(aot_online);
			oos.writeObject(aot_write);
			oos.writeObject(aot_read);

			oos.writeObject(post);
			oos.writeObject(post_online);
			oos.writeObject(post_write);
			oos.writeObject(post_read);

			oos.writeObject(reshuffle);
			oos.writeObject(reshuffle_online);
			oos.writeObject(reshuffle_write);
			oos.writeObject(reshuffle_read);
			oos.writeObject(reshuffle_offline);
			oos.writeObject(reshuffle_offline_write);
			oos.writeObject(reshuffle_offline_read);

			oos.writeObject(eviction);
			oos.writeObject(eviction_online);
			oos.writeObject(eviction_write);
			oos.writeObject(eviction_read);

			oos.writeObject(gcf);
			oos.writeObject(gcf_online);
			oos.writeObject(gcf_write);
			oos.writeObject(gcf_read);
			oos.writeObject(gcf_offline);
			oos.writeObject(gcf_offline_write);
			oos.writeObject(gcf_offline_read);

			oos.writeObject(ssot);
			oos.writeObject(ssot_online);
			oos.writeObject(ssot_write);
			oos.writeObject(ssot_read);

			oos.writeObject(iot);
			oos.writeObject(iot_online);
			oos.writeObject(iot_write);
			oos.writeObject(iot_read);

			oos.writeObject(encrypt);
			oos.writeObject(encrypt_online);
			oos.writeObject(encrypt_write);
			oos.writeObject(encrypt_read);
			*/
			
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

			/*
			access = (StopWatch) ois.readObject();
			access_online = (StopWatch) ois.readObject();
			access_write = (StopWatch) ois.readObject();
			access_read = (StopWatch) ois.readObject();

			decrypt = (StopWatch) ois.readObject();
			decrypt_online = (StopWatch) ois.readObject();
			decrypt_write = (StopWatch) ois.readObject();
			decrypt_read = (StopWatch) ois.readObject();

			oprf = (StopWatch) ois.readObject();
			oprf_online = (StopWatch) ois.readObject();
			oprf_write = (StopWatch) ois.readObject();
			oprf_read = (StopWatch) ois.readObject();

			pet = (StopWatch) ois.readObject();
			pet_online = (StopWatch) ois.readObject();
			pet_write = (StopWatch) ois.readObject();
			pet_read = (StopWatch) ois.readObject();

			aot = (StopWatch) ois.readObject();
			aot_online = (StopWatch) ois.readObject();
			aot_write = (StopWatch) ois.readObject();
			aot_read = (StopWatch) ois.readObject();

			post = (StopWatch) ois.readObject();
			post_online = (StopWatch) ois.readObject();
			post_write = (StopWatch) ois.readObject();
			post_read = (StopWatch) ois.readObject();

			reshuffle = (StopWatch) ois.readObject();
			reshuffle_online = (StopWatch) ois.readObject();
			reshuffle_write = (StopWatch) ois.readObject();
			reshuffle_read = (StopWatch) ois.readObject();
			reshuffle_offline = (StopWatch) ois.readObject();
			reshuffle_offline_write = (StopWatch) ois.readObject();
			reshuffle_offline_read = (StopWatch) ois.readObject();

			eviction = (StopWatch) ois.readObject();
			eviction_online = (StopWatch) ois.readObject();
			eviction_write = (StopWatch) ois.readObject();
			eviction_read = (StopWatch) ois.readObject();

			gcf = (StopWatch) ois.readObject();
			gcf_online = (StopWatch) ois.readObject();
			gcf_write = (StopWatch) ois.readObject();
			gcf_read = (StopWatch) ois.readObject();
			gcf_offline = (StopWatch) ois.readObject();
			gcf_offline_write = (StopWatch) ois.readObject();
			gcf_offline_read = (StopWatch) ois.readObject();

			ssot = (StopWatch) ois.readObject();
			ssot_online = (StopWatch) ois.readObject();
			ssot_write = (StopWatch) ois.readObject();
			ssot_read = (StopWatch) ois.readObject();

			iot = (StopWatch) ois.readObject();
			iot_online = (StopWatch) ois.readObject();
			iot_write = (StopWatch) ois.readObject();
			iot_read = (StopWatch) ois.readObject();

			encrypt = (StopWatch) ois.readObject();
			encrypt_online = (StopWatch) ois.readObject();
			encrypt_write = (StopWatch) ois.readObject();
			encrypt_read = (StopWatch) ois.readObject();
			*/

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

		/*
		out.access = access.add(t.access);
		out.access_online = access_online.add(t.access_online);
		out.access_write = access_write.add(t.access_write);
		out.access_read = access_read.add(t.access_read);

		out.decrypt = decrypt.add(t.decrypt);
		out.decrypt_online = decrypt_online.add(t.decrypt_online);
		out.decrypt_write = decrypt_write.add(t.decrypt_write);
		out.decrypt_read = decrypt_read.add(t.decrypt_read);

		out.oprf = oprf.add(t.oprf);
		out.oprf_online = oprf_online.add(t.oprf_online);
		out.oprf_write = oprf_write.add(t.oprf_write);
		out.oprf_read = oprf_read.add(t.oprf_read);

		out.pet = pet.add(t.pet);
		out.pet_online = pet_online.add(t.pet_online);
		out.pet_write = pet_write.add(t.pet_write);
		out.pet_read = pet_read.add(t.pet_read);

		out.aot = aot.add(t.aot);
		out.aot_online = aot_online.add(t.aot_online);
		out.aot_write = aot_write.add(t.aot_write);
		out.aot_read = aot_read.add(t.aot_read);

		out.post = post.add(t.post);
		out.post_online = post_online.add(t.post_online);
		out.post_write = post_write.add(t.post_write);
		out.post_read = post_read.add(t.post_read);

		out.reshuffle = reshuffle.add(t.reshuffle);
		out.reshuffle_online = reshuffle_online.add(t.reshuffle_online);
		out.reshuffle_write = reshuffle_write.add(t.reshuffle_write);
		out.reshuffle_read = reshuffle_read.add(t.reshuffle_read);
		out.reshuffle_offline = reshuffle_offline.add(t.reshuffle_offline);
		out.reshuffle_offline_write = reshuffle_offline_write
				.add(t.reshuffle_offline_write);
		out.reshuffle_offline_read = reshuffle_offline_read
				.add(t.reshuffle_offline_read);

		out.eviction = eviction.add(t.eviction);
		out.eviction_online = eviction_online.add(t.eviction_online);
		out.eviction_write = eviction_write.add(t.eviction_write);
		out.eviction_read = eviction_read.add(t.eviction_read);

		out.gcf = gcf.add(t.gcf);
		out.gcf_online = gcf_online.add(t.gcf_online);
		out.gcf_write = gcf_write.add(t.gcf_write);
		out.gcf_read = gcf_read.add(t.gcf_read);
		out.gcf_offline = gcf_offline.add(t.gcf_offline);
		out.gcf_offline_write = gcf_offline_write.add(t.gcf_offline_write);
		out.gcf_offline_read = gcf_offline_read.add(t.gcf_offline_read);

		out.ssot = ssot.add(t.ssot);
		out.ssot_online = ssot_online.add(t.ssot_online);
		out.ssot_write = ssot_write.add(t.ssot_write);
		out.ssot_read = ssot_read.add(t.ssot_read);

		out.iot = iot.add(t.iot);
		out.iot_online = iot_online.add(t.iot_online);
		out.iot_write = iot_write.add(t.iot_write);
		out.iot_read = iot_read.add(t.iot_read);

		out.encrypt = encrypt.add(t.encrypt);
		out.encrypt_online = encrypt_online.add(t.encrypt_online);
		out.encrypt_write = encrypt_write.add(t.encrypt_write);
		out.encrypt_read = encrypt_read.add(t.encrypt_read);
		*/

		return out;
	}

	public void divide(int n) {
		for (int i=0; i<PID.size; i++)
			for (int j=0; j<TID.size; j++)
				stopwatch[i][j].divide(n);
		
		/*
		access.divide(n);
		access_online.divide(n);
		access_write.divide(n);
		access_read.divide(n);

		decrypt.divide(n);
		decrypt_online.divide(n);
		decrypt_write.divide(n);
		decrypt_read.divide(n);

		oprf.divide(n);
		oprf_online.divide(n);
		oprf_write.divide(n);
		oprf_read.divide(n);

		pet.divide(n);
		pet_online.divide(n);
		pet_write.divide(n);
		pet_read.divide(n);

		aot.divide(n);
		aot_online.divide(n);
		aot_write.divide(n);
		aot_read.divide(n);

		post.divide(n);
		post_online.divide(n);
		post_write.divide(n);
		post_read.divide(n);

		reshuffle.divide(n);
		reshuffle_online.divide(n);
		reshuffle_write.divide(n);
		reshuffle_read.divide(n);
		reshuffle_offline.divide(n);
		reshuffle_offline_write.divide(n);
		reshuffle_offline_read.divide(n);

		eviction.divide(n);
		eviction_online.divide(n);
		eviction_write.divide(n);
		eviction_read.divide(n);

		gcf.divide(n);
		gcf_online.divide(n);
		gcf_write.divide(n);
		gcf_read.divide(n);
		gcf_offline.divide(n);
		gcf_offline_write.divide(n);
		gcf_offline_read.divide(n);

		ssot.divide(n);
		ssot_online.divide(n);
		ssot_write.divide(n);
		ssot_read.divide(n);

		iot.divide(n);
		iot_online.divide(n);
		iot_write.divide(n);
		iot_read.divide(n);

		encrypt.divide(n);
		encrypt_online.divide(n);
		encrypt_write.divide(n);
		encrypt_read.divide(n);
		*/
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
		/*
		String csv = access.toCSV() + "\n" + access_online.toCSV() + "\n"
				+ access_write.toCSV() + "\n" + access_read.toCSV() + "\n\n"
				+ decrypt.toCSV() + "\n" + decrypt_online.toCSV() + "\n"
				+ decrypt_write.toCSV() + "\n" + decrypt_read.toCSV() + "\n\n"
				+ oprf.toCSV() + "\n" + oprf_online.toCSV() + "\n"
				+ oprf_write.toCSV() + "\n" + oprf_read.toCSV() + "\n\n"
				+ pet.toCSV() + "\n" + pet_online.toCSV() + "\n"
				+ pet_write.toCSV() + "\n" + pet_read.toCSV() + "\n\n"
				+ aot.toCSV() + "\n" + aot_online.toCSV() + "\n"
				+ aot_write.toCSV() + "\n" + aot_read.toCSV() + "\n\n"
				+ post.toCSV() + "\n" + post_online.toCSV() + "\n"
				+ post_write.toCSV() + "\n" + post_read.toCSV() + "\n\n"
				+ reshuffle.toCSV() + "\n" + reshuffle_online.toCSV() + "\n"
				+ reshuffle_write.toCSV() + "\n" + reshuffle_read.toCSV()
				+ "\n" + reshuffle_offline.toCSV() + "\n"
				+ reshuffle_offline_write.toCSV() + "\n"
				+ reshuffle_offline_read.toCSV() + "\n\n" + eviction.toCSV()
				+ "\n" + eviction_online.toCSV() + "\n"
				+ eviction_write.toCSV() + "\n" + eviction_read.toCSV()
				+ "\n\n" + gcf.toCSV() + "\n" + gcf_online.toCSV() + "\n"
				+ gcf_write.toCSV() + "\n" + gcf_read.toCSV() + "\n"
				+ gcf_offline.toCSV() + "\n" + gcf_offline_write.toCSV() + "\n"
				+ gcf_offline_read.toCSV() + "\n\n" + ssot.toCSV() + "\n"
				+ ssot_online.toCSV() + "\n" + ssot_write.toCSV() + "\n"
				+ ssot_read.toCSV() + "\n\n" + iot.toCSV() + "\n"
				+ iot_online.toCSV() + "\n" + iot_write.toCSV() + "\n"
				+ iot_read.toCSV() + "\n\n" + encrypt.toCSV() + "\n"
				+ encrypt_online.toCSV() + "\n" + encrypt_write.toCSV() + "\n"
				+ encrypt_read.toCSV();
				*/
		String csv = "";
		for (int i=0; i<PID.size; i++) {
			for (int j=0; j<TID.size; j++)
				csv += stopwatch[i][j].toCSV() + "\n";
			csv += "\n";
		}
		return "\n" + csv;
	}
}
