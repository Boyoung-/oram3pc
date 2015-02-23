package sprout.util;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

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

	static final int convert = 1000000; // from nanoseconds to milliseconds
	
	private boolean parallelTestSwitch = true;

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

	public void start() {
		if (!parallelTestSwitch)
			return;
		
		if (running) {
			System.err.println(task + ": StopWatch is alrealdy running.");
			return;
		}

		running = true;
		startWallClockTime = System.nanoTime();
		startCPUTime = getCPUTime();
	}

	public void stop() {
		if (!parallelTestSwitch)
			return;
		
		if (!running) {
			System.err.println(task + ":StopWatch is not running.");
			return;
		}

		running = false;
		elapsedCPUTime += getCPUTime() - startCPUTime;
		elapsedWallClockTime += System.nanoTime() - startWallClockTime;
	}

	public void reset() {
		if (!parallelTestSwitch)
			return;
		
		if (running) {
			System.err.println(task
					+ ": StopWatch is still running. Please stop first.");
			return;
		}

		elapsedWallClockTime = 0;
		elapsedCPUTime = 0;
	}

	public StopWatch add_mut(StopWatch sw) {
		if (task == null) {
			; //TODO: fix
		}
		else if (!task.equals(sw.task) && (strict || sw.strict)) {
			System.out.println("Warning: addition between different task!");
		}

		elapsedWallClockTime = elapsedWallClockTime + sw.elapsedWallClockTime;
		elapsedCPUTime = elapsedCPUTime + sw.elapsedCPUTime;
		return this;
	}

	public StopWatch add(StopWatch sw) {
		return (new StopWatch(this)).add_mut(sw);
	}

	public StopWatch subtract(StopWatch sw) {
		if (!task.equals(sw.task)) {
			System.out.println("Warning: subtraction between different task!");
		}

		StopWatch out = new StopWatch(task);
		out.elapsedWallClockTime = elapsedWallClockTime
				- sw.elapsedWallClockTime;
		out.elapsedCPUTime = elapsedCPUTime - sw.elapsedCPUTime;
		return out;
	}

	public void divide(int n) {
		elapsedWallClockTime /= n;
		elapsedCPUTime /= n;
	}

	@Override
	public String toString() {
		String out = " - Wall clock time(ms): " + elapsedWallClockTime
				/ convert + "\n - CPU time(ms): " + elapsedCPUTime / convert;
		if (task == null)
			return out;
		return "Task: " + task + "\n" + out;
	}

	public String toCSV() {
		String csv = task + ",Wall clock(ms)," + elapsedWallClockTime / convert
				+ "\n,CPU(ms)," + elapsedCPUTime / convert;
		return csv;
	}
	
	public String toNumber() {
		String num = elapsedWallClockTime + "\n" + elapsedCPUTime;
		return num;
	}
	
	public String afterConversion() {
		String num = elapsedWallClockTime / convert+ "\n" + elapsedCPUTime / convert;
		return num;
	}

	private long getCPUTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? bean
				.getCurrentThreadCpuTime() : 0L;
	}
}
