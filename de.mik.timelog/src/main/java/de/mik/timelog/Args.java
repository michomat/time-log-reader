package de.mik.timelog;

import java.io.File;

import com.beust.jcommander.Parameter;

public class Args {

	@Parameter(names = "-log", converter = FileConverter.class)
	private File timeLogFile;

	public File getTimeLogFile() {
		return this.timeLogFile;
	}

}
