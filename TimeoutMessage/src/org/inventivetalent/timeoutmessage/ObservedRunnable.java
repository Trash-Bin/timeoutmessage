package org.inventivetalent.timeoutmessage;

public class ObservedRunnable implements Runnable {

	public boolean cancelled = false;
	public int     taskId    = -1;

	@Override
	public void run() {
		if (cancelled) { return; }
	}
}
