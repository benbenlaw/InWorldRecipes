package com.benbenlaw.inworldrecipes.recipe.world.trigger.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerType;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record LightningTrigger() implements IRecipeTrigger {

    public static final MapCodec<LightningTrigger> CODEC = MapCodec.unit(new LightningTrigger());
    public static final StreamCodec<RegistryFriendlyByteBuf, LightningTrigger> STREAM_CODEC = StreamCodec.unit(new LightningTrigger());

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        return ctx.lightningStrike(); 
    }

    @Override
    public TriggerType<?> getType() { return TriggerTypes.LIGHTNING.get(); }
}