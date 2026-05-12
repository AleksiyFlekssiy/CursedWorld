package com.aleksiyflekssiy.cursedworld.client.model;// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.entity.RoundDeerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class RoundDeerModel<T extends RoundDeerEntity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "round_deer"), "main");
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart torso;
	private final ModelPart frontLeftLeg;
	private final ModelPart frontRightLeg;
	private final ModelPart backLeftLeg;
	private final ModelPart backRightLeg;

	public RoundDeerModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = this.body.getChild("head");
		this.torso = this.body.getChild("torso");
		this.frontLeftLeg = this.body.getChild("frontLeftLeg");
		this.frontRightLeg = this.body.getChild("frontRightLeg");
		this.backLeftLeg = this.body.getChild("backLeftLeg");
		this.backRightLeg = this.body.getChild("backRightLeg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 76).addBox(-4.0F, -53.0F, -21.0F, 8.0F, 18.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(2, 52).addBox(-7.0F, -58.0F, -23.0F, 14.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-4.0F, -53.0F, -31.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(56, 72).addBox(-10.0F, -10.0F, 0.0F, 20.0F, 20.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -61.0F, -15.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(56, 52).addBox(-10.0F, -10.0F, 0.0F, 20.0F, 20.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -61.0F, -15.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -38.0F, -18.0F, 20.0F, 16.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition frontLeftLeg = body.addOrReplaceChild("frontLeftLeg", CubeListBuilder.create().texOffs(32, 76).addBox(-3.0F, -16.0F, -1.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(48, 92).addBox(-4.0F, -22.0F, -2.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 0.0F, -11.0F));

		PartDefinition frontRightLeg = body.addOrReplaceChild("frontRightLeg", CubeListBuilder.create().texOffs(72, 92).addBox(-3.0F, -16.0F, -1.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(88, 92).addBox(-4.0F, -22.0F, -2.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 0.0F, -11.0F));

		PartDefinition backLeftLeg = body.addOrReplaceChild("backLeftLeg", CubeListBuilder.create().texOffs(32, 96).addBox(-3.0F, -16.0F, -1.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(96, 52).addBox(-4.0F, -22.0F, -2.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 0.0F, 9.0F));

		PartDefinition backRightLeg = body.addOrReplaceChild("backRightLeg", CubeListBuilder.create().texOffs(96, 64).addBox(-3.0F, -16.0F, -1.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 102).addBox(-4.0F, -22.0F, -2.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 0.0F, 9.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(RoundDeerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}