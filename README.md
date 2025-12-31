# Hostname Whitelist

A simple yet powerful Velocity proxy plugin that restricts server access to
specific hostnames or IP addresses. This is useful for preventing direct IP
connections or enforcing the use of specific domains (e.g., `play.example.com`).

## Features

- **Hostname Whitelisting**: Only allow connections from specified domains or
  IPs.
- **Wildcard Support**: Easily whitelist subdomains (e.g., `*.uwu.org.ua`) or IP
  ranges (e.g., `192.168.1.*`).
- **Customizable Messages**: Configure the kick message shown to blocked
  players.
- **Custom MOTD**: Display a specific MOTD for players pinging with an invalid
  hostname.
- **Discord Webhook Logging**: Optionally log allowed and blocked connections to
  a Discord channel.

## Installation

1. Download the latest release of the plugin.
2. Place the `.jar` file into your Velocity server's `plugins` folder.
3. Restart your proxy server.

## Configuration

The plugin creates a configuration folder at `plugins/hostname-whitelist/`
containing two files: `config.yml` and `hostnames.txt`.

### config.yml

This file handles the general plugin settings.

```yaml
# Hostname Whitelist Configuration

# Message shown when player connects with invalid hostname
# Placeholders: {hostname}
kick-message: "&cUnknown Server: {hostname}\n\n&7Please make sure you are using the correct IP"

# MOTD shown for invalid hostname pings
invalid-motd: "&cInvalid Hostname. Please use the correct IP address."

# Discord Webhook URL for logging connections
# Leave empty or set to "YOUR_WEBHOOK_URL_HERE" to disable
discord-webhook: "YOUR_WEBHOOK_URL_HERE"

# Whether to log allowed connections (true/false)
log-allowed-connections: false

# Whether to log denied connections (true/false)
log-denied-connections: false
```

### hostnames.txt

This file is where you define which hostnames or IPs are allowed. You can add
one entry per line.

**Examples:**

```text
# Allow all subdomains of uwu.org.ua
*.uwu.org.ua

# Allow exact hostname match
uwu.org.ua

# Allow specific IP address
192.168.1.1

# Allow IP range 192.168.1.0-255
192.168.1.*
```

## Discord Webhook

To use the Discord logging feature:

1. Create a Webhook in your Discord server (Server Settings -> Integrations ->
   Webhooks).
2. Copy the Webhook URL.
3. Paste it into the `discord-webhook` field in `config.yml`.
4. Enable `log-allowed-connections` or `log-denied-connections` as desired.

## License

This project is licensed under the MIT License.
