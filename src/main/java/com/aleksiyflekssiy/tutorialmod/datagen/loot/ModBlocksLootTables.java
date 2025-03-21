package com.aleksiyflekssiy.tutorialmod.datagen.loot;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.block.ModBlocks;
import com.aleksiyflekssiy.tutorialmod.block.custom.TomatoCropBlock;
import com.aleksiyflekssiy.tutorialmod.item.ModItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;
import java.util.function.Function;

public class ModBlocksLootTables extends BlockLootSubProvider {
    public ModBlocksLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.dropSelf(ModBlocks.MAGIC_BLOCK.get());
        this.dropSelf(ModBlocks.RUBY_BLOCK.get());
        this.dropSelf(ModBlocks.WHITE_WOODEN_PLANKS.get());
        this.dropSelf(ModBlocks.WHITE_WOODEN_STAIRS.get());
        this.dropSelf(ModBlocks.WHITE_WOODEN_BUTTON.get());
        this.dropSelf(ModBlocks.WHITE_WOODEN_PRESSURE_PLATE.get());
        this.dropSelf(ModBlocks.WHITE_WOODEN_TRAPDOOR.get());
        this.dropSelf(ModBlocks.WHITE_WOODEN_FENCE_GATE.get());
        this.dropSelf(ModBlocks.WHITE_WOODEN_FENCE.get());
        this.dropSelf(ModBlocks.WHITE_WOODEN_WALL.get());

        this.add(ModBlocks.RUBY_ORE.get(), block -> createOreDrop(block, ModItems.RUBY.get()));
        this.add(ModBlocks.DEEPSLATE_RUBY_ORE.get(), block -> createOreDrop(block, ModItems.RUBY.get()));
        this.add(ModBlocks.WHITE_WOODEN_SLAB.get(), block -> createSlabItemTable(ModBlocks.WHITE_WOODEN_SLAB.get()));
        this.add(ModBlocks.WHITE_WOODEN_DOOR.get(), block -> createDoorTable(ModBlocks.WHITE_WOODEN_DOOR.get()));

        LootItemCondition.Builder builder = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.TOMATO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(TomatoCropBlock.AGE, 5));

        this.add(ModBlocks.TOMATO_CROP.get(), createCropDrops(ModBlocks.TOMATO_CROP.get(), ModItems.TOMATO.get(),
                ModItems.TOMATO_SEEDS.get(), builder));
    }



    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
