package com.aleksiyflekssiy.cursedworld.client.renderer;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.phys.OBB;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CursedWorld.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CustomDebugRenderer {
    public static final Map<AABB, Boolean> AABB_LIST = new HashMap<>();
    public static final Map<OBB, Boolean> OBB_LIST = new HashMap<>();

    public static void addAABB(AABB aabb){
        AABB_LIST.put(aabb, true);
    }

    public static void addOBB(OBB obb){
        OBB_LIST.put(obb, true);
    }

    public static void removeAABB(AABB aabb){
        AABB_LIST.put(aabb, false);
    }

    public static void removeOBB(OBB obb){
        OBB_LIST.put(obb, false);
    }

    @SubscribeEvent
    public static void debugRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = event.getPoseStack();
        var buffer = mc.renderBuffers().bufferSource();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        for (AABB box : AABB_LIST.keySet()){
            if (box == null || !AABB_LIST.get(box)) continue;

            poseStack.pushPose();
            LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(RenderType.LINES), box, 1, 1, 1, 1);
            poseStack.popPose();
        }

        for (OBB box : OBB_LIST.keySet()) {
            if (box == null || !OBB_LIST.get(box)) continue;

            poseStack.pushPose();
            poseStack.translate(box.getCenter().x, box.getCenter().y, box.getCenter().z);
            poseStack.mulPose(Axis.YN.rotationDegrees((float) box.getYaw()));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) box.getPitch()));

            Vec3 half = box.getHalfSize();

            float w = (float) half.x;
            float h = (float) half.y;
            float d = (float) half.z;

            Vector3f[] vertices = {
                    new Vector3f(-w, h, -d),
                    new Vector3f(w, h, -d),
                    new Vector3f(-w, -h, -d),
                    new Vector3f(w, -h, -d),
                    new Vector3f(-w, h, d),
                    new Vector3f(w, h, d),
                    new Vector3f(-w, -h, d),
                    new Vector3f(w, -h, d)
            };

            float red = 1;
            float green = 0;
            if (box.intersects(mc.player)){
                green++;
                red--;
            }
            // 12 рёбер OBB
            int[] edges = {
                    2,3, 2,6, 3,7, 6,7,   // нижняя грань
                    0,1, 0,4, 1,5, 4,5,   // верхняя грань
                    0,2, 1,3, 4,6, 5,7    // вертикали
            };

            PoseStack.Pose pose = poseStack.last();

            for (int i = 0; i < edges.length; i += 2) {
                Vector3f v1 = vertices[edges[i]];
                Vector3f v2 = vertices[edges[i + 1]];

                buffer.getBuffer(RenderType.LINES)
                        .vertex(pose.pose(), v1.x, v1.y, v1.z)
                        .color(red, green, 0.0f, 1.0f)
                        .normal(pose.normal(), 0, 0, 0)
                        .endVertex();

                buffer.getBuffer(RenderType.LINES)
                        .vertex(pose.pose(), v2.x, v2.y, v2.z)
                        .color(red, green, 0.0f, 1.0f)
                        .normal(pose.normal(), 0, 0, 0)
                        .endVertex();
            }

            poseStack.popPose();
        }
        AABB_LIST.keySet().removeIf(aabb -> AABB_LIST.get(aabb) == false);
        OBB_LIST.keySet().removeIf(obb -> OBB_LIST.get(obb) == false);
        poseStack.popPose();
        buffer.endBatch(RenderType.lines());
    }

}
