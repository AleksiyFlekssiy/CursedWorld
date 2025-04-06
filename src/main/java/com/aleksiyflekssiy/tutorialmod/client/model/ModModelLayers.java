package com.aleksiyflekssiy.tutorialmod.client.model;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModModelLayers {
    public static final ModelLayerLocation BLUE_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "blue_entity"), "main");
    public static final ModelLayerLocation RED_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "red_entity"), "main");
    public static final ModelLayerLocation HOLLOW_PURPLE_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "hollow_purple_entity"), "main");
    public static final ModelLayerLocation DIVINE_DOG_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "divine_dog"), "main");
    public static final ModelLayerLocation NUE_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "nue"), "main");
    public static final ModelLayerLocation TOAD_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "toad"), "main");
}
