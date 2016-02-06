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

package org.inventivetalent.timeoutmessage.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import org.inventivetalent.timeoutmessage.ConnectionObserver;
import org.inventivetalent.timeoutmessage.ObservedRunnable;
import org.inventivetalent.timeoutmessage.PlayerManager;
import org.inventivetalent.timeoutmessage.bungee.event.PlayerTimeoutEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TimeoutMessage extends Plugin implements Listener {

	public PlayerManager<Plugin, ProxiedPlayer> playerManager;
	public int    threshold = 20;
	public String message   = "";
	public boolean disableQuit = true;

	@Override
	public void onEnable() {
		getProxy().getPluginManager().registerListener(this, this);

		saveDefaultConfig();
		threshold = getConfig().getInt("threshold");
		message = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message"));
		disableQuit = getConfig().getBoolean("disableQuitMessage");

		playerManager = new PlayerManager<Plugin, ProxiedPlayer>(this) {

			@Override
			public int schedule(Plugin plugin, ObservedRunnable runnable, long delay, long interval) {
				return getProxy().getScheduler().schedule(plugin, runnable, delay / 20, interval / 20, TimeUnit.SECONDS).getId();
			}

			@Override
			public void cancelTask(Plugin plugin, int taskId) {
				try {
					getProxy().getScheduler().cancel(taskId);
				} catch (Exception e) {
				}
			}

			@Override
			public UUID getUUID(ProxiedPlayer player) {
				return player.getUniqueId();
			}

			@Override
			public int getPing(ProxiedPlayer player) {
				return player.getPing();
			}
		};

		getProxy().getScheduler().runAsync(this, new Runnable() {
			@Override
			public void run() {
				try {
					MetricsLite metrics = new MetricsLite(TimeoutMessage.this);
					if (metrics.start()) {
						getLogger().info("Metrics started");
					}
				} catch (Exception e) {
				}
			}
		});
	}

	@EventHandler
	public void onJoin(PostLoginEvent event) {
		playerManager.track(event.getPlayer());
	}

	@EventHandler
	public void onQuit(PlayerDisconnectEvent event) {
		ConnectionObserver<ProxiedPlayer> observer = playerManager.untrack(event.getPlayer());
		if (observer != null) {
			if (observer.equalPings > threshold) {
				PlayerTimeoutEvent timeoutEvent = new PlayerTimeoutEvent(event.getPlayer(), observer.ping, observer.equalPings, this.message.replace("%player%", event.getPlayer().getName()));
				getProxy().getPluginManager().callEvent(timeoutEvent);

				if (timeoutEvent.getTimeoutMessage() != null && !timeoutEvent.getTimeoutMessage().isEmpty()) {
					getProxy().getConsole().sendMessage(ChatColor.stripColor(timeoutEvent.getTimeoutMessage()));
					for (ProxiedPlayer player : getProxy().getPlayers()) {
						player.sendMessage(timeoutEvent.getTimeoutMessage());
					}
				}
			}
		}
	}

	@EventHandler
	public void onKick(ServerKickEvent event) {
		playerManager.untrack(event.getPlayer());
	}

	private Configuration configuration;

	public Configuration getConfig() {
		if (configuration == null) {
			try {
				configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return configuration;
	}

	public boolean saveDefaultConfig() {
		try {
			getDataFolder().mkdirs();
			File config = new File(getDataFolder(), "config.yml");
			config.createNewFile();

			InputStream in = getResourceAsStream("config.yml");
			OutputStream out = new FileOutputStream(config);
			byte buf[] = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
