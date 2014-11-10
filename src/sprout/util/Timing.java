package sprout.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Timing
{	
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
	
	public StopWatch eviction;
	public StopWatch eviction_online;
	public StopWatch eviction_write;
	public StopWatch eviction_read;
	
	public StopWatch gcf;
	public StopWatch gcf_online;
	public StopWatch gcf_write;
	public StopWatch gcf_read;
	public StopWatch gtt_write;
	public StopWatch gtt_read;
	
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
	
	public Timing() {}
	
	public void init() {
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
		
		eviction = new StopWatch("eviction");
		eviction_online = new StopWatch("eviction_online");
		eviction_write = new StopWatch("eviction_write");
		eviction_read = new StopWatch("eviction_read");
		
		gcf = new StopWatch("gcf");
		gcf_online = new StopWatch("gcf_online");
		gcf_write = new StopWatch("gcf_write");
		gcf_read = new StopWatch("gcf_read");
		gtt_write = new StopWatch("gtt_write");
		gtt_read = new StopWatch("gtt_read");
		
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
	}
	
	public void writeToFile(String filename) throws IOException {
		FileOutputStream fout = new FileOutputStream(filename);
	    ObjectOutputStream oos = null;
	    try {
	      oos = new ObjectOutputStream(fout);
	      
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
	      
	      oos.writeObject(eviction);
	      oos.writeObject(eviction_online);
	      oos.writeObject(eviction_write);
	      oos.writeObject(eviction_read);
	      
	      oos.writeObject(gcf);
	      oos.writeObject(gcf_online);
	      oos.writeObject(gcf_write);
	      oos.writeObject(gcf_read);
	      
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
	      
	    } finally {
	      if (oos != null)
	        oos.close();
	    }
	}
	
	public void readFromFile(String filename) throws IOException {
		FileInputStream fin = new FileInputStream(filename);
	    ObjectInputStream ois = null;
	    try {
	      ois = new ObjectInputStream(fin);
	      
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
			
			eviction = (StopWatch) ois.readObject();
			eviction_online = (StopWatch) ois.readObject();
			eviction_write = (StopWatch) ois.readObject();
			eviction_read = (StopWatch) ois.readObject();
			
			gcf = (StopWatch) ois.readObject();
			gcf_online = (StopWatch) ois.readObject();
			gcf_write = (StopWatch) ois.readObject();
			gcf_read = (StopWatch) ois.readObject();
			
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
	      
	    } catch (ClassNotFoundException e) {
	      throw new IOException("File contains invalid structure.", e);
	    } finally {
	      if (ois != null)
	        ois.close();
	    }
	}
	
	public Timing add(Timing t) {
		Timing out = new Timing();
		
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
		
		out.eviction = eviction.add(t.eviction);
		out.eviction_online = eviction_online.add(t.eviction_online);
		out.eviction_write = eviction_write.add(t.eviction_write);
		out.eviction_read = eviction_read.add(t.eviction_read);
		
		out.gcf = gcf.add(t.gcf);
		out.gcf_online = gcf_online.add(t.gcf_online);
		out.gcf_write = gcf_write.add(t.gcf_write);
		out.gcf_read = gcf_read.add(t.gcf_read);
		
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
		
		return out;
	}
	
	@Override
	public String toString() {
		String out = access + "\n" + access_online + "\n" + access_write + "\n" + access_read + "\n\n"
				+ decrypt + "\n" + decrypt_online + "\n" + decrypt_write + "\n" + decrypt_read + "\n\n"
				+ oprf + "\n" + oprf_online + "\n" + oprf_write + "\n" + oprf_read + "\n\n"
				+ pet + "\n" + pet_online + "\n" + pet_write + "\n" + pet_read + "\n\n"
				+ aot + "\n" + aot_online + "\n" + aot_write + "\n" + aot_read + "\n\n"
				+ post + "\n" + post_online + "\n" + post_write + "\n" + post_read + "\n\n"
				+ reshuffle + "\n" + reshuffle_online + "\n" + reshuffle_write + "\n" + reshuffle_read + "\n\n"
				+ eviction + "\n" + eviction_online + "\n" + eviction_write + "\n" + eviction_read + "\n\n"
				+ gcf + "\n" + gcf_online + "\n" + gcf_write + "\n" + gcf_read + "\n\n"
				+ ssot + "\n" + ssot_online + "\n" + ssot_write + "\n" + ssot_read + "\n\n"
				+ iot + "\n" + iot_online + "\n" + iot_write + "\n" + iot_read + "\n\n"
				+ encrypt + "\n" + encrypt_online + "\n" + encrypt_write + "\n" + encrypt_read;
		return out;
	}
}
