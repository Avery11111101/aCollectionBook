package tw.avery.collection.integration;

import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class DiscordWebhook {

    public static void sendEmbed(String webhookUrl, String username, String title, String description, int color, String fieldName, String fieldValue, String footerText) {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) return;

        StringBuilder fieldsJson = new StringBuilder();
        if (fieldName != null && !fieldName.isEmpty() && fieldValue != null && !fieldValue.isEmpty()) {
            fieldsJson.append(String.format("""
                  ,"fields": [
                    {
                      "name": "%s",
                      "value": "%s",
                      "inline": true
                    }
                  ]""", escapeJson(fieldName), escapeJson(fieldValue)));
        }

        StringBuilder footerJson = new StringBuilder();
        if (footerText != null && !footerText.isEmpty()) {
            footerJson.append(String.format("""
                  ,"footer": {
                    "text": "%s"
                  }""", escapeJson(footerText)));
        }

        String jsonPayload = String.format("""
            {
              "username": "%s",
              "embeds": [
                {
                  "title": "%s",
                  "description": "%s",
                  "color": %d
                  %s
                  %s
                }
              ]
            }
            """,
                escapeJson(username),
                escapeJson(title),
                escapeJson(description),
                color,
                fieldsJson.toString(),
                footerJson.toString()
        );

        sendAsync(webhookUrl, jsonPayload);
    }

    private static void sendAsync(String webhookUrl, String json) {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            Bukkit.getLogger().warning("[aCollectionBook] Discord Webhook 已啟用，但 config.yml 中的 webhook-url 為空！");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                URL url = URI.create(webhookUrl.trim()).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "aCollectionBook-Plugin");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int code = connection.getResponseCode();
                if (code >= 200 && code < 300) {
                    Bukkit.getLogger().info("[aCollectionBook] 成功傳送 Discord Webhook 通知！(HTTP " + code + ")");
                } else {
                    Bukkit.getLogger().warning("[aCollectionBook] Discord Webhook 傳送失敗，HTTP 狀態碼: " + code);
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("[aCollectionBook] Discord Webhook 傳送失敗: " + e.getMessage());
            }
        });
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
