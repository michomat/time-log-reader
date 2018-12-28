package de.mik.timelog;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class MainTest {
	private static final int LIMIT = 10;

	@Test
	public void test_accumulate_days() throws Exception {
		final List<String> lines = Arrays.asList(
				"Start: 10.12.2018 07:00",
				"End: 10.12.2018 16:30",
				"Start: 11.12.2018 07:00",
				"End: 11.12.2018 15:30");

		final List<String> log = Main.accumulateTime(LIMIT, lines);

		assertThat(log, contains(
				"2018-12-10 Beginn: 07:00 Ende: 16:30 Dauer: 9.5h",
				"2018-12-11 Beginn: 07:00 Ende: 15:30 Dauer: 8.5h"));
	}

	@Test
	public void test_between_days_regression() throws Exception {
		final List<String> lines = Arrays.asList(
				"Start: 28.12.2018 23:00",
				"End: 29.12.2018 02:00");

		final List<String> log = Main.accumulateTime(LIMIT, lines);

		assertThat(log, contains(
				"2018-12-28 Beginn: 23:00 Ende: 23:59 Dauer: 1.0h",
				"2018-12-29 Beginn: 00:00 Ende: 02:00 Dauer: 2.0h"));
	}
}
