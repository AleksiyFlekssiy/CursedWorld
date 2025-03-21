package com.aleksiyflekssiy.tutorialmod.datagen;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.block.ModBlocks;
import com.aleksiyflekssiy.tutorialmod.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TutorialMod.MOD_ID, existingFileHelper);
    }
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModTags.Blocks.METAL_DETECTOR_VALUABLES)
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get())
                .add(ModBlocks.RUBY_ORE.get())
                .addTags(Tags.Blocks.ORES);
        this.tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get())
                .add(ModBlocks.RUBY_ORE.get());
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get(),
                        ModBlocks.RUBY_ORE.get(),
                        ModBlocks.MAGIC_BLOCK.get());
        this.tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.WHITE_WOODEN_PLANKS.get());
        this.tag(BlockTags.FENCES).add(ModBlocks.WHITE_WOODEN_FENCE.get());
        this.tag(BlockTags.FENCE_GATES).add(ModBlocks.WHITE_WOODEN_FENCE_GATE.get());
        this.tag(BlockTags.WALLS).add(ModBlocks.WHITE_WOODEN_WALL.get());

    }
}
