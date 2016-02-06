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
