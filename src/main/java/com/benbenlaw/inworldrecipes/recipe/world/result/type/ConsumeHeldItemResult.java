package com.benbenlaw.inworldrecipes.recipe.world.result.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultType;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ConsumeHeldItemResult(int count) implements IRecipeResult {

    public static final MapCodec<ConsumeHeldItemResult> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.INT.optionalFieldOf("count", 1).forGetter(ConsumeHeldItemResult::count)
    ).apply(inst, ConsumeHeldItemResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConsumeHeldItemResult> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(ConsumeHeldItemResult::new, ConsumeHeldItemResult::count).cast();

    @Override
    public void apply(WorldRecipeContext ctx, List<IRecipeCondition> conditions) {
        if (ctx.player() != null && ctx.hand() != null) {
            ItemStack stack = ctx.player().getItemInHand(ctx.hand());

            if (!stack.isEmpty()) {
                stack.shrink(this.count);
            }
        }
    }

    @Override
    public ResultType<?> getType() {
        return ResultTypes.CONSUME_HELD_ITEM.get();
    }
}