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

public record DamageHeldItemResult(int amount) implements IRecipeResult {

    public static final MapCodec<DamageHeldItemResult> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.INT.optionalFieldOf("amount", 1).forGetter(DamageHeldItemResult::amount)
    ).apply(inst, DamageHeldItemResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DamageHeldItemResult> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(DamageHeldItemResult::new, DamageHeldItemResult::amount).cast();

    @Override
    public void apply(WorldRecipeContext ctx, List<IRecipeCondition> conditions) {
        if (ctx.player() != null && ctx.hand() != null) {
            ItemStack stack = ctx.player().getItemInHand(ctx.hand());

            if (!stack.isEmpty() && stack.isDamageableItem()) {
                stack.hurtAndBreak(this.amount, ctx.player(),
                        ctx.player().getEquipmentSlotForItem(stack));
            }
        }
    }

    @Override
    public ResultType<?> getType() {
        return ResultTypes.DAMAGE_HELD_ITEM.get();
    }
}