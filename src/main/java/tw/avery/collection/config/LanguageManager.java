package tw.avery.collection.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import tw.avery.collection.CollectionBookPlugin;

import java.io.File;

public class LanguageManager {

    private final CollectionBookPlugin plugin;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;
    private YamlConfiguration langConfig;

    public LanguageManager(CollectionBookPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.legacySection();
        loadLanguage();
    }

    public void loadLanguage() {
        String langName = plugin.getConfigManager().getLanguage();
        File langFile = new File(plugin.getDataFolder(), "lang/" + langName + ".yml");

        if (!langFile.exists()) {
            plugin.saveResource("lang/zh_TW.yml", false);
            plugin.saveResource("lang/en_US.yml", false);
        }

        if (!langFile.exists()) {
            langFile = new File(plugin.getDataFolder(), "lang/zh_TW.yml");
        }

        this.langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getRaw(String path) {
        String val = langConfig.getString(path, path);
        String prefix = langConfig.getString("prefix", "");
        return val.replace("%prefix%", prefix);
    }

    public Component getComponent(String path) {
        return miniMessage.deserialize(getRaw(path));
    }

    public String formatRaw(String path, Object... placeholders) {
        String text = getRaw(path);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                text = text.replace("%" + placeholders[i] + "%", String.valueOf(placeholders[i + 1]));
            }
        }
        return text;
    }

    public int getInt(String path, int fallback) {
        return langConfig.getInt(path, fallback);
    }

    public Component formatComponent(String path, Object... placeholders) {
        String text = formatRaw(path, placeholders);
        return miniMessage.deserialize(text);
    }

    public String formatLegacy(String path, Object... placeholders) {
        Component comp = formatComponent(path, placeholders);
        return legacySerializer.serialize(comp);
    }

    public java.util.List<Component> getComponentList(String path, Object... placeholders) {
        java.util.List<String> list = langConfig.getStringList(path);
        java.util.List<Component> result = new java.util.ArrayList<>();
        for (String line : list) {
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 < placeholders.length) {
                    line = line.replace("%" + placeholders[i] + "%", String.valueOf(placeholders[i + 1]));
                }
            }
            result.add(miniMessage.deserialize(line));
        }
        return result;
    }
}
