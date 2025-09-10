package com.atl.vitalstats.api.events;

import net.minecraft.entity.LivingEntity;

/**
 * Event fired when a health display is about to be rendered
 */
public class HealthDisplayEvent {

    private final LivingEntity entity;
    private boolean cancelled = false;

    public HealthDisplayEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @FunctionalInterface
    public interface Listener {
        void onHealthDisplay(HealthDisplayEvent event);
    }
}
