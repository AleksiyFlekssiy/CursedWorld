package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.client.model.ModModelLayers;
import com.aleksiyflekssiy.tutorialmod.client.model.NueModel;
import com.aleksiyflekssiy.tutorialmod.client.model.ToadModel;
import com.aleksiyflekssiy.tutorialmod.entity.NueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ToadRenderer extends MobRenderer<ToadEntity, ToadModel<ToadEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/entity/toad.png");

    public ToadRenderer(EntityRendererProvider.Context context) {
        super(context, new ToadModel<>(context.bakeLayer(ModModelLayers.TOAD_LAYER)), 1.5f);
    }

    @Override
    public void render(ToadEntity entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // Рендеринг модели
        poseStack.scale(1.5f,1.5f,1.5f);
        super.render(entity,pEntityYaw,partialTicks, poseStack, buffer, packedLight);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ToadEntity pEntity) {
        return TEXTURE;
    }
}
