package com.atl.vitalstats.mixin.client;

import com.atl.vitalstats.config.VitalStatsConfig;
import com.atl.vitalstats.core.TotemTracker;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void onPlayerInit(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (VitalStatsConfig.HANDLER.instance().reset_on_respawn) {
            // Reset ALL players' totem counts when the local player respawns
            TotemTracker.resetAllTotemCounts();
        }
    }
}
