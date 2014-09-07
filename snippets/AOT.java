import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AOT {

	public static SecureRandom rand;

	public static void main(String[] args) throws Exception {
		
		rand = SecureRandom.getInstance("SHA1PRNG");
		
		int N = Integer.parseInt(args[0]);
		int[] mvec = new int[N];
		int rinput = Integer.parseInt(args[1]) % N;

		// populate mvec with random stuff
		for (int i = 0; i < N; i++) { 
			mvec[i] = rand.nextInt(N);
		}


	}

}