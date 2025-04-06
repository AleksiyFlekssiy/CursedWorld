package com.aleksiyflekssiy.tutorialmod.client.model;// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.aleksiyflekssiy.tutorialmod.client.model.NueAnimations;
import com.aleksiyflekssiy.tutorialmod.entity.NueEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class NueModel<T extends NueEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("tutorialmod", "nue"), "main");
	private final ModelPart body;
	private final ModelPart leftWing;
	private final ModelPart singlePart;
	private final ModelPart singlePart2;
	private final ModelPart singlePart3;
	private final ModelPart singlePart4;
	private final ModelPart singlePart5;
	private final ModelPart rightWing;
	private final ModelPart singlePart6;
	private final ModelPart singlePart7;
	private final ModelPart singlePart8;
	private final ModelPart singlePart9;
	private final ModelPart singlePart10;
	private final ModelPart rightLeg;
	private final ModelPart rightPaw;
	private final ModelPart leftLeg;
	private final ModelPart leftPaw;
	private final ModelPart torso;
	private final ModelPart head;
	private final ModelPart mask;

	public NueModel(ModelPart root) {
		this.body = root.getChild("body");
		this.leftWing = this.body.getChild("leftWing");
		this.singlePart = this.leftWing.getChild("singlePart");
		this.singlePart2 = this.leftWing.getChild("singlePart2");
		this.singlePart3 = this.leftWing.getChild("singlePart3");
		this.singlePart4 = this.leftWing.getChild("singlePart4");
		this.singlePart5 = this.leftWing.getChild("singlePart5");
		this.rightWing = this.body.getChild("rightWing");
		this.singlePart6 = this.rightWing.getChild("singlePart6");
		this.singlePart7 = this.rightWing.getChild("singlePart7");
		this.singlePart8 = this.rightWing.getChild("singlePart8");
		this.singlePart9 = this.rightWing.getChild("singlePart9");
		this.singlePart10 = this.rightWing.getChild("singlePart10");
		this.rightLeg = this.body.getChild("rightLeg");
		this.rightPaw = this.rightLeg.getChild("rightPaw");
		this.leftLeg = this.body.getChild("leftLeg");
		this.leftPaw = this.leftLeg.getChild("leftPaw");
		this.torso = this.body.getChild("torso");
		this.head = this.body.getChild("head");
		this.mask = this.head.getChild("mask");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(3.0F, 24.0F, 1.0F));

		PartDefinition leftWing = body.addOrReplaceChild("leftWing", CubeListBuilder.create(), PartPose.offsetAndRotation(9.0F, -42.5F, -5.5F, 0.2182F, 0.0F, 0.0F));

		PartDefinition singlePart = leftWing.addOrReplaceChild("singlePart", CubeListBuilder.create().texOffs(38, 95).addBox(9.0F, -34.0F, -5.5F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 85).addBox(9.0F, -39.0F, -7.0F, 12.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.0F, 36.5F, 4.5F));

		PartDefinition singlePart2 = leftWing.addOrReplaceChild("singlePart2", CubeListBuilder.create().texOffs(0, 101).addBox(9.0F, -34.0F, -5.5F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(106, 14).addBox(9.0F, -38.0F, -6.5F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 33.5F, 4.5F));

		PartDefinition singlePart3 = leftWing.addOrReplaceChild("singlePart3", CubeListBuilder.create().texOffs(102, 46).addBox(9.0F, -34.0F, -5.5F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(110, 74).addBox(9.0F, -38.0F, -6.5F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(13.0F, 33.5F, 4.5F));

		PartDefinition singlePart4 = leftWing.addOrReplaceChild("singlePart4", CubeListBuilder.create().texOffs(66, 103).addBox(9.0F, -34.0F, -5.5F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(118, 22).addBox(9.0F, -37.0F, -6.0F, 12.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(23.0F, 30.5F, 4.5F));

		PartDefinition singlePart5 = leftWing.addOrReplaceChild("singlePart5", CubeListBuilder.create().texOffs(112, 82).addBox(9.0F, -34.0F, -5.5F, 12.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(66, 40).addBox(9.0F, -37.0F, -6.0F, 16.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(35.0F, 30.5F, 4.5F));

		PartDefinition rightWing = body.addOrReplaceChild("rightWing", CubeListBuilder.create(), PartPose.offsetAndRotation(-15.0F, -42.5F, -5.5F, -0.2182F, 3.1416F, 0.0F));

		PartDefinition singlePart6 = rightWing.addOrReplaceChild("singlePart6", CubeListBuilder.create().texOffs(94, 103).addBox(9.0F, -34.0F, -5.5F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(78, 93).addBox(9.0F, -39.0F, -7.0F, 12.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.0F, 36.5F, 4.5F));

		PartDefinition singlePart7 = rightWing.addOrReplaceChild("singlePart7", CubeListBuilder.create().texOffs(106, 0).addBox(9.0F, -34.0F, -5.5F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(112, 94).addBox(9.0F, -38.0F, -6.5F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 33.5F, 4.5F));

		PartDefinition singlePart8 = rightWing.addOrReplaceChild("singlePart8", CubeListBuilder.create().texOffs(28, 109).addBox(9.0F, -34.0F, -5.5F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(56, 117).addBox(9.0F, -38.0F, -6.5F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(13.0F, 33.5F, 4.5F));

		PartDefinition singlePart9 = rightWing.addOrReplaceChild("singlePart9", CubeListBuilder.create().texOffs(110, 60).addBox(9.0F, -34.0F, -5.5F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(118, 28).addBox(9.0F, -37.0F, -6.0F, 12.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(23.0F, 30.5F, 4.5F));

		PartDefinition singlePart10 = rightWing.addOrReplaceChild("singlePart10", CubeListBuilder.create().texOffs(0, 115).addBox(9.0F, -34.0F, -5.5F, 12.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 95).addBox(9.0F, -37.0F, -6.0F, 16.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(35.0F, 30.5F, 4.5F));

		PartDefinition rightLeg = body.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(130, 45).addBox(1.5F, -12.0F, -0.5F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-13.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = rightLeg.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(102, 60).addBox(-1.0F, -5.0F, -1.5F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -3.0F, 0.5F, -0.3927F, 0.0F, 0.0F));

		PartDefinition rightPaw = rightLeg.addOrReplaceChild("rightPaw", CubeListBuilder.create().texOffs(132, 126).addBox(1.5F, -2.0F, -3.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(50, 125).addBox(3.0F, -2.0F, -12.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r2 = rightPaw.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 127).addBox(-0.5F, -2.0F, -1.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 0.0F, -10.0F, 0.0F, -0.0873F, 0.0F));

		PartDefinition cube_r3 = rightPaw.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(28, 123).addBox(0.5F, -2.0F, -1.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -10.0F, 0.0F, 0.0873F, 0.0F));

		PartDefinition leftLeg = body.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(72, 125).addBox(1.5F, -12.0F, -0.5F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r4 = leftLeg.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(34, 85).addBox(-1.0F, -5.0F, -1.5F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -3.0F, 0.5F, -0.3927F, 0.0F, 0.0F));

		PartDefinition leftPaw = leftLeg.addOrReplaceChild("leftPaw", CubeListBuilder.create().texOffs(132, 120).addBox(1.5F, -2.0F, -3.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(122, 102).addBox(3.0F, -2.0F, -12.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r5 = leftPaw.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(110, 122).addBox(-0.5F, -2.0F, -1.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 0.0F, -10.0F, 0.0F, -0.0873F, 0.0F));

		PartDefinition cube_r6 = leftPaw.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(88, 122).addBox(0.5F, -2.0F, -1.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -10.0F, 0.0F, 0.0873F, 0.0F));

		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(66, 25).addBox(-14.0F, -43.0F, -13.0F, 22.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r7 = torso.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 0).addBox(-23.0F, -12.0F, -1.0F, 24.0F, 12.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -32.0F, -8.0F, 0.2182F, 0.0F, 0.0F));

		PartDefinition cube_r8 = torso.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 25).addBox(-21.0F, -12.0F, -1.0F, 22.0F, 12.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, -21.0F, -5.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition cube_r9 = torso.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 48).addBox(-19.0F, -10.0F, -1.0F, 20.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -12.0F, -1.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(56, 48).addBox(-9.0F, -49.0F, -14.0F, 12.0F, 12.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(122, 114).addBox(-8.0F, -42.0F, -16.0F, 10.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, -1.0F));

		PartDefinition cube_r10 = head.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 68).addBox(-17.0F, -13.0F, -1.0F, 18.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -38.0F, -2.0F, 0.3054F, 0.0F, 0.0F));

		PartDefinition cube_r11 = head.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(44, 71).addBox(-4.0F, -14.0F, -0.5F, 7.0F, 14.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -46.0F, -12.0F, 0.0F, -0.6109F, 1.5708F));

		PartDefinition cube_r12 = head.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(78, 71).addBox(-5.0F, -12.0F, -1.0F, 6.0F, 12.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -37.0F, -11.0F, 0.0F, -0.4363F, 0.0F));

		PartDefinition cube_r13 = head.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(74, 0).addBox(-4.0F, -6.0F, -5.0F, 6.0F, 12.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1034F, -43.0F, -8.6187F, 0.0F, 0.4363F, 0.0F));

		PartDefinition mask = head.addOrReplaceChild("mask", CubeListBuilder.create().texOffs(104, 40).addBox(-10.0F, -44.0F, -17.0F, 14.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(118, 34).addBox(-9.0F, -46.0F, -17.0F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(88, 117).addBox(-10.0F, -48.0F, -17.0F, 14.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(NueEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.body.getAllParts().forEach(ModelPart::resetPose);
		// Вычисляем горизонтальную скорость
		Vec3 velocity = entity.getDeltaMovement();
		float horizontalSpeed = (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
		float maxSpeed = 1.0F; // Максимальная скорость для нормализации
		float speedFactor = Mth.clamp(horizontalSpeed / maxSpeed, 0.0F, 1.0F); // От 0 до 1
		switch (entity.getAnimation()){
			case NueEntity.IDLE -> this.animate(entity.getAnimationState(NueAnimations.idle), NueAnimations.idle, ageInTicks, 1);
			case NueEntity.FLY -> {
				this.animate(entity.getAnimationState(NueAnimations.fly), NueAnimations.fly, ageInTicks, 1);
				float maxTurnAngle = 60.0F; // Максимальный угол поворота (градусы)
				float turnAngle = speedFactor * maxTurnAngle; // Угол пропорционален скорости
				this.torso.xRot = turnAngle * ((float) Math.PI / 180F); // Направление зависит от velocity.x;
				this.torso.y = -speedFactor * 11;
				this.torso.z = speedFactor * 23;
				this.head.y = -5 + speedFactor * 18;
				this.head.z = -speedFactor * 14;
				this.leftWing.y = this.rightWing.y = -42.5F + speedFactor * 21.5F;
				this.leftWing.z = this.rightWing.z = -speedFactor * 9;
				this.leftLeg.xRot = this.rightLeg.xRot = speedFactor * 75 * ((float) Math.PI / 180F);
				this.leftLeg.y = this.rightLeg.y = -speedFactor * 14;
				this.leftLeg.z = this.rightLeg.z = speedFactor * 24;
			}
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return body;
	}
}