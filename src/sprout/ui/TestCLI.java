package sprout.ui;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.oram.operations.Access;
import sprout.oram.operations.PPEvict;
import sprout.util.Util;
import sprout.communication.Communication;
import sprout.oram.operations.*;

public class TestCLI {
	public static final int DEFAULT_PORT = 8000;
	public static final String DEFAULT_IP = "localhost";
	public static final String DEFAULT_CONFIG_FILE = "config/newConfig.yaml";
	public static final String DEFAULT_DB_FILE = "files/forest.bin";

	// public static final String DEFAULT_DATA_FILE = "config/smallData.txt";

	public static void main(String[] args) {
		// Setup command line argument parser
		Options options = new Options();
		options.addOption("config", true, "ORAM config file");
		options.addOption("debug", true, "Debug flag and output file");
		options.addOption("dbfile", true, "Database file");
		options.addOption("datafile", true, "File for generating forest data");
		options.addOption("build", false, "If enabled, build the forest");
		options.addOption("eddie_port_1", true, "Eddie's port to listen for Debbie");
		options.addOption("eddie_port_2", true, "Eddie's port to listen for Charlie");
		options.addOption("debbie_port", true, "Debbie's port to listen for Charlie");
		options.addOption("eddie_ip", true, "IP to look for eddie");
		options.addOption("debbie_ip", true, "IP to look for debbie");
		options.addOption("test_alg", true, "Algorithim to test");

		// Parse the command line arguments
		CommandLineParser cmdParser = new GnuParser();
		CommandLine cmd;
		try {
			cmd = cmdParser.parse(options, args);

			String party;
			String[] positionalArgs = cmd.getArgs();
			if (positionalArgs.length > 0) {
				party = positionalArgs[0];
			} else {
				throw new ParseException("No party specified");
			}

			if (cmd.hasOption("debug")) {
				Util.debugEnabled = true;
				Util.setLogFile(cmd.getOptionValue("debug"));
			}

			int extra_port = 15;
			//if (party.equals("debbie")) {
			//	extra_port = 1;
			//}

			//int debbiePort = Integer.parseInt(cmd.getOptionValue("debbie_port",
			//		Integer.toString(DEFAULT_PORT)));
			//int charliePort = Integer.parseInt(cmd.getOptionValue(
			//		"charlie_port",
			//		Integer.toString(DEFAULT_PORT + 1 + extra_port)));
			int eddiePort1 = Integer.parseInt(cmd.getOptionValue("eddie_port_1", Integer.toString(DEFAULT_PORT)));
			int eddiePort2 = Integer.parseInt(cmd.getOptionValue("eddie_port_2", Integer.toString(eddiePort1 + extra_port)));
			int debbiePort = Integer.parseInt(cmd.getOptionValue("debbie_port", Integer.toString(eddiePort2 + extra_port)));
			
			String eddieIp = cmd.getOptionValue("eddie_ip", DEFAULT_IP);
			String debbieIp = cmd.getOptionValue("debbie_ip", DEFAULT_IP);

			String configFile = cmd.getOptionValue("config",
					DEFAULT_CONFIG_FILE);
			String dbFile = cmd.getOptionValue("dbfile", DEFAULT_DB_FILE);
			// String dataFile = cmd.getOptionValue("datafile",
			// DEFAULT_DATA_FILE);
			boolean buildForest = false;
			if (cmd.hasOption("build")) {
				buildForest = true;
			}

			Class<? extends Operation> operation = null;
			String alg = cmd.getOptionValue("test_alg", "access").toLowerCase();

			if (alg.equals("access")) {
				operation = Access.class;
			} else if (alg.equals("xot")) {
				operation = sprout.oram.operations.XOT.class;
			} else if (alg.equals("ssxot")) {
				operation = sprout.oram.operations.SSXOT.class;
			} else if (alg.equals("reshuffle")) {
				operation = Reshuffle.class;
			} else if (alg.equals("ppt")) {
				operation = PostProcessT.class;
			} else if (alg.equals("evict")) {
				operation = Eviction.class;
			} else if (alg.equals("gcf")) {
				operation = GCF.class;
			} else if (alg.equals("retrieve")) {
				operation = Retrieve.class;
			} else if (alg.equals("precomp")) {
				operation = Precomputation.class;
			} else if (alg.equals("sscot")) {
				operation = SSCOT.class;
			} else if (alg.equals("ssiot")) {
				operation = SSIOT.class;
			} else if (alg.equals("thread")) {
				operation = ThreadPPEvict.class;
			} else if (alg.equals("ts")) {
				operation = TestSend.class;
			} else {
				System.out.println("Method " + alg + " not supported");
				System.exit(-1);
			}

			Constructor<? extends Operation> operationCtor = operation
					.getDeclaredConstructor(Communication.class,
							Communication.class);
			
			try {
				ForestMetadata.setup(configFile);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			int numTrees = ForestMetadata.getLevels();
			PPEvict.threadCon1 = new Communication[numTrees];
			PPEvict.threadCon2 = new Communication[numTrees];
			//System.out.println("numTrees: " + numTrees);
			
			
			// For now all logic happens here. Eventually this will get wrapped
			// up in party specific classes.
			System.out.println("Starting " + party + "...");
			if (party.equals("eddie")) {
				Communication debbieCon = new Communication();
				debbieCon.start(eddiePort1);

				Communication charlieCon = new Communication();
				charlieCon.start(eddiePort2);

				for (int i=0; i<numTrees; i++) {
					// for charlie
					PPEvict.threadCon1[i] = new Communication();
					PPEvict.threadCon1[i].start(eddiePort2 + i + 1);
					// for debbie
					PPEvict.threadCon2[i] = new Communication();
					PPEvict.threadCon2[i].start(eddiePort1 + i + 1);					
				}

				System.out.println("Waiting to establish connections...");
				
				while (debbieCon.getState() != Communication.STATE_CONNECTED);
				for (int i=0; i<numTrees; ) {
					if (PPEvict.threadCon2[i].getState() == Communication.STATE_CONNECTED)
						i++;
				}
				while (charlieCon.getState() != Communication.STATE_CONNECTED);
				for (int i=0; i<numTrees; ) {
					if (PPEvict.threadCon1[i].getState() == Communication.STATE_CONNECTED)
						i++;
				}
				
				//while (charlieCon.getState() != Communication.STATE_CONNECTED
				//		|| debbieCon.getState() != Communication.STATE_CONNECTED)
				//	;
				System.out.println("Connection established");

				System.out.println("Charlie: " + charlieCon.readString());
				System.out.println("Debbie: " + debbieCon.readString());
				charlieCon.write("Hello charlie, from eddie");

				try {
					operationCtor.newInstance(charlieCon, debbieCon).run(
							Party.Eddie, configFile, dbFile, buildForest);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error in access exiting");
				}

				charlieCon.write("end");
				debbieCon.write("end");
				charlieCon.readString();
				debbieCon.readString();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				charlieCon.stop();
				debbieCon.stop();
				for (int i=0; i<numTrees; i++) {
					PPEvict.threadCon1[i].stop();
					PPEvict.threadCon2[i].stop();
				}
				
			} else if (party.equals("debbie")) {
				Communication eddieCon = new Communication();
				InetSocketAddress eddieAddr = new InetSocketAddress(eddieIp, eddiePort1);
				eddieCon.connect(eddieAddr);

				Communication charlieCon = new Communication();
				charlieCon.start(debbiePort);

				for (int i=0; i<numTrees; i++) {
					// for charlie
					PPEvict.threadCon1[i] = new Communication();
					PPEvict.threadCon1[i].start(debbiePort + i + 1);
					// for eddie
					PPEvict.threadCon2[i] = new Communication();
					eddieAddr = new InetSocketAddress(eddieIp, eddiePort1 + i + 1);
					PPEvict.threadCon2[i].connect(eddieAddr);			
				}

				System.out.println("Waiting to establish connections...");
				
				while (eddieCon.getState() != Communication.STATE_CONNECTED);
				for (int i=0; i<numTrees; ) {
					if (PPEvict.threadCon2[i].getState() == Communication.STATE_CONNECTED)
						i++;
				}
				while (charlieCon.getState() != Communication.STATE_CONNECTED);
				for (int i=0; i<numTrees; ) {
					if (PPEvict.threadCon1[i].getState() == Communication.STATE_CONNECTED)
						i++;
				}
				
				//while (eddieCon.getState() != Communication.STATE_CONNECTED
				//		|| charlieCon.getState() != Communication.STATE_CONNECTED)
				//	;
				System.out.println("Connection established");

				eddieCon.write("Hello eddie, from debbie");
				System.out.println("Charlie: " + charlieCon.readString());

				try {
					operationCtor.newInstance(charlieCon, eddieCon).run(
							Party.Debbie, configFile, dbFile, buildForest);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error in access exiting");
				}
				charlieCon.write("end");
				eddieCon.write("end");
				charlieCon.readString();
				eddieCon.readString();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				eddieCon.stop();
				charlieCon.stop();
				for (int i=0; i<numTrees; i++) {
					PPEvict.threadCon1[i].stop();
					PPEvict.threadCon2[i].stop();
				}
				
			} else if (party.equals("charlie")) {
				Communication debbieCon = new Communication();
				Communication eddieCon = new Communication();

				InetSocketAddress eddieAddr = new InetSocketAddress(eddieIp, eddiePort2);
				eddieCon.connect(eddieAddr);
				
				InetSocketAddress debbieAddr = new InetSocketAddress(debbieIp, debbiePort);
				debbieCon.connect(debbieAddr);

				for (int i=0; i<numTrees; i++) {
					// for debbie
					PPEvict.threadCon1[i] = new Communication();
					debbieAddr = new InetSocketAddress(debbieIp, debbiePort + i + 1);
					PPEvict.threadCon1[i].connect(debbieAddr);
					// for eddie
					PPEvict.threadCon2[i] = new Communication();
					eddieAddr = new InetSocketAddress(eddieIp, eddiePort2 + i + 1);
					PPEvict.threadCon2[i].connect(eddieAddr);					
				}

				System.out.println("Waiting to establish connections...");
				
				while (eddieCon.getState() != Communication.STATE_CONNECTED);
				for (int i=0; i<numTrees; ) {
					if (PPEvict.threadCon2[i].getState() == Communication.STATE_CONNECTED)
						i++;
				}
				while (debbieCon.getState() != Communication.STATE_CONNECTED);
				for (int i=0; i<numTrees; ) {
					if (PPEvict.threadCon1[i].getState() == Communication.STATE_CONNECTED)
						i++;
				}
				
				//while (eddieCon.getState() != Communication.STATE_CONNECTED
				//		|| debbieCon.getState() != Communication.STATE_CONNECTED)
				//	;

				System.out.println("Connection established");

				eddieCon.write("Hello eddie, from charlie");
				debbieCon.write("Hello debbie. from charlie");

				System.out.println("Eddie: " + eddieCon.readString());

				try {
					operationCtor.newInstance(debbieCon, eddieCon).run(
							Party.Charlie, configFile, dbFile, buildForest);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error exiting");
				}
				debbieCon.write("end");
				eddieCon.write("end");
				debbieCon.readString();
				eddieCon.readString();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				eddieCon.stop();
				debbieCon.stop();
				for (int i=0; i<numTrees; i++) {
					PPEvict.threadCon1[i].stop();
					PPEvict.threadCon2[i].stop();
				}
				
			} else {
				throw new ParseException("Invalid party, " + party
						+ ", specified");
			}
			System.out.println(party + " exiting...");

		} catch (ParseException e) {
			Util.error("Parsing error: " + e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("see the options below", options);
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		}

	}
}
