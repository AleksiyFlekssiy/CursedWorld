package com.aleksiyflekssiy.tutorialmod.block;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.block.custom.MagicBlock;
import com.aleksiyflekssiy.tutorialmod.block.custom.TomatoCropBlock;
import com.aleksiyflekssiy.tutorialmod.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TutorialMod.MOD_ID);

    public static final RegistryObject<Block> RUBY_BLOCK = registerBlock("ruby_block", () -> new Block(BlockBehaviour.Properties.copy(Blocks.EMERALD_BLOCK)));
    public static final RegistryObject<Block> RUBY_ORE = registerBlock("ruby_ore", () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.EMERALD_ORE)
            .requiresCorrectToolForDrops(),
            UniformInt.of(3,6)
    ));
    public static final RegistryObject<Block> DEEPSLATE_RUBY_ORE = registerBlock("deepslate_ruby_ore", () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_EMERALD_ORE)
            .requiresCorrectToolForDrops(),
            UniformInt.of(3,6)
    ));
    public static final RegistryObject<Block> MAGIC_BLOCK = registerBlock("magic_block", () -> new MagicBlock(BlockBehaviour.Properties.copy(Blocks.GLOWSTONE)));

    public static final RegistryObject<Block> WHITE_WOODEN_PLANKS = registerBlock("white_wooden_planks", () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<Block> WHITE_WOODEN_STAIRS = registerBlock("white_wooden_stairs",
            () -> new StairBlock(() -> ModBlocks.WHITE_WOODEN_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS)));
    public static final RegistryObject<Block> WHITE_WOODEN_SLAB = registerBlock("white_wooden_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<Block> WHITE_WOODEN_BUTTON = registerBlock("white_wooden_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS), BlockSetType.OAK, 100, true));
    public static final RegistryObject<Block> WHITE_WOODEN_PRESSURE_PLATE = registerBlock("white_wooden_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS), BlockSetType.OAK));
    public static final RegistryObject<Block> WHITE_WOODEN_FENCE = registerBlock("white_wooden_fence", () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<Block> WHITE_WOODEN_FENCE_GATE = registerBlock("white_wooden_fence_gate", () -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS), SoundEvents.FENCE_GATE_OPEN, SoundEvents.FENCE_GATE_CLOSE));
    public static final RegistryObject<Block> WHITE_WOODEN_WALL = registerBlock("white_wooden_wall", () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<Block> WHITE_WOODEN_DOOR = registerBlock("white_wooden_door", () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion(), BlockSetType.OAK));
    public static final RegistryObject<Block> WHITE_WOODEN_TRAPDOOR = registerBlock("white_wooden_trapdoor", () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion(), BlockSetType.OAK));

    public static final RegistryObject<Block> TOMATO_CROP = BLOCKS.register("tomato_crop",
            () -> new TomatoCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
