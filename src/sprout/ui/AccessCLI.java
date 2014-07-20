package sprout.ui;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.util.Util;
import sprout.communication.Communication;

public class AccessCLI
{
	public static final int DEFAULT_PORT = 8000;
	public static final String DEFAULT_IP = "localhost";

	public static void main(String[] args)
	{
		// Setup command line argument parser
		Options options = new Options();
		options.addOption("config", true, "ORAM config file");
		options.addOption("debug", true, "Debug flag and output file");
		options.addOption("dbfile", true, "Database file");
		options.addOption("debbie_port", true, "Port to listen for Debbie");
		options.addOption("charlie_port", true, "Port to listen for Charlie");
		options.addOption("eddie_ip", true, "IP to look for eddie");
		options.addOption("debbie_ip", true, "IP to look for debbie");

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

			Forest forest = new Forest();
			
			// For now all logic happens here. Eventually this will get wrapped
			// up in party specific classes.
			System.out.println("Starting " + party + "...");
			if (party.equals("eddie"))
			{
//				Communication debbieCon = new Communication();
//				debbieCon.start(debbiePort);
				
				try
				{
					forest.buildFromFile("../config/smallConfig.yaml", "../config/smallData.txt", "db.bin");
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
				catch (ForestException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				Communication charlieCon = new Communication();
				charlieCon.start(charliePort);

				System.out.println("Waiting to establish connections...");
				while (charlieCon.getState() != Communication.STATE_CONNECTED
						|| charlieCon.getState() != Communication.STATE_CONNECTED)
					;
				System.out.println("Connection established");

				System.out.println("Eddie: " + charlieCon.readString());
//				System.out.println("Debbie: " + debbieCon.readString());
				
				// Wait for incoming queries from charlie
				String msg = charlieCon.readString();
				if (!msg.equals("exit"))
				{
					if (msg.contains("startget"))
					{
						String[] parts = msg.split(" ");
						// startget 010101
						// <cmd> <tree level> <leaf label>
						String command = parts[0];
						String label = parts[1];
						Util.disp(msg);
						long address = Long.parseLong(label, 2);
						byte[] leaf = forest.getEntryInInitialORAM((int)address);
//						charlieCon.writeLengthEncoded(leaf);
						
						System.out.println("Sending: ");
						Util.disp(leaf);
						charlieCon.write(leaf);
						
						// drop into while loop for each layer of the forest here
						for (int i = 0; i < forest.getNumberOfTrees(); i++)
						{
							// TODO: parse another msg and iteratively retrieve each thing
							msg = charlieCon.readString();
						}
					}
					else if (msg.contains("numlayers"))
					{
						charlieCon.write("" + forest.getNumberOfTrees());
					}
					
					// Get the next memory location query start from charlie, or quit
					msg = charlieCon.readString();
				}

				charlieCon.stop();
//				debbieCon.stop();
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

				eddieCon.write("Hello from debbie");
				System.out.println("Debbie: " + charlieCon.readString());

				eddieCon.stop();
				charlieCon.stop();
			}
			else if (party.equals("charlie"))
			{
//				Communication debbieCon = new Communication();
				Communication eddieCon = new Communication();

				InetSocketAddress eddieAddr = new InetSocketAddress(cmd.getOptionValue("eddie_ip", DEFAULT_IP),
						charliePort);
				eddieCon.connect(eddieAddr);

				// TODO: This +1 should not be here if eddie/debbie are on
				// different machines
//				InetSocketAddress debbieAddr = new InetSocketAddress(cmd.getOptionValue("debbie_ip", DEFAULT_IP), charliePort + 1);
//				debbieCon.connect(debbieAddr);

				System.out.println("Waiting to establish connections...");
//				while (eddieCon.getState() != Communication.STATE_CONNECTED || debbieCon.getState() != Communication.STATE_CONNECTED);
				while (eddieCon.getState() != Communication.STATE_CONNECTED);
				System.out.println("Connection established");

//				eddieCon.write("Hello from charlie");
//				debbieCon.write("Hello debbie from charlie");
//				System.out.println(eddieCon.readString());
				
				eddieCon.write("numlayers");
				int numTrees = Integer.parseInt(eddieCon.readString());
				
				eddieCon.write("startget 0");
				byte[] leaf = eddieCon.read();
				System.out.println("Received:");
				Util.disp(leaf);
				for (int i = 0; i < numTrees; i++)
				{
					// messages are formatted as follows: get <level:int> <label: binary string>
					// TODO
				}
				
				// Quit after only a single execution
				eddieCon.write("quit");
				eddieCon.stop();
//				debbieCon.stop();
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
		}

	}
}
