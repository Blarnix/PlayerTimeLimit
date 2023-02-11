package blarnix.api;

import org.bukkit.entity.Player;

import blarnix.PlayerTimeLimit;
import blarnix.managers.PlayerManager;
import blarnix.model.TimeLimitPlayer;
import blarnix.utils.UtilsTime;

public class PlayerTimeLimitAPI {

	private static PlayerTimeLimit plugin;
	@SuppressWarnings("static-access")
	public PlayerTimeLimitAPI(PlayerTimeLimit plugin) {
		this.plugin = plugin;
	}

	public static String getTimeLeft(Player player) {
		PlayerManager playerManager = plugin.getPlayerManager();
		TimeLimitPlayer p = playerManager.getPlayerByUUID(player.getUniqueId().toString());
		int timeLimit = playerManager.getTimeLimitPlayer(player);
		return playerManager.getTimeLeft(p, timeLimit);
	}

	public static String getTotalTime(Player player) {
		PlayerManager playerManager = plugin.getPlayerManager();
		TimeLimitPlayer p = playerManager.getPlayerByUUID(player.getUniqueId().toString());
		return UtilsTime.getTime(p.getTotalTime(), plugin.getMessagesManager());
	}
}
