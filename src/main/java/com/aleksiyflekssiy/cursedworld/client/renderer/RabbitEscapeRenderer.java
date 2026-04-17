package com.aleksiyflekssiy.cursedworld.client.renderer;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.client.model.ModModelLayers;
import com.aleksiyflekssiy.cursedworld.client.model.RabbitEscapeEntityModel;
import com.aleksiyflekssiy.cursedworld.entity.RabbitEscapeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RabbitEscapeRenderer extends MobRenderer<RabbitEscapeEntity, RabbitEscapeEntityModel<RabbitEscapeEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/entity/rabbit_escape.png");
    public static RabbitEscapeEntityModel<RabbitEscapeEntity> MODEL;

    public RabbitEscapeRenderer(EntityRendererProvider.Context context) {
        super(context, new RabbitEscapeEntityModel<>(context.bakeLayer(ModModelLayers.RABBIT_ESCAPE_LAYER)), 0.5f);
        MODEL = new RabbitEscapeEntityModel<>(context.bakeLayer(ModModelLayers.RABBIT_ESCAPE_LAYER));
        RabbitSwarmRenderer.RABBIT_MODEL = MODEL;
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
