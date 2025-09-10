package com.atl.vitalstats.fabric.commands;

import com.atl.vitalstats.VitalStatsCommon;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.atl.vitalstats.config.VitalStatsConfig;
import com.atl.vitalstats.enums.HealthDisplayTypeEnum;
import com.atl.vitalstats.config.ConfigUtils;
import com.atl.vitalstats.util.math.Maths;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ModCommands {
    @Environment(EnvType.CLIENT)
    public static void registerCommands(){
        ClientCommandRegistrationCallback.EVENT.register(ModCommands::configCommands);
        ClientCommandRegistrationCallback.EVENT.register(ModCommands::openModMenuCommand);
    }

    public static final SuggestionProvider<FabricClientCommandSource> CLIENT_SUMMONABLE_ENTITIES = SuggestionProviders.register(Identifier.of("vitalstats","summonable_entities"), (context, builder) -> CommandSource.suggestFromIdentifier(Registries.ENTITY_TYPE.stream().filter(entityType -> entityType.isEnabled(((CommandSource)context.getSource()).getEnabledFeatures()) && entityType.isSummonable()), builder, EntityType::getId, entityType -> Text.translatable(Util.createTranslationKey("entity", EntityType.getId(entityType)))));

    private static void configCommands(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess) {
        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("vitalstats")
            .then(ClientCommandManager.literal("offset")
                .then(ClientCommandManager.argument("offset", DoubleArgumentType.doubleArg())
                    .executes(context -> {
                        VitalStatsConfig.HANDLER.instance().display_offset = DoubleArgumentType.getDouble(context, "offset");
                        VitalStatsConfig.HANDLER.save();
                        ConfigUtils.sendMessage(context.getSource().getPlayer(), Text.literal("Set heart offset to " + Maths.truncate(VitalStatsConfig.HANDLER.instance().display_offset,2)));
                        return 1;
                    })))

            .then(ClientCommandManager.literal("indicator-type")
                .then(ClientCommandManager.argument("indicator_type", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        builder.suggest("heart");
                        builder.suggest("number");
                        return builder.buildFuture();
                    })
                        .executes(context -> {
                            HealthDisplayTypeEnum displayTypeEnum;
                            if (StringArgumentType.getString(context, "indicator_type").equals("heart")) {
                                displayTypeEnum = HealthDisplayTypeEnum.HEARTS;
                            } else if (StringArgumentType.getString(context, "indicator_type").equals("number")) {
                                displayTypeEnum = HealthDisplayTypeEnum.NUMBER;
                            } else {
                                ConfigUtils.sendMessage(context.getSource().getPlayer(), Text.literal("Unknown argument, please try again."));
                                return 1;
                            }

                            VitalStatsConfig.HANDLER.instance().indicator_type = displayTypeEnum;
                            VitalStatsConfig.HANDLER.save();
                            ConfigUtils.sendMessage(context.getSource().getPlayer(), Text.literal("Set display type to " + VitalStatsConfig.HANDLER.instance().indicator_type));
                            return 1;
                        })))
        );
    }

    private static void openModMenuCommand(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess) {
        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("vitalstats")
            .executes(context -> {
                VitalStatsCommon.openConfig();
                return 1;
        }));
    }
}
