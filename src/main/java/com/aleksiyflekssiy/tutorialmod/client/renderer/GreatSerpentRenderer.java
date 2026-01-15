package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.client.model.GreatSerpentHeadModel;
import com.aleksiyflekssiy.tutorialmod.client.model.ModModelLayers;
import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GreatSerpentRenderer extends MobRenderer<GreatSerpentEntity, GreatSerpentHeadModel<GreatSerpentEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/entity/great_serpent_head.png");
    private final GreatSerpentHeadModel segmentModel;

    public GreatSerpentRenderer(EntityRendererProvider.Context context) {
        super(context, new GreatSerpentHeadModel<>(context.bakeLayer(ModModelLayers.GREAT_SERPENT_HEAD_LAYER)), 1.5f);
        this.segmentModel = new GreatSerpentHeadModel<>(context.bakeLayer(ModModelLayers.GREAT_SERPENT_HEAD_LAYER));
    }


    @Override
    public void render(GreatSerpentEntity entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(2,2,2);
        super.render(entity, pEntityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GreatSerpentEntity entity) {
        return TEXTURE;
    }
}