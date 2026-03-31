package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.client.model.ModModelLayers;
import com.aleksiyflekssiy.tutorialmod.client.model.RabbitEscapeEntityModel;
import com.aleksiyflekssiy.tutorialmod.client.model.ToadModel;
import com.aleksiyflekssiy.tutorialmod.entity.RabbitEscapeEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RabbitEscapeRenderer extends MobRenderer<RabbitEscapeEntity, RabbitEscapeEntityModel<RabbitEscapeEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/entity/rabbit_escape.png");

    public RabbitEscapeRenderer(EntityRendererProvider.Context context) {
        super(context, new RabbitEscapeEntityModel<>(context.bakeLayer(ModModelLayers.RABBIT_ESCAPE_LAYER)), 0.5f);
    }

    @Override
    public void render(RabbitEscapeEntity entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // Рендеринг модели
        super.render(entity,pEntityYaw,partialTicks, poseStack, buffer, packedLight);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(RabbitEscapeEntity pEntity) {
        return TEXTURE;
    }
}
