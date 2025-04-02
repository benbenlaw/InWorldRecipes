package com.benbenlaw.inworldrecipes.event;

import com.benbenlaw.core.recipe.NoInventoryRecipe;
import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

@EventBusSubscriber(modid = InWorldRecipes.MOD_ID)
public class InWorldRecipeEvents {

    @SubscribeEvent
    public static void rightClickOnBlock(PlayerInteractEvent.RightClickBlock event) {

        Level level = event.getLevel();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();

        if(!level.isClientSide()) {

            ItemStack heldItemPlayer = event.getItemStack();
            BlockPos blockPos = event.getPos();
            Block targetBlockPlayer = level.getBlockState(blockPos).getBlock();

            for (RecipeHolder<RightClickOnBlockTransformsBlockRecipe> match : level.getRecipeManager().getRecipesFor(RightClickOnBlockTransformsBlockRecipe.Type.INSTANCE, NoInventoryRecipe.INSTANCE, level)) {

                boolean correctItems = match.value().heldItem().test(heldItemPlayer);
                boolean correctAmountOfItems = heldItemPlayer.getCount() >= match.value().heldItem().count();
                boolean correctBlock = match.value().targetBlock().test(targetBlockPlayer.asItem().getDefaultInstance());

                Block newBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(match.value().newBlock()));
                Fluid newBlockAsFluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(match.value().newBlock()));

                if (correctItems && correctBlock && correctAmountOfItems) {

                    if (newBlock != Blocks.AIR) {
                        level.setBlockAndUpdate(blockPos, newBlock.defaultBlockState());
                    }
                    else {
                        level.setBlockAndUpdate(blockPos, newBlock.defaultBlockState());
                    }

                    SoundType blockSounds = newBlock.getSoundType(newBlock.defaultBlockState(), level, blockPos, null);
                    level.playSound(null, blockPos, blockSounds.getPlaceSound(), SoundSource.BLOCKS, 1, 1);
                    player.swing(hand, true);

                    //Damage Item
                    if (match.value().damageHeldItem()) {
                        EquipmentSlot equipmentSlot;
                        if (hand == InteractionHand.MAIN_HAND) {
                            equipmentSlot = EquipmentSlot.MAINHAND;
                        } else {
                            equipmentSlot = EquipmentSlot.OFFHAND;
                        }
                        heldItemPlayer.hurtAndBreak(1, player, equipmentSlot);
                    }
                    //Consume Item
                    if (match.value().consumeHeldItem()) {
                        heldItemPlayer.shrink(match.value().heldItem().count());
                    }
                    event.setCanceled(true);
                    break;
                }
            }

            for (RecipeHolder<RightClickOnBlockTransformsItemRecipe> match : level.getRecipeManager().getRecipesFor(RightClickOnBlockTransformsItemRecipe.Type.INSTANCE, NoInventoryRecipe.INSTANCE, level)) {

                boolean correctItems = match.value().heldItem().test(heldItemPlayer);
                boolean correctAmountOfItems = heldItemPlayer.getCount() >= match.value().heldItem().count();
                boolean correctBlock = match.value().targetBlock().test(targetBlockPlayer.asItem().getDefaultInstance());

                boolean destroyTargetBlock = match.value().destroyTargetBlock();
                boolean popItem = match.value().popItem();
                boolean consumeHeldItem = match.value().consumeHeldItem();
                List<ItemStack> resultItem = match.value().rollResults(level.random);

                if (correctItems && correctBlock && correctAmountOfItems) {

                    if (destroyTargetBlock) {
                        level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                    }

                    if (popItem) {
                        for (ItemStack itemStack : resultItem) {
                            popOutTheItem(level, blockPos, itemStack);
                        }
                    } else {
                        for (ItemStack itemStack : resultItem) {
                            player.addItem(itemStack);
                        }
                    }

                    if (consumeHeldItem) {
                        heldItemPlayer.shrink(match.value().heldItem().count());
                    }

                    if (match.value().damageHeldItem()) {
                        EquipmentSlot equipmentSlot;
                        if (hand == InteractionHand.MAIN_HAND) {
                            equipmentSlot = EquipmentSlot.MAINHAND;
                        } else {
                            equipmentSlot = EquipmentSlot.OFFHAND;
                        }
                        heldItemPlayer.hurtAndBreak(1, player, equipmentSlot);
                    }

                    player.swing(hand, true);
                    event.setCanceled(true);
                    break;
                }
            }
        }
    }

    //Item In Fluid

    @SubscribeEvent
    public static void onItemEntityTick(EntityTickEvent.Pre event) {
        Level level = event.getEntity().level();

        if (!level.isClientSide()) {

            if (event.getEntity() instanceof ItemEntity) {

                for (RecipeHolder<DropItemInFluidRecipe> match : level.getRecipeManager().getRecipesFor(DropItemInFluidRecipe.Type.INSTANCE, NoInventoryRecipe.INSTANCE, level)) {

                    boolean correctItem = match.value().droppedItem().test(((ItemEntity) event.getEntity()).getItem());
                    boolean correctItemAmount = ((ItemEntity) event.getEntity()).getItem().getCount() >= match.value().droppedItem().count();
                    Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(match.value().fluid()));
                    boolean correctFluid = level.getFluidState(event.getEntity().getOnPos()).is(fluid);
                    boolean consumeFluid = match.value().consumeFluidBlock();
                    List<ItemStack> resultItem = match.value().rollResults(level.random);

                    if (correctItem && correctFluid && correctItemAmount) {

                        for (ItemStack itemStack : resultItem) {
                            ItemEntity itementity = new ItemEntity(level, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), itemStack);
                            itementity.setDefaultPickUpDelay();
                            level.addFreshEntity(itementity);
                        }

                        int stackTotal = ((ItemEntity) event.getEntity()).getItem().getCount();
                        ((ItemEntity) event.getEntity()).getItem().setCount(stackTotal - match.value().droppedItem().count());
                        if (consumeFluid) {
                            level.setBlockAndUpdate(event.getEntity().blockPosition(), Blocks.AIR.defaultBlockState());
                        }
                        break;
                    }
                }

                for (RecipeHolder<DropItemInFluidConvertsFluidRecipe> match : level.getRecipeManager().getRecipesFor(DropItemInFluidConvertsFluidRecipe.Type.INSTANCE, NoInventoryRecipe.INSTANCE, level)) {

                    boolean correctItem = match.value().droppedItem().test(((ItemEntity) event.getEntity()).getItem());
                    int requiredAmount = match.value().droppedItem().count();
                    ItemEntity itemEntity = (ItemEntity) event.getEntity();
                    ItemStack itemStack = itemEntity.getItem();
                    boolean correctItemAmount = itemStack.getCount() >= requiredAmount;
                    boolean destroyItems = match.value().destroyItems();
                    Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(match.value().fluid()));
                    boolean correctFluid = level.getFluidState(event.getEntity().getOnPos()).is(fluid);
                    Fluid newFluid = BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(match.value().newFluid()));

                    if (correctItem && correctFluid && correctItemAmount) {
                        level.setBlockAndUpdate(event.getEntity().blockPosition(), newFluid.defaultFluidState().createLegacyBlock());
                        if (destroyItems) {
                            itemStack.shrink(requiredAmount);
                        }
                        break;
                    }
                }
            }
        }
    }

    //Right Click On Entity

    @SubscribeEvent
    public static void onEntityRightClick(PlayerInteractEvent.EntityInteractSpecific event) {

        Level level = event.getLevel();
        Player player = event.getEntity();
        Entity entity = event.getTarget();
        InteractionHand hand = event.getHand();

        if (!level.isClientSide()) {

            ItemStack heldItemPlayer = event.getItemStack();


            for (RecipeHolder<RightClickOnEntityTransformsItemRecipe> match : level.getRecipeManager().getRecipesFor(RightClickOnEntityTransformsItemRecipe.Type.INSTANCE, NoInventoryRecipe.INSTANCE, level)) {

                Entity targetEntity = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(match.value().entity())).create(level);
                assert targetEntity != null;
                boolean correctEntity = entity.getType() == targetEntity.getType();
                boolean correctItems = match.value().heldItem().test(player.getItemInHand(event.getHand()));
                boolean correctAmountOfItems = player.getItemInHand(event.getHand()).getCount() >= match.value().heldItem().count();
                boolean damageHeldItem = match.value().damageHeldItem();
                boolean consumeHeldItem = match.value().consumeHeldItem();
                boolean destroyEntity = match.value().destroyEntity();
                boolean popItem = match.value().popItem();
                List<ItemStack> resultItem = match.value().rollResults(level.random);

                if (correctEntity && correctItems && correctAmountOfItems) {

                    if (destroyEntity) {
                        if (entity instanceof EnderDragonPart part) {
                            entity = part.parentMob;
                        }
                        entity.remove(Entity.RemovalReason.KILLED);
                    }

                    if (damageHeldItem) {
                        EquipmentSlot equipmentSlot;
                        if (hand == InteractionHand.MAIN_HAND) {
                            equipmentSlot = EquipmentSlot.MAINHAND;
                        } else {
                            equipmentSlot = EquipmentSlot.OFFHAND;
                        }
                        heldItemPlayer.hurtAndBreak(1, player, equipmentSlot);
                    }

                    if (consumeHeldItem) {
                        player.getItemInHand(event.getHand()).shrink(match.value().heldItem().count());
                    }

                    if (popItem) {
                        for (ItemStack itemStack : resultItem) {
                            popOutTheItem(level, entity.blockPosition(), itemStack);
                        }
                    }
                    else {
                        for (ItemStack itemStack : resultItem) {
                            player.addItem(itemStack);
                        }

                    }

                    player.swing(hand, true);
                    event.setCanceled(true);
                    break;
                }
            }
        }
    }


    public static void popOutTheItem(Level level, BlockPos blockPos, ItemStack itemStack) {

            Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockPos, 0.5, 1.1, 0.5).offsetRandom(level.random, 0.7F);
            ItemStack itemstack1 = itemStack.copy();
            ItemEntity itementity = new ItemEntity(level, vec3.x(), vec3.y(), vec3.z(), itemstack1);
            itementity.setDefaultPickUpDelay();
            level.addFreshEntity(itementity);
        }

}
