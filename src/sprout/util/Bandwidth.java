package sprout.util;

import java.io.Serializable;

public class Bandwidth implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public String task;
	public int bandwidth;
	public boolean active;
	
	public Bandwidth(String t) {
		task = t;
		bandwidth = 0;
		active = false;
	}
	
	public boolean isTask(String t) {
		if (task.equals(t))
			return true;
		return false;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void start() {
		if (active) {
			System.err.println("Bandwidth: " + task + " already active");
			return;
		}
		active = true;
	}
	
	public void stop() {
		if (!active) {
			System.err.println("Bandwidth: " + task + " not in use");
			return;
		}
		active = false;
	}
	
	public void reset() {
		if (active) {
			System.err.println("Bandwidth: " + task + " still in use");
			return;
		}
		bandwidth = 0;
	}
	
	public void add(int b) {
		if (!active) {
			System.err.println("Bandwidth: " + task + " not in use");
			return;
		}
		bandwidth += b;
	}
	
	public void divide(int n) {
		if (active) {
			System.err.println("Bandwidth: " + task + " still in use");
			return;
		}
		bandwidth /= n;
	}
	
	public Bandwidth add(Bandwidth b) {
		if (active) {
			System.err.println("Bandwidth: " + task + " still in use");
			return null;
		}
		if (b.active) {
			System.err.println("Bandwidth: " + b.task + " still in use");
			return null;
		}
		if (!task.equals(b.task))
			System.err.println("Warning: adding bandwidth of " + task + " and " + b.task);
		Bandwidth c = new Bandwidth(task);
		c.bandwidth = bandwidth + b.bandwidth;
		return c;
	}
}
