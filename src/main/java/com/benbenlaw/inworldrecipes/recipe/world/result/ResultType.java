package com.benbenlaw.inworldrecipes.recipe.world.result;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ResultType<T extends IRecipeResult>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {

    public static final Codec<IRecipeResult> CODEC = ResultTypes.REGISTRY.byNameCodec()
            .dispatch(IRecipeResult::getType, ResultType::codec);

    public static final StreamCodec<RegistryFriendlyByteBuf, IRecipeResult> STREAM_CODEC =
            ByteBufCodecs.registry(ResultTypes.KEY)
                    .dispatch(IRecipeResult::getType, ResultType::streamCodec);
}