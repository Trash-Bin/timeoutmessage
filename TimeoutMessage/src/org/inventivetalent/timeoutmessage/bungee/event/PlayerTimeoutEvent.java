/*
 * Copyright 2015-2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

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
