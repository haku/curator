package com.vaguehope.curator;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.curator.data.FileData;

public class WritableFileDb implements Closeable {

	private static final Logger LOG = LoggerFactory.getLogger(WritableFileDb.class);

	private final Connection conn;

	protected WritableFileDb(final Connection conn) throws SQLException {
		if (conn.getAutoCommit()) {
			throw new IllegalArgumentException("AutoCommit must not be enabled.");
		}
		this.conn = conn;
	}

	@Override
	public void close() throws IOException {
		boolean committed = false;
		try {
			commitOrRollback();
			committed = true;
		}
		finally {
			try {
				this.conn.close();
			}
			catch (final SQLException e) {
				if (committed) throw new IOException("Failed to close DB connection.", e);
				LOG.error("Failed to close DB connection", e);
			}
		}
	}

	private void commitOrRollback() throws IOException {
		try {
			this.conn.commit();
		}
		catch (final SQLException e) {
			try {
				this.conn.rollback();
			}
			catch (final SQLException e1) {
				LOG.error("Failed to rollback transaction.", e1);
			}
			throw new IOException("Failed to commit.", e);
		}
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// File ID.
	// The read methods are here so they are reading from the same transaction as the writes around them.

	protected FileData readFileData (final File file) throws SQLException {
		return FileDb.readFileDataFromConn(this.conn, file);
	}

	protected void storeFileData (final File file, final FileData fileData) throws SQLException {
		try (final PreparedStatement st = this.conn.prepareStatement(
				"INSERT INTO files (file,size,modified,sha1,md5) VALUES (?,?,?,?,?);")) {
			st.setString(1, file.getAbsolutePath());
			st.setLong(2, fileData.getSize());
			st.setLong(3, fileData.getModified());
			st.setString(4, fileData.getSha1());
			st.setString(5, fileData.getMd5());
			final int n = st.executeUpdate();
			if (n < 1) throw new SQLException("No insert occured inserting file '" + file.getAbsolutePath() + "'.");
		}
		catch (final SQLException e) {
			throw new SQLException(String.format("Failed to store new data for file %s \"%s\".", file, fileData), e);
		}
	}

	protected void updateFileData (final File file, final FileData fileData) throws SQLException {
		try (final PreparedStatement st = this.conn.prepareStatement(
				"UPDATE files SET size=?,modified=?,sha1=?,md5=? WHERE file=?;")) {
			st.setLong(1, fileData.getSize());
			st.setLong(2, fileData.getModified());
			st.setString(3, fileData.getSha1());
			st.setString(4, fileData.getMd5());
			st.setString(5, file.getAbsolutePath());
			final int n = st.executeUpdate();
			if (n < 1) throw new SQLException("No update occured updating file '" + file.getAbsolutePath() + "'.");
		}
		catch (final SQLException e) {
			throw new SQLException(String.format("Failed to update data for file %s to \"%s\".", file, fileData), e);
		}
	}

}
