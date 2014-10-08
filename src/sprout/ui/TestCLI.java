package sprout.ui;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import sprout.oram.ForestException;
import sprout.oram.Party;
import sprout.oram.operations.Access;
import sprout.util.Util;
import sprout.communication.Communication;
import sprout.oram.operations.*;

public class TestCLI
{
	public static final int DEFAULT_PORT = 8000;
	public static final String DEFAULT_IP = "localhost";
	public static final String DEFAULT_CONFIG_FILE = "config/smallConfig.yaml";
	public static final String DEFAULT_DB_FILE = "db.bin";
	public static final String DEFAULT_DATA_FILE = "config/smallData.txt";

	public static void main(String[] args)
	{
		// Setup command line argument parser
		Options options = new Options();
		options.addOption("config", true, "ORAM config file");
		options.addOption("debug", true, "Debug flag and output file");
		options.addOption("dbfile", true, "Database file");
		options.addOption("datafile", true, "File for generating forest data");
		options.addOption("build", false, "If enabled, build the forest");
		options.addOption("debbie_port", true, "Port to listen for Debbie");
		options.addOption("charlie_port", true, "Port to listen for Charlie");
		options.addOption("eddie_ip", true, "IP to look for eddie");
		options.addOption("debbie_ip", true, "IP to look for debbie");
		options.addOption("test_alg", true, "Algorithim to test");

		// Parse the command line arguments
		CommandLineParser cmdParser = new GnuParser();
		CommandLine cmd;
		try
		{
			cmd = cmdParser.parse(options, args);

			String party;
			String[] positionalArgs = cmd.getArgs();
			if (positionalArgs.length > 0)
			{
				party = positionalArgs[0];
			}
			else
			{
				throw new ParseException("No party specified");
			}

			if (cmd.hasOption("debug"))
			{
				Util.debugEnabled = true;
				Util.setLogFile(cmd.getOptionValue("debug"));
			}

			int extra_port = 0;
			if (party.equals("debbie"))
			{
				extra_port = 1;
			}

			int debbiePort = Integer.parseInt(cmd.getOptionValue("debbie_port", Integer.toString(DEFAULT_PORT)));
			int charliePort = Integer.parseInt(cmd.getOptionValue("charlie_port",
					Integer.toString(DEFAULT_PORT + 1 + extra_port)));
			
			String configFile = cmd.getOptionValue("config", DEFAULT_CONFIG_FILE);
			String dbFile = cmd.getOptionValue("dbfile", DEFAULT_DB_FILE);
			String dataFile = cmd.getOptionValue("datafile", DEFAULT_DATA_FILE);
			boolean buildForest = false;
			if (cmd.hasOption("build")) {
			  buildForest = true;
			}
			
			Class<? extends Operation> operation = null;
			String alg = cmd.getOptionValue("test_alg", "access").toLowerCase();
			
			if (alg.equals("access")) {
			  operation = Access.class;
			} else if (alg.equals("encrypt")) {
			  operation = EncryptPath.class;
			} else if (alg.equals("decrypt")) {
			  operation = DecryptPath.class;
			} else if (alg.equals("aot")) {
			  operation = sprout.oram.operations.AOT.class;
			} else if (alg.equals("pet")) {
			  operation = sprout.oram.operations.PET.class;
			} else if (alg.equals("reshuffle")) {
			  operation = Reshuffle.class;
			} else {
			  System.out.println("Method not supported");
			  System.exit(-1);
			}
			
			Constructor<? extends Operation> operationCtor = 
			    operation.getDeclaredConstructor(Communication.class, Communication.class);
			
			// For now all logic happens here. Eventually this will get wrapped
			// up in party specific classes.
			System.out.println("Starting " + party + "...");
			if (party.equals("eddie"))
			{
				Communication debbieCon = new Communication();
				debbieCon.start(debbiePort);

				Communication charlieCon = new Communication();
				charlieCon.start(charliePort);

				System.out.println("Waiting to establish connections...");
				while (charlieCon.getState() != Communication.STATE_CONNECTED
						|| debbieCon.getState() != Communication.STATE_CONNECTED)
					;
				System.out.println("Connection established");

				System.out.println("Charlie: " + charlieCon.readString());
				System.out.println("Debbie: " + debbieCon.readString());
				charlieCon.write("Hello charlie, from eddie");
				
				try {
          operationCtor.newInstance(charlieCon, debbieCon).run(Party.Eddie, configFile, dbFile, dataFile, buildForest);
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error in access exiting");
        }

				charlieCon.stop();
				debbieCon.stop();
			}
			else if (party.equals("debbie"))
			{
				Communication eddieCon = new Communication();
				InetSocketAddress eddieAddr = new InetSocketAddress(cmd.getOptionValue("eddie_ip", DEFAULT_IP),
						debbiePort);
				eddieCon.connect(eddieAddr);

				Communication charlieCon = new Communication();
				charlieCon.start(charliePort);

				System.out.println("Waiting to establish connections...");
				while (eddieCon.getState() != Communication.STATE_CONNECTED
						|| charlieCon.getState() != Communication.STATE_CONNECTED);
				System.out.println("Connection established");

				eddieCon.write("Hello eddie, from debbie");
				System.out.println("Charlie: " + charlieCon.readString());
				
				try {
				  operationCtor.newInstance(charlieCon, eddieCon).run(Party.Debbie, configFile, dbFile, dataFile, buildForest);
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error in access exiting");
        }

				eddieCon.stop();
				charlieCon.stop();
			}
			else if (party.equals("charlie"))
			{
				Communication debbieCon = new Communication();
				Communication eddieCon = new Communication();

				InetSocketAddress eddieAddr = new InetSocketAddress(cmd.getOptionValue("eddie_ip", DEFAULT_IP),
						charliePort);
				eddieCon.connect(eddieAddr);

				// TODO: This +1 should not be here if eddie/debbie are on
				// different machines
				InetSocketAddress debbieAddr = new InetSocketAddress(cmd.getOptionValue("debbie_ip", DEFAULT_IP), charliePort + 1);
				debbieCon.connect(debbieAddr);

				System.out.println("Waiting to establish connections...");
				while (eddieCon.getState() != Communication.STATE_CONNECTED ||
				        debbieCon.getState() != Communication.STATE_CONNECTED);

				System.out.println("Connection established");

				eddieCon.write("Hello eddie, from charlie");
				debbieCon.write("Hello debbie. from charlie");

				System.out.println("Eddie: " + eddieCon.readString());
				
				try {
				  operationCtor.newInstance(debbieCon, eddieCon).run(Party.Charlie, configFile, dbFile, dataFile, buildForest);
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error in access exiting");
        }
				
				eddieCon.stop();
				debbieCon.stop();
			}
			else
			{
				throw new ParseException("Invalid party, " + party + ", specified");
			}
			System.out.println(party + " exiting...");

		}
		catch (ParseException e)
		{
			Util.error("Parsing error: " + e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("see the options below", options);
		} catch (NoSuchMethodException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (SecurityException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

	}
}
