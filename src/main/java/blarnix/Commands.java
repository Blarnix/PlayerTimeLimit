package blarnix;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import blarnix.managers.MessagesManager;
import blarnix.managers.PlayerManager;
import blarnix.model.TimeLimitPlayer;
import blarnix.utils.UtilsTime;




public class Commands implements CommandExecutor {

	private PlayerTimeLimit plugin;
	public Commands(PlayerTimeLimit plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
	   FileConfiguration messages = plugin.getMessages();
	   MessagesManager msgManager = plugin.getMessagesManager();
	   if (!(sender instanceof Player)){
		   if(args.length >= 1) {
			   if(args[0].equalsIgnoreCase("reload")) {
				   reload(args,sender,messages,msgManager);
			   }else if(args[0].equalsIgnoreCase("resettime")) {
				   resettime(args,sender,messages,msgManager);
			   }else if(args[0].equalsIgnoreCase("taketime")) {
				   taketime(args,sender,messages,msgManager);
			   }else if(args[0].equalsIgnoreCase("addtime")) {
				   addtime(args,sender,messages,msgManager);
			   }
		   }

		   return false;
	   }
	   Player jugador = (Player)sender;
	   boolean hasPermissions = false;
	   if(jugador.isOp() || jugador.hasPermission("playertimelimit.admin")) {
		   hasPermissions = true;
	   }
	   if(args.length >= 1) {
		   if(jugador.hasPermission("playertimelimit.command."+args[0].toLowerCase())) {
			   hasPermissions = true;
		   }
		   if(!hasPermissions) {
			   msgManager.sendMessage(sender, messages.getString("noPermissions"), true);
			   return true;
		   }

		   if(args[0].equalsIgnoreCase("reload")) {
			   reload(args,sender,messages,msgManager);
		   }else if(args[0].equalsIgnoreCase("message")) {
			   message(args,jugador,messages,msgManager);
		   }else if(args[0].equalsIgnoreCase("info")) {
			   info(args,jugador,messages,msgManager);
		   }else if(args[0].equalsIgnoreCase("check")) {
			   check(args,jugador,messages,msgManager);
		   }else if(args[0].equalsIgnoreCase("resettime")) {
			   resettime(args,jugador,messages,msgManager);
		   }else if(args[0].equalsIgnoreCase("taketime")) {
			   taketime(args,jugador,messages,msgManager);
		   }else if(args[0].equalsIgnoreCase("addtime")) {
			   addtime(args,jugador,messages,msgManager);
		   }
		   else {
			   help(sender);
		   }
	   }else {
		   if(hasPermissions) {
			   help(sender);
		   }else {
			   msgManager.sendMessage(sender, messages.getString("noPermissions"), true);
		   }
	   }

	   return true;

	}

	public void help(CommandSender sender) {
		sender.sendMessage(MessagesManager.getMessageColor("&c&m                                                                    "));
		sender.sendMessage(MessagesManager.getMessageColor("      &b&lPlayerTime&c&lLimit &eCommands"));
		sender.sendMessage(MessagesManager.getMessageColor(" "));
		sender.sendMessage(MessagesManager.getMessageColor("&8- &c/ptl message &7Enables or disables the time limit information message for yourself."));
		sender.sendMessage(MessagesManager.getMessageColor("&8- &c/ptl info &7Checks the remaining time for playtimes reset."));
		sender.sendMessage(MessagesManager.getMessageColor("&8- &c/ptl check <player> &7Checks player time left and total time."));
		sender.sendMessage(MessagesManager.getMessageColor("&8- &c/ptl resettime <player> &7Resets playtime for a player."));
        sender.sendMessage(MessagesManager.getMessageColor("&8- &c/ptl resetalltime &7Resets playtime for all players."));
		sender.sendMessage(MessagesManager.getMessageColor("&8- &c/ptl addtime <player> <time> &7Adds playtime to a player."));
		sender.sendMessage(MessagesManager.getMessageColor("&8- &c/ptl taketime <player> <time> &7Takes playtime from a player."));
		sender.sendMessage(MessagesManager.getMessageColor("&8- &c/ptl reload &7Reloads the config."));
		sender.sendMessage(MessagesManager.getMessageColor(" "));
		sender.sendMessage(MessagesManager.getMessageColor("&c&m                                                                    "));
	}

	public void reload(String[] args,CommandSender sender,FileConfiguration messages,MessagesManager msgManager) {
		plugin.reloadConfigs();
		msgManager.sendMessage(sender, messages.getString("commandReload"), true);
	}

	public void message(String[] args,Player player,FileConfiguration messages,MessagesManager msgManager) {
		// /ptl message
		TimeLimitPlayer p = plugin.getPlayerManager().getPlayerByUUID(player.getUniqueId().toString());
		boolean messagesEnabled = p.isMessageEnabled();
		if(messagesEnabled) {
			msgManager.sendMessage(player, messages.getString("messageDisabled"), true);
		}else {
			msgManager.sendMessage(player, messages.getString("messageEnabled"), true);
		}
		p.setMessageEnabled(!messagesEnabled);
	}

	public void info(String[] args,Player player,FileConfiguration messages,MessagesManager msgManager) {
		// /ptl info
		String timeReset = plugin.getConfigsManager().getMainConfigManager().getResetTime();
		String remaining = plugin.getServerManager().getRemainingTimeForTimeReset();

		List<String> msg = messages.getStringList("infoCommandMessage");
		for(String m : msg) {
			player.sendMessage(MessagesManager.getMessageColor(m.replace("%reset_time%", timeReset)
					.replace("%remaining%", remaining)));
		}
	}
	public void check(String[] args,Player player,FileConfiguration messages,MessagesManager msgManager) {
		// /ptl check <player>
		TimeLimitPlayer p = null;
		if(args.length == 1) {
			p = plugin.getPlayerManager().getPlayerByUUID(player.getUniqueId().toString());
		}else {
			if(player.isOp() || player.hasPermission("playertimelimit.admin")
					|| player.hasPermission("playertimelimit.command.check.others")) {
				p = plugin.getPlayerManager().getPlayerByName(args[1]);
			}else {
				msgManager.sendMessage(player, messages.getString("noPermissions"), true);
				return;
			}
		}

		if(p == null) {
			msgManager.sendMessage(player, messages.getString("playerDoesNotExists"), true);
			return;
		}

		Player checkPlayer = Bukkit.getPlayer(p.getName());
		if(checkPlayer == null) {
			msgManager.sendMessage(player, messages.getString("playerNotOnline"), true);
			return;
		}

		PlayerManager playerManager = plugin.getPlayerManager();
		int timeLimit = playerManager.getTimeLimitPlayer(checkPlayer);
		String timeLeft = playerManager.getTimeLeft(p, timeLimit);
		String totalTime = UtilsTime.getTime(p.getTotalTime(), msgManager);
		List<String> msg = messages.getStringList("checkCommandMessage");
		for(String m : msg) {
			player.sendMessage(MessagesManager.getMessageColor(m.replace("%player%", p.getName())
					.replace("%time_left%", timeLeft).replace("%total_time%", totalTime)));
		}
	}

	public void resettime(String[] args,CommandSender sender,FileConfiguration messages,MessagesManager msgManager) {
		// /ptl resettime <player>
		if(args.length == 1) {
			msgManager.sendMessage(sender, messages.getString("commandResetTimeError"), true);
			return;
		}

		PlayerManager playerManager = plugin.getPlayerManager();
		TimeLimitPlayer p = playerManager.getPlayerByName(args[1]);
		if(p == null) {
			msgManager.sendMessage(sender, messages.getString("playerDoesNotExists"), true);
			return;
		}

		p.resetTime();

		msgManager.sendMessage(sender, messages.getString("commandResetTimeCorrect")
				.replace("%player%", args[1]), true);
		return;
	}

    public void resetalltime(CommandSender sender,FileConfiguration messages,MessagesManager msgManager) {
        // /ptl resetalltime
        // gets all players from the arraylist in PlayerManager, checks if there are actual players in the list, then resets the time for all players one by one in the for loop

        ArrayList<TimeLimitPlayer> p = plugin.getPlayerManager().getAllPlayers();
        if(p == null || p.size() <= 0) { // if it returned null, we know that there are no players in the database or there has been an error
            msgManager.sendMessage(sender, messages.getString("playerDoesNotExists"), true);
            return;
        }

        plugin.getPlayerManager().resetPlayers(); // calls the resetPlayers method in PlayerManager, resetting the same way it would daily

        msgManager.sendMessage(sender, messages.getString("commandResetTimeCorrect")
                .replace("%player%", "everyones"), true);
        return;
    }

	// can only be used when the player is online
	public void taketime(String[] args,CommandSender sender,FileConfiguration messages,MessagesManager msgManager) {
		// /ptl taketime <player> <time>
		if(args.length <= 2) {
			msgManager.sendMessage(sender, messages.getString("commandTakeTimeError"), true);
			return;
		}

		PlayerManager playerManager = plugin.getPlayerManager();
		TimeLimitPlayer p = playerManager.getPlayerByName(args[1]);
		if(p == null) {
			msgManager.sendMessage(sender, messages.getString("playerDoesNotExists"), true);
			return;
		}

		if(Bukkit.getPlayer(p.getName()) == null) {
			msgManager.sendMessage(sender, messages.getString("playerNotOnline"), true);
			return;
		}

		int time = 0;
		try {
			time = Integer.valueOf(args[2]);
			if(time <= 0) {
				msgManager.sendMessage(sender, messages.getString("invalidNumber"), true);
				return;
			}
		}catch(NumberFormatException e) {
			msgManager.sendMessage(sender, messages.getString("invalidNumber"), true);
			return;
		}

		playerManager.takeTime(p, time);

		msgManager.sendMessage(sender, messages.getString("commandTakeTimeCorrect")
				.replace("%player%", args[1]).replace("%time%", time+""), true);
		return;
	}

	// can only be used when the player is online
	public void addtime(String[] args,CommandSender sender,FileConfiguration messages,MessagesManager msgManager) {
		// /ptl addtime <player> <time>
		if(args.length <= 2) {
			msgManager.sendMessage(sender, messages.getString("commandAddTimeError"), true);
			return;
		}

		PlayerManager playerManager = plugin.getPlayerManager();
		TimeLimitPlayer p = playerManager.getPlayerByName(args[1]);
		if(p == null) {
			msgManager.sendMessage(sender, messages.getString("playerDoesNotExists"), true);
			return;
		}

		if(Bukkit.getPlayer(p.getName()) == null) {
			msgManager.sendMessage(sender, messages.getString("playerNotOnline"), true);
			return;
		}

		int time = 0;
		try {
			time = Integer.valueOf(args[2]);
			if(time <= 0) {
				msgManager.sendMessage(sender, messages.getString("invalidNumber"), true);
				return;
			}
		}catch(NumberFormatException e) {
			msgManager.sendMessage(sender, messages.getString("invalidNumber"), true);
			return;
		}

		playerManager.addTime(p, time);

		msgManager.sendMessage(sender, messages.getString("commandAddTimeCorrect")
				.replace("%player%", args[1]).replace("%time%", time+""), true);
		return;
	}
}
