package tw.avery.collection.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import tw.avery.collection.CollectionBookPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MinecraftTranslation {

    private static final Map<String, String> translationMap = new HashMap<>();

    public static void load(CollectionBookPlugin plugin) {
        translationMap.clear();
        try (InputStream is = plugin.getResource("minecraft_zh_tw.json")) {
            if (is != null) {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    translationMap.put(entry.getKey(), entry.getValue().getAsString());
                }
                plugin.getLogger().info("成功載入原版原聲 Minecraft 繁體中文對照庫，共包含 " + translationMap.size() + " 項官方譯名。");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("無法載入原版 Minecraft 繁體中文語系檔: " + e.getMessage());
        }
    }

    public static String getMobChineseName(String mobName) {
        String key = "entity.minecraft." + mobName.toLowerCase();
        if (translationMap.containsKey(key)) {
            return translationMap.get(key);
        }
        try {
            EntityType type = EntityType.valueOf(mobName.toUpperCase());
            String tKey = type.translationKey();
            if (translationMap.containsKey(tKey)) {
                return translationMap.get(tKey);
            }
        } catch (Exception ignored) {}

        return formatRawName(mobName);
    }

    public static String getItemChineseName(String itemName) {
        String itemKey = "item.minecraft." + itemName.toLowerCase();
        if (translationMap.containsKey(itemKey)) {
            return translationMap.get(itemKey);
        }
        String blockKey = "block.minecraft." + itemName.toLowerCase();
        if (translationMap.containsKey(blockKey)) {
            return translationMap.get(blockKey);
        }

        try {
            Material mat = Material.valueOf(itemName.toUpperCase());
            String tKey = mat.getItemTranslationKey();
            if (tKey != null && translationMap.containsKey(tKey)) {
                return translationMap.get(tKey);
            }
            if (mat.isBlock()) {
                String bKey = mat.getBlockTranslationKey();
                if (bKey != null && translationMap.containsKey(bKey)) {
                    return translationMap.get(bKey);
                }
            }
        } catch (Exception ignored) {}

        return formatRawName(itemName);
    }

    public static Component getMobComponent(String mobName) {
        try {
            EntityType type = EntityType.valueOf(mobName.toUpperCase());
            return Component.translatable(type.translationKey());
        } catch (Exception e) {
            String cn = getMobChineseName(mobName);
            return Component.text(cn);
        }
    }

    public static Component getItemComponent(String itemName) {
        try {
            Material mat = Material.valueOf(itemName.toUpperCase());
            String tKey = mat.getItemTranslationKey();
            if (tKey != null) {
                return Component.translatable(tKey);
            }
        } catch (Exception ignored) {}

        String cn = getItemChineseName(itemName);
        return Component.text(cn);
    }

    private static String formatRawName(String raw) {
        String clean = raw.replace("_", " ");
        String[] parts = clean.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
