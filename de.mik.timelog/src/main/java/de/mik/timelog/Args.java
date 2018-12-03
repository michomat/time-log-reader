package de.mik.timelog;

import java.io.File;

import com.beust.jcommander.Parameter;

public class Args {

	@Parameter(names = "-log", description = "Path to log file with start and shutdown timestamps", converter = FileConverter.class, required = true, help = true)
	private File timeLogFile;

	@Parameter(names = "-limit", description = "Limit the log entries", required = false, help = true)
	private Integer limit;

	public File getTimeLogFile() {
		return this.timeLogFile;
	}

	public Integer getLimit() {
		return this.limit;
	}

}
