package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.event.ClientRecipeCache;
import com.benbenlaw.inworldrecipes.item.InWorldRecipesItems;
import com.benbenlaw.inworldrecipes.recipe.*;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.benbenlaw.inworldrecipes.util.MouseUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static com.benbenlaw.inworldrecipes.integration.jei.JEIInWorldRecipesPlugin.slotDrawable;

public class WorldRecipeCategory implements IRecipeCategory<WorldRecipe> {

    public final static Identifier TEXTURE = InWorldRecipes.identifier("textures/gui/world_recipe_jei.png");
    static final IRecipeType<WorldRecipe> RECIPE_TYPE = IRecipeType.create(InWorldRecipes.MOD_ID, "world_recipe", WorldRecipe.class);

    private final int width = 170;
    private final int height = 34;
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

        //TRIGGERS
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 13)
                .addIngredients(VanillaTypes.ITEM_STACK, getIcons(recipe.triggers())).setBackground(slotDrawable, -1, -1);

        builder.addSlot(RecipeIngredientRole.INPUT, 30, 13)
                .add(VanillaTypes.ITEM_STACK, InWorldRecipesItems.TRIGGER.get().getDefaultInstance())
                .setBackground(slotDrawable, -1, -1)
                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.clear(); // Remove the "Trigger Item" name
                    tooltip.add(Component.translatable("jei.inworldrecipes.triggers").withStyle(ChatFormatting.GOLD));
                    recipe.triggers().forEach(t -> tooltip.add(Component.literal("- ").append(t.getJeiTooltip())));
                });

        //CONDITIONS
        builder.addSlot(RecipeIngredientRole.INPUT, 68, 13)
                .addIngredients(VanillaTypes.ITEM_STACK, getIcons(recipe.conditions())).setBackground(slotDrawable, -1, -1);

        builder.addSlot(RecipeIngredientRole.INPUT, 88, 13)
                .add(VanillaTypes.ITEM_STACK, InWorldRecipesItems.CONDITION.get().getDefaultInstance())
                .setBackground(slotDrawable, -1, -1)
                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.clear();
                    tooltip.add(Component.translatable("jei.inworldrecipes.conditions").withStyle(ChatFormatting.BLUE));
                    recipe.conditions().forEach(c -> tooltip.add(Component.literal("- ").append(c.getJeiTooltip())));
                });

        //RESULTS
        builder.addSlot(RecipeIngredientRole.OUTPUT, 126, 13)
                .addIngredients(VanillaTypes.ITEM_STACK, getIcons(recipe.results())).setBackground(slotDrawable, -1, -1);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 146, 13)
                .add(VanillaTypes.ITEM_STACK, InWorldRecipesItems.RESULT.get().getDefaultInstance())
                .setBackground(slotDrawable, -1, -1)
                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.clear();
                    tooltip.add(Component.translatable("jei.inworldrecipes.results").withStyle(ChatFormatting.GREEN));
                    recipe.results().forEach(r -> tooltip.add(Component.literal("- ").append(r.getJeiTooltip())));
                });
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
            allIcons.add(new ItemStack(Items.CHEST));
        }
        return allIcons;
    }


    @Override
    public void draw(WorldRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 0, width, height, width, height);

        String triggerSize = Component.translatable("jei.inworldrecipes.triggers").getString();
        guiGraphics.text(Minecraft.getInstance().font,
                Component.translatable("jei.inworldrecipes.triggers").withStyle(ChatFormatting.BLACK),
                26 - Minecraft.getInstance().font.width(triggerSize) / 2, 0, 0xFFFFFFFF, false);

        String conditionSize = Component.translatable("jei.inworldrecipes.conditions").getString();
        guiGraphics.text(Minecraft.getInstance().font,
                Component.translatable("jei.inworldrecipes.conditions").withStyle(ChatFormatting.BLACK),
                86 - Minecraft.getInstance().font.width(conditionSize) / 2, 0, 0xFFFFFFFF, false);

        String resultSize = Component.translatable("jei.inworldrecipes.results").getString();
        guiGraphics.text(Minecraft.getInstance().font,
                Component.translatable("jei.inworldrecipes.results").withStyle(ChatFormatting.BLACK),
                144 - Minecraft.getInstance().font.width(resultSize) / 2, 0, 0xFFFFFFFF, false);


    }

    public void createRecipeExtras(IRecipeExtrasBuilder builder, WorldRecipe recipe, IFocusGroup focuses) {

        IRecipeSlotDrawablesView recipeSlots = builder.getRecipeSlots();
        List<IRecipeSlotDrawable> triggerSlots = recipeSlots.getSlots(RecipeIngredientRole.INPUT);
        List<IRecipeSlotDrawable> conditionSlots = recipeSlots.getSlots(RecipeIngredientRole.RENDER_ONLY);
        List<IRecipeSlotDrawable> resultSlots = recipeSlots.getSlots(RecipeIngredientRole.OUTPUT);

        if (triggerSlots.size() > 6) {
            IScrollGridWidget triggersGrid = builder.addScrollGridWidget(triggerSlots, 2, 3);
            triggersGrid.setPosition(2, 13);
        }

        if (conditionSlots.size() > 6) {
            IScrollGridWidget conditionsGrid = builder.addScrollGridWidget(conditionSlots, 2, 3);
            conditionsGrid.setPosition(60, 13);
        }

        if (resultSlots.size() > 6) {
            IScrollGridWidget resultsGrid = builder.addScrollGridWidget(resultSlots, 2, 3);
            resultsGrid.setPosition(118, 13);
        }
    }
}