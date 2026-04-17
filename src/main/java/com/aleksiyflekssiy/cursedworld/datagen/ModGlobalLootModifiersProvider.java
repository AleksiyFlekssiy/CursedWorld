package com.aleksiyflekssiy.cursedworld.datagen;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.item.ModItems;
import com.aleksiyflekssiy.cursedworld.loot.AddItemModifier;
import com.aleksiyflekssiy.cursedworld.loot.AddSusSandItemModifier;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output) {
        super(output, CursedWorld.MOD_ID);
    }

    @Override
    protected void start() {
        add("tomato_from_grass", new AddItemModifier(new LootItemCondition[]{
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.GRASS).build()}, ModItems.TOMATO.get()));
        add("metal_detector_from_suspicious_sand", new AddSusSandItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.parse("archaeology/desert_pyramid")).build()}, ModItems.METAL_DETECTOR.get()));
    }
}
