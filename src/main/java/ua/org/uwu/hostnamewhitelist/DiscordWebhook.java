package ua.org.uwu.hostnamewhitelist;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscordWebhook {

    private final String webhookUrl;
    private final Logger logger;
    private final ExecutorService executor;
    private final boolean enabled;

    public DiscordWebhook(String webhookUrl, Logger logger) {
        this.webhookUrl = webhookUrl;
        this.logger = logger;
        this.enabled = webhookUrl != null && !webhookUrl.isEmpty() && !webhookUrl.equals("YOUR_WEBHOOK_URL_HERE");
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DiscordWebhook-Thread");
            t.setDaemon(true);
            return t;
        });
    }

    public void sendDeniedConnection(String username, String hostname, String ip) {
        if (!enabled)
            return;

        String json = buildEmbed(
                "🚫 З'єднання відхилено",
                String.format("**Гравець:** `%s`\\n**Hostname:** `%s`\\n**IP:** `%s`",
                        escapeJson(username),
                        escapeJson(hostname.isEmpty() ? "немає" : hostname),
                        escapeJson(ip)),
                15158332 // Red color
        );

        sendAsync(json);
    }

    public void sendAllowedConnection(String username, String hostname, String ip) {
        if (!enabled)
            return;

        String json = buildEmbed(
                "✅ З'єднання дозволено",
                String.format("**Гравець:** `%s`\\n**Hostname:** `%s`\\n**IP:** `%s`",
                        escapeJson(username),
                        escapeJson(hostname),
                        escapeJson(ip)),
                3066993 // Green color
        );

        sendAsync(json);
    }

    private String buildEmbed(String title, String description, int color) {
        return String.format("""
                {
                    "embeds": [{
                        "title": "%s",
                        "description": "%s",
                        "color": %d,
                        "timestamp": "%s",
                        "footer": {
                            "text": "Hostname Whitelist"
                        }
                    }]
                }
                """, escapeJson(title), description, color, Instant.now().toString());
    }

    private void sendAsync(String json) {
        executor.submit(() -> {
            try {
                URL url = URI.create(webhookUrl).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 204 && responseCode != 200) {
                    logger.warn("Discord webhook returned status: {}", responseCode);
                }

                conn.disconnect();
            } catch (IOException e) {
                logger.warn("Failed to send Discord webhook: {}", e.getMessage());
            }
        });
    }

    private String escapeJson(String text) {
        if (text == null)
            return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public void shutdown() {
        executor.shutdown();
    }

    public boolean isEnabled() {
        return enabled;
    }
}
