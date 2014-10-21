// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Test;

import java.util.*;
import java.math.*;
import java.security.SecureRandom;

import jargs.gnu.CmdLineParser;

import Utils.*;
import Program.*;

class TestORAMTrialServer {
    static BigInteger bits;
    static int n;
    
    static SecureRandom rnd = new SecureRandom();

    private static void printUsage() {
	System.out.println("Usage: java TestORAMTrialServer [{-n, --bit-length} length] [{-c, --circuit} circuit]");
    }

    private static void process_cmdline_args(String[] args) {
	CmdLineParser parser = new CmdLineParser();
	CmdLineParser.Option optionBitLength = parser.addIntegerOption('n', "bit-length");
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
	ORAMTrialCommon.circuit = (String) parser.getOptionValue(optionCircuit, new String("F2ET"));
    }

    private static void generateData() throws Exception {
	bits = new BigInteger(n, rnd);
	//bits = new BigInteger("00000000000000", 2);
    }

    public static void main(String[] args) throws Exception {

	StopWatch.pointTimeStamp("Starting program");
	process_cmdline_args(args);

	generateData();
	
	ORAMTrialServer oramtrialserver = new ORAMTrialServer(bits, n);
	oramtrialserver.run();
    }
}