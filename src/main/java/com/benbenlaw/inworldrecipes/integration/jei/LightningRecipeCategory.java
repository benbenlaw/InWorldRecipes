package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.LightningCraftingRecipe;
import com.benbenlaw.inworldrecipes.recipes.BlockTarget;
import com.benbenlaw.inworldrecipes.recipes.RightClickOnBlockTransformsBlockRecipe;
import com.benbenlaw.inworldrecipes.util.ClickType;
import com.benbenlaw.inworldrecipes.util.JEIBlockRenderHelper;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LightningRecipeCategory implements IRecipeCategory<LightningCraftingRecipe> {
    public final static ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "lightning_crafting");
    public final static ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "textures/gui/jei_drop_item_in_fluid.png");
    static final RecipeType<LightningCraftingRecipe> RECIPE_TYPE = RecipeType.create(InWorldRecipes.MOD_ID, "lightning_crafting",
            LightningCraftingRecipe.class);

    private IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper helper;


    @Override
    public @Nullable ResourceLocation getRegistryName(LightningCraftingRecipe recipe) {
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RightClickOnBlockTransformsBlockRecipe.Type.INSTANCE).stream()
                .filter(recipeHolder -> recipeHolder.value().equals(recipe))
                .map(RecipeHolder::id)
                .findFirst()
                .orElse(null);
    }

    public LightningRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.background = helper.createDrawable(TEXTURE, 0, 0, 104, 18);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.DIAMOND_BLOCK));
    }


    @Override
    public @NotNull RecipeType<LightningCraftingRecipe> getRecipeType() {
        return JEIInWorldRecipesPlugin.LIGHTNING_CRAFTING_RECIPE;
    }


    @Override
    public @NotNull Component getTitle() {
        return Component.literal("Lightning Crafting");
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
    public void setRecipe(IRecipeLayoutBuilder builder, LightningCraftingRecipe recipe, @NotNull IFocusGroup focusGroup) {


        builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 2)
                .addItemStack(new ItemStack(Blocks.CHEST))
                .setCustomRenderer(VanillaTypes.ITEM_STACK, new IIngredientRenderer<>() {
                    @Override
                    public void render(GuiGraphics guiGraphics, ItemStack stack) {
                        Block renderBlock = Blocks.CHEST;

                        JEIBlockRenderHelper.renderBlock(guiGraphics, renderBlock.defaultBlockState(), 1, 12, 0.60f);
                    }

                    @Override
                    public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
                        List<Component> tooltip = new ArrayList<>();


                        // Show chance results if any
                        List<ChanceResult> results = recipe.chanceResults();
                        if (!results.isEmpty()) {
                            tooltip.add(Component.literal("Chance Results:")
                                    .withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD));
                            for (ChanceResult result : results) {
                                ItemStack stack = result.stack();
                                int count = stack.getCount();
                                Component baseName = stack.getHoverName();

                                Component itemDisplayName = count > 1
                                        ? Component.literal(count + "x ").append(baseName)
                                        : baseName;

                                String chancePercent = String.format("%.1f%%", result.chance() * 100);

                                tooltip.add(Component.literal(" - ")
                                        .append(itemDisplayName)
                                        .append(": ")
                                        .append(Component.literal(chancePercent))
                                        .withStyle(ChatFormatting.GOLD));
                            }
                        }

                        return tooltip;
                    }


                });

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

        builder.addSlot(RecipeIngredientRole.INPUT, 4, 2).addIngredients(recipe.droppedItem().ingredient());
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 40, 2).addItemStack(Items.LIGHTNING_ROD.asItem().getDefaultInstance()).addRichTooltipCallback(
                (stack, tooltip) -> {
                    tooltip.add(Component.literal("Requires a lightning strike to trigger").withStyle(ChatFormatting.GRAY));
                    tooltip.add(Component.literal("Items must be around a 3x3x3 area of the strike").withStyle(ChatFormatting.GRAY));
                }
        );


    }
}
