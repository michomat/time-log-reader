package de.mik.timelog;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.beust.jcommander.JCommander;

public class Main {

	private static final Pattern END_LOG_PATTERN = Pattern.compile("End: (\\d{1,2}.\\d{1,2}.\\d{4} \\d{1,2}:\\d{1,2})");
	private static final Pattern START_LOG_PATTERN = Pattern.compile("Start: (\\d{1,2}.\\d{1,2}.\\d{4} \\d{1,2}:\\d{1,2})");

	private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("HH:mm");

	public static void main(final String[] args) throws IOException {

		final Args programArguments = new Args();
		JCommander.newBuilder()
		.addObject(programArguments)
		.build()
		.parse(args);

		final File timeLogFile = programArguments.getTimeLogFile();

		final List<String> lines = FileUtils.readLines(timeLogFile, Charset.defaultCharset());

		final List<LocalDateTime> startDates = new ArrayList<>();
		final List<LocalDateTime> endDates = new ArrayList<>();

		initDates(lines, startDates, endDates);

		final Map<LocalDate, List<LocalTime>> startTimeByDate = startDates.stream().collect(Collectors.groupingBy(
				LocalDateTime::toLocalDate, Collectors.mapping(LocalDateTime::toLocalTime, Collectors.toList())));

		final Map<LocalDate, List<LocalTime>> endTimeByDate = endDates.stream().collect(Collectors.groupingBy(
				LocalDateTime::toLocalDate, Collectors.mapping(LocalDateTime::toLocalTime, Collectors.toList())));

		final Map<LocalDate, Integer> durationsPerDayInMinutes = calculateDuration(startDates, startTimeByDate, endTimeByDate);

		final LocalDate now = LocalDate.now();
		final TemporalField fieldISO = WeekFields.of(Locale.GERMAN).dayOfWeek();
		final LocalDate monday = now.with(fieldISO, 1);

		final Period period = Period.between(monday,now);
		final int days = period.getDays();

		for (int i = 0; i <= days; i++) {
			final LocalDate current = monday.plusDays(i);

			final Integer minutes = durationsPerDayInMinutes.get(current);

			final float hours = minutes.floatValue() / 60f;

			final String firstStart = startTimeByDate.get(current).stream().findFirst().map(lt -> lt.format(DTF)).orElse("<empty>");
			final String lastEnd = endTimeByDate.get(current).stream().reduce((first, second) -> second).map(lt -> lt.format(DTF)).orElse("<empty>");


			System.out.println(String.format("%s Beginn: %s Ende: %s Dauer: %.1fh", current, firstStart, lastEnd, hours));
		}
	}

	private static Map<LocalDate, Integer> calculateDuration(final List<LocalDateTime> startDates, final Map<LocalDate, List<LocalTime>> startTimeByDate,
			final Map<LocalDate, List<LocalTime>> endTimeByDate) {
		final Map<LocalDate, Integer> durationPerDay = new HashMap<>();
		for (final LocalDateTime dateTime : startDates) {
			final LocalDate localDate = dateTime.toLocalDate();
			final List<LocalTime> startTimes = startTimeByDate.get(localDate);
			final List<LocalTime> endTimes = endTimeByDate.get(localDate);

			Collections.sort(startTimes);
			Collections.sort(endTimes);

			for (int i = 0; i < startTimes.size(); i++) {
				final LocalTime start = startTimes.get(i);

				if (endTimes.size() > i) {

					final LocalTime end = endTimes.get(i);

					if (start.isBefore(end)) {
						final Duration duration = Duration.between(start, end);

						durationPerDay.merge(localDate, (int) duration.toMinutes(), (d1, d2) -> d1 + d2);
					}
				}
			}
		}

		return durationPerDay;
	}

	private static void initDates(final List<String> lines, final List<LocalDateTime> startDates, final List<LocalDateTime> endDates) {
		for (final String line : lines) {
			final String trimLine = line.trim();

			final Matcher startMatcher = START_LOG_PATTERN.matcher(trimLine);
			final Matcher endMatcher = END_LOG_PATTERN.matcher(trimLine);
			if (startMatcher.matches()) {
				final String dateString = startMatcher.group(1);

				startDates.add(LocalDateTime.parse(dateString, (DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
			} else if (endMatcher.matches()) {
				final String dateString = endMatcher.group(1);

				endDates.add(LocalDateTime.parse(dateString, (DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
			}
		}
	}

}
