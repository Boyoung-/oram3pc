package sprout.util;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class StopWatch implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String task = null;
	public long elapsedWallClockTime;
	public long elapsedCPUTime;
	private boolean running;
	
	private long startWallClockTime;
	private long startCPUTime;
	
	public StopWatch() {
		elapsedWallClockTime = 0;
		elapsedCPUTime = 0;
		running = false;
	}
	
	public StopWatch(String t) {
		task = t;
		elapsedWallClockTime = 0;
		elapsedCPUTime = 0;
		running = false;
	}
	
	public void start() {
		if (running) {
			System.err.println(task + ": StopWatch is alrealdy running.");
			return;
		}
		
		running = true;		
		startWallClockTime = System.nanoTime();		
		startCPUTime = getCPUTime();
	}
	
	public void stop() {
		if (!running) {
			System.err.println(task + ":StopWatch is not running.");
			return;
		}
		
		running = false;		
		elapsedCPUTime += getCPUTime() - startCPUTime;
		elapsedWallClockTime += System.nanoTime() - startWallClockTime;
	}
	
	public void reset() {
		if (running) {
			System.err.println(task + ": StopWatch is still running. Please stop first.");
			return;
		}
		
		elapsedWallClockTime = 0;
		elapsedCPUTime = 0;
	}
	
	public StopWatch add(StopWatch sw) {
		if (task != sw.task) {
			System.out.println("StopWatch: different task!");
			return null;
		}
		
		StopWatch out = new StopWatch(task);
		out.elapsedWallClockTime = elapsedWallClockTime + sw.elapsedWallClockTime;
		out.elapsedCPUTime = elapsedCPUTime + sw.elapsedCPUTime;
		return out;
	}
	
	@Override
	public String toString() {
		int convert = 1000000; 
		String out = " - Wall clock time(ms): " + elapsedWallClockTime/convert +
				"\n - CPU time(ms): " + elapsedCPUTime/convert;
		if (task == null)
			return out;
		return "Task: " + task + "\n" + out;
	}
	
	private long getCPUTime() {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        bean.getCurrentThreadCpuTime( ) : 0L;
	}
}
