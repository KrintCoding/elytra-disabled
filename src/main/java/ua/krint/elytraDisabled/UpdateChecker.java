package ua.krint.elytraDisabled;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private final String currentVersion;
    private String latestVersion = null;
    private String downloadUrl = null;
    private boolean updateAvailable = false;

    private static final String PROJECT_SLUG = "3LjsN86o";

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
    }

    public void checkForUpdates(Consumer<Boolean> consumer) {
        if (PROJECT_SLUG == null || PROJECT_SLUG.isEmpty()) {
            plugin.getLogger().warning("Update checker is not configured. Set PROJECT_SLUG in UpdateChecker.java");
            plugin.getLogger().warning("Example: private static final String PROJECT_SLUG = \"elytra-disabled\";");
            consumer.accept(false);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/" + PROJECT_SLUG + "/version");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "ElytraDisabled/" + currentVersion + " (Modrinth)");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    plugin.getLogger().warning("Failed to check for updates. HTTP " + responseCode);
                    plugin.getLogger().warning("Check if PROJECT_SLUG is correct: " + PROJECT_SLUG);
                    consumer.accept(false);
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String jsonResponse = response.toString();
                latestVersion = parseLatestVersion(jsonResponse);

                if (latestVersion != null && !latestVersion.isEmpty()) {
                    downloadUrl = "https://modrinth.com/plugin/" + PROJECT_SLUG;

                    updateAvailable = !currentVersion.equals(latestVersion);

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (updateAvailable) {
                            plugin.getLogger().warning("╔════════════════════════════════════════╗");
                            plugin.getLogger().warning("║     New version available!             ║");
                            plugin.getLogger().warning("║  Current: " + formatVersion(currentVersion) + "→  Latest: " + formatVersion(latestVersion) + "    ║");
                            plugin.getLogger().warning("║  Download: modrinth.com/plugin/3LjsN86o║");
                            plugin.getLogger().warning("╚════════════════════════════════════════╝");
                        } else {
                            plugin.getLogger().info("Plugin is up to date! (v" + currentVersion + ")");
                        }
                        consumer.accept(updateAvailable);
                    });
                } else {
                    plugin.getLogger().warning("Could not parse version from Modrinth API response");
                    consumer.accept(false);
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
                if (e.getMessage() != null && e.getMessage().contains("UnknownHost")) {
                    plugin.getLogger().warning("Check your internet connection");
                }
                consumer.accept(false);
            }
        });
    }

    private String parseLatestVersion(String json) {
        try {
            int versionIndex = json.indexOf("\"version_number\"");
            if (versionIndex == -1) return null;

            int startQuote = json.indexOf("\"", versionIndex + 17);
            if (startQuote == -1) return null;

            int endQuote = json.indexOf("\"", startQuote + 1);
            if (endQuote == -1) return null;

            return json.substring(startQuote + 1, endQuote);
        } catch (Exception e) {
            plugin.getLogger().warning("Error parsing version from JSON: " + e.getMessage());
            return null;
        }
    }

    private String formatVersion(String version) {
        return String.format("%-7s", version);
    }

    public void notifyPlayer(Player player) {
        if (!updateAvailable) return;

        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6§l[ElytraDisabled] §eNew update available!");
        player.sendMessage("§7Current version: §c" + currentVersion);
        player.sendMessage("§7Latest version: §a" + latestVersion);
        player.sendMessage("§7Download: §b" + downloadUrl);
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }
}