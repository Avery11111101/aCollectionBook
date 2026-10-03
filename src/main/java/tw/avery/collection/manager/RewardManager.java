package tw.avery.collection.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import tw.avery.collection.CollectionBookPlugin;
import tw.avery.collection.data.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class RewardManager {

    private final CollectionBookPlugin plugin;

    public RewardManager(CollectionBookPlugin plugin) {
        this.plugin = plugin;
    }

    public static class MilestoneReward {
        public final int percentage;
        public final String name;
        public final int exp;
        public final List<String> commands;

        public MilestoneReward(int percentage, String name, int exp, List<String> commands) {
            this.percentage = percentage;
            this.name = name;
            this.exp = exp;
            this.commands = commands;
        }
    }

    public List<MilestoneReward> getMilestones() {
        List<MilestoneReward> list = new ArrayList<>();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("milestone-rewards");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                try {
                    int percent = Integer.parseInt(key);
                    String name = sec.getString(key + ".name", percent + "% Milestone");
                    int exp = sec.getInt(key + ".exp", 0);
                    List<String> cmds = sec.getStringList(key + ".commands");
                    list.add(new MilestoneReward(percent, name, exp, cmds));
                } catch (Exception ignored) {}
            }
        }
        list.sort((a, b) -> Integer.compare(a.percentage, b.percentage));
        return list;
    }

    public boolean claimReward(Player player, MilestoneReward milestone) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        double currentPercent = plugin.getCollectionManager().calculateCompletionPercentage(data);

        if (data.isRewardClaimed(milestone.percentage)) {
            player.sendMessage(plugin.getLanguageManager().getComponent("messages.reward-already-claimed"));
            return false;
        }

        if (currentPercent < milestone.percentage) {
            player.sendMessage(plugin.getLanguageManager().getComponent("messages.reward-not-eligible"));
            return false;
        }

        data.setRewardClaimed(milestone.percentage);
        plugin.getDataManager().savePlayerDataAsync(data);

        if (milestone.exp > 0) {
            player.giveExp(milestone.exp);
        }

        for (String cmd : milestone.commands) {
            String exec = cmd.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), exec);
        }

        player.sendMessage(plugin.getLanguageManager().formatComponent("messages.reward-claimed-success",
                "name", milestone.name));

        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(player.getLocation(), plugin.getConfigManager().getSoundRewardClaim(), 1.0f, 1.0f);
        }

        return true;
    }
}
