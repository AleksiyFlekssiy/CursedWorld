package com.aleksiyflekssiy.cursedworld.client.renderer;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.client.model.ModModelLayers;
import com.aleksiyflekssiy.cursedworld.client.model.RoundDeerModel;
import com.aleksiyflekssiy.cursedworld.entity.RoundDeerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RoundDeerRenderer extends MobRenderer<RoundDeerEntity, RoundDeerModel<RoundDeerEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/entity/round_deer.png");

    public RoundDeerRenderer(EntityRendererProvider.Context context) {
        super(context, new RoundDeerModel<>(context.bakeLayer(ModModelLayers.ROUND_DEER_LAYER)), 1.5f);
    }

    @Override
    public void render(RoundDeerEntity entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // Рендеринг модели
        super.render(entity,pEntityYaw,partialTicks, poseStack, buffer, packedLight);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(RoundDeerEntity pEntity) {
        return TEXTURE;
    }
}
