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

package org.inventivetalent.timeoutmessage.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.timeoutmessage.ConnectionObserver;
import org.inventivetalent.timeoutmessage.ObservedRunnable;
import org.inventivetalent.timeoutmessage.PlayerManager;
import org.inventivetalent.timeoutmessage.bukkit.event.PlayerTimeoutEvent;

import java.lang.reflect.Field;
import java.util.UUID;

public class TimeoutMessage extends JavaPlugin implements Listener {

	public PlayerManager<JavaPlugin, Player> playerManager;
	public int     threshold   = 20;
	public String  message     = "";
	public boolean disableQuit = true;

	static final Field EntityPlayer_ping = Reflection.getField(Reflection.getNMSClass("EntityPlayer"), "ping");

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
		threshold = getConfig().getInt("threshold");
		message = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message"));
		disableQuit = getConfig().getBoolean("disableQuitMessage");

		playerManager = new PlayerManager<JavaPlugin, Player>(this) {

			@Override
			public int schedule(JavaPlugin javaPlugin, ObservedRunnable runnable, long delay, long interval) {
				return Bukkit.getScheduler().runTaskTimer(javaPlugin, runnable, delay, interval).getTaskId();
			}

			@Override
			public void cancelTask(JavaPlugin javaPlugin, int taskId) {
				try {
					Bukkit.getScheduler().cancelTask(taskId);
				} catch (Exception e) {
				}
			}

			@Override
			public UUID getUUID(Player player) {
				return player.getUniqueId();
			}

			@Override
			public int getPing(Player player) {
				try {
					return (int) EntityPlayer_ping.get(Reflection.getHandle(player));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			}
		};

		try {
			MetricsLite metrics = new MetricsLite(this);
			if (metrics.start()) {
				getLogger().info("Metrics started");
			}
		} catch (Exception e) {
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		playerManager.track(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		ConnectionObserver<Player> observer = playerManager.untrack(event.getPlayer());
		if (observer != null) {
			if (observer.equalPings > threshold) {
				if (disableQuit) { event.setQuitMessage(""); }

				PlayerTimeoutEvent timeoutEvent = new PlayerTimeoutEvent(event.getPlayer(), observer.ping, observer.equalPings, this.message.replace("%player%", event.getPlayer().getName()));
				Bukkit.getPluginManager().callEvent(timeoutEvent);

				if (timeoutEvent.getTimeoutMessage() != null && !timeoutEvent.getTimeoutMessage().isEmpty()) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(timeoutEvent.getTimeoutMessage()));
					for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(timeoutEvent.getTimeoutMessage());
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onKick(PlayerKickEvent event) {
		playerManager.untrack(event.getPlayer());
	}

}
