package de.mik.timelog;

import java.time.LocalTime;

public class WorkingPeriod {

	private final LocalTime start;
	private final LocalTime end;

	public WorkingPeriod(final LocalTime start, final LocalTime end) {
		this.start = start;
		this.end = end;
	}

	public LocalTime getStart() {
		return this.start;
	}

	public LocalTime getEnd() {
		return this.end;
	}
}
