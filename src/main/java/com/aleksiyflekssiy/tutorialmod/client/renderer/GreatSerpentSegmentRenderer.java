package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.client.model.GreatSerpentHeadModel;
import com.aleksiyflekssiy.tutorialmod.client.model.ModModelLayers;
import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentEntity;
import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentPartEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class GreatSerpentSegmentRenderer extends MobRenderer<GreatSerpentPartEntity, GreatSerpentHeadModel<GreatSerpentPartEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/entity/great_serpent_head.png");
    private final GreatSerpentHeadModel segmentModel;

    public GreatSerpentSegmentRenderer(EntityRendererProvider.Context context) {
        super(context, new GreatSerpentHeadModel<>(context.bakeLayer(ModModelLayers.GREAT_SERPENT_LAYER)), 1.5f);
        this.segmentModel = new GreatSerpentHeadModel<>(context.bakeLayer(ModModelLayers.GREAT_SERPENT_LAYER));
    }


    @Override
    public void render(GreatSerpentPartEntity entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        super.render(entity, pEntityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GreatSerpentPartEntity entity) {
        return TEXTURE;
    }
}