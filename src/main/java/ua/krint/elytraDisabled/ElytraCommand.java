package ua.krint.elytraDisabled;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElytraCommand implements CommandExecutor, TabCompleter {

    private final ElytraDisabled plugin;

    public ElytraCommand(ElytraDisabled plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("elytradisabled.reload")) {
            sender.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            try {
                plugin.reloadPluginConfig();
                sender.sendMessage(plugin.getMessage("reload_success"));
            } catch (Exception e) {
                sender.sendMessage(plugin.getMessage("reload_error"));
                sender.sendMessage("§c" + e.getMessage());
                e.printStackTrace();
            }
            return true;
        }

        sendHelp(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("elytradisabled.reload")) {
            return completions;
        }

        if (args.length == 1) {
            completions.add("reload");

            String partial = args[0].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(partial));

            Collections.sort(completions);
            return completions;
        }

        return completions;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessage("help_reload"));
    }
}