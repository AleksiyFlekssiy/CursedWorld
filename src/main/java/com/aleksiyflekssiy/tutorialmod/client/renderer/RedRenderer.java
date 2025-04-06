package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.client.model.ModModelLayers;
import com.aleksiyflekssiy.tutorialmod.client.model.RedModel;
import com.aleksiyflekssiy.tutorialmod.entity.RedEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RedRenderer extends EntityRenderer<RedEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/entity/red_entity.png");
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/entity/red_entity.png"));

    private final RedModel model;

    public RedRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new RedModel(context.bakeLayer(ModModelLayers.RED_LAYER));
    }

    @Override
    public void render(RedEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float rotation = entity.tickCount * 1250f + partialTicks * 1250f; // Увеличено до 750 градусов/тик
        float chargeProgress = entity.getChargeProgress(); // От 0.0 до 1.0
        float scale = (3.0f - (2.25f * chargeProgress)) / 2; // От 3.0 до 1.0

        // Вращение модели с увеличенной скоростью

        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot())); // Горизонтальный поворот
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        poseStack.mulPose(Axis.ZN.rotationDegrees(rotation));

        poseStack.scale(scale, scale, scale);
        VertexConsumer glowBuffer = buffer.getBuffer(GLOW_RENDER_TYPE);
        model.renderToBuffer(poseStack, glowBuffer, 15728880, 655360, 1.0F, 1.0F, 1.0F, 1F);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(RedEntity entity) {
        return TEXTURE;
    }

}

