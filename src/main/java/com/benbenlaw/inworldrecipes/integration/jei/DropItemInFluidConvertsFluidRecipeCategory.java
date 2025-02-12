package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.DropItemInFluidConvertsFluidRecipe;
import com.benbenlaw.inworldrecipes.recipes.DropItemInFluidRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Arrays;

public class DropItemInFluidConvertsFluidRecipeCategory implements IRecipeCategory<DropItemInFluidConvertsFluidRecipe> {
    public final static ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "drop_item_in_fluid_converts_fluid");
    public final static ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "textures/gui/jei_drop_item_in_fluid.png");
    static final RecipeType<DropItemInFluidConvertsFluidRecipe> RECIPE_TYPE = RecipeType.create(InWorldRecipes.MOD_ID, "drop_item_in_fluid_converts_fluid",
            DropItemInFluidConvertsFluidRecipe.class);

    int yOffset = 21;
    int totalMessages;
    private IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper helper;


    @Override
    public @Nullable ResourceLocation getRegistryName(DropItemInFluidConvertsFluidRecipe recipe) {
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(DropItemInFluidConvertsFluidRecipe.Type.INSTANCE).stream()
                .filter(recipeHolder -> recipeHolder.value().equals(recipe))
                .map(RecipeHolder::id)
                .findFirst()
                .orElse(null);
    }

    public DropItemInFluidConvertsFluidRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.background = helper.createDrawable(TEXTURE, 0, 0, 104, 18);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.DIAMOND_BLOCK));
    }


    @Override
    public @NotNull RecipeType<DropItemInFluidConvertsFluidRecipe> getRecipeType() {
        return JEIInWorldRecipesPlugin.DROP_ITEM_IN_FLUID_CONVERTS_FLUID_RECIPE;
    }


    @Override
    public @NotNull Component getTitle() {
        return Component.literal("Drop In Fluid");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DropItemInFluidConvertsFluidRecipe recipe, @NotNull IFocusGroup focusGroup) {

        totalMessages = 0;

        builder.addSlot(RecipeIngredientRole.INPUT, 4, 2).addItemStacks(Arrays.asList(recipe.droppedItem().getItems()));
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(recipe.fluid()));
        Fluid newFluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(recipe.newFluid()));
        builder.addSlot(RecipeIngredientRole.CATALYST, 40, 2).addFluidStack(fluid, 1000);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 2).addFluidStack(newFluid, 1000);

        background = helper.createDrawable(TEXTURE, 0, 0, 104, 18 + (totalMessages * 10));

    }
}
