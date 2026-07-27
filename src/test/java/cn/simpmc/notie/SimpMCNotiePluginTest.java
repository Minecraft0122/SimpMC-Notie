package cn.simpmc.notie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;

class SimpMCNotiePluginTest {

    private static final LegacyComponentSerializer LEGACY_SECTIONS =
            LegacyComponentSerializer.legacySection();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Test
    void prefixColorContinuesIntoContentDespiteTrailingReset() {
        Component result = SimpMCNotiePlugin.formatBroadcast(
                "&6[公告]&r", " ", "服务器将在五分钟后重启");

        assertEquals("§6[公告] 服务器将在五分钟后重启", LEGACY_SECTIONS.serialize(result));
    }

    @Test
    void closedMiniMessagePrefixColorContinuesIntoContent() {
        Component result = SimpMCNotiePlugin.formatBroadcast(
                "<red>[警告]</red>", " ", "请立即回城");

        assertEquals("§c[警告] 请立即回城", LEGACY_SECTIONS.serialize(result));
    }

    @Test
    void closedHexPrefixColorContinuesIntoContent() {
        assertEquals(
                "<#12ABEF>[通知] 正文",
                MINI_MESSAGE.serialize(SimpMCNotiePlugin.formatBroadcast(
                        "<#12ABEF>[通知]</#12ABEF>", " ", "正文")));
    }

    @Test
    void contentColorCanOverrideContinuedPrefixColor() {
        Component result = SimpMCNotiePlugin.formatBroadcast(
                "&c[警告]&r", " ", "&e请立即回城");

        assertEquals("§c[警告] §e请立即回城", LEGACY_SECTIONS.serialize(result));
    }

    @Test
    void translatesColorsInMessage() {
        Component result = SimpMCNotiePlugin.formatBroadcast("&c[警告]", " ", "&e请立即回城");

        assertEquals("§c[警告] §e请立即回城", LEGACY_SECTIONS.serialize(result));
    }

    @Test
    void supportsTraditionalAmpersandCodes() {
        assertEquals(
                MINI_MESSAGE.deserialize("<green>绿色 <bold>粗体<reset>"),
                SimpMCNotiePlugin.parseFormattedText("&a绿色 &l粗体&r"));
    }

    @Test
    void supportsMiniMessageFormatting() {
        String input = "<gradient:#ff0000:#0000ff>渐变文字</gradient>";

        assertEquals(
                MINI_MESSAGE.serialize(MINI_MESSAGE.deserialize(input)),
                MINI_MESSAGE.serialize(SimpMCNotiePlugin.parseFormattedText(input)));
    }

    @Test
    void supportsAmpersandHexColors() {
        assertEquals(
                MINI_MESSAGE.deserialize("<#12ABEF>十六进制"),
                SimpMCNotiePlugin.parseFormattedText("&#12ABEF十六进制"));
    }

    @Test
    void supportsBungeeStyleHexColors() {
        assertEquals(
                MINI_MESSAGE.deserialize("<#12ABEF>十六进制"),
                SimpMCNotiePlugin.parseFormattedText("&x&1&2&A&B&E&F十六进制"));
    }

    @Test
    void supportsMixedLegacyMiniMessageAndHexFormatting() {
        assertEquals(
                MINI_MESSAGE.deserialize("<green>绿色 <bold>粗体</bold> <#12ABEF>十六进制"),
                SimpMCNotiePlugin.parseFormattedText(
                        "&a绿色 <bold>粗体</bold> &#12ABEF十六进制"));
    }

    @Test
    void doesNotEnableInteractiveMiniMessageTags() {
        Component result = SimpMCNotiePlugin.parseFormattedText(
                "<click:run_command:'/op Minecraft0122'><red>不要执行命令</red></click>");

        assertFalse(GsonComponentSerializer.gson().serialize(result).contains("clickEvent"));
    }

    @Test
    void notie2SelectsFirstPrefixExcludedFromRandomPool() {
        List<SimpMCNotiePlugin.PrefixDefinition> prefixes = List.of(
                new SimpMCNotiePlugin.PrefixDefinition("随机一", true),
                new SimpMCNotiePlugin.PrefixDefinition("专用一", false),
                new SimpMCNotiePlugin.PrefixDefinition("专用二", false));

        assertEquals("专用一", SimpMCNotiePlugin.selectPrefix(prefixes, "notie2", new Random(1)).text());
    }

    @Test
    void baseCommandNeverSelectsPrefixExcludedFromRandomPool() {
        List<SimpMCNotiePlugin.PrefixDefinition> prefixes = List.of(
                new SimpMCNotiePlugin.PrefixDefinition("第一个", true),
                new SimpMCNotiePlugin.PrefixDefinition("第二个", true),
                new SimpMCNotiePlugin.PrefixDefinition("指定指令专用", false));

        for (int seed = 0; seed < 100; seed++) {
            SimpMCNotiePlugin.PrefixDefinition selected =
                    SimpMCNotiePlugin.selectPrefix(prefixes, "notie", new Random(seed));
            assertNotNull(selected);
            assertTrue(selected.randomEligible());
        }
    }

    @Test
    void notie2ReturnsNullWhenThereIsNoDedicatedPrefix() {
        List<SimpMCNotiePlugin.PrefixDefinition> prefixes = List.of(
                new SimpMCNotiePlugin.PrefixDefinition("普通", true),
                new SimpMCNotiePlugin.PrefixDefinition("也是普通", true));

        SimpMCNotiePlugin.PrefixDefinition selected =
                SimpMCNotiePlugin.selectPrefix(prefixes, "notie2", new Random(1));

        assertNull(selected);
    }

    @Test
    void pluginMetadataContainsSimpMcBrandingAndCommands() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/plugin.yml")) {
            assertNotNull(input);
            PluginDescriptionFile description = new PluginDescriptionFile(input);

            assertEquals("SimpMC-Notie", description.getName());
            assertEquals("A Plugin For SimpMC Network.", description.getDescription());
            assertEquals(List.of("Minecraft0122", "SimpMC", "GPT-5.6"), description.getAuthors());
            assertEquals("26.1.2", description.getAPIVersion());
            assertTrue(description.isFoliaSupported());
            assertTrue(description.getCommands().containsKey("notie"));
            assertEquals(
                    List.of("notie2"),
                    description.getCommands().get("notie").get("aliases"));
        }
    }
}
