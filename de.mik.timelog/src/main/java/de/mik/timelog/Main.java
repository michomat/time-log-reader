package de.mik.timelog;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;

import com.beust.jcommander.JCommander;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

public class Main {
	private static final Pattern END_LOG_PATTERN = Pattern.compile("End: (\\d{1,2}.\\d{1,2}.\\d{4} \\d{1,2}:\\d{1,2})");
	private static final Pattern START_LOG_PATTERN = Pattern.compile("Start: (\\d{1,2}.\\d{1,2}.\\d{4} \\d{1,2}:\\d{1,2})");

	private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("HH:mm");

	public static void main(final String[] args) throws IOException {
		final Args programArguments = new Args();
		final JCommander jocommander = JCommander.newBuilder()
				.addObject(programArguments)
				.programName("Log Reader")
				.build();

		jocommander.parse(args);

		final File timeLogFile = programArguments.getTimeLogFile();
		final Integer limit = Optional.ofNullable(programArguments.getLimit()).orElse(Integer.MAX_VALUE);

		final List<String> lines = FileUtils.readLines(timeLogFile, Charset.defaultCharset());

		final List<String> accumulatedDurations = accumulateTime(limit, lines);

		accumulatedDurations.forEach(System.out::println);
	}

	public static List<String> accumulateTime(final Integer limit, final List<String> lines) {
		final List<LocalDateTime> startDates = new ArrayList<>();
		final List<LocalDateTime> endDates = new ArrayList<>();

		initDates(lines, startDates, endDates);

		final Map<LocalDate, List<LocalTime>> startTimeByDate = groupByDate(startDates);
		final Map<LocalDate, List<LocalTime>> endTimeByDate = groupByDate(endDates);

		final Multimap<LocalDate, WorkingPeriod> periodsPerDay = createWorkingPeriodsPerDay(startDates, endDates, startTimeByDate, endTimeByDate);

		return periodsPerDay.keySet().stream()
				.limit(limit)
				.sorted()
				.map(date -> createLine(date, periodsPerDay))
				.collect(Collectors.toList());
	}

	private static String createLine(final LocalDate date, final Multimap<LocalDate, WorkingPeriod> periodsByDay) {
		final Collection<WorkingPeriod> periodsPerDay = periodsByDay.get(date);


		final LongSummaryStatistics durationStatistic = periodsPerDay.stream()
				.map(period -> Duration.between(period.getStart(), period.getEnd()))
				.collect(Collectors.summarizingLong(Duration::toMinutes));

		final long durationInMinutes = durationStatistic.getSum();
		final float durationInHours = durationInMinutes / 60f;

		final LocalTime lastEnd = periodsPerDay.stream()
				.map(WorkingPeriod::getEnd)
				.max(Comparator.comparing(x -> x))
				.orElse(LocalTime.MAX);

		final LocalTime firstStart = periodsPerDay.stream()
				.map(WorkingPeriod::getStart)
				.min(Comparator.comparing(x -> x))
				.orElse(LocalTime.MIN);

		return String.format(Locale.ROOT, "%s Beginn: %s Ende: %s Dauer: %.1fh", date, firstStart.format(DTF), lastEnd.format(DTF), durationInHours);
	}

	static Map<LocalDate, List<LocalTime>> groupByDate(final List<LocalDateTime> startDates) {
		return startDates.stream().collect(Collectors.groupingBy(
				LocalDateTime::toLocalDate, Collectors.mapping(LocalDateTime::toLocalTime, Collectors.toList())));
	}


	private static Multimap<LocalDate, WorkingPeriod> createWorkingPeriodsPerDay(final List<LocalDateTime> startDates, final List<LocalDateTime> endDates, final Map<LocalDate, List<LocalTime>> startTimeByDate,
			final Map<LocalDate, List<LocalTime>> endTimeByDate) {

		final Multimap<LocalDate, WorkingPeriod> periodsPerDay = ArrayListMultimap.create();

		for (final LocalDateTime currentDateTime : startDates) {
			final LocalDate currentDate = currentDateTime.toLocalDate();

			final List<LocalTime> startTimesPerDay = startTimeByDate.getOrDefault(currentDate, Collections.emptyList());
			final List<LocalTime> endTimesPerDay = endTimeByDate.getOrDefault(currentDate, Collections.emptyList());

			Collections.sort(startTimesPerDay);
			Collections.sort(endTimesPerDay);

			for (final LocalTime start : startTimesPerDay) {

				final LocalTime end = endTimesPerDay.stream().filter(time -> time.isAfter(start)).findFirst().orElse(LocalTime.MAX);

				periodsPerDay.put(currentDate, new WorkingPeriod(start, end));
			}

			final LocalTime firstStartPerDay = startTimesPerDay.stream().findFirst().orElse(LocalTime.MIN);
			final LocalTime firstEndPerDay = endTimesPerDay.stream().findFirst().orElse(LocalTime.MAX);

			if (firstEndPerDay.isBefore(firstStartPerDay)) {
				periodsPerDay.put(currentDate, new WorkingPeriod(LocalTime.MIN, firstEndPerDay));
			}
		}
		final LocalDateTime lastStartDate = Iterables.getLast(startDates);
		final LocalDate localDate = lastStartDate.toLocalDate();
		final LocalDate tomorrow = localDate.plusDays(1);

		final Optional<LocalDateTime> lastEnd = endDates.stream().filter(end -> end.toLocalDate().isEqual(tomorrow)).findFirst();

		lastEnd.ifPresent(end -> periodsPerDay.put(end.toLocalDate(), new WorkingPeriod(LocalTime.MIN, end.toLocalTime())));

		return periodsPerDay;
	}

	private static void initDates(final List<String> lines, final List<LocalDateTime> startDates, final List<LocalDateTime> endDates) {
		for (final String line : lines) {
			final String trimLine = line.trim();

			final Matcher startMatcher = START_LOG_PATTERN.matcher(trimLine);
			final Matcher endMatcher = END_LOG_PATTERN.matcher(trimLine);
			if (startMatcher.matches()) {
				final String dateString = startMatcher.group(1);

				startDates.add(LocalDateTime.parse(dateString, (DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
			}
			else if (endMatcher.matches()) {
				final String dateString = endMatcher.group(1);

				endDates.add(LocalDateTime.parse(dateString, (DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
			}
		}
	}

}
