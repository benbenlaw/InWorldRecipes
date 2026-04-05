package com.benbenlaw.inworldrecipes.recipe.world.condition;

import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultType;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ConditionType<T extends IRecipeCondition>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {

    public static final Codec<IRecipeCondition> CODEC = ConditionTypes.REGISTRY.byNameCodec()
            .dispatch(IRecipeCondition::getType, ConditionType::codec);

    public static final StreamCodec<RegistryFriendlyByteBuf, IRecipeCondition> STREAM_CODEC =
            ByteBufCodecs.registry(ConditionTypes.KEY)
                    .dispatch(IRecipeCondition::getType, type -> (StreamCodec<RegistryFriendlyByteBuf, IRecipeCondition>) type.streamCodec());
}