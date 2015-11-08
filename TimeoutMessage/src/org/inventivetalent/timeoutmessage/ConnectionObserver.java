package org.inventivetalent.timeoutmessage;

public abstract class ConnectionObserver<TPlayer> extends ObservedRunnable {

	private TPlayer player;
	public  int     ping;
	public int equalPings = 0;

	public ConnectionObserver(TPlayer player) {
		this.player = player;
	}

	protected abstract int getPing(TPlayer player);

	@Override
	public void run() {
		if (cancelled) { return; }
		int lastPing = this.ping;
		this.ping = getPing(this.player);
		if (lastPing == this.ping) {
			equalPings++;
		} else {
			equalPings = 0;
		}
	}
}
