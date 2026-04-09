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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.*;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RabbitSwarmRenderer {
    public static final List<UUID> USERS = new ArrayList<>();

    public static RabbitEscapeEntityModel<RabbitEscapeEntity> RABBIT_MODEL;

    private static final int MODEL_COUNT = 300;        // сколько моделей в куполе
    private static final double RADIUS = 2.5;          // радиус купола

    public static void addUser(UUID uuid) {
        USERS.add(uuid);
    }

    public static void removeUser(UUID uuid) {
        USERS.remove(uuid);
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event){
        Entity entity = event.getTarget();
        entity.getCapability(CursedTechniqueCapability.CURSED_TECHNIQUE).ifPresent(technique -> {
            if (technique instanceof TenShadowsTechnique tenShadows) {
                for (Skill skill : tenShadows.getSkillSet()) {
                    if (skill instanceof RabbitEscape rabbitEscape && rabbitEscape.isActive())
                        addUser(entity.getUUID());
                }
            }
        });
    }

    @SubscribeEvent
    public static void onStopTracking(PlayerEvent.StopTracking event){
        if (USERS.contains(event.getTarget().getUUID())){
            removeUser(event.getTarget().getUUID());
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

        for (UUID uuid : USERS) {
            Player player = mc.level.getPlayerByUUID(uuid);
            if (player == null) continue;

            // Центр — игрок
            double centerX = player.getX();
            double centerY = player.getY() + 1.0;
            double centerZ = player.getZ();

            for (int i = 0; i < MODEL_COUNT; i++) {
                // Равномерное распределение по полусфере
                double phi = Math.acos(2.0 * i / MODEL_COUNT - 1.0);        // полярный угол
                double theta = Math.sqrt(MODEL_COUNT * Math.PI) * phi;      // азимутальный угол

                double x = centerX + RADIUS * Math.sin(phi) * Math.cos(theta);
                double y = centerY + RADIUS * Math.cos(phi);
                double z = centerZ + RADIUS * Math.sin(phi) * Math.sin(theta);

                poseStack.pushPose();

                poseStack.translate(x - centerX, y - centerY, z - centerZ);
                if (player.getUUID().equals(mc.player.getUUID())) poseStack.translate(cameraLookVector.x * 10, player.getY() - camera.getPosition().y + 1.6, cameraLookVector.z * 10);
                else poseStack.translate(centerX -mc.player.getX(), centerY -mc.player.getY(), centerZ -mc.player.getZ());

                poseStack.mulPose(Axis.XP.rotationDegrees(180));
                poseStack.mulPose(Axis.YP.rotationDegrees(player.getViewYRot(0.5F)));

                //KeyframeAnimations.animate(RABBIT_MODEL, RabbitEscapeAnimations.animation, (long) lastTime, 0.4f, new Vector3f());
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
