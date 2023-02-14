package blarnix.configs;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;

import blarnix.PlayerTimeLimit;
import blarnix.model.TimeLimitPlayer;

public class PlayerConfigsManager {

	private ArrayList<PlayerConfig> configPlayers;
	private PlayerTimeLimit plugin;

	public PlayerConfigsManager(PlayerTimeLimit plugin) {
		this.plugin = plugin;
		this.configPlayers = new ArrayList<PlayerConfig>();
	}

	public void configure() {
        try {
            createPlayersFolder();
            registerPlayers();
            loadPlayers();
        } catch(Exception e) {
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not configure players!: " + e.getMessage());
        }
	}

	public void createPlayersFolder(){
		File folder;
        try {
            folder = new File(plugin.getDataFolder() + File.separator + "players");
            if(!folder.exists()){
                folder.mkdirs();
            }
        } catch(SecurityException e) {
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not create players folder! (SecurityException))");
            folder = null;
        } catch(Exception e) {
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not create players folder!: " + e.getMessage());
            folder = null;
        }
	}

	public void savePlayers() {
		try{
            for(int i=0;i<configPlayers.size();i++) {
			    configPlayers.get(i).savePlayerConfig();
		    }
        }catch(Exception e){
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not save players!: " + e.getMessage());
        }
    }

	public void registerPlayers(){
		String path = plugin.getDataFolder() + File.separator + "players";
		try{
            File folder = new File(path);
		    File[] listOfFiles = folder.listFiles();
		    for (int i=0;i<listOfFiles.length;i++) {
			    if(listOfFiles[i].isFile()) {
		            String pathName = listOfFiles[i].getName();
		            PlayerConfig config = new PlayerConfig(pathName,plugin);
		            config.registerPlayerConfig();
		            configPlayers.add(config);
                }
		    }
        }catch(Exception e){
            Bukkit.getConsoleSender().sendMessage("ERROR: Could not register players!: " + e.getMessage());
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

	public ArrayList<PlayerConfig> getPlayerConfigs() {
		return this.configPlayers;
	}

	public boolean registerPlayer(String pathName) {
		if(!fileExists(pathName)) {
			PlayerConfig config = new PlayerConfig(pathName,plugin);
	        config.registerPlayerConfig();
	        configPlayers.add(config);
	        return true;
		}else {
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
		ArrayList<TimeLimitPlayer> jugadores = new ArrayList<TimeLimitPlayer>();
        //keeping the name 'jugador' for compatibility until i can find new ones
        try{
        for(PlayerConfig playerConfig : configPlayers) {
			FileConfiguration players = playerConfig.getConfig();
			String name = players.getString("name");
			String uuid = playerConfig.getPath().replace(".yml", "");

			TimeLimitPlayer p = new TimeLimitPlayer(uuid,name);

			p.setCurrentTime(players.getInt("current_time"));
			p.setTotalTime(players.getInt("total_time"));
			p.setMessageEnabled(players.getBoolean("messages"));

			playerArray.add(p);
		}
		plugin.getPlayerManager().setPlayers(jugadores);
    }catch(Exception e){
        Bukkit.getConsoleSender().sendMessage("ERROR: Could not load players!: " + e.getMessage());
        }
	}

	public void unloadPlayers() {
		for(TimeLimitPlayer player : plugin.getPlayerManager().getPlayers()) {
			String jugador = player.getName();  //keeping the name 'jugador' for compatibility until i can find new ones
			PlayerConfig playerConfig = getPlayerConfig(player.getUuid()+".yml");
			if(playerConfig == null) {
				registerPlayer(player.getUuid()+".yml");
				playerConfig = getPlayerConfig(player.getUuid()+".yml");
			}
			FileConfiguration players = playerConfig.getConfig();

			players.set("name", jugador);
			players.set("current_time", player.getCurrentTime());
			players.set("total_time", player.getTotalTime());
			players.set("messages", player.isMessageEnabled());
		}
		savePlayers();
	}
}
