package com.benbenlaw.inworldrecipes.recipe.world.result.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultType;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultTypes;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.type.CauldronTrigger;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

/**
 * Consumes the item(s) required by the recipe's {@link CauldronTrigger} from the cauldron itself.
 */
public record ConsumeCauldronItemsResult() implements IRecipeResult {
    public static final MapCodec<ConsumeCauldronItemsResult> CODEC = MapCodec.unit(new ConsumeCauldronItemsResult());
    public static final StreamCodec<RegistryFriendlyByteBuf, ConsumeCauldronItemsResult> STREAM_CODEC = StreamCodec.unit(new ConsumeCauldronItemsResult());

    @Override
    public void apply(WorldRecipeContext ctx, List<IRecipeTrigger> triggers, List<IRecipeCondition> conditions) {
        for (IRecipeTrigger trigger : triggers) {
            if (trigger instanceof CauldronTrigger(var fluid, List<SizedIngredient> items, var consumeLevel)) {
                for (SizedIngredient sizedIngredient : items) {
                    consumeFromCauldron(ctx, sizedIngredient);
                }
            }
        }
    }

    private void consumeFromCauldron(WorldRecipeContext ctx, SizedIngredient requirement) {
        List<ItemEntity> entities = ctx.level().getEntitiesOfClass(ItemEntity.class, new AABB(ctx.pos()));

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
        return ResultTypes.CONSUME_CAULDRON_ITEMS.get();
    }
}
