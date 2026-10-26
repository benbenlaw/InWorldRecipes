package com.benbenlaw.inworldrecipes.recipe.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.Locale;

public class CauldronContentCodec {

    public static final Codec<CauldronContent> CODEC = Codec.STRING.xmap(
            s -> switch (s.toLowerCase(Locale.ROOT)) {
                case "minecraft:water", "water" -> CauldronContent.WATER;
                case "minecraft:lava", "lava" -> CauldronContent.LAVA;
                case "minecraft:powder_snow", "powder_snow" -> CauldronContent.POWDER_SNOW;
                case "auroral:liquid_aura", "liquid_aura" -> CauldronContent.LIQUID_AURA;
                default -> throw new IllegalArgumentException("Unknown CauldronContent: " + s);
            },
            content -> switch (content) {
                case WATER -> "minecraft:water";
                case LAVA -> "minecraft:lava";
                case POWDER_SNOW -> "minecraft:powder_snow";
                case LIQUID_AURA -> "auroral:liquid_aura";
            }
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CauldronContent> STREAM_CODEC =
            ByteBufCodecs.idMapper(
                    ByIdMap.continuous(CauldronContent::ordinal, CauldronContent.values(), ByIdMap.OutOfBoundsStrategy.ZERO),
                    CauldronContent::ordinal
            ).cast();
}
