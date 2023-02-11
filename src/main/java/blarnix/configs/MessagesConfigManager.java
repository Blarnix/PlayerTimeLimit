package blarnix.configs;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import blarnix.PlayerTimeLimit;
import blarnix.managers.MessagesManager;

public class MessagesConfigManager {

	private PlayerTimeLimit plugin;
	private FileConfiguration messages = null;
	private File messagesFile = null;
	private String routeMessages;

	public MessagesConfigManager(PlayerTimeLimit plugin) {
		this.plugin = plugin;
	}

	public void configure() {
		registerMessages();
		setMessages();
	}

	public void setMessages() {
		FileConfiguration messages = getMessages();
		MessagesManager msgManager = new MessagesManager(messages.getString("prefix"));
		msgManager.setActionBarMessage(messages.getString("actionBarMessage"));
		msgManager.setBossBarMessage(messages.getString("bossBarMessage"));
		msgManager.setTimeSeconds(messages.getString("timeSeconds"));
		msgManager.setTimeMinutes(messages.getString("timeMinutes"));
		msgManager.setTimeHours(messages.getString("timeHours"));
		msgManager.setTimeDays(messages.getString("timeDays"));
		msgManager.setTimeInfinite(messages.getString("timeInfinite"));

		this.plugin.setMessagesManager(msgManager);
	}

	public void registerMessages(){
		  messagesFile = new File(plugin.getDataFolder(), "messages.yml");
		  routeMessages = messagesFile.getPath();
			if(!messagesFile.exists()){
				this.getMessages().options().copyDefaults(true);
				saveMessages();
			}
		}

		public void saveMessages() {
			try {
				messages.save(messagesFile);
			}catch (IOException e) {
				 e.printStackTrace();
		 	}
		}

		public FileConfiguration getMessages() {
			if (messages == null) {
			   reloadMessages();
			}
			return messages;
		}

		public void reloadMessages() {
			if (messages == null) {
			    messagesFile = new File(plugin.getDataFolder(), "messages.yml");
			}
			messages = YamlConfiguration.loadConfiguration(messagesFile);
			Reader defConfigStream;
			try {
				defConfigStream = new InputStreamReader(plugin.getResource("messages.yml"), "UTF8");
				if (defConfigStream != null) {
				     YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				     messages.setDefaults(defConfig);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			setMessages();
		}

		public String getPath() {
			return routeMessages;
		}
}
