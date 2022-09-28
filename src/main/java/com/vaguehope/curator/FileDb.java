package com.vaguehope.curator;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.Encoding;
import org.sqlite.SQLiteConfig.TransactionMode;

public class FileDb {

	private final String path;
	private final Connection conn;

	public FileDb (final File file) throws SQLException {
		this("jdbc:sqlite:" + file.getAbsolutePath());
	}

	protected FileDb(final String path) throws SQLException {
		this.path = path;
		this.conn = makeDbConnection(path);
		makeSchema();
	}

	private void makeSchema () throws SQLException {
		executeSql("CREATE TABLE IF NOT EXISTS files ("
				+ "file STRING NOT NULL PRIMARY KEY, "
				+ "size INT NOT NULL, "
				+ "modified INT NOT NULL, "
				+ "sha1 STRING NOT NULL);");
	}

	@SuppressWarnings("resource")
	public WritableFileDb getWritable() throws SQLException {
		final Connection c = makeDbConnection(this.path);
		c.setAutoCommit(false);
		return new WritableFileDb(c);
	}

	private static SQLiteConfig makeDbConfig() throws SQLException {
		final SQLiteConfig c = new SQLiteConfig();
		c.setEncoding(Encoding.UTF8);
		c.setSharedCache(true);
		c.setTransactionMode(TransactionMode.DEFERRED);
		c.enforceForeignKeys(true);
		return c;
	}

	private static Connection makeDbConnection (final String dbPath) throws SQLException {
		return DriverManager.getConnection(dbPath, makeDbConfig().toProperties());
	}

	private boolean executeSql (final String sql) throws SQLException {
		final Statement st = this.conn.createStatement();
		try {
			return st.executeUpdate(sql) > 0;
		}
		catch (final SQLException e) {
			throw new SQLException(String.format("Failed to execute SQL \"%s\".", sql), e);
		}
		finally {
			st.close();
		}
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public FileData getFileData(final File file) throws SQLException {
		return readFileDataFromConn(this.conn, file);
	}

	protected static FileData readFileDataFromConn(final Connection conn, final File file) throws SQLException {
		try (final PreparedStatement st = conn.prepareStatement("SELECT size,modified,sha1,md5 FROM files WHERE file=?;")) {
			st.setString(1, file.getAbsolutePath());
			st.setMaxRows(2);
			try (final ResultSet rs = st.executeQuery()) {
				if (!rs.next()) return null;
				final FileData fileData = new FileData(
						rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4));
				if (rs.next()) throw new SQLException("Query for file '" + file.getAbsolutePath() + "' retured more than one result.");
				return fileData;
			}
		}
	}
}
