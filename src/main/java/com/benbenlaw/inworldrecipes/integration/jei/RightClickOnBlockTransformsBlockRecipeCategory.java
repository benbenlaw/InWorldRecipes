package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.RightClickOnBlockTransformsBlockRecipe;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;

public class RightClickOnBlockTransformsBlockRecipeCategory implements IRecipeCategory<RightClickOnBlockTransformsBlockRecipe> {
    public final static ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "right_click_on_block_transforms_block");
    public final static ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "textures/gui/jei_right_click_on_block.png");

    static final RecipeType<RightClickOnBlockTransformsBlockRecipe> RECIPE_TYPE = RecipeType.create(InWorldRecipes.MOD_ID, "right_click_on_block_transforms_block",
            RightClickOnBlockTransformsBlockRecipe.class);
    int yOffset = 21;
    int totalMessages;

    private IDrawable background;
    private final IDrawable icon;

    private final IGuiHelper helper;


    @Override
    public @Nullable ResourceLocation getRegistryName(RightClickOnBlockTransformsBlockRecipe recipe) {
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RightClickOnBlockTransformsBlockRecipe.Type.INSTANCE).stream()
                .filter(recipeHolder -> recipeHolder.value().equals(recipe))
                .map(RecipeHolder::id)
                .findFirst()
                .orElse(null);
    }

    public RightClickOnBlockTransformsBlockRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.background = helper.createDrawable(TEXTURE, 0, 0, 139, 18);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.DIAMOND_BLOCK));
    }


    @Override
    public @NotNull RecipeType<RightClickOnBlockTransformsBlockRecipe> getRecipeType() {
        return JEIInWorldRecipesPlugin.RIGHT_CLICK_ON_BLOCK_TRANSFORM_BLOCK_RECIPE;
    }


    @Override
    public @NotNull Component getTitle() {
        return Component.literal("Right Click On Block");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RightClickOnBlockTransformsBlockRecipe recipe, @NotNull IFocusGroup focusGroup) {

        totalMessages = 0;

        builder.addSlot(RecipeIngredientRole.CATALYST, 4, 2).addItemStacks(Arrays.asList(recipe.heldItem().getItems()));
        builder.addSlot(RecipeIngredientRole.INPUT, 40, 2).addIngredients(recipe.targetBlock());

        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(recipe.newBlock()));
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(recipe.newBlock()));

        if (item != Items.AIR) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 2).addItemStack(item.getDefaultInstance());
        } else {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 2).addFluidStack(fluid, 1000);

        }
        if (recipe.damageHeldItem()) {
            totalMessages += 1;
        }
        if (recipe.consumeHeldItem()) {
            totalMessages += 1;
        }

        background = helper.createDrawable(TEXTURE, 0, 0, 139, 18 + (totalMessages * 10));

    }

    @Override
    public void draw(RightClickOnBlockTransformsBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {

        yOffset = 20;
        if (recipe.damageHeldItem()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Damages Item", 0, 20, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (recipe.consumeHeldItem()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Consumes Item", 0, 20, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
    }

}
