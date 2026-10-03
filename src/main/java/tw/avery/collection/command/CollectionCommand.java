package tw.avery.collection.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import tw.avery.collection.CollectionBookPlugin;
import tw.avery.collection.gui.CategoryMenuHolder;
import tw.avery.collection.gui.LeaderboardMenuHolder;
import tw.avery.collection.gui.MainMenuHolder;

import java.util.ArrayList;
import java.util.List;

public class CollectionCommand implements CommandExecutor, TabCompleter {

    private final CollectionBookPlugin plugin;

    public CollectionCommand(CollectionBookPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("[CollectionBook] 此指令僅限遊戲內玩家使用！");
            return true;
        }

        if (label.equalsIgnoreCase("colltop") || label.equalsIgnoreCase("mobtop") || label.equalsIgnoreCase("itemtop")) {
            player.openInventory(new LeaderboardMenuHolder(plugin).getInventory());
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("mobs") || args[0].equalsIgnoreCase("mob")) {
                player.openInventory(new CategoryMenuHolder(plugin, player, true, 1, CategoryMenuHolder.FilterMode.ALL).getInventory());
                return true;
            } else if (args[0].equalsIgnoreCase("items") || args[0].equalsIgnoreCase("item")) {
                player.openInventory(new CategoryMenuHolder(plugin, player, false, 1, CategoryMenuHolder.FilterMode.ALL).getInventory());
                return true;
            } else if (args[0].equalsIgnoreCase("top") || args[0].equalsIgnoreCase("leaderboard")) {
                player.openInventory(new LeaderboardMenuHolder(plugin).getInventory());
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!player.hasPermission("collectionbook.admin")) {
                    player.sendMessage(plugin.getLanguageManager().getComponent("messages.no-permission"));
                    return true;
                }
                plugin.reloadAll();
                player.sendMessage(plugin.getLanguageManager().getComponent("messages.config-reloaded"));
                return true;
            }
        }

        player.openInventory(new MainMenuHolder(plugin, player).getInventory());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("mobs");
            suggestions.add("items");
            suggestions.add("top");
            if (sender.hasPermission("collectionbook.admin")) {
                suggestions.add("reload");
            }
        }
        return suggestions;
    }
}
