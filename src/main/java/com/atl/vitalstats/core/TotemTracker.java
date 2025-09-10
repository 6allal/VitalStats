package com.atl.vitalstats.core;

import com.atl.vitalstats.VitalStatsCommon;
import com.atl.vitalstats.config.VitalStatsConfig;
import net.minecraft.entity.player.PlayerEntity;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core totem tracking system for managing player totem usage counts
 */
public class TotemTracker {

    private static final ConcurrentHashMap<UUID, TotemData> trackedPlayers = new ConcurrentHashMap<>();

    public static void addTotemPop(PlayerEntity player) {
        UUID playerId = player.getUuid();
        String playerName = player.getNameForScoreboard();

        // Debug logging - only if enabled in config
        if (VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
            VitalStatsCommon.LOGGER.info("🟢 TOTEM POP DETECTED for player: {} (UUID: {})", playerName, playerId);
        }

        TotemData data = trackedPlayers.get(playerId);
        if (data == null) {
            data = new TotemData();
            trackedPlayers.put(playerId, data);
            if (VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
                VitalStatsCommon.LOGGER.info("📝 Created new totem data for player: {}", playerName);
            }
        }

        int oldCount = data.getCount();
        data.incrementCount();
        int newCount = data.getCount();

        if (VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
            VitalStatsCommon.LOGGER.info("🔢 Totem count for {}: {} -> {} (Total tracked players: {})",
                playerName, oldCount, newCount, trackedPlayers.size());
        }
    }

    public static int getTotemCount(PlayerEntity player) {
        TotemData data = trackedPlayers.get(player.getUuid());
        int count = data != null ? data.getCount() : 0;

        // Debug logging when count is requested - only if enabled in config
        if (count > 0 && VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
            VitalStatsCommon.LOGGER.debug("📊 Returning totem count {} for player: {}", count, player.getNameForScoreboard());
        }

        return count;
    }

    public static boolean hasTotemPops(PlayerEntity player) {
        boolean hasPops = getTotemCount(player) > 0;
        if (VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
            VitalStatsCommon.LOGGER.debug("❓ hasTotemPops for {}: {}", player.getNameForScoreboard(), hasPops);
        }
        return hasPops;
    }

    public static void resetTotemCount(PlayerEntity player) {
        UUID playerId = player.getUuid();
        TotemData removed = trackedPlayers.remove(playerId);
        if (removed != null && VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
            VitalStatsCommon.LOGGER.info("🔄 Reset totem count for player: {} (was: {})",
                player.getNameForScoreboard(), removed.getCount());
        }
    }

    public static void resetAllTotemCounts() {
        int count = trackedPlayers.size();
        trackedPlayers.clear();
        if (VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
            VitalStatsCommon.LOGGER.info("🧹 Reset ALL totem counts ({} players cleared)", count);
        }
    }

    public static void resetTotemCountsForWorld() {
        // Reset all totem counts when changing worlds/dimensions
        int count = trackedPlayers.size();
        trackedPlayers.clear();
        if (VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
            VitalStatsCommon.LOGGER.info("🌍 Reset totem counts for world change ({} players cleared)", count);
        }
    }

    public static void cleanup() {
        // Remove old entries (older than 10 minutes)
        long currentTime = System.currentTimeMillis();
        int sizeBefore = trackedPlayers.size();
        trackedPlayers.entrySet().removeIf(entry ->
            currentTime - entry.getValue().getLastUpdate() > 600000);
        int sizeAfter = trackedPlayers.size();

        if (sizeBefore != sizeAfter && VitalStatsConfig.HANDLER.instance().enable_debug_logging) {
            VitalStatsCommon.LOGGER.info("🧽 Cleanup removed {} old totem entries ({} -> {})",
                sizeBefore - sizeAfter, sizeBefore, sizeAfter);
        }
    }

    private static class TotemData {
        private int count;
        private long lastUpdate;

        public TotemData() {
            this.count = 0;
            this.lastUpdate = System.currentTimeMillis();
        }

        public void incrementCount() {
            this.count++;
            this.lastUpdate = System.currentTimeMillis();
        }

        public int getCount() {
            return count;
        }

        public long getLastUpdate() {
            return lastUpdate;
        }
    }
}
