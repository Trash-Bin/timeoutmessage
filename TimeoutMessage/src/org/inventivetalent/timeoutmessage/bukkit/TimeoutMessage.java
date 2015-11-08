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
