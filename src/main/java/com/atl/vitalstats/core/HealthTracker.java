package com.atl.vitalstats.core;

import com.atl.vitalstats.client.render.RenderTracker;
import net.minecraft.entity.LivingEntity;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core health tracking system for managing entity health display state
 */
public class HealthTracker {

    private static final ConcurrentHashMap<UUID, HealthData> trackedEntities = new ConcurrentHashMap<>();

    public static void addEntity(LivingEntity entity) {
        trackedEntities.put(entity.getUuid(), new HealthData(entity));
    }

    public static void removeEntity(UUID entityId) {
        trackedEntities.remove(entityId);
    }

    public static boolean isTracked(LivingEntity entity) {
        return trackedEntities.containsKey(entity.getUuid());
    }

    public static void updateHealth(LivingEntity entity) {
        HealthData data = trackedEntities.get(entity.getUuid());
        if (data != null) {
            data.updateHealth(entity);
        }
    }

    public static void cleanup() {
        trackedEntities.entrySet().removeIf(entry -> entry.getValue().shouldRemove());
    }

    private static class HealthData {
        private float lastHealth;
        private float lastMaxHealth;
        private long lastUpdate;

        public HealthData(LivingEntity entity) {
            this.lastHealth = entity.getHealth();
            this.lastMaxHealth = entity.getMaxHealth();
            this.lastUpdate = System.currentTimeMillis();
        }

        public void updateHealth(LivingEntity entity) {
            this.lastHealth = entity.getHealth();
            this.lastMaxHealth = entity.getMaxHealth();
            this.lastUpdate = System.currentTimeMillis();
        }

        public boolean shouldRemove() {
            return System.currentTimeMillis() - lastUpdate > 60000; // Remove after 1 minute of inactivity
        }
    }
}
