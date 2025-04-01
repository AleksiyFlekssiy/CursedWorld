package com.aleksiyflekssiy.tutorialmod.client.model;// Save this class in your mod and generate all required imports

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Made with Blockbench 4.12.4
 * Exported for Minecraft version 1.19 or later with Mojang mappings
 * @author Author
 */
public class NueAnimations {
	public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength(0.0F)
		.addAnimation("singlePart", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-64.9601F, 52.3016F, -69.7141F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(17.0F, -1.0F, -11.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(86.6948F, -22.1399F, -85.8859F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart2", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(-2.0F, 5.0F, 16.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(92.7234F, -19.9794F, -90.9363F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart3", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(-16.0F, -5.0F, 14.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart4", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, -10.0F, -90.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart4", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(-25.0F, -14.0F, 15.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart5", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(100.425F, -14.7636F, 87.3089F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart5", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(-23.0F, 21.0F, 13.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart6", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(58.7615F, -57.7197F, -67.8738F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart6", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(10.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart7", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-96.8409F, -22.0083F, 94.7959F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart7", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(2.0F, 31.0F, -36.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart8", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 22.5F, -90.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart8", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(-6.0F, -8.0F, -21.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart9", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, -10.0F, 85.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart9", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(-25.0F, 14.0F, -29.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart10", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-92.5024F, 2.4976F, 89.8909F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("singlePart10", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(-35.0F, 13.0F, -27.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.build();

	public static final AnimationDefinition fly = AnimationDefinition.Builder.withLength(1.5F).looping()
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("leftWing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(45.0F, 0.0F, -45.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 45.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(1.5F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("leftWing", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 9.0F, 10.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.5F, KeyframeAnimations.posVec(11.0F, 7.0F, 9.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(1.0F, KeyframeAnimations.posVec(-8.0F, 15.0F, 9.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 9.0F, 10.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("leftWing2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 45.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, -45.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(1.5F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("leftWing2", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 2.0F, 13.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.5F, KeyframeAnimations.posVec(-15.0F, 2.0F, 13.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(1.0F, KeyframeAnimations.posVec(14.0F, 12.0F, 13.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 2.0F, 13.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(75.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 14.0F, 25.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(75.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 14.0F, 24.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("torso", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("torso", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 11.0F, 23.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -18.0F, -14.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.build();
}