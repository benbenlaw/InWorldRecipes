package com.benbenlaw.inworldrecipes.event;

import com.benbenlaw.core.item.CoreItemUtils;
import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.WorldRecipe;
import com.benbenlaw.inworldrecipes.recipes.BlockTarget;
import com.benbenlaw.inworldrecipes.recipes.InWorldRecipesRecipes;
import com.benbenlaw.inworldrecipes.util.ClickType;
import com.benbenlaw.inworldrecipes.util.WeatherType;
import com.benbenlaw.inworldrecipes.util.WeatherUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = InWorldRecipes.MOD_ID)
public class WorldRecipeEvent {


    /* -------------------- TRIGGERS EVENTS -------------------- */
    @SubscribeEvent
    public static void rightClickOnBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        handle(event.getLevel(), event.getEntity(), event.getPos(), event.getHand(), ClickType.RIGHT_CLICK);
    }

    @SubscribeEvent
    public static void leftClickOnBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;
        handle(event.getLevel(), event.getEntity(), event.getPos(), event.getHand(), ClickType.LEFT_CLICK);
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity().level().getGameTime() % 20 != 0) return;
        //handle(event.getEntity().level(), event.getEntity(), event.getEntity().getOnPos(), InteractionHand.MAIN_HAND, null);
    }

    /* -------------------- CORE HANDLER -------------------- */
    public static void handle(Level level, Player player, BlockPos pos, InteractionHand hand, ClickType clickType) {
        if (level.isClientSide()) return;

        getAllBlockInteractionRecipes(level).forEach(recipe ->
                execute(level, player, pos, hand, clickType, recipe)
        );
    }


    /* -------------------- EXECUTION -------------------- */
    private static void execute(Level level, Player player, BlockPos pos, InteractionHand hand, ClickType clickType,
                                RecipeHolder<WorldRecipe> match) {
        var recipe = match.value();
        var trigger = recipe.triggers().getFirst();
        var condition = recipe.conditions().getFirst();
        var result = recipe.results().getFirst();

        if (recipe.options() != null && !recipe.options().isEmpty() && recipe.options().getFirst().onlyVisualRecipe()) return;


        /* ---------- CLICK TYPE ---------- */
        if (trigger.clickType() != null && trigger.clickType() != clickType) return;

        /* ---------- TARGET BLOCK ---------- */
        BlockTarget targetBlock = trigger.targetBlock();
        BlockState clickedState = level.getBlockState(pos);
        if (targetBlock != null && !targetBlock.matches(clickedState, condition.ignoreBlockState())) return;

        /* ---------- STANDING ON BLOCK ---------- */
        BlockTarget standingOn = trigger.standingOnBlock();
        BlockState standingState = level.getBlockState(player.blockPosition().below());
        if (standingOn != null && !standingOn.matches(standingState, false)) return;

        /* ---------- HELD ITEM ---------- */
        if (condition.heldItem() != null) {
            if (hand == null || !condition.heldItem().test(player.getItemInHand(hand))) return;
        }

        /* ---------- WEATHER ---------- */
        if (condition.weatherType() != null) {
            WeatherType current = WeatherUtils.getWeather(level, pos);

            if (condition.weatherType() == WeatherType.RAIN && current == WeatherType.THUNDER) {
            } else if (condition.weatherType() != current) {
                return;
            }
        }

        /* ---------- INVENTORY ITEMS ---------- */
        if (condition.inventoryItems() != null) {
            for (SizedIngredient requiredHeld : condition.inventoryItems()) {
                boolean found = false;
                for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                    if (requiredHeld.test(stack)) {
                        found = true;
                        break;
                    }
                }
                if (!found) return;
            }
        }

        /* ---------- FALLING BLOCK / ANVIL ---------- */
        // Todo: make this actually work because this is awful
        if (trigger.anvilLanded() != null) {
            boolean foundAnvil = false;

            // Scan 3x3 area centered on player
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos checkPos = player.blockPosition().offset(dx, -1, dz); // -1 to check block below player
                    if (level.getBlockState(checkPos).getBlock() == Blocks.ANVIL) {
                        foundAnvil = true;
                        pos = checkPos; // update pos so recipe applies to that anvil
                        break;
                    }
                }
                if (foundAnvil) break;
            }

            if (trigger.anvilLanded() && !foundAnvil) return;
            if (!trigger.anvilLanded() && foundAnvil) return;
        }

        /* ---------- DROPPED ITEMS IN WORLD ---------- */
        if (condition.droppedItems() != null) {
            for (SizedIngredient requiredItem : condition.droppedItems()) {

                int requiredCount = requiredItem.count();
                int foundCount = 0;

                var itemsInWorld = level.getEntitiesOfClass(
                        ItemEntity.class,
                        player.getBoundingBox().inflate(1.5)
                );

                for (ItemEntity entity : itemsInWorld) {
                    ItemStack stack = entity.getItem();

                    if (requiredItem.ingredient().test(stack)) {
                        foundCount += stack.getCount();
                        if (foundCount >= requiredCount) {
                            break;
                        }
                    }
                }

                if (foundCount < requiredCount) {
                    return;
                }
            }
        }

        /* ---------- APPLY RESULTS ---------- */

        // Replace block if needed
        if (result.outputBlockState() != null) {
            level.setBlockAndUpdate(pos, result.outputBlockState());
        }

        // Damage held item
        if (hand != null && result.damageHeldItem() && condition.heldItem() != null) {
            player.getItemInHand(hand).hurtAndBreak(
                    1,
                    player,
                    EquipmentSlot.valueOf(hand.name().replace("_", "").toUpperCase())
            );
        }

        // Consume held item
        if (hand != null && result.consumeHeldItem() && condition.heldItem() != null) {
            player.getItemInHand(hand).shrink(condition.heldItem().count());
        }

        // Consume inventory items
        if (result.consumeInventoryItems() && condition.inventoryItems() != null) {
            for (SizedIngredient requiredHeld : condition.inventoryItems()) {
                int remaining = requiredHeld.count();
                for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                    if (requiredHeld.test(stack)) {
                        int toConsume = Math.min(stack.getCount(), remaining);
                        stack.shrink(toConsume);
                        remaining -= toConsume;
                        if (remaining <= 0) break;
                    }
                }
            }
        }

        // Consume dropped items in the world
        if (result.consumeDroppedItems() && condition.droppedItems() != null) {
            for (SizedIngredient requiredItem : condition.droppedItems()) {
                int remaining = requiredItem.count();

                var itemsInWorld = level.getEntitiesOfClass(ItemEntity.class,
                        player.getBoundingBox().inflate(1.5),
                        itemEntity -> requiredItem.test(itemEntity.getItem()));

                for (ItemEntity entity : itemsInWorld) {
                    ItemStack stack = entity.getItem();
                    int toConsume = Math.min(stack.getCount(), remaining);
                    stack.shrink(toConsume);
                    remaining -= toConsume;

                    if (stack.isEmpty()) entity.remove(ItemEntity.RemovalReason.DISCARDED);
                    if (remaining <= 0) break;
                }
            }
        }

        // Roll and spawn result items
        List<ItemStack> drops = recipe.rollResults(level.getRandom());
        if (result.popItems()) {
            for (ItemStack stack : drops) {
                if (!stack.isEmpty()) CoreItemUtils.popItemStack(level, pos, stack);
            }
        } else {
            for (ItemStack stack : drops) {
                if (!stack.isEmpty()) player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }

    /* -------------------- RECIPE LOOKUP -------------------- */
    private static List<RecipeHolder<WorldRecipe>> getAllBlockInteractionRecipes(Level level) {
        return level.getServer()
                .getRecipeManager()
                .recipeMap()
                .values()
                .stream()
                .filter(r -> r.value().getType() == InWorldRecipesRecipes.WORLD_RECIPE_TYPE.get())
                .map(r -> (RecipeHolder<WorldRecipe>) r)
                .toList();
    }
}