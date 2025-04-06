package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.BlockConversionRecipe;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class BlockConversionRecipeCategory implements IRecipeCategory<BlockConversionRecipe> {
    public final static ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "block_conversion");
    public final static ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "textures/gui/jei_block_conversion.png");
    static final RecipeType<BlockConversionRecipe> RECIPE_TYPE = RecipeType.create(InWorldRecipes.MOD_ID, "block_conversion",
            BlockConversionRecipe.class);

    int yOffset = 21;
    int totalMessages;
    private IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper helper;


    @Override
    public @Nullable ResourceLocation getRegistryName(BlockConversionRecipe recipe) {
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BlockConversionRecipe.Type.INSTANCE).stream()
                .filter(recipeHolder -> recipeHolder.value().equals(recipe))
                .map(RecipeHolder::id)
                .findFirst()
                .orElse(null);
    }

    public BlockConversionRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.background = helper.createDrawable(TEXTURE, 0, 0, 139, 18);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.DIAMOND_BLOCK));
    }


    @Override
    public @NotNull RecipeType<BlockConversionRecipe> getRecipeType() {
        return JEIInWorldRecipesPlugin.BLOCK_CONVERSION_RECIPE;
    }


    @Override
    public @NotNull Component getTitle() {
        return Component.literal("In World Conversion");
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
    public void setRecipe(IRecipeLayoutBuilder builder, BlockConversionRecipe recipe, @NotNull IFocusGroup focusGroup) {

        totalMessages = 1;

        ItemStack blockToConvert = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(recipe.blockToConvert())).asItem().getDefaultInstance();
        ItemStack convertedBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(recipe.convertedBlock())).asItem().getDefaultInstance();

        builder.addSlot(RecipeIngredientRole.INPUT, 40, 2).addItemStack(blockToConvert);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 2).addItemStack(convertedBlock);

        if (recipe.requiresSunlight()) {
            totalMessages += 1;
        }
        if (recipe.requiresMoonlight()) {
            totalMessages += 1;
        }
        if (recipe.popBlock()) {
            totalMessages += 1;
        }
        if (!recipe.dimension().isBlank()) {
            totalMessages += 1;
        }

        background = helper.createDrawable(TEXTURE, 0, 0, 139, 18 + (totalMessages * 10));

    }

    @Override
    public void draw(BlockConversionRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        yOffset = 20;

        if (recipe.requiresMoonlight()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Converts at Night", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (recipe.requiresSunlight()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Coverts in Day Light", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (recipe.popBlock()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Block Drops In World", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (!recipe.dimension().isBlank()) {
            if (recipe.dimension().contains("none")) {
                guiGraphics.drawString(Minecraft.getInstance().font, "In: Any Dimension", 0, yOffset, Color.GRAY.getRGB(), false);
            } else {
                guiGraphics.drawString(Minecraft.getInstance().font, "In: " + recipe.dimension(), 0, yOffset, Color.GRAY.getRGB(), false);
            }
            yOffset += 10;
        }
        if (recipe.duration() > 0) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Duration: " + recipe.duration() + " ticks", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
    }

}
