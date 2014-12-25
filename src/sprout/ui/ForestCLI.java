package sprout.ui;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.TreeException;
import sprout.util.Util;
import sprout.visualization.ORAMVisualizer;

public class ForestCLI {
	public static final String DEFAULT_CONFIG = "config/smallConfig.yaml";
	public static final String DEFAULT_DATA = "config/smallData.txt";
	public static final String DEFAULT_DB = "output/db.bin";

	public static void main(String[] args) {
		/*
		 * // Setup command line argument parser Options options = new
		 * Options(); options.addOption("config", true, "ORAM tree fanout (f)");
		 * options.addOption("data", true, "Database source file");
		 * options.addOption("fanout", true, "ORAM tree fanout (f)");
		 * options.addOption("debug", true, "Debug flag and output file");
		 * options.addOption("viz", true, "Visualization output file");
		 * options.addOption("dbfile", true, "Database file");
		 * options.addOption("levels", true,
		 * "Number of levels in ORAM forest/hierarchy (H)");
		 * options.addOption("tupleBitsL", true,
		 * "Number of bits in each tuple label L");
		 * options.addOption("tupleBitsN", true,
		 * "Number of bits in each tuple tag N");
		 * options.addOption("tupleBitsD", true,
		 * "Number of bits in each tuple data element D");
		 * options.addOption("bucketDepth", true,
		 * "Number of tuples in each bucket (w)");
		 * options.addOption("numLeaves", true,
		 * "Number of base ORAM leaves (N)");
		 * 
		 * // Parse the command line arguments CommandLineParser cmdParser = new
		 * GnuParser(); CommandLine cmd; try { cmd = cmdParser.parse(options,
		 * args);
		 * 
		 * String command = "create"; String[] positionalArgs = cmd.getArgs();
		 * if (positionalArgs.length > 0) { command = positionalArgs[0]; }
		 * 
		 * // TODO: If debug enabled, forward all output to the debug file if
		 * (cmd.hasOption("debug")) { Util.debugEnabled = true;
		 * Util.setLogFile(cmd.getOptionValue("debug")); }
		 * 
		 * // Create ForestMetadata instance Forest forest = null;
		 * 
		 * // TODO: Unify the if and the else. There is a lot of repeated code,
		 * // and being able to specify paramaters manually is useful. if
		 * (cmd.hasOption(ForestMetadata.FANOUT_NAME) &&
		 * cmd.hasOption(ForestMetadata.LEVELS_NAME) &&
		 * cmd.hasOption(ForestMetadata.TUPLEBITSN_NAME) &&
		 * cmd.hasOption(ForestMetadata.BUCKETDEPTH_NAME) &&
		 * cmd.hasOption(ForestMetadata.ADDRSPACE_NAME) &&
		 * cmd.hasOption(ForestMetadata.DATASIZE_NAME)) { int fanout =
		 * Integer.parseInt(cmd.getOptionValue(ForestMetadata.FANOUT_NAME)); int
		 * levels =
		 * Integer.parseInt(cmd.getOptionValue(ForestMetadata.LEVELS_NAME)); int
		 * tupleBitsN =
		 * Integer.parseInt(cmd.getOptionValue(ForestMetadata.TUPLEBITSN_NAME));
		 * int bucketDepth =
		 * Integer.parseInt(cmd.getOptionValue(ForestMetadata.BUCKETDEPTH_NAME
		 * )); long addrSpace =
		 * Long.parseLong(cmd.getOptionValue(ForestMetadata.ADDRSPACE_NAME));
		 * int dataSize =
		 * Integer.parseInt(cmd.getOptionValue(ForestMetadata.DATASIZE_NAME));
		 * ForestMetadata metadata = new ForestMetadata(fanout, levels,
		 * tupleBitsN, bucketDepth, addrSpace, dataSize);
		 * 
		 * forest = new Forest();
		 * 
		 * // TODO: Inform the user what is happening. if (command == "load") {
		 * forest.setMetaData(metadata); } else // create {
		 * forest.setMetaData(metadata);
		 * forest.buildFromFile(cmd.getOptionValue("data"),
		 * cmd.getOptionValue("dbfile", DEFAULT_DB)); } } else // load from
		 * config file { String configFile = cmd.getOptionValue("config",
		 * DEFAULT_CONFIG); forest = new Forest();
		 * 
		 * if (command == "load") { forest.loadFile(configFile,
		 * cmd.getOptionValue("dbfile", DEFAULT_DB)); } else {
		 * forest.buildFromFile(configFile, cmd.getOptionValue("data",
		 * DEFAULT_DATA), cmd.getOptionValue("dbfile", DEFAULT_DB)); } }
		 * 
		 * // Debug if (cmd.hasOption("debug")) { PrintStream writer = new
		 * PrintStream(new
		 * FileOutputStream(cmd.getOptionValue("viz").toString()));
		 * writer.println(ORAMVisualizer.renderTree(forest.getTree(0), writer));
		 * } } catch (ParseException e) { Util.error("Parsing error: " +
		 * e.getMessage()); HelpFormatter formatter = new HelpFormatter();
		 * formatter.printHelp("see the options below", options); } catch
		 * (ForestException e) { e.printStackTrace();
		 * Util.error("Forest error: " + e.getMessage()); } catch (TreeException
		 * e) { e.printStackTrace(); Util.error("Tree error: " +
		 * e.getMessage()); } catch (NumberFormatException e) {
		 * Util.error("Parsing error: " + e.getMessage()); HelpFormatter
		 * formatter = new HelpFormatter(); formatter.printHelp("usgage: " +
		 * args[0] + " [options] [command] \n see the options below", options);
		 * } catch (FileNotFoundException e) {
		 * Util.error("Invalid config file: " + e.getMessage()); HelpFormatter
		 * formatter = new HelpFormatter(); formatter.printHelp("usgage: " +
		 * args[0] + " [options] [command] \n see the options below", options);
		 * } catch (IOException e) { Util.error("IO error: " + e.getMessage());
		 * e.printStackTrace(); }
		 */
	}
}
