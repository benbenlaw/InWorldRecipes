package com.benbenlaw.inworldrecipes.event;

import com.benbenlaw.core.recipe.NoInventoryRecipe;
import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.LightningCraftingRecipe;
import com.benbenlaw.inworldrecipes.util.DelayedTaskManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

@EventBusSubscriber(modid = InWorldRecipes.MOD_ID)
public class LightningCraftingEvent {

    @SubscribeEvent
    public static void onLightningStrike(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof LightningBolt bolt)) return;

        BlockPos strikePos = bolt.blockPosition();
        AABB box = new AABB(strikePos).inflate(3);

        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
            ItemStack droppedStack = item.getItem();

            for (RecipeHolder<LightningCraftingRecipe> holder :
                    level.getRecipeManager().getRecipesFor(
                            LightningCraftingRecipe.Type.INSTANCE,
                            NoInventoryRecipe.INSTANCE,
                            level)) {

                LightningCraftingRecipe recipe = holder.value();

                if (!recipe.droppedItem().test(droppedStack)) continue;

                ItemStack stackCopy = droppedStack.copy();

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.DUST_PLUME,
                            item.getX(), item.getY(), item.getZ(),
                            15, 0.5, 0.5, 0.5, 0.1
                    );
                }

                item.discard();

                DelayedTaskManager.schedule(level, 20, () -> {
                    int count = stackCopy.getCount();
                    for (int i = 0; i < count; i++) {
                        List<ItemStack> results = recipe.rollResults(level.getRandom());
                        for (ItemStack resultStack : results) {
                            level.addFreshEntity(new ItemEntity(
                                    level,
                                    item.getX(),
                                    item.getY() + 1,
                                    item.getZ(),
                                    resultStack
                            ));
                        }
                    }
                });
            }
        }
    }


    @SubscribeEvent
    public static void onServerTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            DelayedTaskManager.tick(level);
        }
    }

}
