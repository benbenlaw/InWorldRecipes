package com.benbenlaw.inworldrecipes.recipe.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;

public class FluidTargetCodec {

    public static final Codec<FluidTarget> CODEC = Codec.either(
                    BuiltInRegistries.FLUID.byNameCodec().xmap(FluidTarget.Single::new, FluidTarget.Single::fluid),
                    TagKey.hashedCodec(Registries.FLUID).xmap(FluidTarget.Tag::new, FluidTarget.Tag::tag))
            .xmap(
                    either -> either.map(s -> s, t -> (FluidTarget) t),
                    target -> {
                        if (target instanceof FluidTarget.Single s) return Either.left(s);
                        if (target instanceof FluidTarget.Tag t) return Either.right(t);
                        throw new IllegalStateException("Unknown FluidTarget: " + target);
                    });

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidTarget> STREAM_CODEC =
            ByteBufCodecs.either(
                    ByteBufCodecs.registry(Registries.FLUID),
                    TagKey.streamCodec(Registries.FLUID)
            ).map(
                    either -> either.map(
                            FluidTarget.Single::new,
                            FluidTarget.Tag::new
                    ),
                    target -> {
                        if (target instanceof FluidTarget.Single single)
                            return Either.left(single.fluid());
                        if (target instanceof FluidTarget.Tag tag)
                            return Either.right(tag.tag());
                        throw new IllegalStateException();
                    }
            );
}
