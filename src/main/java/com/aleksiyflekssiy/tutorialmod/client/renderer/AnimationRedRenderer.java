package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.client.model.RedModel;
import com.aleksiyflekssiy.tutorialmod.entity.animation.AnimationRedEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class AnimationRedRenderer extends EntityRenderer<AnimationRedEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("tutorialmod", "textures/entity/red_entity.png");
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(new ResourceLocation("tutorialmod", "textures/entity/red_entity.png"));

    private final RedModel model;

    public AnimationRedRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new RedModel(context.bakeLayer(TutorialMod.ClientModEvents.RED_LAYER));
    }

    @Override
    public void render(AnimationRedEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot())); // Горизонтальный поворот
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        VertexConsumer glowBuffer = buffer.getBuffer(GLOW_RENDER_TYPE);
        model.renderToBuffer(poseStack, glowBuffer, 15728880, 655360, 1.0F, 1.0F, 1.0F, 1F);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(AnimationRedEntity entity) {
        return TEXTURE;
    }
}
