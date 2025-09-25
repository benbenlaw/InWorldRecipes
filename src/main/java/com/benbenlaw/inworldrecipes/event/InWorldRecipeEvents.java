package com.benbenlaw.inworldrecipes.event;

import com.benbenlaw.core.recipe.NoInventoryRecipe;
import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.*;
import com.benbenlaw.inworldrecipes.util.DimensionPositionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.*;

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
                BlockPos fluidPos = event.getEntity().blockPosition();

                for (RecipeHolder<DropItemInFluidRecipe> match : level.getRecipeManager().getRecipesFor(DropItemInFluidRecipe.Type.INSTANCE, NoInventoryRecipe.INSTANCE, level)) {
                    DropItemInFluidRecipe recipe = match.value();

                    Fluid requiredFluid = BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(recipe.fluid()));
                    boolean correctFluid = level.getFluidState(fluidPos).is(requiredFluid);
                    if (!correctFluid) continue;

                    boolean consumeFluid = recipe.consumeFluidBlock();
                    List<SizedIngredient> ingredients = recipe.getDroppedItems();

                    // Collect all ItemEntities at the fluid block's position
                    List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class,
                            new AABB(fluidPos).inflate(0.5),
                            entity -> entity.isAlive() && !entity.getItem().isEmpty());

                    // Try to match all required ingredients to item entities
                    List<ItemEntity> matchedEntities = new ArrayList<>();
                    boolean allIngredientsMatched = true;

                    for (SizedIngredient ingredient : ingredients) {
                        boolean matched = false;
                        for (ItemEntity itemEntity : itemEntities) {
                            ItemStack stack = itemEntity.getItem();
                            if (!matchedEntities.contains(itemEntity) && ingredient.test(stack) && stack.getCount() >= ingredient.count()) {
                                matchedEntities.add(itemEntity);
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            allIngredientsMatched = false;
                            break;
                        }
                    }

                    if (allIngredientsMatched) {
                        // All ingredients are present — complete the recipe
                        List<ItemStack> resultItems = recipe.rollResults(level.random);
                        for (ItemStack itemStack : resultItems) {
                            ItemEntity outputEntity = new ItemEntity(level, fluidPos.getX() + 0.5, fluidPos.getY() + 1, fluidPos.getZ() + 0.5, itemStack);
                            outputEntity.setDefaultPickUpDelay();
                            level.addFreshEntity(outputEntity);
                        }

                        // Consume the required input items
                        for (int i = 0; i < ingredients.size(); i++) {
                            SizedIngredient ingredient = ingredients.get(i);
                            ItemEntity itemEntity = matchedEntities.get(i);
                            itemEntity.getItem().shrink(ingredient.count());
                        }

                        // Consume fluid block
                        if (consumeFluid) {
                            level.setBlockAndUpdate(fluidPos, Blocks.AIR.defaultBlockState());
                        }

                        break; // Stop checking other recipes after one matches
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

                        if (entity instanceof EnderDragon dragon) {
                            if (dragon.level().getServer() != null) {
                                ServerLevel serverLevel = (ServerLevel) dragon.level();
                                EndDragonFight fight = serverLevel.getDragonFight();

                                if (fight != null) {
                                    fight.setDragonKilled(dragon);
                                }
                            }
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

    //Block Conversion
    private static final Map<DimensionPositionHelper, Long> placedBlocks = new HashMap<>();
    @SubscribeEvent
    public static void onBlockConversion(BlockEvent.EntityPlaceEvent event) {

        Level level = Objects.requireNonNull(event.getEntity()).level();
        Block block = event.getPlacedBlock().getBlock();

        for (RecipeHolder<BlockConversionRecipe> match : level.getRecipeManager().getRecipesFor(BlockConversionRecipe.Type.INSTANCE, NoInventoryRecipe.INSTANCE, level)) {

            Block recipeBlock = match.value().getBlockToConvert();
            TagKey<Block> recipeBlockTag = match.value().getBlockToConvertTag();

            if (recipeBlock != null) {
                if (block == recipeBlock) {
                    if (level instanceof ServerLevel serverLevel) {
                        DimensionPositionHelper dimensionPositionHelper = new DimensionPositionHelper(serverLevel.dimension(), event.getPos());
                        placedBlocks.put(dimensionPositionHelper, level.getGameTime());
                    }
                }
            }

            if (recipeBlockTag != null) {
                if (block.defaultBlockState().is(recipeBlockTag)) {
                    if (level instanceof ServerLevel serverLevel) {
                        DimensionPositionHelper dimensionPositionHelper = new DimensionPositionHelper(serverLevel.dimension(), event.getPos());
                        placedBlocks.put(dimensionPositionHelper, level.getGameTime());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {

        Level level = event.getPlayer().level();
        Block block = event.getState().getBlock();

        for (RecipeHolder<BlockConversionRecipe> match : level.getRecipeManager().getRecipesFor(BlockConversionRecipe.Type.INSTANCE, NoInventoryRecipe.INSTANCE, level)) {

            Block recipeBlock = match.value().getBlockToConvert();
            TagKey<Block> recipeBlockTag = match.value().getBlockToConvertTag();

            if (recipeBlock != null) {
                if (block == recipeBlock) {
                    if (level instanceof ServerLevel serverLevel) {
                        DimensionPositionHelper dimensionPositionHelper = new DimensionPositionHelper(serverLevel.dimension(), event.getPos());
                        placedBlocks.remove(dimensionPositionHelper);
                    }
                }
            }

            if (recipeBlockTag != null) {
                if (block.defaultBlockState().is(recipeBlockTag)) {
                    if (level instanceof ServerLevel serverLevel) {
                        DimensionPositionHelper dimensionPositionHelper = new DimensionPositionHelper(serverLevel.dimension(), event.getPos());
                        placedBlocks.remove(dimensionPositionHelper);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        Iterator<Map.Entry<DimensionPositionHelper, Long>> iterator = placedBlocks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<DimensionPositionHelper, Long> entry = iterator.next();
            DimensionPositionHelper dimensionPositionHelper = entry.getKey();

            ServerLevel level = event.getServer().getLevel(dimensionPositionHelper.dimension());

            assert level != null;
            long currentTime = level.getGameTime();

            String dimension = dimensionPositionHelper.dimension().location().toString();


            if (!dimensionPositionHelper.dimension().equals(level.dimension())) continue;


            BlockPos pos = dimensionPositionHelper.pos();
            long placedTime = entry.getValue();

            if (!level.isLoaded(pos)) continue;

            BlockState state = level.getBlockState(pos);

            for (RecipeHolder<BlockConversionRecipe> match : level.getRecipeManager().getRecipesFor(BlockConversionRecipe.Type.INSTANCE, NoInventoryRecipe.INSTANCE, level)) {

                Block recipeBlock = match.value().getBlockToConvert();
                TagKey<Block> recipeBlockTag = match.value().getBlockToConvertTag();
                Block convertedBlock = match.value().getConvertedBlock();
                int duration = match.value().duration();
                boolean popItem = match.value().popBlock();

                assert recipeBlock != null;
                if (state.is(recipeBlock) || (recipeBlockTag != null && state.is(recipeBlockTag))) {
                    if (currentTime - placedTime >= duration) {

                        // Day time check
                        boolean requiresSunlight = match.value().requiresSunlight();
                        if (requiresSunlight && (!level.canSeeSky(pos.above()) || !level.isDay())) {
                            continue;
                        }

                        // Nighttime check
                        boolean requiresMoonlight = match.value().requiresMoonlight();
                        if (requiresMoonlight && (!level.canSeeSky(pos.above()) || level.isDay())) {
                            continue;
                        }

                        // Dimension check
                        String requiredDimension = match.value().dimension();
                        boolean requiresDimension = !requiredDimension.contains("none");

                        if (requiresDimension) {
                            if (!dimension.equals(requiredDimension)) {
                                continue;
                            }
                        }

                        // Pop item
                        if (popItem) {
                            popOutTheItem(level, pos, new ItemStack(convertedBlock));
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        } else {
                            level.setBlockAndUpdate(pos, convertedBlock.defaultBlockState());
                        }

                        SoundType blockSounds = convertedBlock.getSoundType(convertedBlock.defaultBlockState(), level, pos, null);
                        level.playSound(null, pos, blockSounds.getPlaceSound(), SoundSource.BLOCKS, 1, 1);

                        iterator.remove(); // Done tracking this block
                        break;
                    }
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