package com.benbenlaw.inworldrecipes.event;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.WorldRecipe;
import com.benbenlaw.inworldrecipes.recipes.InWorldRecipeRecipes;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = InWorldRecipes.MOD_ID)
public class RecipeEvents {

    @SubscribeEvent
    public static void onDataPackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(InWorldRecipeRecipes.WORLD_RECIPE_TYPE.get());
    }

    @SubscribeEvent
    public static void onRecipeReceived(RecipesReceivedEvent event) {
        RecipeMap recipeMap = event.getRecipeMap();

        //Block Interaction Recipes
        Collection<RecipeHolder<WorldRecipe>> worldRecipes = recipeMap.byType(InWorldRecipeRecipes.WORLD_RECIPE_TYPE.get());
        Map<Identifier, WorldRecipe>  worldRecipeMap = new HashMap<>();

        for (RecipeHolder<WorldRecipe> holder : worldRecipes) {
            worldRecipeMap.put(holder.id().identifier(), holder.value());
        }

        ClientRecipeCache.setCachedWorldRecipes(worldRecipeMap);

    }

}
