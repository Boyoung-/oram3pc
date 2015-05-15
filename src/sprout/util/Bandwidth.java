package sprout.util;

import java.io.Serializable;

public class Bandwidth implements Serializable {
	private static final long serialVersionUID = 1L;

	public String task;
	public int bandwidth;
	
	// TODO: what is this?
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
	
	public void reset() {
		bandwidth = 0;
	}

	public synchronized void add(int b) {
		bandwidth += b;
	}

	public void divide(int n) {
		bandwidth /= n;
	}

	public void clear() {
		bandwidth = 0;
	}

	public Bandwidth add_mut(Bandwidth b) {
		if (task != null && b.task != null && !task.equals(b.task) && (b.strict || strict))
			System.err.println("Warning: adding bandwidth of " + task + " and "
					+ b.task);

		bandwidth = bandwidth + b.bandwidth;
		return this;
	}

	public Bandwidth add(Bandwidth b) {
		return new Bandwidth(this).add_mut(b);
	}
	
	public String toTab() {
		return task + "(bytes):\t" + bandwidth;
	}

	@Override
	public String toString() {
		return task + ": " + bandwidth + " bytes";
	}
}
