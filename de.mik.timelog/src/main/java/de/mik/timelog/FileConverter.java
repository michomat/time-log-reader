package de.mik.timelog;

import java.io.File;

import com.beust.jcommander.IStringConverter;

public class FileConverter implements IStringConverter<File> {

	@Override
	public File convert(final String value) {
		return new File(value);
	}

}
