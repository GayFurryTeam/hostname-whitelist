package ua.org.uwu.hostnamewhitelist;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "hostname-whitelist", name = "Hostname Whitelist", version = "1.0.0", authors = {
        "uwu.org.ua" }, description = "Whitelist hostnames for connecting to the server")
public class HostnameWhitelistPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private PluginConfig config;
    private DiscordWebhook discordWebhook;

    @Inject
    public HostnameWhitelistPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        loadConfig();
        initDiscordWebhook();
        server.getEventManager().register(this, new ConnectionListener(this));
        logger.info("Hostname Whitelist plugin enabled!");
        logger.info("Loaded {} hostname patterns", config.getHostnames().size());
        if (discordWebhook.isEnabled()) {
            logger.info("Discord webhook logging enabled");
        }
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        loadConfig();
        initDiscordWebhook();
        logger.info("Configuration reloaded! Loaded {} hostname patterns", config.getHostnames().size());
    }

    private void loadConfig() {
        this.config = new PluginConfig(dataDirectory, logger);
        config.load();
    }

    private void initDiscordWebhook() {
        if (discordWebhook != null) {
            discordWebhook.shutdown();
        }
        discordWebhook = new DiscordWebhook(config.getDiscordWebhookUrl(), logger);
    }

    public PluginConfig getConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }

    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (discordWebhook != null) {
            discordWebhook.shutdown();
        }
    }
}
