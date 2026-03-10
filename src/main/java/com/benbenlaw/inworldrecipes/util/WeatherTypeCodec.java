package com.benbenlaw.inworldrecipes.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;

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

            WeatherType::name
    );

    public static void writeToBuffer(RegistryFriendlyByteBuf buffer, WeatherType weatherType) {
        buffer.writeEnum(weatherType);
    }

    public static WeatherType readFromBuffer(RegistryFriendlyByteBuf buffer) {
        return buffer.readEnum(WeatherType.class);
    }

}
