package cn.simpmc.notie;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpMCNotiePlugin extends JavaPlugin implements TabExecutor {

    private static final TagResolver FORMATTING_TAGS = TagResolver.resolver(
            StandardTags.color(),
            StandardTags.decorations(),
            StandardTags.font(),
            StandardTags.gradient(),
            StandardTags.rainbow(),
            StandardTags.transition(),
            StandardTags.pride(),
            StandardTags.shadowColor(),
            StandardTags.reset(),
            StandardTags.newline());
    private static final MiniMessage MINI_MESSAGE =
            MiniMessage.builder().tags(FORMATTING_TAGS).build();
    private static final Pattern BUNGEE_HEX =
            Pattern.compile("(?i)&x(?:&[0-9a-f]){6}");
    private static final Pattern AMPERSAND_HEX =
            Pattern.compile("(?i)&#([0-9a-f]{6})");
    private static final Pattern LEGACY_CODE =
            Pattern.compile("(?i)&([0-9a-fk-or])");
    private static final Pattern TRAILING_STYLE_BOUNDARY = Pattern.compile(
            "(?i)(?:(?:&|§)r|<reset>|</(?:#[0-9a-f]{6}|[a-z_][a-z0-9_-]*(?::[^>]*)?)>)+$");

    record PrefixDefinition(String text, boolean randomEligible) {}

    @Override
    public void onEnable() {
        saveDefaultConfig();
        PluginCommand command = Objects.requireNonNull(
                getCommand("notie"), "Command 'notie' is missing from plugin.yml");
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public synchronized boolean onCommand(
            CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("notie")
                && args.length == 1
                && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("simpmc.notie.reload")) {
                sender.sendMessage(color(getConfig().getString(
                        "messages.reload-no-permission", "&c你没有权限重载此插件。")));
                return true;
            }
            reloadConfig();
            sender.sendMessage(color(getConfig().getString(
                    "messages.reload-success", "&aSimpMC-Notie 配置已重载。")));
            return true;
        }

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
            String messagePath = label.equalsIgnoreCase("notie2")
                    ? "messages.no-fixed-prefix"
                    : "messages.no-random-prefixes";
            String fallback = label.equalsIgnoreCase("notie2")
                    ? "&c配置文件中没有 random: false 的专用前缀。"
                    : "&c配置文件中没有参与随机的前缀。";
            sender.sendMessage(color(getConfig().getString(messagePath, fallback)));
            return true;
        }

        String separator = getConfig().getString("separator", " ");
        String content = String.join(" ", args);

        Bukkit.broadcast(formatBroadcast(prefix.text(), separator, content));

        String success = getConfig().getString("messages.success", "");
        if (success != null && !success.isBlank()) {
            sender.sendMessage(color(success));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender, Command command, String alias, String[] args) {
        if (alias.equalsIgnoreCase("notie")
                && args.length == 1
                && sender.hasPermission("simpmc.notie.reload")
                && "reload".startsWith(args[0].toLowerCase(Locale.ROOT))) {
            return List.of("reload");
        }
        return List.of();
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
        if (label.equalsIgnoreCase("notie2")) {
            return prefixes.stream()
                    .filter(prefix -> !prefix.randomEligible())
                    .findFirst()
                    .orElse(null);
        }

        List<PrefixDefinition> randomPrefixes = prefixes.stream()
                .filter(PrefixDefinition::randomEligible)
                .toList();
        if (randomPrefixes.isEmpty()) {
            return null;
        }
        return randomPrefixes.get(random.nextInt(randomPrefixes.size()));
    }

    static Component formatBroadcast(String prefix, String separator, String content) {
        return parseFormattedText(continuePrefixStyle(prefix) + separator + content);
    }

    static String continuePrefixStyle(String prefix) {
        return TRAILING_STYLE_BOUNDARY.matcher(prefix).replaceFirst("");
    }

    private static Component color(String text) {
        return parseFormattedText(text);
    }

    static Component parseFormattedText(String text) {
        return MINI_MESSAGE.deserialize(convertLegacyCodes(text));
    }

    static String convertLegacyCodes(String text) {
        String converted = replaceBungeeHex(text);
        converted = replacePattern(converted, AMPERSAND_HEX, match -> "<#" + match.group(1) + ">");
        return replacePattern(converted, LEGACY_CODE, match -> legacyTag(match.group(1).charAt(0)));
    }

    private static String replaceBungeeHex(String text) {
        return replacePattern(text, BUNGEE_HEX, match -> {
            String value = match.group();
            return "<#" + value.charAt(3) + value.charAt(5) + value.charAt(7)
                    + value.charAt(9) + value.charAt(11) + value.charAt(13) + ">";
        });
    }

    private static String replacePattern(
            String text, Pattern pattern, java.util.function.Function<Matcher, String> replacement) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.apply(matcher)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String legacyTag(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> "<black>";
            case '1' -> "<dark_blue>";
            case '2' -> "<dark_green>";
            case '3' -> "<dark_aqua>";
            case '4' -> "<dark_red>";
            case '5' -> "<dark_purple>";
            case '6' -> "<gold>";
            case '7' -> "<gray>";
            case '8' -> "<dark_gray>";
            case '9' -> "<blue>";
            case 'a' -> "<green>";
            case 'b' -> "<aqua>";
            case 'c' -> "<red>";
            case 'd' -> "<light_purple>";
            case 'e' -> "<yellow>";
            case 'f' -> "<white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default -> throw new IllegalArgumentException("Unsupported legacy color code: " + code);
        };
    }
}
