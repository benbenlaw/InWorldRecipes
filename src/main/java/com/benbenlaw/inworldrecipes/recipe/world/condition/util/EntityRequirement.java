package com.benbenlaw.inworldrecipes.recipe.world.condition.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public record EntityRequirement(EntityType<?> type, Optional<CompoundTag> nbt, float damage) {
    public static final Codec<EntityRequirement> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityType.CODEC.fieldOf("type").forGetter(EntityRequirement::type),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(EntityRequirement::nbt),
            Codec.FLOAT.optionalFieldOf("damage", 0.0f).forGetter(EntityRequirement::damage)
    ).apply(inst, EntityRequirement::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityRequirement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ENTITY_TYPE), EntityRequirement::type,
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), EntityRequirement::nbt,
            ByteBufCodecs.FLOAT, EntityRequirement::damage,
            EntityRequirement::new
    );
}