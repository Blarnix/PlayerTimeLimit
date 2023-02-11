package blarnix.listeners;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import blarnix.PlayerTimeLimit;
import blarnix.configs.MainConfigManager;
import blarnix.managers.MessagesManager;
import blarnix.managers.PlayerManager;
import blarnix.model.TimeLimitPlayer;
import blarnix.utils.BypassTimes;

public class PlayerListener implements Listener{

	private PlayerTimeLimit plugin;
	public PlayerListener(PlayerTimeLimit plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPreJoin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();
		PlayerManager playerManager = plugin.getPlayerManager();
		TimeLimitPlayer p = playerManager.getPlayerByUUID(uuid);
		if(p != null) {
			//If you enter the server and the world whitelist is activated, you will not be removed when trying to enter the server
			if(plugin.getConfigsManager().getMainConfigManager().isWorldWhitelistEnabled()) {
				return;
			}

			int currentTime = p.getCurrentTime();
			int timeLimit = playerManager.getTimeLimitPlayer(player);
			if(currentTime >= timeLimit && timeLimit != 0 && !BypassTimes.isBypassNow(plugin)) {
				FileConfiguration messages = plugin.getMessages();
				List<String> msg = messages.getStringList("joinErrorMessage");
				String finalMessage = "";
				for(String line : msg) {
					finalMessage = finalMessage+line+"\n";
				}
				finalMessage = MessagesManager.getMessageColor(finalMessage);
				event.disallow(Result.KICK_OTHER, finalMessage);
			}
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		PlayerManager playerManager = plugin.getPlayerManager();
		TimeLimitPlayer p = playerManager.getPlayerByUUID(player.getUniqueId().toString());
		if(p == null) {
			p = playerManager.createPlayer(player);
		}
		p.setPlayer(player);
		p.setName(player.getName());

		FileConfiguration config = plugin.getConfig();
		if(config.getString("update_notification").equals("true")) {
			if(player.isOp() && !(plugin.version.equals(plugin.latestversion))){
				player.sendMessage(PlayerTimeLimit.namePlugin + ChatColor.RED +" There is a new version available. "+ChatColor.YELLOW+
		  				  "("+ChatColor.GRAY+plugin.latestversion+ChatColor.YELLOW+")");
				player.sendMessage(ChatColor.RED+"You can download it at: "+ChatColor.GREEN+"https://www.spigotmc.org/resources/96577/");
			}
		}
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		PlayerManager playerManager = plugin.getPlayerManager();
		TimeLimitPlayer p = playerManager.getPlayerByUUID(player.getUniqueId().toString());
		if(p != null) {
			p.setPlayer(null);
			p.eliminarBossBar();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTeleport(PlayerTeleportEvent event) {
		TeleportCause cause = event.getCause();
		if(cause.equals(TeleportCause.PLUGIN) || cause.equals(TeleportCause.COMMAND)) {
			Player player = event.getPlayer();
			PlayerManager playerManager = plugin.getPlayerManager();
			TimeLimitPlayer p = playerManager.getPlayerByUUID(player.getUniqueId().toString());
			if(p == null) {
				return;
			}

			MainConfigManager mainConfig = plugin.getConfigsManager().getMainConfigManager();
			if(!mainConfig.isWorldWhitelistEnabled()) {
				return;
			}

			//Check if the world where you are going is active
			World worldTo = event.getTo().getWorld();
			List<String> worlds = mainConfig.getWorldWhitelistWorlds();
			if(!worlds.contains(worldTo.getName())) {
				return;
			}

			//Check if a player's time is up
			if(!playerManager.hasTimeLeft(p)) {
				FileConfiguration messages = plugin.getMessages();
				List<String> msg = messages.getStringList("joinErrorMessage");
				for(String m : msg) {
					player.sendMessage(MessagesManager.getMessageColor(m));
				}
				event.setCancelled(true);
				return;
			}
		}
	}
}
