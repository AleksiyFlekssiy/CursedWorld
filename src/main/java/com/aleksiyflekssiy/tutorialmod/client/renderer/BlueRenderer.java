package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.client.model.BlueModel;
import com.aleksiyflekssiy.tutorialmod.entity.BlueEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Axis;

public class BlueRenderer extends EntityRenderer<BlueEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("tutorialmod", "textures/entity/blue_entity.png");
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(new ResourceLocation("tutorialmod", "textures/entity/blue_entity.png"));

    private final BlueModel model;

    public BlueRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new BlueModel(context.bakeLayer(TutorialMod.ClientModEvents.BLUE_LAYER));
    }

    @Override
    public void render(BlueEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.5, 0.0); // Центрируем модель

        // Вращение
        float rotation = entity.tickCount * 1250f + partialTicks * 1250f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Пульсация
        int multiplier = entity.getChant() == 0 ? 1 : entity.getChant() + 1;
        float scale = (1.0f + (float) Math.sin((entity.tickCount + partialTicks) * 0.2f) * 0.2f) * multiplier;
        poseStack.scale(scale, scale, scale);

        // Рендеринг модели
        VertexConsumer glowBuffer = buffer.getBuffer(GLOW_RENDER_TYPE);
        model.renderToBuffer(poseStack, glowBuffer, 15728880, 655360, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(BlueEntity entity) {
        return TEXTURE;
    }
}
