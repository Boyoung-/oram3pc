package sprout.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class StopWatch
{
	private String task = null;
	private long elapsedWallClockTime;
	private long elapsedCPUTime;
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
			System.out.println("StopWatch is alrealdy running.");
			return;
		}
		
		running = true;		
		startWallClockTime = System.nanoTime();		
		startCPUTime = getCPUTime();
	}
	
	public void stop() {
		if (!running) {
			System.out.println("StopWatch is not running.");
			return;
		}
		
		running = false;		
		elapsedCPUTime += getCPUTime() - startCPUTime;
		elapsedWallClockTime += System.nanoTime() - startWallClockTime;
	}
	
	public void reset() {
		if (running) {
			System.out.println("StopWatch is still running. Please stop first.");
			return;
		}
		
		elapsedWallClockTime = 0;
		elapsedCPUTime = 0;
	}
	
	@Override
	public String toString() {
		int convert = 1000000; 
		String out = "Wall clock time(ms): " + elapsedWallClockTime/convert +
				"\nCPU time: " + elapsedCPUTime/convert;
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
