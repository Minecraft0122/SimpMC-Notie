package cn.simpmc.notie;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpMCNotiePlugin extends JavaPlugin implements CommandExecutor {

    private static final LegacyComponentSerializer LEGACY_COLORS =
            LegacyComponentSerializer.legacyAmpersand();

    record PrefixDefinition(String text, boolean randomEligible) {}

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Objects.requireNonNull(getCommand("notie"), "Command 'notie' is missing from plugin.yml")
                .setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(color(getConfig().getString(
                    "messages.usage", "&c用法: /" + label + " <内容>")));
            return true;
        }

        List<PrefixDefinition> prefixes = loadPrefixes();
        if (prefixes.isEmpty()) {
            sender.sendMessage(color(getConfig().getString(
                    "messages.no-prefixes", "&c配置文件中没有可用的前缀。")));
            return true;
        }

        PrefixDefinition prefix = selectPrefix(prefixes, label, ThreadLocalRandom.current());
        if (prefix == null) {
            if (requestedIndex(label) == -1) {
                sender.sendMessage(color(getConfig().getString(
                        "messages.no-random-prefixes", "&c配置文件中没有参与随机的前缀。")));
                return true;
            }
            String message = getConfig().getString(
                    "messages.prefix-not-found", "&c前缀 {number} 不存在，目前共有 {count} 个前缀。");
            sender.sendMessage(color(message
                    .replace("{number}", requestedNumber(label))
                    .replace("{count}", Integer.toString(prefixes.size()))));
            return true;
        }

        String separator = getConfig().getString("separator", " ");
        String content = String.join(" ", args);
        boolean allowMessageColors = getConfig().getBoolean("allow-message-colors", true);

        Bukkit.broadcast(formatBroadcast(prefix.text(), separator, content, allowMessageColors));

        String success = getConfig().getString("messages.success", "");
        if (success != null && !success.isBlank()) {
            sender.sendMessage(color(success));
        }
        return true;
    }

    private List<PrefixDefinition> loadPrefixes() {
        List<?> configured = getConfig().getList("prefixes");
        if (configured == null) {
            return List.of();
        }

        List<PrefixDefinition> prefixes = new ArrayList<>();
        for (Object value : configured) {
            if (value instanceof String text) {
                // 兼容 1.1.0 的纯字符串配置，旧前缀默认参与随机。
                prefixes.add(new PrefixDefinition(text, true));
                continue;
            }
            if (value instanceof Map<?, ?> entry && entry.get("text") instanceof String text) {
                Object randomValue = entry.get("random");
                boolean randomEligible = !(randomValue instanceof Boolean) || (Boolean) randomValue;
                prefixes.add(new PrefixDefinition(text, randomEligible));
            }
        }
        return prefixes;
    }

    static PrefixDefinition selectPrefix(
            List<PrefixDefinition> prefixes, String label, RandomGenerator random) {
        int requestedIndex = requestedIndex(label);
        if (requestedIndex == -1) {
            List<PrefixDefinition> randomPrefixes = prefixes.stream()
                    .filter(PrefixDefinition::randomEligible)
                    .toList();
            if (randomPrefixes.isEmpty()) {
                return null;
            }
            return randomPrefixes.get(random.nextInt(randomPrefixes.size()));
        }
        if (requestedIndex >= prefixes.size()) {
            return null;
        }
        return prefixes.get(requestedIndex);
    }

    private static int requestedIndex(String label) {
        String normalized = label.toLowerCase(Locale.ROOT);
        if (normalized.equals("notie")) {
            return -1;
        }
        return Integer.parseInt(normalized.substring("notie".length())) - 1;
    }

    private static String requestedNumber(String label) {
        return label.substring("notie".length());
    }

    static Component formatBroadcast(String prefix, String separator, String content) {
        return formatBroadcast(prefix, separator, content, true);
    }

    static Component formatBroadcast(
            String prefix, String separator, String content, boolean allowMessageColors) {
        Component message = allowMessageColors
                ? LEGACY_COLORS.deserialize(content)
                : Component.text(content);
        return LEGACY_COLORS.deserialize(prefix)
                .append(Component.text(separator))
                .append(message);
    }

    private static Component color(String text) {
        return LEGACY_COLORS.deserialize(text);
    }
}
