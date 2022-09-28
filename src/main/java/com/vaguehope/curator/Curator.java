package com.vaguehope.curator;

import java.io.File;

import com.vaguehope.curator.Args.ArgsException;

public class Curator {

	private final FileDb db;
	private final File srcDir;
	private final File destDir;

	public Curator(final FileDb db, final Args args) throws ArgsException {
		this.db = db;
		this.srcDir = args.getSrc();
		this.destDir = args.getDest();
	}

	public void run() {

	}

}
