package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.client.model.NueModel;
import com.aleksiyflekssiy.tutorialmod.entity.DivineDogEntity;
import com.aleksiyflekssiy.tutorialmod.entity.NueEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class NueRenderer extends MobRenderer<NueEntity, NueModel<NueEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("tutorialmod", "textures/entity/nue.png");

    public NueRenderer(EntityRendererProvider.Context context) {
        super(context, new NueModel<>(context.bakeLayer(TutorialMod.ClientModEvents.NUE_LAYER)), 2.5f);
    }

    @Override
    public void render(NueEntity entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // Рендеринг модели

        super.render(entity,pEntityYaw,partialTicks, poseStack, buffer, packedLight);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(NueEntity pEntity) {
        return TEXTURE;
    }
}
