package com.benbenlaw.inworldrecipes.jei;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.event.ClientRecipeCache;
import com.benbenlaw.inworldrecipes.item.InWorldRecipesItems;
import com.benbenlaw.inworldrecipes.recipes.*;
import com.benbenlaw.inworldrecipes.util.ClickType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
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
import net.minecraft.core.component.DataComponents;
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

    private final int width = 170;
    private final int height = 68;
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

        int triggersStartX = 2;
        int conditionsStartX = 60;
        int resultsStartX = 118;

        int row1Y = 13;
        int row2Y = row1Y + spacing;

        /* ---------------- TRIGGERS ---------------- */

        int triggerIndex = 0;

        for (Trigger trigger : recipe.triggers()) {

            ItemStack leftClick = new ItemStack(InWorldRecipesItems.TRIGGER.asItem());
            leftClick.set(DataComponents.CUSTOM_NAME, Component.literal("Left Click"));

            ItemStack rightClick = new ItemStack(InWorldRecipesItems.TRIGGER.asItem());
            rightClick.set(DataComponents.CUSTOM_NAME, Component.literal("Right Click"));

            // Click Type
            if (trigger.clickType() != null) {

                int col = triggerIndex % 3;
                int row = triggerIndex / 3;

                int x = triggersStartX + col * spacing;
                int y = row == 0 ? row1Y : row2Y;

                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .add(trigger.clickType() == ClickType.LEFT_CLICK ? leftClick : rightClick)
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1);

                triggerIndex++;
            }

            // Target Block
            BlockTarget targetBlock = trigger.targetBlock();

            if (targetBlock instanceof BlockTarget.Single single) {

                int col = triggerIndex % 3;
                int row = triggerIndex / 3;

                int x = triggersStartX + col * spacing;
                int y = row == 0 ? row1Y : row2Y;

                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .add(single.blockState().getBlock().asItem().getDefaultInstance())
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1);

                triggerIndex++;
            }

            if (targetBlock instanceof BlockTarget.Tag tag) {

                TagKey<Item> itemTag = TagKey.create(Registries.ITEM, tag.tag().location());
                List<ItemStack> stacks = new ArrayList<>();

                for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(itemTag)) {
                    stacks.add(new ItemStack(holder.value()));
                }

                int col = triggerIndex % 3;
                int row = triggerIndex / 3;

                int x = triggersStartX + col * spacing;
                int y = row == 0 ? row1Y : row2Y;

                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .addItemStacks(stacks)
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1);

                triggerIndex++;
            }

            // Lightning
            if (Boolean.TRUE.equals(trigger.lightningStrike())) {

                ItemStack lightning = new ItemStack(InWorldRecipesItems.TRIGGER.asItem());
                lightning.set(DataComponents.CUSTOM_NAME, Component.literal("Lightning"));

                int col = triggerIndex % 3;
                int row = triggerIndex / 3;

                int x = triggersStartX + col * spacing;
                int y = row == 0 ? row1Y : row2Y;

                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .add(lightning)
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1);

                triggerIndex++;
            }

            // Standing On Block
            BlockTarget standing = trigger.standingOnBlock();

            if (standing instanceof BlockTarget.Single single) {

                int col = triggerIndex % 3;
                int row = triggerIndex / 3;

                int x = triggersStartX + col * spacing;
                int y = row == 0 ? row1Y : row2Y;

                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .add(single.blockState().getBlock().asItem().getDefaultInstance())
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1);

                triggerIndex++;
            }
        }


        /* ---------------- CONDITIONS ---------------- */

        int conditionIndex = 0;
        var condition = recipe.conditions().getFirst();

        if (condition.heldItem() != null) {

            int col = conditionIndex % 3;
            int row = conditionIndex / 3;

            int x = conditionsStartX + col * spacing;
            int y = row == 0 ? row1Y : row2Y;

            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, y)
                    .add(condition.heldItem().ingredient())
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1);

            conditionIndex++;
        }

        if (condition.ignoreBlockState()) {

            ItemStack stack = new ItemStack(InWorldRecipesItems.CONDITION.asItem());
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("Ignore Block State"));

            int col = conditionIndex % 3;
            int row = conditionIndex / 3;

            int x = conditionsStartX + col * spacing;
            int y = row == 0 ? row1Y : row2Y;

            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, y)
                    .add(stack)
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1);

            conditionIndex++;
        }

        if (condition.droppedItems() != null) {
            for (SizedIngredient dropped : condition.droppedItems()) {

                int col = conditionIndex % 3;
                int row = conditionIndex / 3;

                int x = conditionsStartX + col * spacing;
                int y = row == 0 ? row1Y : row2Y;

                builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, y)
                        .add(new ItemStack(dropped.ingredient().getValues().get(0).value(), dropped.count()))
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1);

                conditionIndex++;
            }
        }

        if (condition.weatherType() != null) {

            ItemStack weather = new ItemStack(InWorldRecipesItems.CONDITION.asItem());
            weather.set(DataComponents.CUSTOM_NAME, Component.literal("Weather"));

            int col = conditionIndex % 3;
            int row = conditionIndex / 3;

            int x = conditionsStartX + col * spacing;
            int y = row == 0 ? row1Y : row2Y;

            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, y)
                    .add(weather)
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1);

            conditionIndex++;
        }


        /* ---------------- RESULTS ---------------- */

        int resultIndex = 0;
        var result = recipe.results().getFirst();

        if (result.chanceResults() != null) {
            for (ChanceResult chance : result.chanceResults()) {

                int col = resultIndex % 3;
                int row = resultIndex / 3;

                int x = resultsStartX + col * spacing;
                int y = row == 0 ? row1Y : row2Y;

                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                        .add(chance.template().create())
                        .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1);

                resultIndex++;
            }
        }

        if (result.damageHeldItem()) {

            ItemStack stack = new ItemStack(InWorldRecipesItems.RESULT.asItem());
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("Damage Held Item"));

            int col = resultIndex % 3;
            int row = resultIndex / 3;

            int x = resultsStartX + col * spacing;
            int y = row == 0 ? row1Y : row2Y;

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .add(stack)
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1);

            resultIndex++;
        }

        if (result.consumeHeldItem()) {

            ItemStack stack = new ItemStack(InWorldRecipesItems.RESULT.asItem());
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("Consume Held Item"));

            int col = resultIndex % 3;
            int row = resultIndex / 3;

            int x = resultsStartX + col * spacing;
            int y = row == 0 ? row1Y : row2Y;

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .add(stack)
                    .setBackground(JEIInWorldRecipesPlugin.slotDrawable, -1, -1);

            resultIndex++;
        }
    }

    @Override
    public void draw(WorldRecipe recipe, IRecipeSlotsView slots, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 0, width, height, width, height);

        String triggerSize = Component.translatable("jei.inworldrecipes.triggers").getString();
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei.inworldrecipes.triggers").withStyle(ChatFormatting.BLACK),
                26 - Minecraft.getInstance().font.width(triggerSize) / 2, 0, 0xFFFFFFFF, false);

        String conditionSize = Component.translatable("jei.inworldrecipes.conditions").getString();
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei.inworldrecipes.conditions").withStyle(ChatFormatting.BLACK),
                86 - Minecraft.getInstance().font.width(conditionSize) / 2, 0, 0xFFFFFFFF, false);

        String resultSize = Component.translatable("jei.inworldrecipes.results").getString();
        guiGraphics.drawString(Minecraft.getInstance().font,
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

