package com.aleksiyflekssiy.cursedworld.client.renderer;

import com.aleksiyflekssiy.cursedworld.client.model.WheelOfHarmonyModel;
import com.aleksiyflekssiy.cursedworld.item.custom.WheelOfHarmonyItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class WheelOfHarmonyArmorRenderer extends GeoArmorRenderer<WheelOfHarmonyItem> {
    public WheelOfHarmonyArmorRenderer() {
        super(new WheelOfHarmonyModel());
    }
}
