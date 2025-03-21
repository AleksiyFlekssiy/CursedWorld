package com.aleksiyflekssiy.tutorialmod.datagen;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.block.ModBlocks;
import com.aleksiyflekssiy.tutorialmod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    private static final List<ItemLike> RUBY_SMELTABLES = List.of(ModBlocks.RUBY_ORE.get(), ModBlocks.DEEPSLATE_RUBY_ORE.get());
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        oreSmelting(consumer, RUBY_SMELTABLES, RecipeCategory.MISC, ModItems.RUBY.get(), 0.5f, 200, "ruby");
        oreBlasting(consumer, RUBY_SMELTABLES, RecipeCategory.MISC, ModItems.RUBY.get(), 0.5f, 100, "ruby");

//        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.RUBY_BLOCK.get())
//                .pattern("###")
//                .pattern("###")
//                .pattern("###")
//                .define('#', ModItems.RUBY.get())
//                .save(consumer);
//
//        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RUBY.get(), 9)
//                .requires(ModBlocks.RUBY_BLOCK.get())
//                .save(consumer);
        RecipeProvider.nineBlockStorageRecipes(consumer, RecipeCategory.MISC, ModItems.RUBY.get(), RecipeCategory.MISC, ModBlocks.RUBY_BLOCK.get());
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.COPPER_PICKAXE.get())
                .pattern("---")
                .pattern(" | ")
                .pattern(" | ")
                .define('-', Items.COPPER_INGOT)
                .define('|', Items.STICK)
                .unlockedBy("copper", has(Items.COPPER_INGOT))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.COPPER_AXE.get())
                .pattern("-- ")
                .pattern("-| ")
                .pattern(" | ")
                .define('-', Items.COPPER_INGOT)
                .define('|', Items.STICK)
                .unlockedBy("copper", has(Items.COPPER_INGOT))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.COPPER_SHOVEL.get())
                .pattern(" - ")
                .pattern(" | ")
                .pattern(" | ")
                .define('-', Items.COPPER_INGOT)
                .define('|', Items.STICK)
                .unlockedBy("copper", has(Items.COPPER_INGOT))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.COPPER_HOE.get())
                .pattern("-- ")
                .pattern(" | ")
                .pattern(" | ")
                .define('-', Items.COPPER_INGOT)
                .define('|', Items.STICK)
                .unlockedBy("copper", has(Items.COPPER_INGOT))
                .save(consumer);
    }

    protected static void oreSmelting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(new ItemLike[]{itemlike}), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike)).save(pFinishedRecipeConsumer, TutorialMod.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }

    }
}
