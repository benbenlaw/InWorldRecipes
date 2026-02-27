package com.benbenlaw.inworldrecipes.event;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.BlockInteractionRecipe;
import com.benbenlaw.inworldrecipes.recipes.InWorldRecipeRecipes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = InWorldRecipes.MOD_ID)
public class RecipeEvents {

    @SubscribeEvent
    public static void onDataPackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(InWorldRecipeRecipes.BLOCK_INTERACTION_RECIPE_TYPE.get());
    }

    @SubscribeEvent
    public static void onRecipeReceived(RecipesReceivedEvent event) {
        RecipeMap recipeMap = event.getRecipeMap();

        //Block Interaction Recipes
        Collection<RecipeHolder<BlockInteractionRecipe>> blockInteractionRecipes = recipeMap.byType(InWorldRecipeRecipes.BLOCK_INTERACTION_RECIPE_TYPE.get());
        Map<Identifier, BlockInteractionRecipe>  blockInteractionRecipeMap = new HashMap<>();

        for (RecipeHolder<BlockInteractionRecipe> holder : blockInteractionRecipes) {
            blockInteractionRecipeMap.put(holder.id().identifier(), holder.value());
        }

    }
}
