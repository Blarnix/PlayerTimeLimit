package blarnix.configs;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import blarnix.PlayerTimeLimit;
import blarnix.model.TimeLimitPlayer;

public class PlayerConfigsManager {

	private ArrayList<PlayerConfig> configPlayers;
	private PlayerTimeLimit plugin;

	public PlayerConfigsManager(PlayerTimeLimit plugin) {
		try{
            this.plugin = plugin;
            this.configPlayers = new ArrayList<PlayerConfig>();
            configure();
            Bukkit.getConsoleSender().sendMessage("INFO: Created player config aray."); // debug
        }catch(Exception e){
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not create player configs array!: " + e.getMessage());
            e.printStackTrace();
        }
	}

	public void configure() {
        try {
            createPlayersFolder();
            registerPlayers();
            loadPlayers();
        } catch(Exception e) {
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not configure players!: " + e.getMessage());
            e.printStackTrace();
        }
	}

	public void createPlayersFolder(){
		File folder;
        try {
            folder = new File(plugin.getDataFolder() + File.separator + "players");
            if(!folder.exists()){
                folder.mkdirs();
            }
            Bukkit.getConsoleSender().sendMessage("INFO: Successfully created player folder."); // debug
        } catch(SecurityException e) {
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not create players folder! (SecurityException))");
            folder = null;
            e.printStackTrace();
        } catch(Exception e) {
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not create players folder!: " + e.getMessage());
            folder = null;
            e.printStackTrace();
        }
	}

	public void savePlayers() {
		try{
            for(int i=0;i<configPlayers.size();i++) {
			    configPlayers.get(i).savePlayerConfig();
                Bukkit.getConsoleSender().sendMessage("INFO: Saved players: " + configPlayers);
		    }
            if(configPlayers.size() == 0)
            throw new Exception("No players to save.");// ISSUE: This is being output to console after "Got player config" and before
        }catch(Exception e){
            Bukkit.getConsoleSender().sendMessage("WARNING: Could not save players!: " + e.getMessage()); // debug
            e.printStackTrace();
        }
    }

	public void registerPlayers(){  // issue is probably here, since it's not registering any players (even though the folder exists)
        try{
            File folder = new File(plugin.getDataFolder() + File.separator + "players");
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    String pathName = listOfFiles[i].getName();
                    if(!fileExists(pathName)) {
                        PlayerConfig config = new PlayerConfig(pathName,plugin);
                        config.registerPlayerConfig();
                        configPlayers.add(config);
                    }
                }
            }
        }catch(Exception e){
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not register players!: " + e.getMessage()); // debug
            e.printStackTrace();
        }
    }
	public boolean fileExists(String pathName) {
		for(int i=0;i<configPlayers.size();i++) {
			if(configPlayers.get(i).getPath().equals(pathName)) {
				return true;
			}
		}
		return false;
	}

	public PlayerConfig getPlayerConfig(String pathName) {
		for(int i=0;i<configPlayers.size();i++) {
			if(configPlayers.get(i).getPath().equals(pathName)) {
				return configPlayers.get(i);
			}
		}
		return null;
	}

	public ArrayList<PlayerConfig> getConfigPlayers() {
		return this.configPlayers;
	}

	public boolean registerPlayer(String pathName) {
        try{
		    if(!fileExists(pathName)) {
			    PlayerConfig config = new PlayerConfig(pathName,plugin);
	            config.registerPlayerConfig();
	            configPlayers.add(config);
	            return true;
		    }else {
			    return false;
		    }
        }catch(Exception e){
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not register player!: " + e.getMessage());  //debug
            e.printStackTrace();
            return false;
        }
	}

	public void removeConfigPlayer(String path) {
		for(int i=0;i<configPlayers.size();i++) {
			if(configPlayers.get(i).getPath().equals(path)) {
				configPlayers.remove(i);
			}
		}
	}

    public void loadPlayers() {
        try{
		    ArrayList<TimeLimitPlayer> jugadores = new ArrayList<TimeLimitPlayer>();
		    for(PlayerConfig playerConfig : configPlayers) {
		    	FileConfiguration players = playerConfig.getConfig();
		    	String name = players.getString("name");
		    	String uuid = playerConfig.getPath().replace(".yml", "");

		    	TimeLimitPlayer p = new TimeLimitPlayer(uuid,name);

		    	p.setCurrentTime(players.getInt("current_time"));
		    	p.setTotalTime(players.getInt("total_time"));
		    	p.setMessageEnabled(players.getBoolean("messages"));

		    	jugadores.add(p);
		    }
		    plugin.getPlayerManager().setPlayers(jugadores);
            Bukkit.getConsoleSender().sendMessage("INFO: Loaded players: " + jugadores); // debug
        }catch(Exception e){
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not load players!: " + e.getMessage()); // debug
        }
	}

	public void unloadPlayers() {
        try{
		    for(TimeLimitPlayer player : plugin.getPlayerManager().getPlayers()) {
			    PlayerConfig playerConfig = getPlayerConfig(player.getUuid()+".yml");
			    if(playerConfig == null) {
			    	registerPlayer(player.getUuid()+".yml");
				    playerConfig = getPlayerConfig(player.getUuid()+".yml");
			    }
			    FileConfiguration players = playerConfig.getConfig();

			    players.set("name", player.getName());
			    players.set("current_time", player.getCurrentTime());
			    players.set("total_time", player.getTotalTime());
			    players.set("messages", player.isMessageEnabled());
		    }
		    savePlayers();
            Bukkit.getConsoleSender().sendMessage("INFO: Unloaded players."); // debug
        }catch(Exception e){
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not unload players!: " + e.getMessage()); // debug
        }
    }
}

