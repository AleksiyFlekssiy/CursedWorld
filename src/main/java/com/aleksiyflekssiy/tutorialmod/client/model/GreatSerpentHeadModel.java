package com.aleksiyflekssiy.tutorialmod.client.model;// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class GreatSerpentHeadModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "great_serpent"), "main");
	private final ModelPart head;
	private final ModelPart upper;
	private final ModelPart lower;

	public GreatSerpentHeadModel(ModelPart root) {
		this.head = root.getChild("head");
		this.upper = this.head.getChild("upper");
		this.lower = this.head.getChild("lower");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition upper = head.addOrReplaceChild("upper", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, -12.0F, -5.0F, 18.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(38, 17).addBox(-7.0F, -12.0F, -9.0F, 14.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(42, 48).addBox(-6.0F, -25.0F, -8.0F, 12.0F, 13.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 17).addBox(-7.0F, -26.0F, -5.0F, 14.0F, 14.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(-1, 50).addBox(-7.0F, -16.0F, -9.0F, 14.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 57).addBox(-6.0F, -28.0F, -4.0F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 2.0F));

		PartDefinition lower = head.addOrReplaceChild("lower", CubeListBuilder.create().texOffs(46, 0).addBox(-7.0F, -12.0F, 3.0F, 14.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(32, 64).addBox(-6.0F, -23.0F, 3.0F, 12.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 36).addBox(-9.0F, -12.0F, 0.0F, 18.0F, 12.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(42, 33).addBox(-7.0F, -24.0F, 0.0F, 14.0F, 12.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(58, 64).addBox(-6.0F, -27.0F, 0.0F, 12.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 2.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}


	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return head;
	}

	@Override
	public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

	}
}