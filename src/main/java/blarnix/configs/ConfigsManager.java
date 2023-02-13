package blarnix.configs;

import blarnix.PlayerTimeLimit;

// TODO: Add comments
// TODO: Add a daily time limit scheduler
public class ConfigsManager {

	private PlayerConfigsManager playerConfigsManager;
	private MessagesConfigManager messagesConfigManager;
	private MainConfigManager mainConfigManager;

	public ConfigsManager(PlayerTimeLimit plugin) {
		this.mainConfigManager = new MainConfigManager(plugin);
		this.playerConfigsManager = new PlayerConfigsManager(plugin);
		this.messagesConfigManager = new MessagesConfigManager(plugin);
	}

	public void configure() {
		this.mainConfigManager.configure();
		this.playerConfigsManager.configure();
		this.messagesConfigManager.configure();
	}

	public MessagesConfigManager getMessagesConfigManager() {
		return messagesConfigManager;
	}

	public PlayerConfigsManager getPlayerConfigsManager() {
		return playerConfigsManager;
	}

	public MainConfigManager getMainConfigManager() {
		return mainConfigManager;
	}


}
