package com.aleksiyflekssiy.tutorialmod.item;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TutorialMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TUTORIAL_TAB = CREATIVE_MODE_TABS.register("tutorial_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.RUBY.get()))
                    .title(Component.translatable("creativetab.tutorial_tab"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(ModItems.RUBY.get());
                        output.accept(ModBlocks.RUBY_BLOCK.get());
                        output.accept(ModBlocks.RUBY_ORE.get());
                        output.accept(ModBlocks.DEEPSLATE_RUBY_ORE.get());
                        output.accept(ModItems.METAL_DETECTOR.get());
                        output.accept(ModBlocks.MAGIC_BLOCK.get());
                        output.accept(ModItems.TOMATO.get());
                        output.accept(ModItems.TOMATO_SEEDS.get());
                        output.accept(ModBlocks.WHITE_WOODEN_PLANKS.get());
                        output.accept(ModBlocks.WHITE_WOODEN_STAIRS.get());
                        output.accept(ModBlocks.WHITE_WOODEN_SLAB.get());
                        output.accept(ModBlocks.WHITE_WOODEN_BUTTON.get());
                        output.accept(ModBlocks.WHITE_WOODEN_PRESSURE_PLATE.get());
                        output.accept(ModBlocks.WHITE_WOODEN_FENCE.get());
                        output.accept(ModBlocks.WHITE_WOODEN_FENCE_GATE.get());
                        output.accept(ModBlocks.WHITE_WOODEN_WALL.get());
                        output.accept(ModBlocks.WHITE_WOODEN_DOOR.get());
                        output.accept(ModBlocks.WHITE_WOODEN_TRAPDOOR.get());
                        output.accept(ModItems.WAND_OF_HEALING.get());
                        output.accept(ModItems.INFINITY.get());
                        output.accept(ModItems.BLUE.get());
                        output.accept(ModItems.RED.get());
                        output.accept(ModItems.HOLLOW_PURPLE.get());
                        output.accept(ModItems.UNLIMITED_VOID.get());
                        output.accept(ModItems.COPPER_SWORD.get());
                        output.accept(ModItems.COPPER_AXE.get());
                        output.accept(ModItems.COPPER_HOE.get());
                        output.accept(ModItems.COPPER_SHOVEL.get());
                        output.accept(ModItems.COPPER_PICKAXE.get());
                        output.accept(ModItems.COPPER_HELMET.get());
                        output.accept(ModItems.COPPER_CHESTPLATE.get());
                        output.accept(ModItems.COPPER_LEGGINGS.get());
                        output.accept(ModItems.COPPER_BOOTS.get());
                    }))
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
