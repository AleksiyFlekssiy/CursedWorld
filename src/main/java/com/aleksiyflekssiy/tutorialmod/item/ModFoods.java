package com.aleksiyflekssiy.tutorialmod.item;

import net.minecraft.world.food.FoodProperties;

public class ModFoods {
    public static final FoodProperties TOMATO = new FoodProperties.Builder()
            .nutrition(1)
            .saturationMod(0.5f)
            .fast()
            .build();
}
