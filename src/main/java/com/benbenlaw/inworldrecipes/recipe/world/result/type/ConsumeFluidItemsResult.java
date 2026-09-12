package com.benbenlaw.inworldrecipes.recipe.world.result.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultType;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultTypes;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.type.FluidTrigger;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

/**
 * Consumes the item(s) required by the recipe's {@link FluidTrigger} from the fluid itself.
 */
public record ConsumeFluidItemsResult() implements IRecipeResult {
    public static final MapCodec<ConsumeFluidItemsResult> CODEC = MapCodec.unit(new ConsumeFluidItemsResult());
    public static final StreamCodec<RegistryFriendlyByteBuf, ConsumeFluidItemsResult> STREAM_CODEC = StreamCodec.unit(new ConsumeFluidItemsResult());

    @Override
    public void apply(WorldRecipeContext ctx, List<IRecipeTrigger> triggers, List<IRecipeCondition> conditions) {
        for (IRecipeTrigger trigger : triggers) {
            if (trigger instanceof FluidTrigger(var fluid, List<SizedIngredient> items)) {
                for (SizedIngredient sizedIngredient : items) {
                    consumeFromFluid(ctx, sizedIngredient);
                }
            }
        }
    }

    private void consumeFromFluid(WorldRecipeContext ctx, SizedIngredient requirement) {
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
        return ResultTypes.CONSUME_FLUID_ITEMS.get();
    }
}
