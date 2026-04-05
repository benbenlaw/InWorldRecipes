package com.benbenlaw.inworldrecipes.recipe.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTargetCodec {

    public static final Codec<BlockTarget> CODEC = Codec.either(
                    BlockState.CODEC.xmap(BlockTarget.Single::new, BlockTarget.Single::blockState),
                    TagKey.hashedCodec(Registries.BLOCK).xmap(BlockTarget.Tag::new, BlockTarget.Tag::tag))
            .xmap(
                    either -> either.map(s -> s, t -> (BlockTarget) t),
                    target -> {
                        if (target instanceof BlockTarget.Single s) return Either.left(s);
                        if (target instanceof BlockTarget.Tag t) return Either.right(t);
                        throw new IllegalStateException("Unknown BlockTarget: " + target);
                    });

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockTarget> STREAM_CODEC = ByteBufCodecs.either(
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY),
            TagKey.streamCodec(Registries.BLOCK)
    ).map(
            either -> either.map(BlockTarget.Single::new, tag -> (BlockTarget) new BlockTarget.Tag(tag)),
            target -> {
                if (target instanceof BlockTarget.Single(BlockState blockState)) return Either.left(blockState);
                if (target instanceof BlockTarget.Tag(TagKey<Block> tag)) return Either.right(tag);
                throw new IllegalStateException("Unknown BlockTarget: " + target);
            }).cast();
}