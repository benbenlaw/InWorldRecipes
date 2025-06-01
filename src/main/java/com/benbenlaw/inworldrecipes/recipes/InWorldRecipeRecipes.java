package com.benbenlaw.inworldrecipes.recipes;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class InWorldRecipeRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZER =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, InWorldRecipes.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, InWorldRecipes.MOD_ID);

    //Right Click On Block Transforms Block

    public static final Supplier<RecipeSerializer<RightClickOnBlockTransformsBlockRecipe>> RIGHT_CLICK_ON_BLOCK_TRANSFORMS_BLOCK_SERIALIZER =
            SERIALIZER.register("right_click_on_block_transforms_block", () -> RightClickOnBlockTransformsBlockRecipe.Serializer.INSTANCE);

    public static final Supplier<RecipeType<RightClickOnBlockTransformsBlockRecipe>> RIGHT_CLICK_ON_BLOCK_TRANSFORMS_BLOCK_TYPE =
            TYPES.register("right_click_on_block_transforms_block", () -> RightClickOnBlockTransformsBlockRecipe.Type.INSTANCE);

    //Right Click On Block Transforms Item

    public static final Supplier<RecipeSerializer<RightClickOnBlockTransformsItemRecipe>> RIGHT_CLICK_ON_BLOCK_TRANSFORMS_ITEM_SERIALIZER =
            SERIALIZER.register("right_click_on_block_transforms_item", () -> RightClickOnBlockTransformsItemRecipe.Serializer.INSTANCE);

    public static final Supplier<RecipeType<RightClickOnBlockTransformsItemRecipe>> RIGHT_CLICK_ON_BLOCK_TRANSFORMS_ITEM_TYPE =
            TYPES.register("right_click_on_block_transforms_item", () -> RightClickOnBlockTransformsItemRecipe.Type.INSTANCE);

    //Drop Item In Fluid

    public static final Supplier<RecipeSerializer<DropItemInFluidRecipe>> DROP_ITEM_IN_FLUID_SERIALIZER =
            SERIALIZER.register("drop_item_in_fluid", () -> DropItemInFluidRecipe.Serializer.INSTANCE);

    public static final Supplier<RecipeType<DropItemInFluidRecipe>> DROP_ITEM_IN_FLUID_TYPE =
            TYPES.register("drop_item_in_fluid", () -> DropItemInFluidRecipe.Type.INSTANCE);

    //Right Click On Entity Transforms Item

    public static final Supplier<RecipeSerializer<RightClickOnEntityTransformsItemRecipe>> RIGHT_CLICK_ON_ENTITY_TRANSFORMS_ITEM_SERIALIZER =
            SERIALIZER.register("right_click_on_entity_transforms_item", () -> RightClickOnEntityTransformsItemRecipe.Serializer.INSTANCE);

    public static final Supplier<RecipeType<RightClickOnEntityTransformsItemRecipe>> RIGHT_CLICK_ON_ENTITY_TRANSFORMS_ITEM_TYPE =
            TYPES.register("right_click_on_entity_transforms_item", () -> RightClickOnEntityTransformsItemRecipe.Type.INSTANCE);

    //Fluid to Fluid
    public static final Supplier<RecipeSerializer<DropItemInFluidConvertsFluidRecipe>> DROP_ITEM_IN_FLUID_CONVERTS_FLUID_RECIPE_SERIALIZER =
            SERIALIZER.register("drop_item_in_fluid_converts_fluid", () -> DropItemInFluidConvertsFluidRecipe.Serializer.INSTANCE);

    public static final Supplier<RecipeType<DropItemInFluidConvertsFluidRecipe>> DROP_ITEM_IN_FLUID_CONVERTS_FLUID_RECIPE_TYPE =
            TYPES.register("drop_item_in_fluid_converts_fluid", () -> DropItemInFluidConvertsFluidRecipe.Type.INSTANCE);

    //Block Conversion
    public static final Supplier<RecipeSerializer<BlockConversionRecipe>> BLOCK_CONVERSION_RECIPE_SERIALIZER =
            SERIALIZER.register("block_conversion", () -> BlockConversionRecipe.Serializer.INSTANCE);

    public static final Supplier<RecipeType<BlockConversionRecipe>> BLOCK_CONVERSION_RECIPE_TYPE =
            TYPES.register("block_conversion", () -> BlockConversionRecipe.Type.INSTANCE);


    //NEW 1.22
    //Block Interaction Recipe
    public static final Supplier<RecipeSerializer<BlockInteractionRecipe>> BLOCK_INTERACTION_RECIPE_SERIALIZER =
            SERIALIZER.register("block_interaction", () -> BlockInteractionRecipe.Serializer.INSTANCE);
    public static final Supplier<RecipeType<BlockInteractionRecipe>> BLOCK_INTERACTION_RECIPE_TYPE =
            TYPES.register("block_interaction", () -> BlockInteractionRecipe.Type.INSTANCE);


    public static void register(IEventBus eventBus) {
        SERIALIZER.register(eventBus);
        TYPES.register(eventBus);
    }

}
