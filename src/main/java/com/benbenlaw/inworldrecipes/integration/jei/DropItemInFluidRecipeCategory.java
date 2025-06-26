package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.inworldrecipes.InWorldRecipes;
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
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

public class DropItemInFluidRecipeCategory implements IRecipeCategory<DropItemInFluidRecipe> {
    public final static ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "drop_item_in_fluid");
    public final static ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "textures/gui/jei_drop_item_in_fluid.png");
    static final RecipeType<DropItemInFluidRecipe> RECIPE_TYPE = RecipeType.create(InWorldRecipes.MOD_ID, "drop_item_in_fluid",
            DropItemInFluidRecipe.class);

    int yOffset = 21;
    int totalMessages;
    private IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper helper;


    @Override
    public @Nullable ResourceLocation getRegistryName(DropItemInFluidRecipe recipe) {
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(DropItemInFluidRecipe.Type.INSTANCE).stream()
                .filter(recipeHolder -> recipeHolder.value().equals(recipe))
                .map(RecipeHolder::id)
                .findFirst()
                .orElse(null);
    }

    public DropItemInFluidRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.background = helper.createDrawable(TEXTURE, 0, 0, 104, 18);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.DIAMOND_BLOCK));
    }


    @Override
    public @NotNull RecipeType<DropItemInFluidRecipe> getRecipeType() {
        return JEIInWorldRecipesPlugin.DROP_ITEM_IN_FLUID_RECIPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, DropItemInFluidRecipe recipe, @NotNull IFocusGroup focusGroup) {

        totalMessages = 0;

        NonNullList<SizedIngredient> ingredients = recipe.droppedItems();
        List<ItemStack> itemStacks = new ArrayList<>();
        for (SizedIngredient ingredient : ingredients) {
            itemStacks.add(ingredient.getItems()[0].copy());
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 4, 2).addItemStacks(itemStacks);
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(recipe.fluid()));
        builder.addSlot(RecipeIngredientRole.CATALYST, 40, 2).addFluidStack(fluid, 1000);

        List<ItemStack> results = recipe.getResults();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 2).addItemStacks(results);

        if (recipe.consumeFluidBlock()) {
            totalMessages += 1;
        }

        background = helper.createDrawable(TEXTURE, 0, 0, 104, 18 + (totalMessages * 10));

    }

    @Override
    public void draw(DropItemInFluidRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        yOffset = 20;

        if (recipe.consumeFluidBlock()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Consumes Fluid", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
    }

}
