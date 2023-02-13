package blarnix.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import blarnix.PlayerTimeLimit;
import blarnix.configs.MainConfigManager;
import blarnix.configs.others.TimeLimit;
import blarnix.model.TimeLimitPlayer;
import blarnix.utils.BypassTimes;
import blarnix.utils.UtilsTime;

public class PlayerManager {

    public ArrayList<TimeLimitPlayer> players;
	private PlayerTimeLimit plugin;

	public PlayerManager(PlayerTimeLimit plugin) {
		this.plugin = plugin;
		this.players = new ArrayList<TimeLimitPlayer>();
	}

	public ArrayList<TimeLimitPlayer> getPlayers() {
		return players;
	}

	public void setPlayers(ArrayList<TimeLimitPlayer> players) {
		this.players = players;
	}

	public TimeLimitPlayer getPlayerByUUID(String uuid) {
		for(TimeLimitPlayer p : players) {
			if(p.getUuid().equals(uuid)) {
				return p;
			}
		}
		return null;
	}

	public TimeLimitPlayer getPlayerByName(String name) {
		for(TimeLimitPlayer p : players) {
			if(p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

    public ArrayList<TimeLimitPlayer> getAllPlayers(){  // return the ArrayList of player objects, couldn't be simpler
        return players;
    }

	public TimeLimitPlayer createPlayer(Player player) {
		TimeLimitPlayer p = new TimeLimitPlayer(player.getUniqueId().toString(),player.getName());
		players.add(p);
		FileConfiguration config = plugin.getConfig();
		p.setMessageEnabled(config.getBoolean("information_message_enabled_by_default"));

		return p;
	}

	public void checkUserTime(final Player player,TimeLimitPlayer p) {
		if(hasTimeLeft(p)) {
			return;
		}

		//The player has already completed their time
		final FileConfiguration messages = plugin.getMessages();
		new BukkitRunnable() {
			public void run() {
				MainConfigManager mainConfig = plugin.getConfigsManager().getMainConfigManager();
				if(mainConfig.isWorldWhitelistEnabled()) {
					worldWhitelistSystemKick(player);
					return;
				}

				List<String> msg = messages.getStringList("kickMessage");
				String finalMessage = "";
				for(String line : msg) {
					finalMessage = finalMessage+line+"\n";
				}
				finalMessage = MessagesManager.getMessageColor(finalMessage);
				player.kickPlayer(finalMessage);
			}
		}.runTask(plugin);
	}

	public void resetPlayers() {
		for(TimeLimitPlayer p : players) {  // iterate over a list of players in the P object
			p.setCurrentTime(0);
		}
	}

	public boolean hasTimeLeft(TimeLimitPlayer p) {
		int currentTime = p.getCurrentTime();
		int timeLimit = getTimeLimitPlayer(p.getPlayer());
		if(currentTime < timeLimit || timeLimit == 0 || BypassTimes.isBypassNow(plugin)) {
			return true;
		}
		return false;
	}

	public int getTimeLimitPlayer(Player player) {
		int timeReal = 0;
		ArrayList<TimeLimit> timeLimits = plugin.getConfigsManager().getMainConfigManager().getTimeLimits();
		for(TimeLimit timeLimit : timeLimits) {

			String name = timeLimit.getName();
			int time = timeLimit.getTime();
			if(name.equals("default")) {
				timeReal = time;
				continue;
			}
			if(name.equals("op")) {
				if(player.isOp()) {
					return time;
				}
			}

			String permission = "playertimelimit.limit."+name;
			if(player.hasPermission(permission)) {
				timeReal = time;
			}
		}

		return timeReal;
	}

	public String getTimeLeft(TimeLimitPlayer p,int timeLimit) {
		MessagesManager msgManager = plugin.getMessagesManager();
		int remainingTime = timeLimit-p.getCurrentTime();
		String timeString = "";
		if(timeLimit == 0) {
			timeString = msgManager.getTimeInfinite();
		}else {
			timeString = UtilsTime.getTime(remainingTime,msgManager);
		}
		return timeString;
	}

	public void worldWhitelistSystemKick(Player player) {
		MainConfigManager mainConfig = plugin.getConfigsManager().getMainConfigManager();
		String coordinates = mainConfig.getWorldWhitelistTeleportCoordinates();
		try {
			String[] sep = coordinates.split(";");
			World world = Bukkit.getWorld(sep[0]);
			double x = Double.valueOf(sep[1]);
			double y = Double.valueOf(sep[2]);
			double z = Double.valueOf(sep[3]);
			float yaw = Float.valueOf(sep[4]);
			float pitch = Float.valueOf(sep[5]);

			player.teleport(new Location(world,x,y,z,yaw,pitch));

			FileConfiguration messages = plugin.getMessages();
			List<String> msg = messages.getStringList("kickMessage");
			for(String m : msg) {
				player.sendMessage(MessagesManager.getMessageColor(m));
			}
		}catch(Exception e) {
			player.sendMessage(PlayerTimeLimit.namePlugin+" &cError! Impossible to teleport &7"+player.getName()
			+" &cto this coordinates: &7"+coordinates);
		}
	}

	public void takeTime(TimeLimitPlayer p,int time) {
		int timeLimit = getTimeLimitPlayer(p.getPlayer());
		if(timeLimit == 0) {
			return;
		}
		p.takeTime(time);
		int remainingTime = timeLimit-p.getCurrentTime();
		if(remainingTime <= 0) {
			p.setCurrentTime(timeLimit);
		}
	}

	public void addTime(TimeLimitPlayer p,int time) {
		int timeLimit = getTimeLimitPlayer(p.getPlayer());
		if(timeLimit == 0) {
			return;
		}
		p.addTime(time);
	}
}
