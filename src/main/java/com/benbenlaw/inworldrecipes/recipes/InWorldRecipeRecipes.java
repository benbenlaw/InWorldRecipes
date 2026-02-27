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

    //Block Interaction Recipe
    public static final Supplier<RecipeSerializer<BlockInteractionRecipe>> BLOCK_INTERACTION_RECIPE_SERIALIZER =
            SERIALIZER.register("block_interaction", () -> BlockInteractionRecipe.SERIALIZER);

    public static final Supplier<RecipeType<BlockInteractionRecipe>> BLOCK_INTERACTION_RECIPE_TYPE =
            TYPES.register("block_interaction", () -> BlockInteractionRecipe.TYPE);


    public static void register(IEventBus eventBus) {
        SERIALIZER.register(eventBus);
        TYPES.register(eventBus);
    }

}
