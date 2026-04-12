package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.client.model.RabbitEscapeEntityModel;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.TenShadowsTechnique;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows.RabbitEscape;
import com.aleksiyflekssiy.tutorialmod.entity.RabbitEscapeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.*;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RabbitSwarmRenderer {
    public static final Map<Integer, Boolean> ID_RENDER_TARGETS = new HashMap<>();

    public static RabbitEscapeEntityModel<RabbitEscapeEntity> RABBIT_MODEL;

    private static final int MODEL_COUNT = 300;        // сколько моделей в куполе
    private static final double RADIUS = 2.5;          // радиус купола

    public static void addUser(int id, boolean selfUse) {
        ID_RENDER_TARGETS.put(id, selfUse);
    }

    public static void removeUser(int id) {
        ID_RENDER_TARGETS.remove(id);
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event){
        Entity entity = event.getTarget();
        entity.getCapability(CursedTechniqueCapability.CURSED_TECHNIQUE).ifPresent(technique -> {
            if (technique instanceof TenShadowsTechnique tenShadows) {
                for (Skill skill : tenShadows.getSkillSet()) {
                    if (skill instanceof RabbitEscape rabbitEscape && rabbitEscape.isActive())
                        addUser(entity.getId(), true);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onStopTracking(PlayerEvent.StopTracking event){
        if (ID_RENDER_TARGETS.containsKey(event.getTarget().getId())){
            removeUser(event.getTarget().getId());
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vector3f cameraLookVector = camera.getLookVector();

        poseStack.pushPose();

        for (int id : ID_RENDER_TARGETS.keySet()) {
            LivingEntity entity = (LivingEntity) mc.level.getEntity(id);
            if (entity == null) continue;
            // Центр — игрок
            double centerX = entity.getX();
            double centerY = entity.getY() + 1.0;
            double centerZ = entity.getZ();

            for (int i = 0; i < MODEL_COUNT; i++) {
                // Равномерное распределение по полусфере
                double phi = Math.acos(2.0 * i / MODEL_COUNT - 1.0);        // полярный угол
                double theta = Math.sqrt(MODEL_COUNT * Math.PI) * phi;      // азимутальный угол

                double x = centerX + RADIUS * Math.sin(phi) * Math.cos(theta);
                double y = centerY + RADIUS * Math.cos(phi);
                double z = centerZ + RADIUS * Math.sin(phi) * Math.sin(theta);

                poseStack.pushPose();

                poseStack.translate(x - centerX, y - centerY, z - centerZ);
                if (ID_RENDER_TARGETS.get(id) && mc.player.getId() == id) poseStack.translate(cameraLookVector.x * 10, entity.getY() - camera.getPosition().y + 1.6, cameraLookVector.z * 10);
                else poseStack.translate(centerX - mc.player.getX(), centerY - mc.player.getY(), centerZ - mc.player.getZ());

                poseStack.mulPose(Axis.XP.rotationDegrees(180));
                poseStack.mulPose(Axis.YP.rotationDegrees(entity.getViewYRot(0.5F)));

                RABBIT_MODEL.setupAnim(null, 1, 1, mc.level.getGameTime(), 0, 0);

                RABBIT_MODEL.renderToBuffer(
                        poseStack,
                        buffer.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/entity/rabbit_escape.png"))),
                        15728880,
                        OverlayTexture.NO_OVERLAY,
                        1.0f, 1.0f, 1.0f, 1.0f
                );
                poseStack.popPose();
            }

        }

        poseStack.popPose();
        buffer.endBatch();
    }
}
