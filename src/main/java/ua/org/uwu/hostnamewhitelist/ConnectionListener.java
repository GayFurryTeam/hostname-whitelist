package ua.org.uwu.hostnamewhitelist;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ConnectionListener {

        private final HostnameWhitelistPlugin plugin;

        public ConnectionListener(HostnameWhitelistPlugin plugin) {
                this.plugin = plugin;
        }

        @Subscribe
        public void onLogin(LoginEvent event) {
                String hostname = event.getPlayer().getVirtualHost()
                                .map(addr -> addr.getHostString())
                                .orElse("");

                String playerIp = event.getPlayer().getRemoteAddress().getAddress().getHostAddress();
                String username = event.getPlayer().getUsername();

                if (!plugin.getConfig().isHostnameAllowed(hostname)) {
                        String kickMessage = plugin.getConfig().getKickMessage()
                                        .replace("{hostname}", hostname);

                        Component message = LegacyComponentSerializer.legacyAmpersand()
                                        .deserialize(kickMessage);

                        event.setResult(LoginEvent.ComponentResult.denied(message));

                        plugin.getLogger().info("Denied connection from {} using hostname: {}",
                                        username, hostname);

                        // Discord webhook logging
                        if (plugin.getConfig().isLogDeniedConnections()) {
                                plugin.getDiscordWebhook().sendDeniedConnection(username, hostname, playerIp);
                        }
                } else {
                        // Log allowed connections to Discord (optional, based on config)
                        if (plugin.getConfig().isLogAllowedConnections()) {
                                plugin.getDiscordWebhook().sendAllowedConnection(username, hostname, playerIp);
                        }
                }
        }

        @Subscribe(priority = Short.MIN_VALUE)
        public void onProxyPing(ProxyPingEvent event) {
                String hostname = event.getConnection().getVirtualHost()
                                .map(addr -> addr.getHostString())
                                .orElse("");

                if (!plugin.getConfig().isHostnameAllowed(hostname)) {
                        Component motd = LegacyComponentSerializer.legacyAmpersand()
                                        .deserialize(plugin.getConfig().getInvalidMotd());

                        ServerPing originalPing = event.getPing();
                        ServerPing.Builder builder = originalPing.asBuilder()
                                        .description(motd)
                                        .clearSamplePlayers()
                                        .onlinePlayers(0)
                                        .maximumPlayers(0);

                        // Hide server version info
                        builder.version(new ServerPing.Version(0, ""));

                        // Remove favicon
                        builder.clearFavicon();

                        event.setPing(builder.build());
                }
        }
}
