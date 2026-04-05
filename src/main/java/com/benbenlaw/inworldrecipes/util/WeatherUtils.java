package com.benbenlaw.inworldrecipes.util;

import com.benbenlaw.inworldrecipes.recipe.util.WeatherType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class WeatherUtils {

    public static WeatherType getWeather(Level level, BlockPos pos) {

        if (level.isThundering()) {
            return WeatherType.THUNDER;
        }

        if (level.isRaining()) {
            if (level.getBiome(pos).value().coldEnoughToSnow(pos, level.getSeaLevel())) {
                return WeatherType.SNOW;
            }
            return WeatherType.RAIN;
        }

        return WeatherType.CLEAR;
    }
}
