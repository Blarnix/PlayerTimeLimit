package blarnix;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import blarnix.api.ExpansionPlayerTimeLimit;
import blarnix.configs.ConfigsManager;
import blarnix.listeners.PlayerListener;
import blarnix.managers.MessagesManager;
import blarnix.managers.PlayerManager;
import blarnix.managers.ServerManager;
import blarnix.tasks.DataSaveTask;
import blarnix.tasks.PlayerTimeTask;
import blarnix.tasks.ServerTimeResetTask;


public class PlayerTimeLimit extends JavaPlugin {

	PluginDescriptionFile pdfFile = getDescription();
	public String version = pdfFile.getVersion();
	public String latestversion;

	public String routeConfig;

	private PlayerManager playerManager;
	private ConfigsManager configsManager;
	private MessagesManager messagesManager;
	private ServerManager serverManager;

	private DataSaveTask dataSaveTask;

	public static String namePlugin = ChatColor.translateAlternateColorCodes('&', "&8[&bPlayerTime&cLimit&8] ");

	public void onEnable(){
	   this.playerManager = new PlayerManager(this);
	   this.serverManager = new ServerManager(this);
	   registerEvents();
	   registerCommands();
	   registerConfig();
	   this.configsManager = new ConfigsManager(this);
	   this.configsManager.configure();

	   serverManager.executeDataTime();

	   PlayerTimeTask timeTask = new PlayerTimeTask(this);
	   timeTask.start();
	   ServerTimeResetTask serverTask = new ServerTimeResetTask(this);
	   serverTask.start();

	   reloadDataSaveTask();
	   checkMessagesUpdate();

	   if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
		   new ExpansionPlayerTimeLimit(this).register();
	   }

	   Bukkit.getConsoleSender().sendMessage(namePlugin+ChatColor.YELLOW + "Has been enabled! " + ChatColor.WHITE + "Version: " + version);

	   updateChecker();
	}

	public void onDisable(){
		this.configsManager.getPlayerConfigsManager().savePlayers();
		serverManager.saveDataTime();
		Bukkit.getConsoleSender().sendMessage(namePlugin+ChatColor.YELLOW + "Has been disabled! " + ChatColor.WHITE + "Version: " + version);
	}
	public void registerCommands(){
		this.getCommand("playertimelimit").setExecutor(new Commands(this));
	}

	public void registerEvents(){
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(this), this);
	}

	public void registerConfig(){
		File config = new File(this.getDataFolder(), "config.yml");
		routeConfig = config.getPath();
		if(!config.exists()){   // if the config file doesn't exist, create a new one.
			this.getConfig().options().copyDefaults(true);
			saveConfig();
		}
	}

	public void reloadConfigs() {
		this.configsManager.getMessagesConfigManager().reloadMessages();
		this.configsManager.getPlayerConfigsManager().savePlayers();
		reloadConfig();
		this.configsManager.getMainConfigManager().configure();

		reloadDataSaveTask();
	}

	public void reloadDataSaveTask() {
		if(dataSaveTask != null) {
			dataSaveTask.end();
		}
		dataSaveTask = new DataSaveTask(this);
		dataSaveTask.start(getConfig().getInt("data_save_time"));
	}

	public PlayerManager getPlayerManager() {
		return playerManager;
	}

	public MessagesManager getMessagesManager() {
		return messagesManager;
	}

	public void setMessagesManager(MessagesManager messagesManager) {
		this.messagesManager = messagesManager;
	}

	public FileConfiguration getMessages() {
		return this.configsManager.getMessagesConfigManager().getMessages();
	}

	public ConfigsManager getConfigsManager() {
		return configsManager;
	}

	public ServerManager getServerManager() {
		return serverManager;
	}

	public void checkMessagesUpdate(){
		  Path archiveConfig = Paths.get(routeConfig);
		  Path archiveMessages = Paths.get(configsManager.getMessagesConfigManager().getPath());
		  try{
			  String textConfig = new String(Files.readAllBytes(archiveConfig));
			  String textMessages = new String(Files.readAllBytes(archiveMessages));
			  FileConfiguration messages = configsManager.getMessagesConfigManager().getMessages();

			  if(!textMessages.contains("commandResetTimeError:")){
				  messages.set("commandResetTimeError", "&cYou need to use: &7/ptl resettime <player>");
				  messages.set("commandResetTimeCorrect", "&aCurrent time has been reset for player &7%player%&a!");
				  messages.set("commandTakeTimeError", "&cYou need to use: &7/ptl taketime <player> <time>");
				  messages.set("invalidNumber", "&cYou need to use a valid number!");
				  messages.set("commandTakeTimeCorrect", "&aTaken &7%time% seconds &afrom &7%player% &atime!");
				  messages.set("playerNotOnline", "&cThat player is not online.");
				  messages.set("commandAddTimeError", "&cYou need to use: &7/ptl addtime <player> <time>");
				  messages.set("commandAddTimeCorrect", "&aAdded &7%time% seconds &ato &7%player% &atime!");
				  configsManager.getMessagesConfigManager().saveMessages();
			  }
			  if(!textConfig.contains("world_whitelist_system:")){
				  getConfig().set("world_whitelist_system.enabled", false);
				  List<String> list = new ArrayList<String>();
				  list.add("world");list.add("world_nether");list.add("world_the_end");
				  getConfig().set("world_whitelist_system.worlds", list);
				  getConfig().set("world_whitelist_system.teleport_coordinates_on_kick", "spawn;0;60;0;90;0");
				  saveConfig();
			  }
			  if(!textConfig.contains("update_notification:")){
				  getConfig().set("update_notification", true);
				  saveConfig();
				  messages.set("playerDoesNotExists", "&cThat player doesn't exists.");
				  List<String> list = new ArrayList<String>();
				  list.add("&c&m                                          ");
				  list.add("&7&l%player% Data:");
				  list.add("&7Time left: &a%time_left%");
				  list.add("&7Total played time: &a%total_time%");
				  list.add("&c&m                                          ");
				  messages.set("checkCommandMessage", list);
				  list = new ArrayList<String>();
				  list.add("&c&m                                          ");
				  list.add("&7Exact time when playtimes will be reset:");
				  list.add("&e%reset_time%");
				  list.add("");
				  list.add("&7Remaining time until reset:");
				  list.add("&e%remaining%");
				  list.add("&c&m                                          ");
				  messages.set("infoCommandMessage", list);
				  configsManager.getMessagesConfigManager().saveMessages();
			  }
		  }catch(IOException e){
			  e.printStackTrace();
		  }
	}

	public void updateChecker(){    // get the latest version from api.github.com and compare it with the current version, if they are different, send a message to the console
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(
					"https://api.github.com/Blarnix/PlayerTimeLimit/releases").openConnection();
			int timed_out = 1250;
			con.setConnectTimeout(timed_out);
			con.setReadTimeout(timed_out);
			latestversion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
			if (latestversion.length() <= 16) {
				if(!version.equals(latestversion)){
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED +"There is a new version of " + namePlugin + " available. "+ChatColor.YELLOW+"("+ChatColor.GRAY+latestversion+ChatColor.YELLOW+")");
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"You can download it here: "+ChatColor.WHITE+"https://github.com/Blarnix/PlayerTimeLimit/releases/");
				}
			}
		} catch (Exception ex) {
			Bukkit.getConsoleSender().sendMessage(namePlugin + ChatColor.RED +"Error while checking for update.");
		}
	}
}
