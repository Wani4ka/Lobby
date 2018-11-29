package me.wani4ka.lobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.WeatherType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Lobby extends JavaPlugin implements Listener {

	private final List<String> allowedCmds = new ArrayList<>();
	private String cmdDeniedMsg;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		File f = new File(getDataFolder(), "config.yml");
		if (!f.exists())
			saveResource("config.yml", false);
		FileConfiguration config = YamlConfiguration.loadConfiguration(f);
		allowedCmds.addAll(config.getStringList("allowed-cmds"));
		cmdDeniedMsg = ChatColor.translateAlternateColorCodes('&', config.getString("cmd-denied-msg", "&eКоманда не найдена!"));
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.getPlayer().setGameMode(GameMode.ADVENTURE);
		e.getPlayer().setPlayerWeather(WeatherType.CLEAR);
		e.getPlayer().setPlayerTime(16000, false);
		e.getPlayer().setFlySpeed(0.2f);
	}

	@EventHandler(ignoreCancelled = true)
	public void onCmd(PlayerCommandPreprocessEvent e) {
		for (String cmd : allowedCmds)
			if (e.getMessage().toLowerCase().startsWith("/" + cmd.toLowerCase())) return;
		e.setCancelled(true);
		e.getPlayer().sendMessage(cmdDeniedMsg);
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player)
			e.setCancelled(true);
	}

	@EventHandler
	public void onFood(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlace(BlockPlaceEvent e) {
		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBreak(BlockBreakEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onGameModeChange(PlayerGameModeChangeEvent e) {
		if (!e.getNewGameMode().equals(GameMode.ADVENTURE)) {
			e.setCancelled(true);
			Bukkit.getScheduler().runTask(this, () -> e.getPlayer().setGameMode(GameMode.ADVENTURE));
		}
	}
}
