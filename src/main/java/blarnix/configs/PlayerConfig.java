package blarnix.configs;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Bukkit;

import blarnix.PlayerTimeLimit;


public class PlayerConfig {

	private FileConfiguration config;
	private File configFile;
	private String filePath;
	private PlayerTimeLimit plugin;

	public PlayerConfig(String filePath,PlayerTimeLimit plugin){
		this.config = null;
		this.configFile = null;
		this.filePath = filePath;
		this.plugin = plugin;
	}

	public String getPath(){
		return this.filePath;
	}

	public FileConfiguration getConfig(){
		 if (config == null) {
		        reloadPlayerConfig();
		    }
		return this.config;
	}

	public void registerPlayerConfig(){
		  configFile = new File(plugin.getDataFolder() +File.separator + "players",filePath);
		  if(!configFile.exists()){
			  try {
				configFile.createNewFile();
			} catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage("File failed to register (IOException): " + filePath);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		  config = new YamlConfiguration();
		  try {
	            config.load(configFile);
	      } catch (IOException e) {
	            e.printStackTrace();
                Bukkit.getLogger().info("File failed to load (IOException): " + filePath);
	      } catch (InvalidConfigurationException e) {
            Bukkit.getConsoleSender().sendMessage("File failed to load (InvalidConfigurationException): " + filePath);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void savePlayerConfig() {
		 try {
			 config.save(configFile);
             if(config == null || configFile == null) {
                 throw new NullPointerException("Config or ConfigFile is null!");
             }
		 } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("File failed to save (IOException): " + e.getMessage());
			e.printStackTrace();
	 	}
	 }

	public void reloadPlayerConfig() {
		    if (config == null) {
		    	configFile = new File(plugin.getDataFolder() +File.separator + "players", filePath);
		    }
		    config = YamlConfiguration.loadConfiguration(configFile);

			if (configFile != null) {
			    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(configFile);
			    config.setDefaults(defConfig);
			}
		}
}
