package org.inventivetalent.timeoutmessage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class PlayerManager<TPlugin, TPlayer> {

	private TPlugin plugin;

	private final Map<UUID, ConnectionObserver<TPlayer>> connectionObserverMap = new HashMap<>();

	public PlayerManager(TPlugin plugin) {
		this.plugin = plugin;
	}

	public abstract int schedule(TPlugin plugin, ObservedRunnable runnable, long delay, long interval);

	public abstract void cancelTask(TPlugin plugin, int taskId);

	public abstract UUID getUUID(TPlayer player);

	public abstract int getPing(TPlayer player);

	public void track(TPlayer player) {
		UUID uuid = getUUID(player);
		if (connectionObserverMap.containsKey(uuid)) {
			untrack(player);
		}
		ConnectionObserver<TPlayer> observer = new ConnectionObserver<TPlayer>(player) {
			@Override
			protected int getPing(TPlayer player) {
				return PlayerManager.this.getPing(player);
			}
		};
		observer.taskId = schedule(this.plugin, observer, 20, 20);

		connectionObserverMap.put(uuid, observer);
	}

	public ConnectionObserver<TPlayer> untrack(TPlayer player) {
		ConnectionObserver<TPlayer> observer = connectionObserverMap.remove(getUUID(player));
		if (observer != null) {
			observer.cancelled = true;
			cancelTask(this.plugin, observer.taskId);
		}
		return observer;
	}
}
