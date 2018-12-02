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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

public class Main {

	private static Pattern endPattern = Pattern.compile("End: (\\d{1,2}.\\d{1,2}.\\d{4} \\d{1,2}:\\d{1,2})");
	private static Pattern startPattern = Pattern.compile("Start: (\\d{1,2}.\\d{1,2}.\\d{4} \\d{1,2}:\\d{1,2})");

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			throw new IllegalStateException("Illegal amount of parameters " + args);
		}

		List<String> lines = FileUtils.readLines(new File(args[0]), Charset.defaultCharset());

		List<LocalDateTime> startDates = new ArrayList<>();
		List<LocalDateTime> endDates = new ArrayList<>();

		initDates(lines, startDates, endDates);

		Map<LocalDate, List<LocalTime>> startTimeByDate = startDates.stream().collect(Collectors.groupingBy(
				LocalDateTime::toLocalDate, Collectors.mapping(LocalDateTime::toLocalTime, Collectors.toList())));

		Map<LocalDate, List<LocalTime>> endTimeByDate = endDates.stream().collect(Collectors.groupingBy(
				LocalDateTime::toLocalDate, Collectors.mapping(LocalDateTime::toLocalTime, Collectors.toList())));

		Map<LocalDate, Duration> durationsPerDay = calculateDuration(startDates, startTimeByDate, endTimeByDate);
		
		Set<Entry<LocalDate, Duration>> entries = durationsPerDay.entrySet();
		Set<Entry<LocalDate, Duration>> sortedEntries = new TreeSet<Entry<LocalDate, Duration>>(Comparator.comparing(Entry::getKey));
		sortedEntries.addAll(entries);
		for (Entry<LocalDate, Duration> entry : sortedEntries) {
				System.out.println(entry.getKey() +":" + entry.getValue().toHours() + "h");
		}
	}

	private static Map<LocalDate, Duration> calculateDuration(List<LocalDateTime> startDates, Map<LocalDate, List<LocalTime>> startTimeByDate,
			Map<LocalDate, List<LocalTime>> endTimeByDate) {
		Map<LocalDate, Duration> durationPerDay = new HashMap<>();
		for (LocalDateTime dateTime : startDates) {
			LocalDate localDate = dateTime.toLocalDate();
			List<LocalTime> startTimes = startTimeByDate.get(localDate);
			List<LocalTime> endTimes = endTimeByDate.get(localDate);

			Collections.sort(startTimes);
			Collections.sort(endTimes);

			for (int i = 0; i < startTimes.size(); i++) {
				LocalTime start = startTimes.get(i);

				if (endTimes.size() > i) {

					LocalTime end = endTimes.get(i);

					if (start.isBefore(end)) {
						Duration duration = Duration.between(start, end);

						durationPerDay.put(dateTime.toLocalDate(), duration);
					}
				}
			}
		}
		
		return durationPerDay;
	}

	private static void initDates(List<String> lines, List<LocalDateTime> startDates, List<LocalDateTime> endDates) {
		for (String line : lines) {
			Matcher startMatcher = startPattern.matcher(line);
			Matcher endMatcher = endPattern.matcher(line);
			if (startMatcher.matches()) {
				String dateString = startMatcher.group(1);

				startDates.add(LocalDateTime.parse(dateString, (DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
			} else if (endMatcher.matches()) {
				String dateString = endMatcher.group(1);

				endDates.add(LocalDateTime.parse(dateString, (DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
			}
		}
	}

}
