package com.aleksiyflekssiy.cursedworld.client.model;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.item.custom.WheelOfHarmonyItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WheelOfHarmonyModel extends GeoModel<WheelOfHarmonyItem> {
    @Override
    public ResourceLocation getModelResource(WheelOfHarmonyItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "geo/wheel_of_harmony.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WheelOfHarmonyItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/item/armor/wheel_of_harmony.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WheelOfHarmonyItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "animations/wheel_rotation.animation.json");
    }
}
