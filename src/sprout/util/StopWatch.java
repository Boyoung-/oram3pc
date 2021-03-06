package sprout.util;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

// TODO: clean code

public class StopWatch implements Serializable {
	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	public String task = null;
	public long elapsedWallClockTime;
	public long elapsedCPUTime;
	private boolean running;
	private boolean strict = true;

	private long startWallClockTime;
	private long startCPUTime;

	public static final int to_ms = 1000000; // from nanoseconds to milliseconds
	
	private long getCPUTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? bean
				.getCurrentThreadCpuTime() : 0L;
	}

	public StopWatch() {
		elapsedWallClockTime = 0;
		elapsedCPUTime = 0;
		running = false;
		strict = false;
	}

	public StopWatch(String t) {
		task = t;
		elapsedWallClockTime = 0;
		elapsedCPUTime = 0;
		running = false;
	}

	public StopWatch(StopWatch sw) {
		task = sw.task;
		elapsedWallClockTime = sw.elapsedWallClockTime;
		elapsedCPUTime = sw.elapsedCPUTime;
		running = sw.running;
		strict = sw.strict;
	}

	public StopWatch(String t, boolean strict) {
		task = t;
		elapsedWallClockTime = 0;
		elapsedCPUTime = 0;
		running = false;
		this.strict = strict;
	}

	public void start() {if (running) {
			System.err.println(task + ": StopWatch is alrealdy running.");
			return;
		}

		running = true;
		startWallClockTime = System.nanoTime();
		startCPUTime = getCPUTime();
	}

	public void stop() {if (!running) {
			System.err.println(task + ":StopWatch is not running.");
			return;
		}

		running = false;
		elapsedCPUTime += getCPUTime() - startCPUTime;
		elapsedWallClockTime += System.nanoTime() - startWallClockTime;
	}

	public void reset() {if (running) {
			System.err.println(task
					+ ": StopWatch is still running. Please stop first.");
			return;
		}

		elapsedWallClockTime = 0;
		elapsedCPUTime = 0;
	}

	public void divide(int n) {
		elapsedWallClockTime /= n;
		elapsedCPUTime /= n;
	}

	public StopWatch add_mut(StopWatch sw) {
		if (task != null && sw.task != null && !task.equals(sw.task) && (strict || sw.strict)) {
			System.err.println("Warning: addition between different task!");
		}

		elapsedWallClockTime = elapsedWallClockTime + sw.elapsedWallClockTime;
		elapsedCPUTime = elapsedCPUTime + sw.elapsedCPUTime;
		return this;
	}

	public StopWatch add(StopWatch sw) {
		return (new StopWatch(this)).add_mut(sw);
	}

	public StopWatch subtract_mut(StopWatch sw) {
		if (task != null && sw.task != null && !task.equals(sw.task) && (strict || sw.strict)) {
			System.err.println("Warning: subtraction between different task!");
		}

		elapsedWallClockTime = elapsedWallClockTime - sw.elapsedWallClockTime;
		elapsedCPUTime = elapsedCPUTime - sw.elapsedCPUTime;
		return this;
	}
	
	public StopWatch subtract(StopWatch sw) {
		return (new StopWatch(this)).subtract_mut(sw);
	}

	@Override
	public String toString() {
		String out = " - Wall clock time(ms): " + elapsedWallClockTime / to_ms
				+ "\n - CPU time(ms): " + elapsedCPUTime / to_ms;
		if (task == null)
			return out;
		return "Task: " + task + "\n" + out;
	}

	public String toTab() {
		String out = "\n WC(ms):\t" + (elapsedWallClockTime / to_ms)
				+ "\nCPU(ms):\t" + (elapsedCPUTime / to_ms);
		if (task == null)
			out = "Task: (un-specified)" + out;
		else
			out = "Task: " + task + out;
		return out;

	}

	public String toCSV() {
		String csv = task + ",Wall clock(ms)," + elapsedWallClockTime / to_ms
				+ "\n,CPU(ms)," + elapsedCPUTime / to_ms;
		return csv;
	}

	public String toNumber() {
		String num = elapsedWallClockTime + "\n" + elapsedCPUTime;
		return num;
	}

	public String toMS() {
		String num = elapsedWallClockTime / to_ms + "\n" + elapsedCPUTime
				/ to_ms;
		return num;
	}
}
