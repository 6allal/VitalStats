package com.atl.vitalstats.mixin.client;

import com.atl.vitalstats.config.VitalStatsConfig;
import com.atl.vitalstats.core.TotemTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    public ClientWorld world;

    @Shadow
    public ClientPlayerEntity player;

    private ClientWorld lastWorld = null;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onClientTick(CallbackInfo ci) {
        // Check if world has changed
        if (world != lastWorld) {
            if (lastWorld != null && VitalStatsConfig.HANDLER.instance().reset_on_world_change) {
                TotemTracker.resetTotemCountsForWorld();
            }
            lastWorld = world;
        }
    }
}
