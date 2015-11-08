package org.inventivetalent.timeoutmessage.bukkit.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a player times out
 */
public class PlayerTimeoutEvent extends Event {

	private Player player;
	private int    lastPing;
	private int    equalPings;
	private String message;

	public PlayerTimeoutEvent(Player player, int lastPing, int equalPings, String message) {
		this.player = player;
		this.lastPing = lastPing;
		this.equalPings = equalPings;
		this.message = message;
	}

	/**
	 * @return The {@link Player} involved in the event
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @return The last known ping value of the player
	 */
	public int getLastPing() {
		return lastPing;
	}

	/**
	 * @return The amount of equal pings tracked
	 */
	public int getEqualPingCount() {
		return equalPings;
	}

	/**
	 * @return The message sent
	 */
	public String getTimeoutMessage() {
		return message;
	}

	/**
	 * @param message The new message
	 */
	public void setTimeoutMessage(String message) {
		this.message = message;
	}

	private static HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
