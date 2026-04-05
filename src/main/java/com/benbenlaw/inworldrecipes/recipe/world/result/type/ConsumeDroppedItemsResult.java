package com.benbenlaw.inworldrecipes.recipe.world.result.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.condition.type.DroppedItemsCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultType;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;


public record ConsumeDroppedItemsResult() implements IRecipeResult {
    public static final MapCodec<ConsumeDroppedItemsResult> CODEC = MapCodec.unit(new ConsumeDroppedItemsResult());
    public static final StreamCodec<RegistryFriendlyByteBuf, ConsumeDroppedItemsResult> STREAM_CODEC = StreamCodec.unit(new ConsumeDroppedItemsResult());

    @Override
    public void apply(WorldRecipeContext ctx, List<IRecipeCondition> conditions) {
        for (IRecipeCondition condition : conditions) {
            if (condition instanceof DroppedItemsCondition(List<SizedIngredient> items)) {

                for (SizedIngredient sizedIngredient : items) {
                    consumeFromWorld(ctx, sizedIngredient);
                }
            }
        }
    }

    private void consumeFromWorld(WorldRecipeContext ctx, SizedIngredient requirement) {
        AABB area = new AABB(ctx.pos()).inflate(5);
        List<ItemEntity> entities = ctx.level().getEntitiesOfClass(ItemEntity.class, area);

        int remaining = requirement.count();

        for (ItemEntity entity : entities) {
            ItemStack stack = entity.getItem();
            if (requirement.ingredient().test(stack)) {
                int toTake = Math.min(remaining, stack.getCount());

                stack.shrink(toTake);
                remaining -= toTake;

                if (stack.isEmpty()) {
                    entity.discard();
                } else {
                    entity.setItem(stack);
                }

                if (remaining <= 0) break;
            }
        }
    }

    @Override
    public ResultType<?> getType() {
        return ResultTypes.CONSUME_DROPPED_ITEMS.get();
    }
}