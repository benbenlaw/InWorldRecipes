package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.RightClickOnBlockTransformsBlockRecipe;
import com.benbenlaw.inworldrecipes.recipes.RightClickOnBlockTransformsItemRecipe;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Arrays;

public class RightClickOnBlockTransformsItemRecipeCategory implements IRecipeCategory<RightClickOnBlockTransformsItemRecipe> {
    public final static ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "right_click_on_block_transforms_item");
    public final static ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "textures/gui/jei_right_click_on_block_item.png");
    static final RecipeType<RightClickOnBlockTransformsItemRecipe> RECIPE_TYPE = RecipeType.create(InWorldRecipes.MOD_ID, "right_click_on_block_transforms_item",
            RightClickOnBlockTransformsItemRecipe.class);

    int yOffset = 21;
    int totalMessages;
    private IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper helper;


    @Override
    public @Nullable ResourceLocation getRegistryName(RightClickOnBlockTransformsItemRecipe recipe) {
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RightClickOnBlockTransformsBlockRecipe.Type.INSTANCE).stream()
                .filter(recipeHolder -> recipeHolder.value().equals(recipe))
                .map(RecipeHolder::id)
                .findFirst()
                .orElse(null);
    }

    public RightClickOnBlockTransformsItemRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.background = helper.createDrawable(TEXTURE, 0, 0, 139, 18);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.DIAMOND_BLOCK));
    }


    @Override
    public @NotNull RecipeType<RightClickOnBlockTransformsItemRecipe> getRecipeType() {
        return JEIInWorldRecipesPlugin.RIGHT_CLICK_ON_BLOCK_TRANSFORM_ITEM_RECIPE;
    }


    @Override
    public @NotNull Component getTitle() {
        return Component.literal("Right Click On Block Item");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RightClickOnBlockTransformsItemRecipe recipe, @NotNull IFocusGroup focusGroup) {

        totalMessages = 0;

        builder.addSlot(RecipeIngredientRole.CATALYST, 4, 2).addItemStacks(Arrays.asList(recipe.heldItem().getItems()));
        builder.addSlot(RecipeIngredientRole.INPUT, 40, 2).addIngredients(recipe.targetBlock());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 2).addItemStack(recipe.resultItem());

        if (recipe.damageHeldItem()) {
            totalMessages += 1;
        }
        if (recipe.consumeHeldItem()) {
            totalMessages += 1;
        }
        if (recipe.popItem()) {
            totalMessages += 1;
        }
        if (recipe.destroyTargetBlock()) {
            totalMessages += 1;
        }

        background = helper.createDrawable(TEXTURE, 0, 0, 139, 18 + (totalMessages * 10));

    }

    @Override
    public void draw(RightClickOnBlockTransformsItemRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        yOffset = 20;

        if (recipe.damageHeldItem()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Damages Item", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (recipe.consumeHeldItem()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Consumes Item", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (recipe.popItem()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Item Drops In World", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (recipe.destroyTargetBlock()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Destroys Target Block", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
    }

}
