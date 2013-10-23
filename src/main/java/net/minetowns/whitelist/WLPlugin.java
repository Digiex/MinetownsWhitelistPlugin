package net.minetowns.whitelist;

import java.util.ArrayList;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class WLPlugin extends JavaPlugin implements Listener {

	private Permission provider;

	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent e) {
		if (needsWhitelist(e.getPlayer())) {
			e.getPlayer()
					.sendMessage(
							ChatColor.AQUA
									+ ""
									+ ChatColor.UNDERLINE
									+ "Notice:"
									+ ChatColor.RED
									+ " You need to accept the rules before you can build. "
									+ ChatColor.GREEN
									+ "Type /rules to continue.");
		}
	}

	@Override
	public void onEnable() {
		if (getVault() == null) {
			getServer().getPluginManager().disablePlugin(this);
		}
		if (getConfig().getStringList("rules") == null
				|| getConfig().getStringList("rules").size() == 0) {
			ArrayList<String> ruleList = new ArrayList<String>();
			ruleList.add(ChatColor.RED + "Rules:");
			ruleList.add(ChatColor.BLUE + "Use common sense!");
			getConfig().set("rules", ruleList);
			saveConfig();
		}
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				for (Player player : getServer().getOnlinePlayers()) {
					if (needsWhitelist(player)) {
						player.sendMessage(ChatColor.AQUA
								+ ""
								+ ChatColor.UNDERLINE
								+ "Notice:"
								+ ChatColor.RED
								+ " You need to accept the rules before you can build. "
								+ ChatColor.GREEN + "Type /rules to continue.");
					}
				}
			}
		}, 20, 300 * 20);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("rules")) {
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("reload") && sender.isOp()) {
					this.reloadConfig();
					sender.sendMessage("Rules reloaded");
					return true;
				} else if (args[0].equalsIgnoreCase("accept")) {
					// TODO: accept
					sender.sendMessage("Not implemented yet");
					return true;
				}
			} else {
				for (String rule : this.getConfig().getStringList("rules")) {
					sender.sendMessage(rule);
				}
				sender.sendMessage(ChatColor.GREEN
						+ "Accept the rules by typing " + ChatColor.UNDERLINE
						+ "/rules accept");
				return true;
			}
		}
		return false;
	}

	private Permission getVault() {
		if (provider != null) {
			return provider;
		}
		Plugin plugin = getServer().getPluginManager().getPlugin("Vault");
		if (plugin == null || !(plugin instanceof net.milkbowl.vault.Vault)) {
			getServer().getPluginManager().disablePlugin(this);
			return null;
		}
		RegisteredServiceProvider<Permission> rsp = Bukkit.getServer()
				.getServicesManager().getRegistration(Permission.class);
		if (rsp == null) {
			getServer().getPluginManager().disablePlugin(this);
			return null;
		}
		return provider = rsp.getProvider();
	}

	public boolean needsWhitelist(Player player) {
		return getVault().getPrimaryGroup(player).equals("Player");
	}
}
