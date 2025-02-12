package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JEIInWorldRecipesPlugin implements IModPlugin {


    public static RecipeType<RightClickOnBlockTransformsBlockRecipe> RIGHT_CLICK_ON_BLOCK_TRANSFORM_BLOCK_RECIPE =
            new RecipeType<>(RightClickOnBlockTransformsBlockRecipeCategory.UID, RightClickOnBlockTransformsBlockRecipe.class);

    public static RecipeType<RightClickOnBlockTransformsItemRecipe> RIGHT_CLICK_ON_BLOCK_TRANSFORM_ITEM_RECIPE =
            new RecipeType<>(RightClickOnBlockTransformsItemRecipeCategory.UID, RightClickOnBlockTransformsItemRecipe.class);

    public static RecipeType<DropItemInFluidRecipe> DROP_ITEM_IN_FLUID_RECIPE =
            new RecipeType<>(DropItemInFluidRecipeCategory.UID, DropItemInFluidRecipe.class);

    public static RecipeType<RightClickOnEntityTransformsItemRecipe> RIGHT_CLICK_ON_ENTITY_TRANSFORM_ITEM_RECIPE =
            new RecipeType<>(RightClickOnEntityTransformsItemRecipeCategory.UID, RightClickOnEntityTransformsItemRecipe.class);

    public static RecipeType<DropItemInFluidConvertsFluidRecipe> DROP_ITEM_IN_FLUID_CONVERTS_FLUID_RECIPE =
            new RecipeType<>(DropItemInFluidConvertsFluidRecipeCategory.UID, DropItemInFluidConvertsFluidRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {

    //    registration.addRecipeCatalyst(new ItemStack(Blocks.DIAMOND_BLOCK), RightClickOnBlockRecipeCategory.RECIPE_TYPE);

    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {

        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(new
                RightClickOnBlockTransformsBlockRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                RightClickOnBlockTransformsItemRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                DropItemInFluidRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                RightClickOnEntityTransformsItemRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                DropItemInFluidConvertsFluidRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        assert Minecraft.getInstance().level != null;
        final var recipeManager = Minecraft.getInstance().level.getRecipeManager();

        registration.addRecipes(RightClickOnBlockTransformsBlockRecipeCategory.RECIPE_TYPE,
                recipeManager.getAllRecipesFor(InWorldRecipeRecipes.RIGHT_CLICK_ON_BLOCK_TRANSFORMS_BLOCK_TYPE.get()).stream().map(RecipeHolder::value).toList());

        registration.addRecipes(RightClickOnBlockTransformsItemRecipeCategory.RECIPE_TYPE,
                recipeManager.getAllRecipesFor(InWorldRecipeRecipes.RIGHT_CLICK_ON_BLOCK_TRANSFORMS_ITEM_TYPE.get()).stream().map(RecipeHolder::value).toList());

        registration.addRecipes(DropItemInFluidRecipeCategory.RECIPE_TYPE,
                recipeManager.getAllRecipesFor(InWorldRecipeRecipes.DROP_ITEM_IN_FLUID_TYPE.get()).stream().map(RecipeHolder::value).toList());

        registration.addRecipes(DropItemInFluidConvertsFluidRecipeCategory.RECIPE_TYPE,
                recipeManager.getAllRecipesFor(InWorldRecipeRecipes.DROP_ITEM_IN_FLUID_CONVERTS_FLUID_RECIPE_TYPE.get()).stream().map(RecipeHolder::value).toList());


    }

}
