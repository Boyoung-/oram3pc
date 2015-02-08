package sprout.util;

import java.io.Serializable;

public class Bandwidth implements Serializable {
	private static final long serialVersionUID = 1L;

	public String task;
	public int bandwidth;
	public boolean active = false;
	public boolean strict = true;

	public Bandwidth(String t) {
		task = t;
		bandwidth = 0;
	}

	public Bandwidth(String t, boolean strict) {
		task = t;
		bandwidth = 0;
		this.strict = strict;
	}

	public Bandwidth(Bandwidth b) {
		task = b.task;
		bandwidth = b.bandwidth;
		strict = b.strict;
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
			System.err.println("Bandwidth.start: " + task + " already active");
			return;
		}
		active = true;
	}

	public void stop() {
		if (!active) {
			System.err.println("Bandwidth.stop: " + task + " not in use");
			return;
		}
		active = false;
	}

	public void reset() {
		if (active) {
			System.err.println("Bandwidth.reset: " + task + " still in use");
			return;
		}
		bandwidth = 0;
	}

	public void add(int b) {
		if (!active) {
			System.err.println("Bandwidth.add: " + task + " not in use");
			return;
		}
		bandwidth += b;
	}

	public void divide(int n) {
		if (active) {
			System.err.println("Bandwidth.divide: " + task + " still in use");
			return;
		}
		bandwidth /= n;
	}

	/*
	 * This is slightly more efficient version of add, but mutates the current
	 * bandwidth
	 */
	public Bandwidth add_mut(Bandwidth b) {
		if (active) {
			System.err.println("Bandwidth.add2: " + task + " still in use");
			return null;
		}
		if (b.active) {
			System.err.println("Bandwidth.add2: " + b.task + " still in use");
			return null;
		}
		if (task != null && !task.equals(b.task) && (b.strict || strict))
			System.err.println("Warning: adding bandwidth of " + task + " and "
					+ b.task);

		bandwidth = bandwidth + b.bandwidth;
		return this;
	}

	public Bandwidth add(Bandwidth b) {
		return new Bandwidth(this).add_mut(b);
	}

	public void clear() {
		bandwidth = 0;
	}

	@Override
	public String toString() {
		return task + ": " + bandwidth + " bytes";
	}
}
