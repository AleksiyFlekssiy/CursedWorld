package com.aleksiyflekssiy.tutorialmod.client.renderer;

import com.aleksiyflekssiy.tutorialmod.client.model.WheelOfHarmonyModel;
import com.aleksiyflekssiy.tutorialmod.item.custom.WheelOfHarmonyItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class WheelOfHarmonyArmorRenderer extends GeoArmorRenderer<WheelOfHarmonyItem> {
    public WheelOfHarmonyArmorRenderer() {
        super(new WheelOfHarmonyModel());
    }
}
