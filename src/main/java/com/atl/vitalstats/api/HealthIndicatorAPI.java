package com.atl.vitalstats.api;

import com.atl.vitalstats.api.events.HealthDisplayEvent;
import net.minecraft.entity.LivingEntity;

/**
 * Public API for VitalStats mod integration
 * Other mods can use this to interact with the health indicator system
 */
public class HealthIndicatorAPI {

    /**
     * Register a custom health display event listener
     */
    public static void registerHealthDisplayListener(HealthDisplayEvent.Listener listener) {
        // Implementation will be added when event system is fully implemented
    }

    /**
     * Check if health indicators are currently enabled for an entity
     */
    public static boolean isHealthIndicatorEnabled(LivingEntity entity) {
        // This would integrate with the existing RenderTracker logic
        return true; // Placeholder
    }

    /**
     * Force enable health indicator for a specific entity
     */
    public static void forceEnableHealthIndicator(LivingEntity entity) {
        // Implementation for API control
    }
}
