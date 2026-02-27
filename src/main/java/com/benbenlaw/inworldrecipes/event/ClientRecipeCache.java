package com.benbenlaw.inworldrecipes.event;

import com.benbenlaw.inworldrecipes.recipes.BlockInteractionRecipe;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClientRecipeCache {

    //Summoning Recipe Cache
    public static Map<Identifier, BlockInteractionRecipe> cachedBlockInteractionRecipes = new HashMap<>();

    public static Collection<BlockInteractionRecipe> getCachedBlockInteractionRecipes() {
        return cachedBlockInteractionRecipes.values();
    }

    public static void setCachedBlockInteractionRecipes(Map<Identifier, BlockInteractionRecipe> recipes) {
        cachedBlockInteractionRecipes = recipes;
    }


}
