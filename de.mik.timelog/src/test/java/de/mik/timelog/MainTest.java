package de.mik.timelog;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class MainTest {
	@Test
	public void test_between_days_regression() throws Exception {
		final List<LocalDateTime> startDates = Arrays.asList(LocalDateTime.of(2018, 12, 01, 20, 00));
		final List<LocalDateTime> endDates = Arrays.asList(LocalDateTime.of(2018, 12, 02, 3, 00));

		final Map<LocalDate, Integer> duration = Main.calculateDuration(startDates, Main.groupByDate(startDates), Main.groupByDate(endDates));

		assertEquals(2, duration.keySet().size());
		assertEquals(4, (int) duration.get(LocalDate.of(2018, 12, 01)));
		assertEquals(3, (int) duration.get(LocalDate.of(2018, 12, 02)));
	}
}
