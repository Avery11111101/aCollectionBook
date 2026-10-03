package tw.avery.collection.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import tw.avery.collection.CollectionBookPlugin;
import tw.avery.collection.data.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class CollectionAdminCommand implements CommandExecutor, TabCompleter {

    private final CollectionBookPlugin plugin;

    public CollectionAdminCommand(CollectionBookPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("collectionbook.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getComponent("messages.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendAdminHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                plugin.reloadAll();
                sender.sendMessage(plugin.getLanguageManager().getComponent("messages.config-reloaded"));
            }
            case "resetall" -> {
                plugin.getDataManager().saveAll();
                java.io.File dir = new java.io.File(plugin.getDataFolder(), "data/players");
                if (dir.exists()) {
                    java.io.File[] files = dir.listFiles();
                    if (files != null) {
                        for (java.io.File f : files) f.delete();
                    }
                }
                plugin.getDataManager().loadGlobalData();
                sender.sendMessage(plugin.getLanguageManager().getComponent("messages.admin-reset-all"));
            }
            case "resetplayer" -> {
                if (args.length < 2) {
                    sender.sendMessage(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize("§c用法: /colladmin resetplayer <玩家名稱>"));
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                java.io.File f = new java.io.File(plugin.getDataFolder(), "data/players/" + target.getUniqueId() + ".yml");
                if (f.exists()) f.delete();
                plugin.getDataManager().unloadPlayer(target.getUniqueId());
                sender.sendMessage(plugin.getLanguageManager().formatComponent("messages.admin-reset-player", "player", args[1]));
            }
            case "unlock" -> {
                if (args.length < 3) {
                    sender.sendMessage(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize("§c用法: /colladmin unlock <玩家名稱> <項目名稱>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getLanguageManager().getComponent("messages.admin-player-not-found"));
                    return true;
                }
                String entry = args[2].toLowerCase();
                plugin.getCollectionManager().unlockMob(target, entry);
                plugin.getCollectionManager().unlockItem(target, entry);
                sender.sendMessage(plugin.getLanguageManager().formatComponent("messages.admin-unlock-success",
                        "player", target.getName(), "target", entry));
            }
            case "check" -> {
                if (args.length < 2) {
                    sender.sendMessage(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize("§c用法: /colladmin check <玩家名稱>"));
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
                sender.sendMessage(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize("§7玩家 §e" + args[1] + " §7解鎖總數: §a" + data.getTotalUnlockedCount() + " §7項 (進度: §e" +
                        String.format("%.1f", plugin.getCollectionManager().calculateCompletionPercentage(data)) + "%§7)"));
            }
            default -> sendAdminHelp(sender);
        }
        return true;
    }

    private void sendAdminHelp(CommandSender sender) {
        var legacy = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection();
        sender.sendMessage(legacy.deserialize("§8========== §6圖鑑系統管理員指令幫助 §8=========="));
        sender.sendMessage(legacy.deserialize("§e/colladmin reload §7- 重載設定檔與語言檔"));
        sender.sendMessage(legacy.deserialize("§e/colladmin unlock <玩家> <項目> §7- 強制幫玩家解鎖指定項目"));
        sender.sendMessage(legacy.deserialize("§e/colladmin resetplayer <玩家> §7- 重置指定玩家的圖鑑資料"));
        sender.sendMessage(legacy.deserialize("§e/colladmin resetall §7- 清空全服所有圖鑑資料"));
        sender.sendMessage(legacy.deserialize("§e/colladmin check <玩家> §7- 查詢指定玩家的解鎖進度"));
        sender.sendMessage(legacy.deserialize("§8=========================================="));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("reload");
            suggestions.add("unlock");
            suggestions.add("resetplayer");
            suggestions.add("resetall");
            suggestions.add("check");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("unlock") || args[0].equalsIgnoreCase("resetplayer") || args[0].equalsIgnoreCase("check"))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                suggestions.add(p.getName());
            }
        }
        return suggestions;
    }
}
