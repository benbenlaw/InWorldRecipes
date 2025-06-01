package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.MouseUtil;
import com.benbenlaw.inworldrecipes.recipes.BlockConversionRecipe;
import com.benbenlaw.inworldrecipes.recipes.BlockInteractionRecipe;
import com.benbenlaw.inworldrecipes.recipes.RightClickOnBlockTransformsBlockRecipe;
import com.benbenlaw.inworldrecipes.recipes.RightClickOnBlockTransformsItemRecipe;
import com.benbenlaw.inworldrecipes.util.ClickType;
import com.benbenlaw.inworldrecipes.util.JEIBlockRenderHelper;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethodStage;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BlockInteractionRecipeCategory implements IRecipeCategory<BlockInteractionRecipe> {
    public final static ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "block_interaction");
    public final static ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "textures/gui/jei_block_interaction.png");
    static final RecipeType<BlockInteractionRecipe> RECIPE_TYPE = RecipeType.create(InWorldRecipes.MOD_ID, "block_interaction",
            BlockInteractionRecipe.class);

    int yOffset = 21;
    int totalMessages;
    private IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper helper;


    @Override
    public @Nullable ResourceLocation getRegistryName(BlockInteractionRecipe recipe) {
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RightClickOnBlockTransformsBlockRecipe.Type.INSTANCE).stream()
                .filter(recipeHolder -> recipeHolder.value().equals(recipe))
                .map(RecipeHolder::id)
                .findFirst()
                .orElse(null);
    }

    public BlockInteractionRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.background = helper.createDrawable(TEXTURE, 0, 0, 139, 18);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.DIAMOND_BLOCK));
    }


    @Override
    public @NotNull RecipeType<BlockInteractionRecipe> getRecipeType() {
        return JEIInWorldRecipesPlugin.BLOCK_INTERACTION_RECIPE;
    }


    @Override
    public @NotNull Component getTitle() {
        return Component.literal("Block Interaction");
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
    public void setRecipe(IRecipeLayoutBuilder builder, BlockInteractionRecipe recipe, @NotNull IFocusGroup focusGroup) {

        totalMessages = 0;

        builder.addSlot(RecipeIngredientRole.INPUT, 4, 2).addItemStack(new ItemStack(recipe.targetBlockState().getBlock()))
                        .setCustomRenderer(VanillaTypes.ITEM_STACK, new IIngredientRenderer<>() {
                            @Override
                            public void render(GuiGraphics guiGraphics, ItemStack stack) {
                                JEIBlockRenderHelper.renderBlock(guiGraphics, recipe.targetBlockState(), 5 - 4, 14 - 2, 0.60f);

                            }

                            @Override
                            public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
                                List<Component> tooltip = new ArrayList<>();

                                tooltip.add(recipe.targetBlockState().getBlock().getName());

                                for (Map.Entry<Property<?>, Comparable<?>> entry : recipe.targetBlockState().getValues().entrySet()) {
                                    String key = entry.getKey().getName();
                                    String value = entry.getValue().toString();
                                    tooltip.add(Component.literal(key + ": " + value));
                                }

                                return tooltip;
                            }
                        });

        Block output = recipe.outputBlockState().getBlock();

        if (output == Blocks.AIR) {
            output = Blocks.BARRIER;
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 2).addItemStack(new ItemStack(output))
                .setCustomRenderer(VanillaTypes.ITEM_STACK, new IIngredientRenderer<>() {
                    @Override
                    public void render(GuiGraphics guiGraphics, ItemStack stack) {
                        Block outputBlock = recipe.outputBlockState().getBlock();
                        if (outputBlock != Blocks.AIR) {
                            JEIBlockRenderHelper.renderBlock(guiGraphics, recipe.outputBlockState(), 5 - 4, 14 - 2, 0.60f);
                        }
                        else if (recipe.chanceResults().isEmpty()) {
                            JEIBlockRenderHelper.renderBlock(guiGraphics, Blocks.BARRIER.defaultBlockState(), 5 - 4, 14 - 2, 0.60f);
                        }
                        else {
                            JEIBlockRenderHelper.renderBlock(guiGraphics, Blocks.CHEST.defaultBlockState(), 5 - 4, 14 - 2, 0.60f);
                        }
                    }

                    @Override
                    public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
                        List<Component> tooltip = new ArrayList<>();

                        // Add the main block name
                        Block outputBlock = recipe.outputBlockState().getBlock();
                        if (outputBlock != Blocks.AIR) {
                            tooltip.add(outputBlock.getName());
                        }

                        for (Map.Entry<Property<?>, Comparable<?>> entry : recipe.outputBlockState().getValues().entrySet()) {
                            String key = entry.getKey().getName();
                            String value = entry.getValue().toString();
                            tooltip.add(Component.literal(key + ": " + value));
                        }

                        // Add all chance results (items)
                        List<ChanceResult> results = recipe.chanceResults();
                        if (!results.isEmpty()) {
                            tooltip.add(Component.literal("Chance Results:").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD));
                            for (ChanceResult result : results) {
                                ItemStack stack = result.stack();  // or result.stack() depending on your method

                                // Get the item name with formatting
                                Component itemName = stack.getHoverName();

                                // Format chance as percentage
                                String chancePercent = String.format("%.1f%%", result.chance() * 100);

                                tooltip.add(Component.literal(" - ").append(itemName).append(": ").append(Component.literal(chancePercent))
                                        .withStyle(ChatFormatting.GOLD));
                            }
                        }

                        return tooltip;
                    }
                });


        builder.addSlot(RecipeIngredientRole.INPUT, 76, 2).addIngredients(recipe.heldItem().ingredient());

        List<ChanceResult> results = recipe.chanceResults();
        if (!results.isEmpty()) {

            for (ChanceResult result : results) {
                ItemStack stack = result.stack();

                builder.addSlot(RecipeIngredientRole.OUTPUT, 76, 2).addItemStack(stack)
                        .setCustomRenderer(VanillaTypes.ITEM_STACK, new IIngredientRenderer<>() {
                            @Override
                            public void render(GuiGraphics guiGraphics, ItemStack stack) {
                                // Intentionally empty to prevent rendering the item icon
                            }

                            @Override
                            public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
                                // Return the tooltip you want to show when hovering
                                return List.of(ingredient.getHoverName());
                            }
                        });

            }
        }


        if (recipe.damageHeldItem()) {
            totalMessages += 1;
        }
        if (recipe.consumeHeldItem()) {
            totalMessages += 1;
        }
        if (recipe.popItems()) {
            totalMessages += 1;
        }
        if (recipe.outputBlockState().is(Blocks.AIR)) {
            totalMessages += 1;
        }

        background = helper.createDrawable(TEXTURE, 0, 0, 139, 18 + (totalMessages * 10));

    }


    @Override
    public void draw(BlockInteractionRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {

        if (recipe.clickType() == ClickType.LEFT_CLICK) {
            guiGraphics.blit(TEXTURE, 39, 1, 158, 0, 18, 18);
        }
        if (recipe.clickType() == ClickType.RIGHT_CLICK) {
            guiGraphics.blit(TEXTURE, 39, 1, 140, 0, 18, 18);
        }

        if (mouseX >= 40 && mouseX < 40 + 18 && mouseY >= 2 && mouseY < 2 + 18) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.literal("Click Type: " + recipe.clickType()), (int) mouseX, (int) mouseY);
        }

        yOffset = 20;

        if (recipe.damageHeldItem()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Damages Item", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (recipe.consumeHeldItem()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Consumes Item", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (recipe.popItems()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Item Drops In World", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (recipe.outputBlockState().is(Blocks.AIR)) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Destroys Target Block", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
    }

}
