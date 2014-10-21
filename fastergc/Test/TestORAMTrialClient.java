// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Test;

import java.util.*;
import java.math.*;
import java.security.SecureRandom;

import jargs.gnu.CmdLineParser;

import Utils.*;
import Program.*;

class TestORAMTrialClient {
    static BigInteger bits;
    static int n;
    
    static SecureRandom rnd = new SecureRandom();
    static String circuit;

    private static void printUsage() {
	System.out.println("Usage: java TestORAMTrialClient [{-n, --bit-length} length] [{-s, --server} servername] [{-r, --iteration} r] [{-c, --circuit} circuit]");
    }

    private static void process_cmdline_args(String[] args) {
	CmdLineParser parser = new CmdLineParser();
	CmdLineParser.Option optionServerIPname = parser.addStringOption('s', "server");
	CmdLineParser.Option optionBitLength = parser.addIntegerOption('n', "bit-Length");
	CmdLineParser.Option optionIterCount = parser.addIntegerOption('r', "iteration");
	CmdLineParser.Option optionCircuit = parser.addStringOption('c', "circuit");

	try {
	    parser.parse(args);
	}
	catch (CmdLineParser.OptionException e) {
	    System.err.println(e.getMessage());
	    printUsage();
	    System.exit(2);
	}

	n = ((Integer) parser.getOptionValue(optionBitLength, new Integer(100))).intValue();
	ProgClient.serverIPname = (String) parser.getOptionValue(optionServerIPname, new String("localhost"));
	Program.iterCount = ((Integer) parser.getOptionValue(optionIterCount, new Integer(1))).intValue();
	ORAMTrialCommon.circuit = (String) parser.getOptionValue(optionCircuit, new String("F2ET"));
    }

    private static void generateData() throws Exception {
	bits = new BigInteger(n, rnd);	
	//bits = new BigInteger("0000", 2);
    }

    public static void main(String[] args) throws Exception {
	StopWatch.pointTimeStamp("Starting program");
	process_cmdline_args(args);

	generateData();
	
	ORAMTrialClient oramtrialclient = new ORAMTrialClient(bits, n);
	oramtrialclient.run();
    }
}