package ua.org.uwu.hostnamewhitelist;

import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PluginConfig {

    private final Path dataDirectory;
    private final Logger logger;
    private final Path configFile;

    private List<String> hostnames = new ArrayList<>();
    private List<Pattern> patterns = new ArrayList<>();
    private String kickMessage = "&cUnknown Server: {hostname}\n\n&7Please make sure you are using the correct IP";
    private String invalidMotd = "&cInvalid Hostname. Please make sure you're using the correct IP address";
    private String discordWebhookUrl = "";
    private boolean logAllowedConnections = false;
    private boolean logDeniedConnections = false;

    public PluginConfig(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.configFile = dataDirectory.resolve("hostnames.txt");
    }

    public void load() {
        try {
            Files.createDirectories(dataDirectory);

            // Load main config.yml
            loadMainConfig();

            // Load hostnames.txt
            loadHostnames();

        } catch (IOException e) {
            logger.error("Failed to load configuration", e);
        }
    }

    private void loadMainConfig() throws IOException {
        Path configPath = dataDirectory.resolve("config.yml");

        if (!Files.exists(configPath)) {
            // Create default config
            String defaultConfig = """
                    # Hostname Whitelist Configuration

                    # Message shown when player connects with invalid hostname
                    # Placeholders: {hostname}
                    kick-message: "&cUnknown Server: {hostname}\\n\\n&7Please make sure you are using the correct IP"

                    # MOTD shown for invalid hostname pings
                    invalid-motd: "&cInvalid Hostname. Please use the correct IP address."

                    # Discord Webhook URL for logging connections
                    # Leave empty or set to "YOUR_WEBHOOK_URL_HERE" to disable
                    # The most useless feature that can only be used to spam your channel is not recommended.
                    discord-webhook: "YOUR_WEBHOOK_URL_HERE"

                    # Whether to log allowed connections (true/false)
                    log-allowed-connections: false

                    # Whether to log denied connections (true/false)
                    log-denied-connections: false
                    """;
            Files.writeString(configPath, defaultConfig, StandardCharsets.UTF_8);
        }

        // Parse simple YAML-like config
        List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("#") || line.isEmpty())
                continue;

            if (line.startsWith("kick-message:")) {
                kickMessage = line.substring("kick-message:".length()).trim();
                if (kickMessage.startsWith("\"") && kickMessage.endsWith("\"")) {
                    kickMessage = kickMessage.substring(1, kickMessage.length() - 1);
                }
                kickMessage = kickMessage.replace("\\n", "\n");
            } else if (line.startsWith("invalid-motd:")) {
                invalidMotd = line.substring("invalid-motd:".length()).trim();
                if (invalidMotd.startsWith("\"") && invalidMotd.endsWith("\"")) {
                    invalidMotd = invalidMotd.substring(1, invalidMotd.length() - 1);
                }
            } else if (line.startsWith("discord-webhook:")) {
                discordWebhookUrl = line.substring("discord-webhook:".length()).trim();
                if (discordWebhookUrl.startsWith("\"") && discordWebhookUrl.endsWith("\"")) {
                    discordWebhookUrl = discordWebhookUrl.substring(1, discordWebhookUrl.length() - 1);
                }
            } else if (line.startsWith("log-allowed-connections:")) {
                String value = line.substring("log-allowed-connections:".length()).trim().toLowerCase();
                logAllowedConnections = value.equals("true");
            } else if (line.startsWith("log-denied-connections:")) {
                String value = line.substring("log-denied-connections:".length()).trim().toLowerCase();
                logDeniedConnections = value.equals("true");
            }
        }
    }

    private void loadHostnames() throws IOException {
        if (!Files.exists(configFile)) {
            // Create default hostnames.txt with examples
            String defaultHostnames = """
                    # Hostname & IP Whitelist
                    # Add one hostname or IP per line
                    # Use * as wildcard for subdomains or IP ranges
                    #
                    # Examples:
                    # *.uwu.org.ua     - Allows all subdomains of uwu.org.ua
                    # uwu.org.ua       - Allows exact hostname match
                    # 192.168.1.1      - Allows exact IP address
                    # 192.168.1.*      - Allows IP range 192.168.1.0-255
                    # 10.*.*.*         - Allows all 10.x.x.x addresses
                    #
                    # Lines starting with # are comments

                    *.uwu.org.ua
                    uwu.org.ua
                    felixhost.xyz
                    """;
            Files.writeString(configFile, defaultHostnames, StandardCharsets.UTF_8);
        }

        hostnames.clear();
        patterns.clear();

        List<String> lines = Files.readAllLines(configFile, StandardCharsets.UTF_8);
        for (String line : lines) {
            line = line.trim();

            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            hostnames.add(line);
            patterns.add(createPattern(line));
            logger.info("Loaded hostname pattern: {}", line);
        }
    }

    private Pattern createPattern(String hostname) {
        // Convert wildcard pattern to regex
        // *.example.com -> ^.*\.example\.com$
        // example.com -> ^example\.com$

        String regex = hostname
                .replace(".", "\\.") // Escape dots
                .replace("*", ".*"); // Convert * to .*

        return Pattern.compile("^" + regex + "$", Pattern.CASE_INSENSITIVE);
    }

    public boolean isHostnameAllowed(String hostname) {
        if (hostname == null || hostname.isEmpty()) {
            return false;
        }

        // Remove port if present
        if (hostname.contains(":")) {
            hostname = hostname.substring(0, hostname.indexOf(":"));
        }

        for (Pattern pattern : patterns) {
            if (pattern.matcher(hostname).matches()) {
                return true;
            }
        }

        return false;
    }

    public List<String> getHostnames() {
        return hostnames;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public String getInvalidMotd() {
        return invalidMotd;
    }

    public String getDiscordWebhookUrl() {
        return discordWebhookUrl;
    }

    public boolean isLogAllowedConnections() {
        return logAllowedConnections;
    }

    public boolean isLogDeniedConnections() {
        return logDeniedConnections;
    }
}
