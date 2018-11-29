package me.wani4ka.lobby;

import org.bukkit.*;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Lobby extends JavaPlugin implements Listener {

	private final List<String> allowedCmds = new ArrayList<>();
	private String cmdDeniedMsg;
	private Location spawn;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		File f = new File(getDataFolder(), "config.yml");
		if (!f.exists())
			saveResource("config.yml", false);
		FileConfiguration config = YamlConfiguration.loadConfiguration(f);
		allowedCmds.addAll(config.getStringList("allowed-cmds"));
		cmdDeniedMsg = ChatColor.translateAlternateColorCodes('&', config.getString("cmd-denied-msg", "&eКоманда не найдена!"));
		parseSpawn(config.getString("spawn", "0:100:0:0:0"));
	}

	private void parseSpawn(String source) {
		String[] loc = source.split(":");
		int x, y, z;
		float yaw, pitch;
		try {
			x = Integer.parseInt(loc[0]);
			y = Integer.parseInt(loc[1]);
			z = Integer.parseInt(loc[2]);
			yaw = Float.parseFloat(loc[3].replace(',', '.'));
			pitch = Float.parseFloat(loc[4].replace(',', '.'));
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			x = 0;
			y = 100;
			z = 0;
			yaw = 0;
			pitch = 0;
			e.printStackTrace();
		}
		spawn = new Location(Bukkit.getWorld("world"), (double) x+0.5, y, (double) z+0.5, yaw, pitch);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.getPlayer().setGameMode(GameMode.ADVENTURE);
		e.getPlayer().teleport(spawn);
		e.getPlayer().setPlayerWeather(WeatherType.CLEAR);
		e.getPlayer().setPlayerTime(16000, false);
		e.getPlayer().setFlySpeed(0.2f);
	}

	@EventHandler(ignoreCancelled = true)
	public void onFall(PlayerMoveEvent e) {
		if (e.getTo().getY() < 1.0) {
			e.getPlayer().teleport(spawn);
		}
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
		if (e.getEntity() instanceof Player) {
			e.setCancelled(true);
			if (e.getFinalDamage() >= ((Player) e.getEntity()).getHealth()) {
				e.getEntity().teleport(spawn);
			}
		}
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
