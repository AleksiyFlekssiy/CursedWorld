package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.client.model.RabbitEscapeAnimations;
import com.aleksiyflekssiy.tutorialmod.client.model.RabbitEscapeEntityModel;
import com.aleksiyflekssiy.tutorialmod.entity.RabbitEscapeEntity;
import com.aleksiyflekssiy.tutorialmod.mixin.CameraMixin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.kosmx.playerAnim.mixin.firstPerson.CameraAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.*;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VortexRenderer {
    public static final List<UUID> USERS = new ArrayList<>();

    public static RabbitEscapeEntityModel<RabbitEscapeEntity> RABBIT_MODEL;

    private static double lastTime = 0;

    private static final int MODEL_COUNT = 3;        // сколько моделей в куполе
    private static final double RADIUS = 2.5;          // радиус купола

    public static void addUser(UUID uuid) {
        USERS.add(uuid);
    }

    public static void removeUser(UUID uuid) {
        USERS.remove(uuid);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        poseStack.pushPose();

        for (UUID uuid : USERS) {
            LocalPlayer player = (LocalPlayer) mc.level.getPlayerByUUID(uuid);
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
                poseStack.mulPose(Axis.XP.rotationDegrees(180));
                poseStack.mulPose(Axis.YP.rotationDegrees(player.getViewYRot(20)));

                lastTime++;
                System.out.println("Lasttime: " + lastTime);
                //KeyframeAnimations.animate(RABBIT_MODEL, RabbitEscapeAnimations.animation, (long) lastTime, 0.4f, new Vector3f());
                RABBIT_MODEL.setupAnim(null, 1, 1, mc.level.getGameTime(), 0, 0);
//                if (lastTime >= 40.0) {
//                    RABBIT_MODEL.root().getAllParts().forEach(ModelPart::resetPose);
//                    lastTime = 0;
//                    System.out.println("Reset");
//                }
                RABBIT_MODEL.renderToBuffer(
                        poseStack,
                        buffer.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/entity/rabbit_escape.png"))),
                        0,
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
