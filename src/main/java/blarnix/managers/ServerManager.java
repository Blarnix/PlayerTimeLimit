package blarnix.managers;

import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import blarnix.PlayerTimeLimit;
import blarnix.configs.MainConfigManager;
import blarnix.utils.UtilsTime;

public class ServerManager {

	private PlayerTimeLimit plugin;

	public ServerManager(PlayerTimeLimit plugin) {
		this.plugin = plugin;
	}

	public void saveDataTime() {
		FileConfiguration config = plugin.getConfig();

		//The millis of the next restart time is saved in case the server is closed when it reaches the restart time
		String resetTimeHour = plugin.getConfigsManager().getMainConfigManager().getResetTime();
		long finalMillis = UtilsTime.getNextResetMillis(resetTimeHour);

		config.set("Data.next_millis_reset", finalMillis);
		plugin.saveConfig();
	}

	public void executeDataTime() {
		FileConfiguration config = plugin.getConfig();

		// checks if the millis obtained is less than the current one, if so, the players' times are restarted
		if(config.contains("Data.next_millis_reset")) {
			long millisReset = config.getLong("Data.next_millis_reset");
			if(System.currentTimeMillis() > millisReset) {
				plugin.getPlayerManager().resetPlayers();
			}
		}
	}

	public String getRemainingTimeForTimeReset() {
		String resetTimeHour = plugin.getConfigsManager().getMainConfigManager().getResetTime();
		long finalMillis = UtilsTime.getNextResetMillis(resetTimeHour);
		long remainingMillis = finalMillis-System.currentTimeMillis();
		long seconds = remainingMillis/1000;
		return UtilsTime.getTime(seconds, plugin.getMessagesManager());
	}

	public boolean isValidWorld(World world) {
		MainConfigManager mainConfig = plugin.getConfigsManager().getMainConfigManager();
		if(!mainConfig.isWorldWhitelistEnabled()) {
			return true;
		}

		List<String> worlds = mainConfig.getWorldWhitelistWorlds();
		if(worlds.contains(world.getName())) {
			return true;
		}

		return false;
	}
}
