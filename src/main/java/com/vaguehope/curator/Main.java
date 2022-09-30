package com.vaguehope.curator;

import java.io.PrintStream;

import org.kohsuke.args4j.CmdLineParser;

import com.vaguehope.curator.Args.ArgsException;

public class Main {

	public static void main(final String[] rawArgs) throws Exception {
		final PrintStream err = System.err;
		final Args args = new Args();
		final CmdLineParser parser = new CmdLineParser(args);
		try {
			parser.parseArgument(rawArgs);
			if (args.isHelp()) {
				help(parser, System.out);
				return;
			}
			run(args);
		}
		catch (final ArgsException e) {
			err.println(e.getMessage());
			help(parser, err);
			return;
		}
		catch (final Exception e) {
			err.println("An unhandled error occured.");
			e.printStackTrace(err);
			System.exit(1);
		}
	}

	private static void run(final Args args) throws Exception {
		final FileDb db = new FileDb(args.getDb());
		final FileCopier fileCopier = new FileCopier(args.isDryRun());
		final FileHasher fileHasher = new FileHasher(db);
		new Curator(fileCopier, fileHasher, args).run();
	}

	private static void help(final CmdLineParser parser, final PrintStream ps) {
		ps.print("Usage:");
		parser.printSingleLineUsage(ps);
		ps.println();
		parser.printUsage(ps);
		ps.println();
	}

}
