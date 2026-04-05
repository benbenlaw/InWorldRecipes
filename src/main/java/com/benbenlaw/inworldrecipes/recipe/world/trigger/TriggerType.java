package com.benbenlaw.inworldrecipes.recipe.world.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TriggerType<T extends IRecipeTrigger>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {

    public static final Codec<IRecipeTrigger> CODEC = TriggerTypes.REGISTRY.byNameCodec()
            .dispatch(IRecipeTrigger::getType, TriggerType::codec);

    public static final StreamCodec<RegistryFriendlyByteBuf, IRecipeTrigger> STREAM_CODEC =
            ByteBufCodecs.registry(TriggerTypes.KEY)
                    .dispatch(IRecipeTrigger::getType, TriggerType::streamCodec);


}