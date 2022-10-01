package com.vaguehope.curator;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryFileDb extends FileDb {

	public InMemoryFileDb() throws SQLException {
		super(dbName());
	}

	private static final AtomicInteger NUMBER = new AtomicInteger(0);

	private static String dbName() {
		return "jdbc:sqlite:file:testdb-" + NUMBER.incrementAndGet() + "?mode=memory&cache=shared";
	}

}
