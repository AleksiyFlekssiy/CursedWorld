package com.aleksiyflekssiy.tutorialmod.client.model;

import com.aleksiyflekssiy.tutorialmod.item.custom.WheelOfHarmonyItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WheelOfHarmonyModel extends GeoModel<WheelOfHarmonyItem> {
    @Override
    public ResourceLocation getModelResource(WheelOfHarmonyItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("tutorialmod", "geo/wheel_of_harmony.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WheelOfHarmonyItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/item/armor/wheel_of_harmony.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WheelOfHarmonyItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("tutorialmod", "animations/wheel_rotation.animation.json");
    }
}
