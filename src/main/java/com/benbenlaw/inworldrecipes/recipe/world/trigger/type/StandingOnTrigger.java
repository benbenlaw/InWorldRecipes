package com.benbenlaw.inworldrecipes.recipe.world.trigger.type;

import com.benbenlaw.inworldrecipes.recipe.util.BlockTarget;
import com.benbenlaw.inworldrecipes.recipe.util.BlockTargetCodec;
import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerType;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

public record StandingOnTrigger(BlockTarget targetBlock) implements IRecipeTrigger {

    public static final MapCodec<StandingOnTrigger> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            BlockTargetCodec.CODEC.fieldOf("target_block").forGetter(StandingOnTrigger::targetBlock)
    ).apply(inst, StandingOnTrigger::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StandingOnTrigger> STREAM_CODEC =
            BlockTargetCodec.STREAM_CODEC.map(StandingOnTrigger::new, StandingOnTrigger::targetBlock);

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        if (ctx.player() == null) return false;
        BlockState state = ctx.level().getBlockState(ctx.player().blockPosition().below());
        return this.targetBlock.matches(state, false);
    }

    @Override
    public TriggerType<?> getType() { return TriggerTypes.STANDING_ON.get(); }
}