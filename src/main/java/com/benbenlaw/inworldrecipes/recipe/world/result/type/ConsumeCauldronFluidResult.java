package com.benbenlaw.inworldrecipes.recipe.world.result.type;

import com.benbenlaw.inworldrecipes.recipe.util.CauldronFluids;
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
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Lowers the cauldron's fluid level by the amount configured on the recipe's {@link CauldronTrigger}.
 */
public record ConsumeCauldronFluidResult() implements IRecipeResult {
    public static final MapCodec<ConsumeCauldronFluidResult> CODEC = MapCodec.unit(new ConsumeCauldronFluidResult());
    public static final StreamCodec<RegistryFriendlyByteBuf, ConsumeCauldronFluidResult> STREAM_CODEC = StreamCodec.unit(new ConsumeCauldronFluidResult());

    @Override
    public void apply(WorldRecipeContext ctx, List<IRecipeTrigger> triggers, List<IRecipeCondition> conditions) {
        for (IRecipeTrigger trigger : triggers) {
            if (trigger instanceof CauldronTrigger cauldronTrigger) {
                BlockState state = ctx.level().getBlockState(ctx.pos());
                if (CauldronFluids.isCauldronWithFluid(state)) {
                    CauldronFluids.consume(state, ctx.level(), ctx.pos(), cauldronTrigger.consumeLevel());
                }
            }
        }
    }

    @Override
    public ResultType<?> getType() {
        return ResultTypes.CONSUME_CAULDRON_FLUID.get();
    }
}
