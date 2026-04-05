package com.benbenlaw.inworldrecipes.recipe.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.Locale;


//Move to core 1.22


public class WeatherTypeCodec {

    public static final Codec<WeatherType> WEATHER_TYPE_CODEC = Codec.STRING.xmap(
            s -> switch (s.toLowerCase(Locale.ROOT)) {
                case "clear", "" -> WeatherType.CLEAR;
                case "raining", "rain" -> WeatherType.RAIN;
                case "thunder", "thundering" -> WeatherType.THUNDER;
                case "snowing", "snow" -> WeatherType.SNOW;
                default -> throw new IllegalArgumentException("Unknown WeatherType: " + s);
            },
            weather -> weather.name().toLowerCase(Locale.ROOT)
    );

    /**
     * StreamCodec for networking.
     * Uses the built-in Enum handler for efficiency.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, WeatherType> STREAM_CODEC =
            ByteBufCodecs.idMapper(
                    ByIdMap.continuous(WeatherType::ordinal, WeatherType.values(), ByIdMap.OutOfBoundsStrategy.ZERO),
                    WeatherType::ordinal
            ).cast(); // <--- .cast() bridges ByteBuf to RegistryFriendlyByteBuf

    /* --- Legacy methods if needed elsewhere --- */

    public static void writeToBuffer(RegistryFriendlyByteBuf buffer, WeatherType weatherType) {
        buffer.writeEnum(weatherType);
    }

    public static WeatherType readFromBuffer(RegistryFriendlyByteBuf buffer) {
        return buffer.readEnum(WeatherType.class);
    }
}