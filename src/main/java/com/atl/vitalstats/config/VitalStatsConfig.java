package com.atl.vitalstats.config;

import com.google.common.collect.Lists;
import net.fabricmc.loader.api.FabricLoader;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.Label;
import dev.isxander.yacl3.config.v2.api.autogen.*;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import com.atl.vitalstats.enums.HealthDisplayTypeEnum;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import java.nio.file.Path;
import java.util.List;

public class VitalStatsConfig {
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("vitalstats_config.json");

    public static final ConfigClassHandler<VitalStatsConfig> HANDLER = ConfigClassHandler.createBuilder(VitalStatsConfig.class)
            .id(Identifier.of("vitalstats", "main_config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(CONFIG_PATH)
                    .build())
            .build();

    @Label
    @AutoGen(category = "health_indicator")
    private final Text filtersProTip = Text.literal("Pro Tip: You can temporarily override the below criteria and force health display for all living entities by holding the Right-Arrow key (customizable)").formatted(Formatting.GOLD);

    @Label
    @AutoGen(category = "health_indicator")
    private final Text healthIndicatorLabel = Text.literal("Health Indicator Settings").formatted(Formatting.BOLD, Formatting.GREEN);

    @SerialEntry
    @AutoGen(category = "health_indicator")
    @EnumCycler
    public HealthDisplayTypeEnum indicator_type = HealthDisplayTypeEnum.HEARTS;

    @SerialEntry
    @AutoGen(category = "health_indicator")
    @TickBox
    public boolean passive_mobs = true;

    @SerialEntry
    @AutoGen(category = "health_indicator")
    @TickBox
    public boolean hostile_mobs = true;

    @SerialEntry
    @AutoGen(category = "health_indicator")
    @TickBox
    public boolean players = true;

    @SerialEntry
    @AutoGen(category = "health_indicator")
    @TickBox
    public boolean self = false;

    @Label
    @AutoGen(category = "health_indicator")
    private final Text blacklistLabel = Text.literal("Entity Exclusions").formatted(Formatting.BOLD, Formatting.RED);

    @SerialEntry
    @AutoGen(category = "health_indicator")
    @ListGroup(valueFactory = EntitiesListGroup.class, controllerFactory = EntitiesListGroup.class)
    public List<String> blacklist = Lists.newArrayList("minecraft:armor_stand");

    // Appearance settings (hidden from config menu but accessible via code)
    @SerialEntry
    public int icons_per_row = 10;
    @SerialEntry
    public boolean use_vanilla_textures = true;
    @SerialEntry
    public boolean show_heart_effects = true;
    @SerialEntry
    public boolean render_number_display_shadow = false;
    @SerialEntry
    public boolean render_number_display_background_color = false;
    @SerialEntry
    public boolean percentage_based_health = false;
    @SerialEntry
    public int max_health = 20;
    @SerialEntry
    public float size = 0.025f;
    @SerialEntry
    public double display_offset = 0;
    @SerialEntry
    public double offset_step_size = 1;
    @SerialEntry
    public boolean force_higher_offset_for_players = false;
    @SerialEntry
    public java.awt.Color number_color = java.awt.Color.RED;
    @SerialEntry
    public java.awt.Color number_display_background_color = java.awt.Color.BLACK;

    // Advanced filtering settings (hidden from config menu but accessible via code)
    @SerialEntry
    public boolean after_attack = false;
    @SerialEntry
    public int time_after_hit = 60;
    @SerialEntry
    public boolean damaged_only = false;
    @SerialEntry
    public int max_health_percentage = 100;
    @SerialEntry
    public boolean looking_at = false;
    @SerialEntry
    public int reach = 3;
    @SerialEntry
    public boolean within_distance = false;
    @SerialEntry
    public int distance = 64;
    @SerialEntry
    public boolean override_players = false;

    //TOTEM COUNTER
    @Label
    @AutoGen(category = "totem_counter")
    private final Text totemCounterLabel = Text.literal("Totem Pop Counter").formatted(Formatting.BOLD, Formatting.GOLD);

    @SerialEntry
    @AutoGen(category = "totem_counter")
    @TickBox
    public boolean show_totem_counter = true;

    @SerialEntry
    @AutoGen(category = "totem_counter")
    @TickBox
    public boolean show_for_self = true;

    @SerialEntry
    @AutoGen(category = "totem_counter")
    @TickBox
    public boolean show_for_other_players = true;

    // Hidden totem counter settings (accessible via code only)
    @SerialEntry
    public boolean reset_on_world_change = true;
    @SerialEntry
    public boolean reset_on_respawn = true;
    @SerialEntry
    public boolean enable_debug_logging = false;

    // Hidden messages and commands settings (accessible via code only)
    @SerialEntry
    public com.atl.vitalstats.enums.MessageTypeEnum message_type = com.atl.vitalstats.enums.MessageTypeEnum.ACTIONBAR;
    @SerialEntry
    public boolean colored_messages = true;
    @SerialEntry
    public boolean enable_commands = true;

    public static Screen createScreen(@Nullable Screen parent) {
        return HANDLER.generateGui().generateScreen(parent);
    }

    public Screen createConfigScreen(Screen parent) {
        if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
            return createScreen(parent);
        }
        return null;
    }
}
