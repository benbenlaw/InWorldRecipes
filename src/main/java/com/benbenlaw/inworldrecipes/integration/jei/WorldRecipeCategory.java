package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.event.ClientRecipeCache;
import com.benbenlaw.inworldrecipes.recipe.*;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.constants.VanillaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.benbenlaw.inworldrecipes.integration.jei.JEIInWorldRecipesPlugin.slotDrawable;

public class WorldRecipeCategory implements IRecipeCategory<WorldRecipe> {

    public final static Identifier TEXTURE = InWorldRecipes.identifier("textures/gui/world_recipe_jei.png");
    static final IRecipeType<WorldRecipe> RECIPE_TYPE = IRecipeType.create(InWorldRecipes.MOD_ID, "world_recipe", WorldRecipe.class);

    private final int width = 118;
    private final int height = 22;
    private final IDrawable icon;

    @Override
    public @Nullable Identifier getIdentifier(WorldRecipe recipe) {
        return ClientRecipeCache.getCachedWorldRecipes().stream()
                .filter(r -> r.equals(recipe))
                .findFirst()
                .map(r -> {
                    for (Map.Entry<Identifier, WorldRecipe> entry : ClientRecipeCache.cachedWorldRecipes.entrySet()) {
                        if (entry.getValue().equals(r)) {
                            return entry.getKey();
                        }
                    }
                    return null;
                }).orElse(null);
    }

    public WorldRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.DIAMOND_BLOCK));
    }

    @Override
    public @NotNull IRecipeType<WorldRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.inworldrecipes.world_recipe");
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WorldRecipe recipe, IFocusGroup focuses) {

        builder.addSlot(RecipeIngredientRole.INPUT, 3, 3).addIngredients(VanillaTypes.ITEM_STACK, getIcons(recipe.triggers()));
        builder.addSlot(RecipeIngredientRole.INPUT, 41, 3).addIngredients(VanillaTypes.ITEM_STACK, getIcons(recipe.conditions()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 3).addIngredients(VanillaTypes.ITEM_STACK, getIcons(recipe.results()));

        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 99, 3)
                .add(VanillaTypes.ITEM_STACK, new ItemStack(Items.COMMAND_BLOCK))
                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.clear();

                    tooltip.add(Component.translatable("jei.inworldrecipes.triggers").withStyle(ChatFormatting.GOLD));
                    for (IRecipeTrigger t : recipe.triggers()) {
                        tooltip.add(Component.literal("- ").append(t.getJeiTooltip()));
                    }

                    tooltip.add(Component.translatable("jei.inworldrecipes.conditions").withStyle(ChatFormatting.BLUE));
                    for (IRecipeCondition c : recipe.conditions()) {
                        tooltip.add(Component.literal("- ").append(c.getJeiTooltip()));
                    }

                    tooltip.add(Component.translatable("jei.inworldrecipes.results").withStyle(ChatFormatting.GREEN));
                    for (IRecipeResult r : recipe.results()) {
                        tooltip.add(Component.literal("- ").append(r.getJeiTooltip()));
                    }
                });
    }

    @Override
    public void draw(WorldRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 0, width, height, width, height);

        Font font = net.minecraft.client.Minecraft.getInstance().font;
        /*

        String triggerLabel = Component.translatable("jei.inworldrecipes.triggers").getString();
        guiGraphics.text(font,
                Component.translatable("jei.inworldrecipes.triggers").withStyle(ChatFormatting.BLACK),
                19 - font.width(triggerLabel) / 2, 0, 0xFFFFFFFF, false);

        String conditionLabel = Component.translatable("jei.inworldrecipes.conditions").getString();
        guiGraphics.text(font,
                Component.translatable("jei.inworldrecipes.conditions").withStyle(ChatFormatting.BLACK),
                77 - font.width(conditionLabel) / 2, 0, 0xFFFFFFFF, false);

        String resultLabel = Component.translatable("jei.inworldrecipes.results").getString();
        guiGraphics.text(font,
                Component.translatable("jei.inworldrecipes.results").withStyle(ChatFormatting.BLACK),
                135 - font.width(resultLabel) / 2, 0, 0xFFFFFFFF, false);

         */
    }

    private List<ItemStack> getIcons(List<?> modules) {
        List<ItemStack> allIcons = new ArrayList<>();

        for (Object module : modules) {
            if (module instanceof IRecipeTrigger t) {
                allIcons.addAll(t.getJeiIcons());
            } else if (module instanceof IRecipeCondition c) {
                allIcons.addAll(c.getJeiIcons());
            } else if (module instanceof IRecipeResult r) {
                allIcons.addAll(r.getJeiIcons());
            }
        }

        if (allIcons.isEmpty()) {
            allIcons.add(new ItemStack(net.minecraft.world.item.Items.CHEST));
        }
        return allIcons;
    }
}