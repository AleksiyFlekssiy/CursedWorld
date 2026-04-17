package com.aleksiyflekssiy.cursedworld.client.renderer;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.client.model.MaxElephantModel;
import com.aleksiyflekssiy.cursedworld.client.model.ModModelLayers;
import com.aleksiyflekssiy.cursedworld.entity.RabbitEscapeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MaxElephantRenderer extends MobRenderer<RabbitEscapeEntity, MaxElephantModel<RabbitEscapeEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/entity/max_elephant.png");
    private final MaxElephantModel model;

    public MaxElephantRenderer(EntityRendererProvider.Context context) {
        super(context, new MaxElephantModel<>(context.bakeLayer(ModModelLayers.MAX_ELEPHANT_LAYER)), 2.5f);
        this.model = new MaxElephantModel<>(context.bakeLayer(ModModelLayers.MAX_ELEPHANT_LAYER));
    }

    @Override
    public void render(RabbitEscapeEntity entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        super.render(entity, pEntityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(RabbitEscapeEntity pEntity) {
        return TEXTURE;
    }
}
