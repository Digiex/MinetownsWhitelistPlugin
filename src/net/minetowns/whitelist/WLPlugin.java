package net.minetowns.whitelist;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WLPlugin extends JavaPlugin implements Listener {

	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent e) {
		if (e.getPlayer().isOp()
				|| e.getPlayer().hasPermission("wlplugin.admin")) {
			String message = listWPlayers(false);
			if (message.length() > 1) {
				e.getPlayer().sendMessage(message);
			}
		}
	}

	public String listWPlayers(Boolean retNoPlayers) {
		List<String> reqList = this.getConfig().getStringList("requests");
		if (reqList != null && reqList.size() > 0) {
			return ChatColor.BOLD
					+ ""
					+ ChatColor.BLUE
					+ "[Whitelist] The following players have requested whitelisting: "
					+ ChatColor.RED + "" + ChatColor.UNDERLINE
					+ StringUtils.join(reqList, ", ") + ChatColor.RESET
					+ " Whitelist using /wl add <player>";
		} else if (retNoPlayers) {
			return ChatColor.BLUE + "[Whitelist] No players in whitelist queue";
		}
		return "";
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("wl")) {
			if (args.length < 1) {
				return false;
			}
			if (!sender.isOp() && !sender.hasPermission("groupmanager.manuadd")) {
				sender.sendMessage(ChatColor.RED
						+ "[Whitelist] You don't have groupmanager.manuadd permission!");
				return true;
			}
			List<String> reqList = this.getConfig().getStringList("requests");
			if (reqList == null) {
				reqList = new ArrayList<String>();
			}
			if (args[0].equalsIgnoreCase("add")) {
				if (args.length < 2) {
					return false;
				}
				String pname = args[1];
				List<String> nameMatch = matchOfflinePlayer(pname);
				if (nameMatch.size() > 1) {
					sender.sendMessage(ChatColor.RED
							+ "[Whitelist] Multiple players found. ("
							+ StringUtils.join(nameMatch, ", ")
							+ ") Please be more specific.");
					return true;
				} else if (nameMatch.size() == 1) {
					pname = nameMatch.get(0);
				}
				if (reqList.contains(pname)) {
					this.getServer().dispatchCommand(sender, "lookup " + pname);
					if (this.getServer().dispatchCommand(sender,
							"manuadd " + pname + " Member")) {
						reqList.remove(pname);
						this.getConfig().set("requests", reqList);
						this.saveConfig();
						this.getLogger().info(
								sender.getName() + " whitelisted " + pname);
						sender.sendMessage(ChatColor.BLUE + "[Whitelist] "
								+ pname + " whitelisted!");
					} else {
						sender.sendMessage(ChatColor.RED
								+ "[Whitelist] /manuadd failed");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "[Whitelist] Player "
							+ pname + " not in the whitelisting queue. See /"
							+ cmd.getName() + " list");
				}
			} else if (args[0].equalsIgnoreCase("list")) {
				sender.sendMessage(listWPlayers(true));
			} else if (args[0].equalsIgnoreCase("addtolist")) {
				if (args.length < 2) {
					return false;
				}
				if (!reqList.contains(args[1])) {
					reqList.add(args[1]);
					this.getConfig().set("requests", reqList);
					this.saveConfig();
					sender.sendMessage("[Whitelist] " + args[1]
							+ " added into the whitelist queue.");
					this.getLogger().info(
							args[1] + " added into the whitelist queue.");
					for (Player p : this.getServer().getOnlinePlayers()) {
						if (p.isOp() || p.hasPermission("wlplugin.admin")) {
							p.sendMessage(listWPlayers(false));
						}
						if (p.getName().equalsIgnoreCase(args[1])) {
							p.sendMessage(ChatColor.BLUE
									+ "[Whitelist] You are now added to the whitelist waiting queue. Please contact an admin to get whitelisted.");
						}
					}
				} else {
					sender.sendMessage("[Whitelist] " + args[1]
							+ " was already in the whitelist queue!");
				}
			} else if (args[0].equalsIgnoreCase("check")) {
				if (args.length < 2) {
					return false;
				}
				try { // TODO: Change this to something else:
					if (this.getServer().getPlayer(args[1])
							.hasPermission("iConomy.accounts.give")) {
						sender.sendMessage("true");
					} else {
						sender.sendMessage("false");
					}
				} catch (Exception ex) {
					sender.sendMessage("false");
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (args.length < 2) {
					return false;
				}
				String pname = args[1];
				List<String> nameMatch = matchOfflinePlayer(pname);
				if (nameMatch.size() > 1) {
					sender.sendMessage(ChatColor.RED
							+ "[Whitelist] Multiple players found. ("
							+ StringUtils.join(nameMatch, ", ")
							+ ") Please be more specific.");
					return true;
				} else if (nameMatch.size() == 1) {
					pname = nameMatch.get(0);
				}
				if (reqList.contains(pname)) {
					reqList.remove(pname);
					this.getConfig().set("requests", reqList);
					this.saveConfig();
					sender.sendMessage(ChatColor.BLUE + "[Whitelist] Player "
							+ pname + " removed from the whitelist queue.");
					this.getLogger().info(
							pname + " removed from the whitelist queue.");
				} else {
					sender.sendMessage(ChatColor.RED + "[Whitelist] Player "
							+ pname + " not in the whitelisting queue. See /"
							+ cmd.getName() + " list");
				}
			} else {
				return false;
			}
			return true;
		}
		return false;
	}

	public List<String> matchOfflinePlayer(String partialName) {
		List<String> reqList = this.getConfig().getStringList("requests");
		List<String> matchedOfflinePlayers = new ArrayList<String>();
		if (reqList != null) {
			List<String> found = new ArrayList<String>();
			for (String player : reqList) {
				if (!found.contains(player)) {
					found.add(player);
					if (partialName.equalsIgnoreCase(player)) {
						// Exact match
						matchedOfflinePlayers.clear();
						matchedOfflinePlayers.add(player);
						break;
					}
					if (player.toLowerCase().indexOf(partialName.toLowerCase()) != -1) {
						// Partial match
						matchedOfflinePlayers.add(player);
					}
				}
			}
		}
		return matchedOfflinePlayers;
	}
}
