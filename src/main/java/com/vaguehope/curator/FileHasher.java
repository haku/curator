package com.vaguehope.curator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.vaguehope.curator.data.FileAndData;
import com.vaguehope.curator.data.FileData;

public class FileHasher {

	private final FileDb db;

	public FileHasher(final FileDb db) {
		this.db = db;
	}

	public Collection<FileAndData> dataForFiles(final Collection<File> files) throws IOException, SQLException {
		final Collection<FileAndData> ret = new ArrayList<>();
		try (final WritableFileDb w = this.db.getWritable()) {
			for (final File file : files) {
				ret.add(dataForFile(w, file));
			}
		}
		return ret;
	}

	private static FileAndData dataForFile(final WritableFileDb w, final File file) throws SQLException, IOException {
		final FileData fileDataInDb = w.readFileData(file);
		if (fileDataInDb != null && fileDataInDb.upToDate(file)) {
			return new FileAndData(file, fileDataInDb);
		}

		final FileData fileData = FileData.forFile(file);
		w.storeFileData(file, fileData);
		return new FileAndData(file, fileData);
	}

}
