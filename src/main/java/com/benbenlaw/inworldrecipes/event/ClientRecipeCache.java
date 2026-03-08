package com.benbenlaw.inworldrecipes.event;

import com.benbenlaw.inworldrecipes.recipes.WorldRecipe;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClientRecipeCache {

    //Summoning Recipe Cache
    public static Map<Identifier, WorldRecipe> cachedWorldRecipes = new HashMap<>();

    public static Collection<WorldRecipe> getCachedWorldRecipes() {
        return cachedWorldRecipes.values();
    }

    public static void setCachedWorldRecipes(Map<Identifier, WorldRecipe> recipes) {
        cachedWorldRecipes = recipes;
    }




}
