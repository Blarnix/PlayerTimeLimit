package blarnix.libs.bossbar;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import blarnix.managers.MessagesManager;

public class BossBarAPI {

	public static BossBar create(Player player,String title,BarColor color,BarStyle style) {
		BossBar bossBar = Bukkit.getServer().createBossBar(MessagesManager.getMessageColor(title), color, style, new BarFlag[0]);
		bossBar.removeAll();
		bossBar.addPlayer(player);
		bossBar.setProgress(0);
		bossBar.setVisible(true);
		return bossBar;
	}
}
