package com.atl.vitalstats.fabric;

import com.atl.vitalstats.VitalStatsCommon;
import com.atl.vitalstats.client.render.RenderTracker;
import com.atl.vitalstats.config.Config;
import com.atl.vitalstats.config.VitalStatsConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public class VitalStatsMod implements ClientModInitializer {
    public static final String MOD_ID = VitalStatsCommon.MOD_ID;

    public static final KeyBinding HEARTS_RENDERING_ENABLED = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".renderingEnabled",
            InputUtil.GLFW_KEY_LEFT,
            "key.categories." + MOD_ID
    ));

    public static final KeyBinding OVERRIDE_ALL_FILTERS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".overrideAllFilters",
            InputUtil.GLFW_KEY_RIGHT,
            "key.categories." + MOD_ID
    ));
    public static final KeyBinding INCREASE_HEART_OFFSET = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".increaseHeartOffset",
            InputUtil.GLFW_KEY_UP,
            "key.categories." + MOD_ID
    ));
    public static final KeyBinding DECREASE_HEART_OFFSET = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".decreaseHeartOffset",
            InputUtil.GLFW_KEY_DOWN,
            "key.categories." + MOD_ID
    ));

    public static final KeyBinding OPEN_CONFIG_SCREEN = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".openModMenuConfig",
            InputUtil.GLFW_KEY_I,
            "key.categories." + MOD_ID
    ));

    public static final KeyBinding RESET_TOTEM_COUNTERS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.vitalstats.reset_totem_counters",
            InputUtil.Type.KEYSYM,
            -1, // No default key
            "category.vitalstats.keybindings"
    ));

    @Override
    public void onInitializeClient() {
        VitalStatsCommon.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            VitalStatsCommon.tick();

            while (HEARTS_RENDERING_ENABLED.wasPressed()) {
                VitalStatsCommon.enableHeartsRendering();
            }

            while (INCREASE_HEART_OFFSET.wasPressed()) {
                VitalStatsCommon.increaseOffset();
            }

            while (DECREASE_HEART_OFFSET.wasPressed()) {
                VitalStatsCommon.decreaseOffset();
            }
            if (OVERRIDE_ALL_FILTERS.wasPressed()) {
                VitalStatsCommon.overrideFilters();
            }
            else if(Config.getOverrideAllFiltersEnabled()) {
                VitalStatsCommon.disableOverrideFilters();
            }

            if(OPEN_CONFIG_SCREEN.wasPressed()) VitalStatsCommon.openConfigScreen();

            while (RESET_TOTEM_COUNTERS.wasPressed()) {
                VitalStatsCommon.resetTotemCounts();
            }
        });

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            RenderTracker.removeFromUUIDS(entity);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            VitalStatsConfig.HANDLER.save();
        });
    }
}
