package com.benbenlaw.inworldrecipes.jei;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.event.ClientRecipeCache;
import com.benbenlaw.inworldrecipes.recipes.*;
import com.benbenlaw.inworldrecipes.util.ClickType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class WorldRecipeCategory implements IRecipeCategory<WorldRecipe> {

    public final static Identifier TEXTURE = InWorldRecipes.identifier("textures/gui/world_recipe_jei.png");
    static final IRecipeType<WorldRecipe> RECIPE_TYPE = IRecipeType.create(InWorldRecipes.MOD_ID, "world_recipe", WorldRecipe.class);

    private final int width = 146;
    private final int height = 90;
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

        int spacing = 18;
        int startX = 2;
        int triggersY = 13;
        int conditionsY = triggersY * 2 + spacing;
        int resultsY = triggersY * 3 + spacing * 2;

        // --- Place Triggers ---
        int triggerIndex = 0;
        for (Trigger trigger : recipe.triggers()) {
            int x = startX + spacing * triggerIndex;
            int y = triggersY;

            // ClickType slot (optional)
            if (trigger.clickType() != null) {
                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .add(Items.BARRIER)
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                        .addRichTooltipCallback((recipeSlotView, tooltip) ->
                                tooltip.add(Component.translatable("jei.inworldrecipes.click_type", Component.translatable("jei.inworldrecipes." + trigger.clickType().name().toLowerCase())).withStyle(ChatFormatting.GOLD)));
                triggerIndex++;
            }

            // TargetBlock slot
            BlockTarget targetBlock = trigger.targetBlock();
            if (targetBlock != null && targetBlock instanceof BlockTarget.Single single) {
                builder.addSlot(RecipeIngredientRole.INPUT, x + spacing * triggerIndex, y)
                        .add(single.blockState().getBlock().asItem().getDefaultInstance())
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                        .addRichTooltipCallback((recipeSlotView, tooltip) ->
                                tooltip.add(Component.translatable("jei.inworldrecipes.target_block").withStyle(ChatFormatting.GOLD)));
                triggerIndex++;

            }

            if (targetBlock != null && targetBlock instanceof BlockTarget.Tag tag) {
                TagKey<Item> itemTag = TagKey.create(Registries.ITEM, tag.tag().location());
                List<ItemStack> tagStacks = new ArrayList<>();

                for (Holder<Item> itemHolder : BuiltInRegistries.ITEM.getTagOrEmpty(itemTag)) {
                    ItemStack stack = new ItemStack(itemHolder.value());
                    tagStacks.add(stack);
                }

                builder.addSlot(RecipeIngredientRole.INPUT, x + spacing * triggerIndex, y)
                        .addItemStacks(tagStacks)
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                        .addRichTooltipCallback((recipeSlotView, tooltip) ->
                                tooltip.add(Component.translatable("jei.inworldrecipes.target_block_tag", itemTag.location().toString()).withStyle(ChatFormatting.GOLD)));
                triggerIndex++;

            }

            // LightningStrike slot
            if (trigger.lightningStrike() != null && trigger.lightningStrike()) {
                builder.addSlot(RecipeIngredientRole.INPUT, x + spacing * triggerIndex, y)
                        .add(Items.BARRIER)
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                        .addRichTooltipCallback((recipeSlotView, tooltip) ->
                                tooltip.add(Component.translatable("jei.inworldrecipes.lightning").withStyle(ChatFormatting.GOLD)));
                triggerIndex++;
            }

            // StandingOnBlock slot
            BlockTarget standingOnBlock = trigger.standingOnBlock();
            if (standingOnBlock != null && standingOnBlock instanceof BlockTarget.Single single) {
                builder.addSlot(RecipeIngredientRole.INPUT, x + spacing * triggerIndex, y)
                        .add(single.blockState().getBlock().asItem().getDefaultInstance())
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                        .addRichTooltipCallback((recipeSlotView, tooltip) ->
                                tooltip.add(Component.translatable("jei.inworldrecipes.standing_on").withStyle(ChatFormatting.GOLD)));
                triggerIndex++;
            }
        }

        // --- Place Conditions ---
        int conditionIndex = 0;
        int x = startX;
        int y = conditionsY;

        var condition = recipe.conditions().getFirst();

        // Held Item
        if (condition.heldItem() != null) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .add(condition.heldItem().ingredient())
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                    .addRichTooltipCallback((recipeSlotView, tooltip) ->
                            tooltip.add(Component.translatable("jei.inworldrecipes.held_item").withStyle(ChatFormatting.GOLD)));
            conditionIndex++;
        }

        // Ignore Block State
        if (condition.ignoreBlockState()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x + spacing * conditionIndex, y)
                    .add(Items.BARRIER)
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                    .addRichTooltipCallback((recipeSlotView, tooltip) ->
                            tooltip.add(Component.translatable("jei.inworldrecipes.ignore_block_state").withStyle(ChatFormatting.GOLD)));
            conditionIndex++;
        }

        // Dropped Items
        if (condition.droppedItems() != null && !condition.droppedItems().isEmpty()) {
            for (SizedIngredient droppedItem : condition.droppedItems()) {
                builder.addSlot(RecipeIngredientRole.INPUT, x + spacing * conditionIndex, y)
                        .add(new ItemStack(droppedItem.ingredient().getValues().get(0).value(), droppedItem.count()))
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                        .addRichTooltipCallback((recipeSlotView, tooltip) ->
                                tooltip.add(Component.translatable("jei.inworldrecipes.dropped_item").withStyle(ChatFormatting.GOLD)));
                conditionIndex++;
            }
        }

        // Inventory Items
        if (condition.inventoryItems() != null && !condition.inventoryItems().isEmpty()) {
            for (SizedIngredient inventoryItem : condition.inventoryItems()) {
                builder.addSlot(RecipeIngredientRole.INPUT, x + spacing * conditionIndex, y)
                        .add(inventoryItem.ingredient())
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                        .addRichTooltipCallback((recipeSlotView, tooltip) ->
                                tooltip.add(Component.translatable("jei.inworldrecipes.inventory_item").withStyle(ChatFormatting.GOLD)));
                conditionIndex++;
            }
        }

        // --- Place Results ---
        int resultIndex = 0;
        if (recipe.results().getFirst().chanceResults() != null && !recipe.results().getFirst().chanceResults().isEmpty()) {
            for (ChanceResult chanceResult : Objects.requireNonNull(recipe.results().getFirst().chanceResults())) {
                int displayChance = (int) (chanceResult.chance() * 100);
                builder.addSlot(RecipeIngredientRole.OUTPUT, x + spacing * resultIndex, resultsY)
                        .add(chanceResult.template().create())
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                        .addRichTooltipCallback((recipeSlotView, tooltip) ->
                                tooltip.add(Component.translatable("jei.inworldrecipes.chance", displayChance).withStyle(ChatFormatting.GOLD)));
                resultIndex++;
            }
        }

        if (recipe.results().getFirst().damageHeldItem()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x + spacing * resultIndex, resultsY)
                    .add(Items.BARRIER)
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                    .addRichTooltipCallback((recipeSlotView, tooltip) ->
                            tooltip.add(Component.translatable("jei.inworldrecipes.damage_held_item").withStyle(ChatFormatting.GOLD)));
            resultIndex++;
        }

        if (recipe.results().getFirst().consumeHeldItem()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x + spacing * resultIndex, resultsY)
                    .add(Items.BARRIER)
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                    .addRichTooltipCallback((recipeSlotView, tooltip) ->
                            tooltip.add(Component.translatable("jei.inworldrecipes.consume_held_item").withStyle(ChatFormatting.GOLD)));
            resultIndex++;
        }

        if (recipe.results().getFirst().popItems()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x + spacing * resultIndex, resultsY)
                    .add(Items.BARRIER)
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                    .addRichTooltipCallback((recipeSlotView, tooltip) ->
                            tooltip.add(Component.translatable("jei.inworldrecipes.pop_items").withStyle(ChatFormatting.GOLD)));
            resultIndex++;
        }

        if (recipe.results().getFirst().outputBlockState() != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x + spacing * resultIndex, resultsY)
                    .add(new ItemStack(recipe.results().getFirst().outputBlockState().getBlock()))
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                    .addRichTooltipCallback((recipeSlotView, tooltip) ->
                            tooltip.add(Component.translatable("jei.inworldrecipes.output_block_state").withStyle(ChatFormatting.GOLD)));
            resultIndex++;
        }

        if (recipe.results().getFirst().consumeInventoryItems()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x + spacing * resultIndex, resultsY)
                    .add(Items.BARRIER)
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                    .addRichTooltipCallback((recipeSlotView, tooltip) ->
                            tooltip.add(Component.translatable("jei.inworldrecipes.consume_inventory_items").withStyle(ChatFormatting.GOLD)));
            resultIndex++;
        }

        if (recipe.results().getFirst().consumeDroppedItems()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x + spacing * resultIndex, resultsY)
                    .add(Items.BARRIER)
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1)
                    .addRichTooltipCallback((recipeSlotView, tooltip) ->
                            tooltip.add(Component.translatable("jei.inworldrecipes.consume_dropped_items").withStyle(ChatFormatting.GOLD)));
            resultIndex++;
        }


    }

    @Override
    public void draw(WorldRecipe recipe, IRecipeSlotsView slots, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 0, width, height, width, height);

        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei.inworldrecipes.triggers").withStyle(ChatFormatting.BLACK),
                0, 0, 0xFFFFFFFF, false);

        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei.inworldrecipes.conditions").withStyle(ChatFormatting.BLACK),
                0, 32, 0xFFFFFFFF, false);

        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei.inworldrecipes.results").withStyle(ChatFormatting.BLACK),
                0, 64, 0xFFFFFFFF, false);


    }
}

/*

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, WorldRecipe recipe, IFocusGroup focuses) {
        int spacing = 18;
        int boxWidth = 120; // visible width in pixels
        int boxHeight = 30;

        // --- Triggers ---
        IScrollBoxWidget triggersScroll = builder.addScrollBoxWidget(boxWidth, boxHeight, 2, 13);
        triggersScroll.setContents(new IDrawable() {
            @Override
            public int getWidth() {
                int count = 0;
                for (Trigger t : recipe.triggers()) {
                    if (t.clickType() != null) count++;
                    if (t.targetBlock() instanceof BlockTarget.Single) count++;
                    if (Boolean.TRUE.equals(t.lightningStrike())) count++;
                    if (t.standingOnBlock() instanceof BlockTarget.Single) count++;
                }
                return count * spacing;
            }

            @Override
            public int getHeight() { return spacing; }

            @Override
            public void draw(GuiGraphics gui, int xOffset, int yOffset) {
                int index = 0;
                for (Trigger t : recipe.triggers()) {
                    int slotX = xOffset + index * spacing;

                    if (t.clickType() != null) {
                        gui.renderItem(new ItemStack(Items.BARRIER), slotX, yOffset);
                        if (gui. (slotX, yOffset, 16, 16)) {
                            gui.renderTooltip(Minecraft.getInstance().font,
                                    Component.translatable("jei.inworldrecipes.click_type",
                                            Component.translatable("jei.inworldrecipes." + t.clickType().name().toLowerCase())
                                    ), null, xOffset + 8, yOffset + 8);
                        }
                        index++;
                    }

                    if (t.targetBlock() instanceof BlockTarget.Single single) {
                        gui.renderItem(single.blockState().getBlock().asItem().getDefaultInstance(), slotX, yOffset);
                        if (gui.isHovered(slotX, yOffset, 16, 16)) {
                            gui.renderTooltip(Minecraft.getInstance().font,
                                    Component.translatable("jei.inworldrecipes.target_block"), null, slotX + 8, yOffset + 8);
                        }
                        index++;
                    }

                    if (Boolean.TRUE.equals(t.lightningStrike())) {
                        // Render lightning icon here (custom texture)
                        index++;
                    }

                    if (t.standingOnBlock() instanceof BlockTarget.Single standing) {
                        gui.renderItem(standing.blockState().getBlock().asItem().getDefaultInstance(), slotX, yOffset);
                        if (gui.isHovered(slotX, yOffset, 16, 16)) {
                            gui.renderTooltip(Minecraft.getInstance().font,
                                    Component.translatable("jei.inworldrecipes.standing_on"), null, slotX + 8, yOffset + 8);
                        }
                        index++;
                    }
                }
            }
        });

        // --- Conditions ---
        IScrollBoxWidget conditionsScroll = builder.addScrollBoxWidget(boxWidth, boxHeight, 2, 32);
        Condition condition = recipe.conditions().getFirst();
        conditionsScroll.setContents(new IDrawable() {
            @Override
            public int getWidth() {
                int count = 0;
                if (condition.heldItem() != null) count++;
                if (condition.ignoreBlockState()) count++;
                if (condition.droppedItems() != null) count += condition.droppedItems().size();
                if (condition.inventoryItems() != null) count += condition.inventoryItems().size();
                return count * spacing;
            }

            @Override
            public int getHeight() { return spacing; }

            @Override
            public void draw(GuiGraphics gui, int xOffset, int yOffset) {
                int index = 0;

                if (condition.heldItem() != null) {
                    ItemStack stack = condition.heldItem().ingredient().getValues().get(0).value().getDefaultInstance();
                    gui.renderItem(stack, xOffset + index * spacing, yOffset);
                    if (gui.isHovered(xOffset + index * spacing, yOffset, 16, 16)) {
                        gui.renderTooltip(Minecraft.getInstance().font,
                                Component.translatable("jei.inworldrecipes.held_item"), null,
                                xOffset + index * spacing + 8, yOffset + 8);
                    }
                    index++;
                }

                if (condition.ignoreBlockState()) {
                    gui.renderItem(new ItemStack(Items.BARRIER), xOffset + index * spacing, yOffset);
                    if (gui.isHovered(xOffset + index * spacing, yOffset, 16, 16)) {
                        gui.renderTooltip(Minecraft.getInstance().font,
                                Component.translatable("jei.inworldrecipes.ignore_block_state"), null,
                                xOffset + index * spacing + 8, yOffset + 8);
                    }
                    index++;
                }

                if (condition.droppedItems() != null) {
                    for (SizedIngredient dropped : condition.droppedItems()) {
                        ItemStack stack = dropped.ingredient().getValues().get(0).value().getDefaultInstance();
                        gui.renderItem(stack, xOffset + index * spacing, yOffset);
                        if (gui.isHovered(xOffset + index * spacing, yOffset, 16, 16)) {
                            gui.renderTooltip(Minecraft.getInstance().font,
                                    Component.translatable("jei.inworldrecipes.dropped_item"), null,
                                    xOffset + index * spacing + 8, yOffset + 8);
                        }
                        index++;
                    }
                }

                if (condition.inventoryItems() != null) {
                    for (SizedIngredient inv : condition.inventoryItems()) {
                        ItemStack stack = inv.ingredient().getValues().get(0).value().getDefaultInstance();
                        gui.renderItem(stack, xOffset + index * spacing, yOffset);
                        if (gui.isHovered(xOffset + index * spacing, yOffset, 16, 16)) {
                            gui.renderTooltip(Minecraft.getInstance().font,
                                    Component.translatable("jei.inworldrecipes.inventory_item"), null,
                                    xOffset + index * spacing + 8, yOffset + 8);
                        }
                        index++;
                    }
                }
            }
        });

        // --- Results ---
        IScrollBoxWidget resultsScroll = builder.addScrollBoxWidget(boxWidth, boxHeight, 2, 64);
        Result result = recipe.results().getFirst();
        resultsScroll.setContents(new IDrawable() {
            @Override
            public int getWidth() {
                return result.chanceResults() != null ? result.chanceResults().size() * spacing : 0;
            }

            @Override
            public int getHeight() { return spacing; }

            @Override
            public void draw(GuiGraphics gui, int xOffset, int yOffset) {
                int index = 0;
                if (result.chanceResults() != null) {
                    for (ChanceResult chance : result.chanceResults()) {
                        ItemStack stack = chance.template().create();
                        gui.renderItem(stack, xOffset + index * spacing, yOffset);
                        if (gui.isHovered(xOffset + index * spacing, yOffset, 16, 16)) {
                            gui.renderTooltip(Minecraft.getInstance().font,
                                    Component.translatable("jei.inworldrecipes.chance"), null,
                                    xOffset + index * spacing + 8, yOffset + 8);
                        }
                        index++;
                    }
                }
            }
        });
    }
}

 */

