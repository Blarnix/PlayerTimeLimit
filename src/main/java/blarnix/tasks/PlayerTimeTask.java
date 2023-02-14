package blarnix.tasks;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import blarnix.PlayerTimeLimit;
import blarnix.configs.MainConfigManager;
import blarnix.configs.others.Notification;
import blarnix.libs.actionbar.ActionBarAPI;
import blarnix.libs.bossbar.BossBarAPI;
import blarnix.managers.MessagesManager;
import blarnix.managers.PlayerManager;
import blarnix.managers.ServerManager;
import blarnix.model.TimeLimitPlayer;
import blarnix.utils.BypassTimes;

public class PlayerTimeTask {

	private PlayerTimeLimit plugin;
	public PlayerTimeTask(PlayerTimeLimit plugin) {
		this.plugin = plugin;
	}

	public void start() {
		new BukkitRunnable() {
			public void run() {
				execute();
			}

		}.runTaskTimer(plugin, 0L, 20L);
	}

	public void execute() {
		new BukkitRunnable() {
			public void run() {
				MainConfigManager mainConfig = plugin.getConfigsManager().getMainConfigManager();

				boolean actionBar = mainConfig.isActionBar();
				boolean bossBar = mainConfig.isBossBar();
				String bossBarColor = mainConfig.getBossBarColor();
				String bossBarStyle = mainConfig.getBossBarStyle();

				PlayerManager playerManager = plugin.getPlayerManager();
				ServerManager serverManager = plugin.getServerManager();
				for(TimeLimitPlayer p : playerManager.getPlayers()) {
					Player player = p.getPlayer();
					if(player != null) {
						//if connected
						World world = player.getWorld();
						if(!serverManager.isValidWorld(world)) {
							p.eliminarBossBar();
							continue;
						}

						if(!BypassTimes.isBypassNow(plugin))
							p.increaseTime();

						sendActionBar(player,p,actionBar);
						sendBossBar(player,p,bossBar,bossBarColor,bossBarStyle);
						sendNotification(player,p,mainConfig);
						playerManager.checkUserTime(player, p);
					}
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public void sendNotification(Player player,TimeLimitPlayer p,MainConfigManager mainConfig) {
		PlayerManager playerManager = plugin.getPlayerManager();
		int timeLimit = playerManager.getTimeLimitPlayer(player);
		int remainingTime = timeLimit-p.getCurrentTime();

		Notification notification = mainConfig.getNotificationAtTime(remainingTime);
		if(notification == null) {
			return;
		}

		List<String> message = notification.getMessage();
		if(message != null) {
			for(String line : message) {
				player.sendMessage(MessagesManager.getMessageColor(line));
			}
		}
	}

	public void sendActionBar(Player player,TimeLimitPlayer p,boolean enabled) {
		if(!p.isMessageEnabled()) {
			return;
		}
		if(!enabled) {
			return;
		}

		PlayerManager playerManager = plugin.getPlayerManager();
		int timeLimit = playerManager.getTimeLimitPlayer(player);
		MessagesManager msgManager = plugin.getMessagesManager();
		String actionBarMessage = msgManager.getActionBarMessage();
		String timeString = playerManager.getTimeLeft(p, timeLimit);

		actionBarMessage = actionBarMessage.replace("%time%", timeString);

		ActionBarAPI.sendActionBar(player, actionBarMessage);
	}

	public void sendBossBar(Player player,TimeLimitPlayer p,boolean enabled,String bossBarColor,String bossBarStyle) {
		if(!p.isMessageEnabled()) {
			p.eliminarBossBar();
			return;
		}
		if(!enabled) {
			p.eliminarBossBar();
			return;
		}

		if(Bukkit.getVersion().contains("1.8")) {
			return;
		}

		PlayerManager playerManager = plugin.getPlayerManager();
		int timeLimit = playerManager.getTimeLimitPlayer(player);
		MessagesManager msgManager = plugin.getMessagesManager();
		String bossBarMessage = msgManager.getBossBarMessage();
		int remainingTime = timeLimit-p.getCurrentTime();
		String timeString = playerManager.getTimeLeft(p, timeLimit);
		bossBarMessage = bossBarMessage.replace("%time%", timeString);

		BossBar bossBar = p.getBossBar();
		if(bossBar == null) {
			//Create BossBar
			String title = bossBarMessage;
			BarColor color = BarColor.valueOf(bossBarColor);
			BarStyle style = BarStyle.valueOf(bossBarStyle);
			bossBar = BossBarAPI.create(player, title, color, style);
			p.setBossBar(bossBar);
		}
		bossBar.setTitle(MessagesManager.getMessageColor(bossBarMessage));
		double ratio = 1;
		if(timeLimit != 0) {
			ratio = (double) remainingTime/timeLimit;
		}
		if(ratio < 0) {
			ratio = 0;
		}else if(ratio > 1) {
			ratio = 1;
		}
		bossBar.setProgress(ratio);
	}
}
