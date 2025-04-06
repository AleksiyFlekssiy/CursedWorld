package com.aleksiyflekssiy.tutorialmod.datagen;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.block.ModBlocks;
import com.aleksiyflekssiy.tutorialmod.block.custom.TomatoCropBlock;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TutorialMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithBlockItem(ModBlocks.DEEPSLATE_RUBY_ORE);
        blockWithBlockItem(ModBlocks.MAGIC_BLOCK);
        blockWithBlockItem(ModBlocks.RUBY_BLOCK);
        blockWithBlockItem(ModBlocks.RUBY_ORE);
        blockWithBlockItem(ModBlocks.WHITE_WOODEN_PLANKS);

        stairsBlock((StairBlock) ModBlocks.WHITE_WOODEN_STAIRS.get(), blockTexture(ModBlocks.WHITE_WOODEN_PLANKS.get()));
        slabBlock((SlabBlock) ModBlocks.WHITE_WOODEN_SLAB.get(), blockTexture(ModBlocks.WHITE_WOODEN_PLANKS.get()), blockTexture(ModBlocks.WHITE_WOODEN_PLANKS.get()));
        buttonBlock((ButtonBlock) ModBlocks.WHITE_WOODEN_BUTTON.get(), blockTexture(ModBlocks.WHITE_WOODEN_PLANKS.get()));
        pressurePlateBlock((PressurePlateBlock) ModBlocks.WHITE_WOODEN_PRESSURE_PLATE.get(), blockTexture(ModBlocks.WHITE_WOODEN_PLANKS.get()));
        fenceBlock((FenceBlock) ModBlocks.WHITE_WOODEN_FENCE.get(), blockTexture(ModBlocks.WHITE_WOODEN_PLANKS.get()));
        fenceGateBlock((FenceGateBlock) ModBlocks.WHITE_WOODEN_FENCE_GATE.get(), blockTexture(ModBlocks.WHITE_WOODEN_PLANKS.get()));
        wallBlock((WallBlock) ModBlocks.WHITE_WOODEN_WALL.get(), blockTexture(ModBlocks.WHITE_WOODEN_PLANKS.get()));
        doorBlockWithRenderType((DoorBlock) ModBlocks.WHITE_WOODEN_DOOR.get(), modLoc("block/white_wooden_door_bottom"), modLoc("block/white_wooden_door_top"), "cutout");
        trapdoorBlockWithRenderType((TrapDoorBlock) ModBlocks.WHITE_WOODEN_TRAPDOOR.get(), modLoc("block/white_wooden_door_top"), true, "cutout");

        makeTomatoCrop((CropBlock) ModBlocks.TOMATO_CROP.get(), "tomato_stage", "tomato_stage");
    }

    public void makeTomatoCrop(CropBlock block, String modelName, String textureName) {
        Function<BlockState, ConfiguredModel[]> function = state -> tomatoStates(state, block, modelName, textureName);

        getVariantBuilder(block).forAllStates(function);
    }

    private ConfiguredModel[] tomatoStates(BlockState state, CropBlock block, String modelName, String textureName) {
        ConfiguredModel[] models = new ConfiguredModel[1];
        models[0] = new ConfiguredModel(models().crop(modelName + state.getValue(((TomatoCropBlock) block).getAgeProperty()),
                ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "block/" + textureName + state.getValue(((TomatoCropBlock) block).getAgeProperty()))).renderType("cutout"));

        return models;
    }

    private void blockWithBlockItem(RegistryObject<Block> registryBlock) {
        simpleBlockWithItem(registryBlock.get(), cubeAll(registryBlock.get()));
    }
}
