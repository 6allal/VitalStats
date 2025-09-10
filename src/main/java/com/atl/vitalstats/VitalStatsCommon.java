package com.atl.vitalstats;

import com.atl.vitalstats.config.Config;
import com.atl.vitalstats.config.VitalStatsConfig;
import com.atl.vitalstats.config.ConfigUtils;
import com.atl.vitalstats.util.math.Maths;
import com.atl.vitalstats.client.render.RenderTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VitalStatsCommon {
    public static final String MOD_ID = "vitalstats";
    public static final MinecraftClient client = MinecraftClient.getInstance();
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean changed = false;
    private static boolean openConfig = false;

    public static void init() {
        VitalStatsConfig.HANDLER.load();
        Config.load();
        LOGGER.info("Never be heartless!");
    }

    public static void tick(){
        if(openConfig){
            Screen configScreen = VitalStatsConfig.createScreen(client.currentScreen);
            client.setScreen(configScreen);
            openConfig = false;
        }

        if(client.world == null) return;
        if(changed && client.world.getTime() % 200 == 0){
            VitalStatsConfig.HANDLER.save();
            changed = false;
        }

        RenderTracker.tick(client);

        // Clean up old totem tracking data
        com.atl.vitalstats.core.TotemTracker.cleanup();
    }

    public static void openConfig(){
        openConfig = client.world != null;
    }

    public static void enableHeartsRendering(){
        Config.setHeartsRenderingEnabled(!Config.getHeartsRenderingEnabled());
        if (client.player != null) {
            Formatting formatting;
            if(VitalStatsConfig.HANDLER.instance().colored_messages) formatting = Config.getHeartsRenderingEnabled() ? Formatting.GREEN : Formatting.RED;
            else formatting = Formatting.WHITE;
            ConfigUtils.sendMessage(client.player, Text.literal((Config.getHeartsRenderingEnabled() ? "Enabled" : "Disabled") + " Health Indicators").formatted(formatting));
        }
    }

    public static void increaseOffset(){
        VitalStatsConfig.HANDLER.instance().display_offset = (VitalStatsConfig.HANDLER.instance().display_offset + VitalStatsConfig.HANDLER.instance().offset_step_size);
        changed = true;
        if (client.player != null) {
            ConfigUtils.sendMessage(client.player, Text.literal("Set heart offset to " + Maths.truncate(VitalStatsConfig.HANDLER.instance().display_offset,2)));
        }
    }
    public static void decreaseOffset(){
        VitalStatsConfig.HANDLER.instance().display_offset = (VitalStatsConfig.HANDLER.instance().display_offset - VitalStatsConfig.HANDLER.instance().offset_step_size);
        changed = true;
        if (client.player != null) {
            ConfigUtils.sendMessage(client.player, Text.literal("Set heart offset to " + Maths.truncate(VitalStatsConfig.HANDLER.instance().display_offset,2)));
        }
    }

    public static void overrideFilters(){
        Config.setOverrideAllFiltersEnabled(true);
        if (client.player != null) {
            ConfigUtils.sendOverlayMessage(client.player, Text.literal( " Config Criteria " + (Config.getOverrideAllFiltersEnabled() ? "Temporarily Overridden" : "Re-implemented")));
        }
    }

    public static void disableOverrideFilters(){
        Config.setOverrideAllFiltersEnabled(false);
        client.inGameHud.setOverlayMessage(Text.literal(""), false);
    }

    public static void openConfigScreen(){
        openConfig();
    }

    public static void resetTotemCounts(){
        com.atl.vitalstats.core.TotemTracker.resetAllTotemCounts();
        if (client.player != null) {
            Formatting formatting = VitalStatsConfig.HANDLER.instance().colored_messages ? Formatting.GREEN : Formatting.WHITE;
            ConfigUtils.sendMessage(client.player, Text.literal("Reset all totem counters").formatted(formatting));
        }
    }
}
