package blarnix.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import blarnix.PlayerTimeLimit;

public class DataSaveTask {

	private PlayerTimeLimit plugin;
	private boolean end;
	public DataSaveTask(PlayerTimeLimit plugin) {
		this.plugin = plugin;
		this.end = false;
	}

	public void end() {
		end = true;
	}

	public void start(int minutes) {  // takes minutes as input, converts to ticks, then runs a bukkit runnable every x ticks, then calls execute()
		long ticks = minutes*60*20;

		new BukkitRunnable() {
			public void run() {
				if(end) {
					this.cancel();
				}else {
					execute();
				}
			}

		}.runTaskTimerAsynchronously(plugin, 0L, ticks);
	}

	public void execute() { // saves all player data and server data
 		plugin.getConfigsManager().getPlayerConfigsManager().savePlayers();
		plugin.getServerManager().saveDataTime();
	}
}
