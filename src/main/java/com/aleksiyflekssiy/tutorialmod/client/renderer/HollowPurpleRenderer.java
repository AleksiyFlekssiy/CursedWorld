package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.client.model.HollowPurpleModel;
import com.aleksiyflekssiy.tutorialmod.client.model.ModModelLayers;
import com.aleksiyflekssiy.tutorialmod.client.model.RedModel;
import com.aleksiyflekssiy.tutorialmod.entity.HollowPurpleEntity;
import com.aleksiyflekssiy.tutorialmod.entity.RedEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class HollowPurpleRenderer extends EntityRenderer<HollowPurpleEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/entity/hollow_purple_entity.png");
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/entity/hollow_purple_glow.png"));
    private final HollowPurpleModel model;


    public HollowPurpleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new HollowPurpleModel(context.bakeLayer(ModModelLayers.HOLLOW_PURPLE_LAYER));
    }

    @Override
    public void render(HollowPurpleEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float scale = entity.getChant() > 0 ? 2.5f * 2 * entity.getChant() : 2.5f;
        float offset = 0.250f * scale;
        float rotation = entity.tickCount * 1250f + partialTicks * 1250f;
        // Вращение модели с увеличенной скоростью

        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot())); // Горизонтальный поворот
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        poseStack.mulPose(Axis.ZN.rotationDegrees(rotation));

        poseStack.translate(offset,offset,offset);
        poseStack.scale(scale,scale,scale);

        VertexConsumer glowBuffer = buffer.getBuffer(GLOW_RENDER_TYPE);
        model.renderToBuffer(poseStack, glowBuffer, 15728880, 655360, 1.0F, 1.0F, 1.0F, 0.8F);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(HollowPurpleEntity hollowPurpleEntity) {
        return ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/entity/hollow_purple_glow.png");
    }

}
