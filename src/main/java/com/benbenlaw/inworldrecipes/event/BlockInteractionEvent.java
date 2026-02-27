package com.benbenlaw.inworldrecipes.event;

import com.benbenlaw.core.recipe.NoInventoryRecipe;
import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.BlockInteractionRecipe;
import com.benbenlaw.inworldrecipes.recipes.BlockTarget;
import com.benbenlaw.inworldrecipes.recipes.InWorldRecipeRecipes;
import com.benbenlaw.inworldrecipes.util.ClickType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@EventBusSubscriber(modid = InWorldRecipes.MOD_ID)
public class BlockInteractionEvent {

    @SubscribeEvent
    public static void rightClickOnBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();

        if (level.isClientSide()) return;

        Optional<Collection<BlockPos>> ultiminePositions = Optional.of(List.of(pos));

        for (BlockPos targetPos : ultiminePositions.orElseThrow()) {
            getAllBlockInteractionRecipes(level).forEach(match -> {
                // Execute the recipe regardless of click type
                executeBlockInteractionEvent(level, player, targetPos, hand, match);
            });
        }
    }

    @SubscribeEvent
    public static void leftClickOnBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();

        if (level.isClientSide()) return;

        Optional<Collection<BlockPos>> ultiminePositions = Optional.of(List.of(pos));

        for (BlockPos targetPos : ultiminePositions.orElseThrow()) {
            getAllBlockInteractionRecipes(level).forEach(match -> {
                executeBlockInteractionEvent(level, player, targetPos, hand, match);
            });
        }
    }

    private static List<RecipeHolder<BlockInteractionRecipe>> getAllBlockInteractionRecipes(Level level) {
        return level.getServer()
                .getRecipeManager()
                .recipeMap()
                .values()
                .stream()
                .filter(r -> r.value().getType() == InWorldRecipeRecipes.BLOCK_INTERACTION_RECIPE_TYPE.get())
                .map(r -> (RecipeHolder<BlockInteractionRecipe>) r)
                .toList();
    }

    public static void executeBlockInteractionEvent(Level level, Player player, BlockPos pos, InteractionHand hand, RecipeHolder<BlockInteractionRecipe> match) {
        BlockTarget recipeTarget = match.value().targetBlock();
        BlockState levelTargetBlockState = level.getBlockState(pos);

        boolean matches = recipeTarget.matches(levelTargetBlockState, match.value().ignoreBlockState());

        if (matches) {
            SizedIngredient recipeHeldItem = match.value().heldItem();

            if (recipeHeldItem.test(player.getItemInHand(hand))) {

                BlockState recipeOutputBlockState = match.value().outputBlockState();
                List<ItemStack> resultItems = match.value().rollResults(level.getRandom());

                // Replace the block at the position with the output block state if specified; otherwise, air
                level.setBlockAndUpdate(pos, Objects.requireNonNullElseGet(recipeOutputBlockState, Blocks.AIR::defaultBlockState));

                // Damage held item
                if (match.value().damageHeldItem()) {
                    player.getItemInHand(hand).hurtAndBreak(
                            1,
                            player,
                            EquipmentSlot.valueOf(hand.name().replace("_", "").toUpperCase())
                    );
                }

                // Consume held item
                if (match.value().consumeHeldItem()) {
                    player.getItemInHand(hand).shrink(match.value().heldItem().count());
                }

                // Handle item drops
                if (match.value().popItems()) {
                    for (ItemStack itemStack : resultItems) {
                        if (!itemStack.isEmpty()) popOutTheItem(level, pos, itemStack);
                    }
                } else {
                    for (ItemStack itemStack : resultItems) {
                        if (!itemStack.isEmpty()) player.getInventory().placeItemBackInInventory(itemStack);
                    }
                }
            }
        }
    }

    public static void popOutTheItem(Level level, BlockPos blockPos, ItemStack itemStack) {
        Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockPos, 0.5, 0.5, 0.5).offsetRandom(level.getRandom(), 0.7F);
        ItemStack copy = itemStack.copy();
        ItemEntity entity = new ItemEntity(level, vec3.x(), vec3.y(), vec3.z(), copy);
        entity.setDefaultPickUpDelay();
        level.addFreshEntity(entity);
    }
}