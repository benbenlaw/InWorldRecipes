package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.event.ClientRecipeCache;
import com.benbenlaw.inworldrecipes.recipe.Option;
import com.benbenlaw.inworldrecipes.recipe.WorldRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEIInWorldRecipesPlugin implements IModPlugin {

    public static IDrawableStatic slotDrawable;

    @Override
    public @NonNull Identifier getPluginUid() {
        return InWorldRecipes.identifier("jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(WorldRecipeCategory.RECIPE_TYPE, new ItemStack(Items.DIAMOND_BLOCK));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {

        slotDrawable = registration.getJeiHelpers().getGuiHelper().getSlotDrawable();

        registration.addRecipeCategories(new WorldRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        List<WorldRecipe> recipes = new ArrayList<>(ClientRecipeCache.getCachedWorldRecipes().stream()
                .filter(recipe -> recipe.options().stream().allMatch(Option::showInJEI))
                .toList());

        recipes.addAll(StrippingRecipeProvider.getStrippingRecipes());

        registration.addRecipes(WorldRecipeCategory.RECIPE_TYPE, recipes);
    }
}