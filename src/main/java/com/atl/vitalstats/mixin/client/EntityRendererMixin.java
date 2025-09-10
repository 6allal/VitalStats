package com.atl.vitalstats.mixin.client;

import com.atl.vitalstats.client.render.RenderTracker;
import com.atl.vitalstats.config.Config;
import com.atl.vitalstats.config.VitalStatsConfig;
import com.atl.vitalstats.enums.HealthDisplayTypeEnum;
import com.atl.vitalstats.enums.HeartTypeEnum;
import com.atl.vitalstats.enums.TotemTypeEnum;
import com.atl.vitalstats.util.render.HeartJumpData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.atl.vitalstats.enums.HeartTypeEnum.addHardcoreIcon;
import static com.atl.vitalstats.enums.HeartTypeEnum.addStatusIcon;


@Mixin(LivingEntityRenderer.class)
public abstract class EntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {

    @Unique private final MinecraftClient client = MinecraftClient.getInstance();
    protected EntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    public void render(T livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {

        if (RenderTracker.isInUUIDS(livingEntity) || (Config.getOverrideAllFiltersEnabled() && !RenderTracker.isInvalid(livingEntity))) {
            if(Config.getHeartsRenderingEnabled() || Config.getOverrideAllFiltersEnabled()) {
                if (VitalStatsConfig.HANDLER.instance().indicator_type == HealthDisplayTypeEnum.HEARTS)
                    renderHearts(livingEntity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
                else if (VitalStatsConfig.HANDLER.instance().indicator_type == HealthDisplayTypeEnum.NUMBER)
                    renderNumber(livingEntity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
            }
        }

        // Render totem counter for players - moved outside main rendering condition
        if (livingEntity instanceof PlayerEntity player && VitalStatsConfig.HANDLER.instance().show_totem_counter) {
            boolean shouldShow = false;
            if (player == client.player && VitalStatsConfig.HANDLER.instance().show_for_self) {
                shouldShow = true;
            } else if (player != client.player && VitalStatsConfig.HANDLER.instance().show_for_other_players) {
                shouldShow = true;
            }

            if (shouldShow) {
                renderTotemCounter(player, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
            }
        }
    }

    @Unique private void renderHearts(T livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light){
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexConsumer;

        double d = this.dispatcher.getSquaredDistanceToCamera(livingEntity);

        int healthRed = MathHelper.ceil(livingEntity.getHealth());
        int maxHealth = MathHelper.ceil(livingEntity.getMaxHealth());
        int healthYellow = MathHelper.ceil(livingEntity.getAbsorptionAmount());

        if(VitalStatsConfig.HANDLER.instance().percentage_based_health) {
            healthRed = MathHelper.ceil(((float) healthRed /maxHealth) * VitalStatsConfig.HANDLER.instance().max_health);
            maxHealth = MathHelper.ceil(VitalStatsConfig.HANDLER.instance().max_health);
            healthYellow = MathHelper.ceil(livingEntity.getAbsorptionAmount());
        }

        int heartsRed = MathHelper.ceil(healthRed / 2.0F);
        boolean lastRedHalf = (healthRed & 1) == 1;
        int heartsNormal = MathHelper.ceil(maxHealth / 2.0F);
        int heartsYellow = MathHelper.ceil(healthYellow / 2.0F);
        boolean lastYellowHalf = (healthYellow & 1) == 1;
        int heartsTotal = heartsNormal + heartsYellow;

        int heartsPerRow = VitalStatsConfig.HANDLER.instance().icons_per_row;
        int pixelsTotal = Math.min(heartsTotal, heartsPerRow) * 8 + 1;
        float maxX = pixelsTotal / 2.0f;

        double heartDensity = 50F - (Math.max(4F - Math.ceil((double) heartsTotal / heartsPerRow), -3F) * 5F);
        double h = 0;

        for (int isDrawingEmpty = 0; isDrawingEmpty < 2; isDrawingEmpty++) {
            for (int heart = 0; heart < heartsTotal; heart++) {
                if (heart % heartsPerRow == 0) {
                    h = heart / heartDensity;
                }

                matrixStack.push();
                float scale = VitalStatsConfig.HANDLER.instance().size;
                vertexConsumer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

                matrixStack.translate(0, livingEntity.getHeight() + 0.5f + h, 0);

                if (livingEntity.hasStatusEffect(StatusEffects.REGENERATION) && VitalStatsConfig.HANDLER.instance().show_heart_effects) {
                    if(HeartJumpData.getWhichHeartJumping(livingEntity) == heart){
                        matrixStack.translate(0.0D, 1.15F * scale, 0.0D);
                    }
                }

                if ((this.hasLabel(livingEntity)
                        || (VitalStatsConfig.HANDLER.instance().force_higher_offset_for_players && livingEntity instanceof PlayerEntity && livingEntity != client.player))
                        && d <= 4096.0) {
                    matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                    if (d < 100.0 && livingEntity instanceof PlayerEntity && livingEntity.getEntityWorld().getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                        matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                    }
                }

                matrixStack.multiply(this.dispatcher.getRotation());
                matrixStack.scale(-scale, scale, scale);
                matrixStack.translate(0, VitalStatsConfig.HANDLER.instance().display_offset, 0);
                Matrix4f model = matrixStack.peek().getPositionMatrix();

                float x = maxX - (heart % heartsPerRow) * 8;

                if (isDrawingEmpty == 0) {
                    drawHeart(model, vertexConsumer, x, HeartTypeEnum.EMPTY, livingEntity);
                } else {
                    HeartTypeEnum type;
                    if (heart < heartsRed) {
                        type = HeartTypeEnum.RED_FULL;
                        if (heart == heartsRed - 1 && lastRedHalf) {
                            type = HeartTypeEnum.RED_HALF;
                        }
                    } else if (heart < heartsNormal) {
                        type = HeartTypeEnum.EMPTY;
                    } else {
                        type = HeartTypeEnum.YELLOW_FULL;
                        if (heart == heartsTotal - 1 && lastYellowHalf) {
                            type = HeartTypeEnum.YELLOW_HALF;
                        }
                    }
                    if (type != HeartTypeEnum.EMPTY) {
                        drawHeart(model, vertexConsumer, x, type, livingEntity);
                    }
                }

                BuiltBuffer builtBuffer;
                try {
                    builtBuffer = vertexConsumer.endNullable();
                    if(builtBuffer != null){
                        BufferRenderer.drawWithGlobalProgram(builtBuffer);
                        builtBuffer.close();
                    }
                }
                catch (Exception e){
                    // Silently handle rendering exceptions to prevent crashes during heart rendering
                }
                matrixStack.pop();
            }
        }
    }

    @Unique
    private void renderNumber(T livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light){
        double d = this.dispatcher.getSquaredDistanceToCamera(livingEntity);

        float health = livingEntity.getHealth();
        float maxHealth = livingEntity.getMaxHealth();
        float absorption = livingEntity.getAbsorptionAmount();

        String healthText;
        if(VitalStatsConfig.HANDLER.instance().percentage_based_health) {
            // Only use actual health, not absorption, for percentage calculation
            float percentage = (health / maxHealth) * 100;
            healthText = String.format("%.0f%%", percentage);
        } else {
            // Only display actual health, not absorption
            healthText = String.format("%.0f", health);
        }

        matrixStack.push();
        float scale = VitalStatsConfig.HANDLER.instance().size;

        matrixStack.translate(0, livingEntity.getHeight() + 0.5f, 0);
        if ((this.hasLabel(livingEntity)
                || (VitalStatsConfig.HANDLER.instance().force_higher_offset_for_players && livingEntity instanceof PlayerEntity && livingEntity != client.player))
                && d <= 4096.0) {
            matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
            if (d < 100.0 && livingEntity instanceof PlayerEntity && livingEntity.getEntityWorld().getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
            }
        }

        matrixStack.multiply(this.dispatcher.getRotation());

        // Render the heart icon first with correct scaling
        if (!VitalStatsConfig.HANDLER.instance().percentage_based_health) {
            matrixStack.push(); // Separate matrix stack for heart icon
            matrixStack.scale(-scale, scale, scale); // Use same scaling as hearts rendering
            matrixStack.translate(0, VitalStatsConfig.HANDLER.instance().display_offset, 0);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexConsumer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

            Matrix4f model = matrixStack.peek().getPositionMatrix();

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            float textWidth = textRenderer.getWidth(healthText);
            float heartIconX = -1.5f; // Fixed position for heart icon (won't change based on number width)
            float heartIconY = 1.0f; // Move heart icon slightly higher

            // Determine heart type based on health status
            HeartTypeEnum heartType = HeartTypeEnum.RED_FULL;
            if (absorption > 0) {
                heartType = HeartTypeEnum.YELLOW_FULL;
            }

            // Draw heart with Y offset
            drawHeartWithOffset(model, vertexConsumer, heartIconX, heartIconY, heartType, livingEntity);

            BuiltBuffer builtBuffer;
            try {
                builtBuffer = vertexConsumer.endNullable();
                if(builtBuffer != null){
                    BufferRenderer.drawWithGlobalProgram(builtBuffer);
                    builtBuffer.close();
                }
            }
            catch (Exception e){
                // Silently handle rendering exceptions to prevent crashes during number display rendering
            }
            matrixStack.pop(); // End heart icon rendering
        }

        // Render the text with text-appropriate scaling
        matrixStack.scale(scale, -scale, scale);
        matrixStack.translate(0, -VitalStatsConfig.HANDLER.instance().display_offset, 0);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        float x = -textRenderer.getWidth(healthText) / 1.3f;
        Matrix4f model = matrixStack.peek().getPositionMatrix();

        textRenderer.draw(healthText, x, 0, VitalStatsConfig.HANDLER.instance().number_color.getRGB(), VitalStatsConfig.HANDLER.instance().render_number_display_shadow, model, vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL, VitalStatsConfig.HANDLER.instance().render_number_display_background_color ? VitalStatsConfig.HANDLER.instance().number_display_background_color.getRGB() : 0, light);
        matrixStack.pop();
    }



    @Unique
    private static void drawHeart(Matrix4f model, VertexConsumer vertexConsumer, float x, HeartTypeEnum type, LivingEntity livingEntity) {
        String additionalIconEffects = "";
        if(type != HeartTypeEnum.YELLOW_FULL && type != HeartTypeEnum.YELLOW_HALF && type != HeartTypeEnum.EMPTY && VitalStatsConfig.HANDLER.instance().show_heart_effects) additionalIconEffects = (addStatusIcon(livingEntity) + addHardcoreIcon(livingEntity));
        Identifier heartIcon = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + additionalIconEffects + type.icon + ".png");
        Identifier vanillaHeartIcon = Identifier.of("vitalstats", "textures/gui/heart/" + additionalIconEffects + type.icon + ".png");

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, VitalStatsConfig.HANDLER.instance().use_vanilla_textures ? vanillaHeartIcon : heartIcon);
        RenderSystem.enableDepthTest();

        float minU = 0F;
        float maxU = 1F;
        float minV = 0F;
        float maxV = 1F;

        float heartSize = 9F;

        vertexConsumer.vertex(model, x, 0F - heartSize, 0.0F).texture(minU, maxV);
        vertexConsumer.vertex(model, x - heartSize, 0F - heartSize, 0.0F).texture(maxU, maxV);
        vertexConsumer.vertex(model, x - heartSize, 0F, 0.0F).texture(maxU, minV);
        vertexConsumer.vertex(model, x, 0F, 0.0F).texture(minU, minV);
    }

    @Unique
    private static void drawTotem(Matrix4f model, VertexConsumer vertexConsumer, float x, TotemTypeEnum type) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        Identifier totemIcon = VitalStatsConfig.HANDLER.instance().use_vanilla_textures ? type.vanillaIcon : type.icon;
        RenderSystem.setShaderTexture(0, totemIcon);
        RenderSystem.enableDepthTest();

        float minU = 0F;
        float maxU = 1F;
        float minV = 0F;
        float maxV = 1F;

        float iconSize = 9F;

        vertexConsumer.vertex(model, x, 0F - iconSize, 0.0F).texture(minU, maxV);
        vertexConsumer.vertex(model, x - iconSize, 0F - iconSize, 0.0F).texture(maxU, maxV);
        vertexConsumer.vertex(model, x - iconSize, 0F, 0.0F).texture(maxU, minV);
        vertexConsumer.vertex(model, x, 0F, 0.0F).texture(minU, minV);
    }

    @Unique
    private void renderTotemCounter(PlayerEntity player, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        int totemCount = com.atl.vitalstats.core.TotemTracker.getTotemCount(player);

        if (totemCount <= 0) return;

        double d = this.dispatcher.getSquaredDistanceToCamera(player);

        matrixStack.push();
        float scale = VitalStatsConfig.HANDLER.instance().size;

        // Check if health indicators are enabled and if this player should show health indicators
        boolean healthIndicatorsEnabled = Config.getHeartsRenderingEnabled() || Config.getOverrideAllFiltersEnabled();
        boolean playerHasHealthIndicator = (RenderTracker.isInUUIDS(player) || Config.getOverrideAllFiltersEnabled()) && healthIndicatorsEnabled;

        // Position totem counter based on health indicator status
        if (playerHasHealthIndicator) {
            // Position relative to health indicator (above it)
            matrixStack.translate(0, player.getHeight() + 0.5f, 0); // Same base height as health indicator

            // Account for existing labels like health indicators do
            @SuppressWarnings("unchecked")
            T playerAsT = (T) player;
            if ((this.hasLabel(playerAsT)
                    || (VitalStatsConfig.HANDLER.instance().force_higher_offset_for_players && player != client.player))
                    && d <= 4096.0) {
                matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                if (d < 100.0 && player.getEntityWorld().getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                    matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                }
            }

            // Calculate additional height for absorption hearts in hearts display mode
            float absorptionHeightOffset = 0.0f;
            if (VitalStatsConfig.HANDLER.instance().indicator_type == HealthDisplayTypeEnum.HEARTS) {
                // Calculate absorption hearts and their rows to determine height offset
                int healthYellow = MathHelper.ceil(player.getAbsorptionAmount());
                if (healthYellow > 0) {
                    int heartsYellow = MathHelper.ceil(healthYellow / 2.0F);
                    int heartsPerRow = VitalStatsConfig.HANDLER.instance().icons_per_row;
                    int absorptionRows = MathHelper.ceil((float) heartsYellow / heartsPerRow);

                    // Each row adds height based on heart density calculation - increased multiplier for higher positioning
                    double heartDensity = 50F - (Math.max(4F - Math.ceil((double) heartsYellow / heartsPerRow), -3F) * 5F);
                    absorptionHeightOffset = (float) (absorptionRows * (heartsYellow / heartDensity)) * scale * 15.0f; // Increased from 10.0f to 15.0f
                }
            }

            // Position above health indicator with absorption height consideration
            matrixStack.translate(0.0D, 16.0F * scale + absorptionHeightOffset, 0.0D);
        } else {
            // Position above player name when health indicators are disabled
            matrixStack.translate(0, player.getHeight() + 0.4f, 0);

            // Account for player name label
            @SuppressWarnings("unchecked")
            T playerAsT = (T) player;
            if (this.hasLabel(playerAsT) && d <= 4096.0) {
                matrixStack.translate(0.0D, 12.0F * scale, 0.0D); // Above the name
                if (d < 100.0 && player.getEntityWorld().getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                    matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                }
            } else {
                // If no name label, position slightly above player
                matrixStack.translate(0.0D, 8.0F * scale, 0.0D);
            }
        }

        matrixStack.multiply(this.dispatcher.getRotation());
        matrixStack.scale(-scale, scale, scale);
        matrixStack.translate(0, VitalStatsConfig.HANDLER.instance().display_offset, 0);

        // Render single totem icon
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexConsumer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        Matrix4f model = matrixStack.peek().getPositionMatrix();
        float iconX = 10.0f; // Center the icon

        drawTotem(model, vertexConsumer, iconX, TotemTypeEnum.TOTEM);

        BuiltBuffer builtBuffer;
        try {
            builtBuffer = vertexConsumer.endNullable();
            if(builtBuffer != null){
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
                builtBuffer.close();
            }
        }
        catch (Exception e){
            // Handle exception
        }

        matrixStack.pop(); // End totem icon rendering

        // Render count text with separate matrix stack
        if (totemCount > 0) {
            matrixStack.push();

            // Use the same positioning logic as the icon
            if (playerHasHealthIndicator) {
                // Position relative to health indicator (above it)
                matrixStack.translate(0, player.getHeight() + 0.5f, 0);

                @SuppressWarnings("unchecked")
                T playerAsT = (T) player;
                if ((this.hasLabel(playerAsT)
                        || (VitalStatsConfig.HANDLER.instance().force_higher_offset_for_players && player != client.player))
                        && d <= 4096.0) {
                    matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                    if (d < 100.0 && player.getEntityWorld().getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                        matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                    }
                }

                // Calculate additional height for absorption hearts in hearts display mode (for text)
                float absorptionHeightOffsetText = 0.0f;
                if (VitalStatsConfig.HANDLER.instance().indicator_type == HealthDisplayTypeEnum.HEARTS) {
                    // Calculate absorption hearts and their rows to determine height offset
                    int healthYellow = MathHelper.ceil(player.getAbsorptionAmount());
                    if (healthYellow > 0) {
                        int heartsYellow = MathHelper.ceil(healthYellow / 2.0F);
                        int heartsPerRow = VitalStatsConfig.HANDLER.instance().icons_per_row;
                        int absorptionRows = MathHelper.ceil((float) heartsYellow / heartsPerRow);

                        // Each row adds height based on heart density calculation - increased multiplier for higher positioning
                        double heartDensity = 50F - (Math.max(4F - Math.ceil((double) heartsYellow / heartsPerRow), -3F) * 5F);
                        absorptionHeightOffsetText = (float) (absorptionRows * (heartsYellow / heartDensity)) * scale * 15.0f; // Increased from 10.0f to 15.0f
                    }
                }

                // Position above health indicator (slightly lower than icon for text) with absorption height consideration
                matrixStack.translate(0.0D, 13.0F * scale + absorptionHeightOffsetText, 0.0D);
            } else {
                // Position above player name when health indicators are disabled
                matrixStack.translate(0, player.getHeight() + 0.4f, 0);

                @SuppressWarnings("unchecked")
                T playerAsT = (T) player;
                if (this.hasLabel(playerAsT) && d <= 4096.0) {
                    matrixStack.translate(0.0D, 9.0F * scale, 0.0D); // Above the name (slightly lower than icon)
                    if (d < 100.0 && player.getEntityWorld().getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                        matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                    }
                } else {
                    // If no name label, position slightly above player
                    matrixStack.translate(0.0D, 5.0F * scale, 0.0D);
                }
            }

            matrixStack.multiply(this.dispatcher.getRotation());
            matrixStack.scale(scale, -scale, scale); // Proper text scaling (no negative X scale)
            matrixStack.translate(0, -VitalStatsConfig.HANDLER.instance().display_offset, 0);

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            String countText = "x" + totemCount;
            Matrix4f textModel = matrixStack.peek().getPositionMatrix();

            // Position text to the right of the totem icon
            float textX = 0.0f; // Position to the right of the totem icon (positive X for right side)
            float textY = -2.0f; // Slightly below center

            textRenderer.draw(countText, textX, textY, 0xFFFFFF, true, textModel, vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL, 0, light);

            matrixStack.pop(); // End text rendering
        }
    }

    @Unique
    @Deprecated
    private static void drawVertex(Matrix4f model, VertexConsumer vertices, float x, float y, float u, float v) {
        vertices.vertex(model, x, y, 0.0F).texture(u, v);
    }

    @Unique
    private static void drawHeartWithOffset(Matrix4f model, VertexConsumer vertexConsumer, float x, float y, HeartTypeEnum type, LivingEntity livingEntity) {
        String additionalIconEffects = "";
        if(type != HeartTypeEnum.YELLOW_FULL && type != HeartTypeEnum.YELLOW_HALF && type != HeartTypeEnum.EMPTY && VitalStatsConfig.HANDLER.instance().show_heart_effects) additionalIconEffects = (addStatusIcon(livingEntity) + addHardcoreIcon(livingEntity));
        Identifier heartIcon = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + additionalIconEffects + type.icon + ".png");
        Identifier vanillaHeartIcon = Identifier.of("vitalstats", "textures/gui/heart/" + additionalIconEffects + type.icon + ".png");

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, VitalStatsConfig.HANDLER.instance().use_vanilla_textures ? vanillaHeartIcon : heartIcon);
        RenderSystem.enableDepthTest();

        float minU = 0F;
        float maxU = 1F;
        float minV = 0F;
        float maxV = 1F;

        float heartSize = 9F;

        vertexConsumer.vertex(model, x, y - heartSize, 0.0F).texture(minU, maxV);
        vertexConsumer.vertex(model, x - heartSize, y - heartSize, 0.0F).texture(maxU, maxV);
        vertexConsumer.vertex(model, x - heartSize, y, 0.0F).texture(maxU, minV);
        vertexConsumer.vertex(model, x, y, 0.0F).texture(minU, minV);
    }
}
