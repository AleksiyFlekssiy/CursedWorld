package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.client.model.DivineDogModel;
import com.aleksiyflekssiy.tutorialmod.client.model.ModModelLayers;
import com.aleksiyflekssiy.tutorialmod.entity.DivineDogEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DivineDogRenderer extends MobRenderer<DivineDogEntity, DivineDogModel<DivineDogEntity>> {
    private static final ResourceLocation[] TEXTURES = {ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/entity/divine_dog.png"),
    ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/entity/black_dog.png")};

    public DivineDogRenderer(EntityRendererProvider.Context context) {
        super(context, new DivineDogModel<>(context.bakeLayer(ModModelLayers.DIVINE_DOG_LAYER)), 1.5f);
    }

    @Override
    public void render(DivineDogEntity entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // Рендеринг модели

        super.render(entity,pEntityYaw,partialTicks, poseStack, buffer, packedLight);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(DivineDogEntity entity) {
        return TEXTURES[entity.getColor().ordinal()];
    }
}
