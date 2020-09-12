package utils;

import java.util.*;
import java.util.Map.*;

public class Profiler {
	public static class LogEntry {
		public String text;
		public long duration;
		public LogEntry(String text, long duration) {
			this.text = text;
			this.duration = duration;
		}
		
	}
	private static HashMap<String, Long> timings = new HashMap<>();
	private static LinkedList<LogEntry> entries = new LinkedList<>();
	private static long startTime;
	private static long startTime2;
	public static void start() {
		startTime = System.currentTimeMillis();
	}
	public static void end(String text) {
		long delta = System.currentTimeMillis() - startTime;
		entries.add(new LogEntry(text, delta));
	}
	public static void start2() {
		startTime2 = System.currentTimeMillis();
	}
	public static void end2(String text) {
		long delta = System.currentTimeMillis() - startTime2;
		if(!timings.containsKey(text)) {
			timings.put(text, delta);
		}
		else {
			timings.put(text, timings.get(text) + delta);
		}
	}
	
	public static void printLog() {
		System.out.println("action: duration");
		for(LogEntry e : entries) {
			System.out.println(e.text + ": " + e.duration);
		}
		System.out.println("table format:");
		for(LogEntry e : entries) {
			System.out.print(e.text + ",");
		}
		System.out.println();
		for(LogEntry e : entries) {
			System.out.print(e.duration + ",");
		}
		System.out.println();
		
		String categories = "";
		String times = "";
		for(Entry<String, Long> entry : timings.entrySet()) {
			categories += entry.getKey() + ",";
			times += entry.getValue() + ",";
		}
		System.out.println("Cumulative table:");
		System.out.println(categories);
		System.out.println(times);
	}
}
