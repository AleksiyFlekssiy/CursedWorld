package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.client.model.GreatSerpentSegmentModel;
import com.aleksiyflekssiy.tutorialmod.client.model.ModModelLayers;
import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentSegment;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GreatSerpentSegmentRenderer extends MobRenderer<GreatSerpentSegment, GreatSerpentSegmentModel<GreatSerpentSegment>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/entity/great_serpent_segment.png");
    private final GreatSerpentSegmentModel segmentModel;

    public GreatSerpentSegmentRenderer(EntityRendererProvider.Context context) {
        super(context, new GreatSerpentSegmentModel<>(context.bakeLayer(ModModelLayers.GREAT_SERPENT_SEGMENT_LAYER)), 1.5f);
        this.segmentModel = new GreatSerpentSegmentModel<>(context.bakeLayer(ModModelLayers.GREAT_SERPENT_SEGMENT_LAYER));
    }


    @Override
    public void render(GreatSerpentSegment entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(2,2,2);
        super.render(entity, pEntityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GreatSerpentSegment entity) {
        return TEXTURE;
    }
}