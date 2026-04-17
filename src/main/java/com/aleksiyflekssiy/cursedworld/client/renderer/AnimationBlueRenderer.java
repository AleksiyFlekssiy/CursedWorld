package com.aleksiyflekssiy.cursedworld.client.renderer;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.client.model.BlueModel;
import com.aleksiyflekssiy.cursedworld.client.model.ModModelLayers;
import com.aleksiyflekssiy.cursedworld.entity.animation.AnimationBlueEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class AnimationBlueRenderer extends EntityRenderer<AnimationBlueEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/entity/blue_entity.png");
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/entity/blue_entity.png"));

    private final BlueModel model;

    public AnimationBlueRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new BlueModel(context.bakeLayer(ModModelLayers.BLUE_LAYER));
    }

    @Override
    public void render(AnimationBlueEntity entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot())); // Горизонтальный поворот
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        // Рендеринг модели
        VertexConsumer glowBuffer = buffer.getBuffer(GLOW_RENDER_TYPE);
        model.renderToBuffer(poseStack, glowBuffer, 15728880, 655360, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(AnimationBlueEntity entity) {
        return TEXTURE;
    }
}
