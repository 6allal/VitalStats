package com.atl.vitalstats.mixin.client;

import com.atl.vitalstats.VitalStatsCommon;
import com.atl.vitalstats.config.VitalStatsConfig;
import com.atl.vitalstats.core.TotemTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect totem usage through entity status packets (multiplayer-compatible)
 * Based on the proven approach from uku3lig's totemcounter mod
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientWorld world;

    @Inject(method = "onEntityStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V"))
    public void onTotemParticles(EntityStatusS2CPacket packet, CallbackInfo ci) {
        Entity entity = packet.getEntity(world);

        // Debug logging for all entity status packets that trigger particle effects - only if enabled in config
        if (VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
            VitalStatsCommon.LOGGER.info("🎆 Entity status packet received - Entity: {} (Type: {}), Status: {}",
                entity != null ? entity.getName().getString() : "null",
                entity != null ? entity.getType().toString() : "null",
                packet.getStatus());
        }

        // Handle both other players and the local player
        if (entity instanceof PlayerEntity player) {
            if (VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
                VitalStatsCommon.LOGGER.info("🎯 TOTEM DETECTION TRIGGERED - Player: {} (Class: {})",
                    player.getNameForScoreboard(),
                    player.getClass().getSimpleName());
            }

            TotemTracker.addTotemPop(player);
        } else if (entity != null && VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
            VitalStatsCommon.LOGGER.info("❌ Entity is not a player: {} (Type: {})",
                entity.getName().getString(),
                entity.getClass().getSimpleName());
        } else if (entity == null && VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
            VitalStatsCommon.LOGGER.warn("⚠️ Entity is null in totem detection mixin!");
        }
    }
}
