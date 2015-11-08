package org.inventivetalent.timeoutmessage.bungee.event;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event called when a player times out
 */
public class PlayerTimeoutEvent extends Event {

	private ProxiedPlayer player;
	private int           lastPing;
	private int           equalPings;
	private String        message;

	public PlayerTimeoutEvent(ProxiedPlayer player, int lastPing, int equalPings, String message) {
		this.player = player;
		this.lastPing = lastPing;
		this.equalPings = equalPings;
		this.message = message;
	}

	/**
	 * @return The {@link ProxiedPlayer} involved in the event
	 */
	public ProxiedPlayer getPlayer() {
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

}
