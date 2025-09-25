package com.benbenlaw.inworldrecipes.event;

import com.benbenlaw.core.recipe.NoInventoryRecipe;
import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.BlockInteractionRecipe;
import com.benbenlaw.inworldrecipes.recipes.BlockTarget;
import com.benbenlaw.inworldrecipes.util.ClickType;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;
import java.util.Objects;

@EventBusSubscriber(modid = InWorldRecipes.MOD_ID)
public class BlockInteractionEvent {


    @SubscribeEvent
    public static void rightClickOnBlock(PlayerInteractEvent.RightClickBlock event) {

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();

        if (level.isClientSide()) return;


        for (RecipeHolder<BlockInteractionRecipe> match : level.getRecipeManager().getRecipesFor(BlockInteractionRecipe.Type.INSTANCE, NoInventoryRecipe.INSTANCE, level)) {

            if (match.value().clickType() == ClickType.RIGHT_CLICK) {
                executeBlockInteractionEvent(level, player, pos, hand, match);
            }
        }
    }

    @SubscribeEvent
    public static void leftClickOnBlock(PlayerInteractEvent.LeftClickBlock event) {

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();

        if (level.isClientSide()) return;

        for (RecipeHolder<BlockInteractionRecipe> match : level.getRecipeManager().getRecipesFor(BlockInteractionRecipe.Type.INSTANCE, NoInventoryRecipe.INSTANCE, level)) {

            if (match.value().clickType() == ClickType.LEFT_CLICK && event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START) {
                executeBlockInteractionEvent(level, player, pos, hand, match);
            }
        }
    }

    public static void executeBlockInteractionEvent(Level level, Player player, BlockPos pos, InteractionHand hand, RecipeHolder<BlockInteractionRecipe> match) {

        BlockTarget recipeTarget = match.value().targetBlock();
        BlockState levelTargetBlockState = level.getBlockState(pos);

        boolean matches = recipeTarget.matches(levelTargetBlockState, match.value().ignoreBlockState());

        if (matches) {
            SizedIngredient recipeHeldItem = match.value().heldItem();

            if (recipeHeldItem.test(player.getItemInHand(hand))) {

                BlockState recipeOutputBlockState = match.value().outputBlockState();
                List<ItemStack> resultItems = match.value().rollResults(level.random);

                // Replace the block at the position with the output block state if recipe requires if empty then replace with air
                level.setBlockAndUpdate(pos, Objects.requireNonNullElseGet(recipeOutputBlockState, Blocks.AIR::defaultBlockState));

                // Damage Item
                if (match.value().damageHeldItem()) {
                    player.getItemInHand(hand).hurtAndBreak(
                            1,
                            player,
                            EquipmentSlot.valueOf(hand.name().replace("_", "").toUpperCase())
                    );
                }

                // Consume Item
                if (match.value().consumeHeldItem()) {
                    player.getItemInHand(hand).shrink(match.value().heldItem().count());
                }

                // Item Drops
                if (match.value().popItems()) {
                    for (ItemStack itemStack : resultItems) {
                        if (!itemStack.isEmpty()) {
                            popOutTheItem(level, pos, itemStack);
                        }
                    }
                } else {
                    for (ItemStack itemStack : resultItems) {
                        if (!itemStack.isEmpty()) {
                            player.getInventory().placeItemBackInInventory(itemStack);
                        }
                    }
                }
            }
        }
    }


    public static void popOutTheItem(Level level, BlockPos blockPos, ItemStack itemStack) {

        Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockPos, 0.5, 0.5, 0.5).offsetRandom(level.random, 0.7F);
        ItemStack itemstack1 = itemStack.copy();
        ItemEntity itementity = new ItemEntity(level, vec3.x(), vec3.y(), vec3.z(), itemstack1);
        itementity.setDefaultPickUpDelay();
        level.addFreshEntity(itementity);
    }


}
