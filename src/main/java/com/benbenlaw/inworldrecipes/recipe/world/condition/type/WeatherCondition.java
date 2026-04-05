package com.benbenlaw.inworldrecipes.recipe.world.condition.type;

import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionTypes;
import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionType;
import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.util.WeatherType;
import com.benbenlaw.inworldrecipes.recipe.util.WeatherTypeCodec;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.util.WeatherUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record WeatherCondition(WeatherType weather) implements IRecipeCondition {

    public static final MapCodec<WeatherCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            WeatherTypeCodec.WEATHER_TYPE_CODEC.fieldOf("weather").forGetter(WeatherCondition::weather)
    ).apply(inst, WeatherCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeatherCondition> STREAM_CODEC = StreamCodec.composite(
            WeatherTypeCodec.STREAM_CODEC, WeatherCondition::weather,
            WeatherCondition::new
    );

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        var current = WeatherUtils.getWeather(ctx.level(), ctx.pos());
        if (weather == WeatherType.RAIN && current == WeatherType.THUNDER) return true;
        return current == weather;
    }

    @Override
    public ConditionType<?> getType() {
        return ConditionTypes.WEATHER.get();
    }

    @Override
    public Component getJeiTooltip() {
        Identifier id = ConditionTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Condition Type");

        String translationKey = "jei." + id.getNamespace() + "." + id.getPath();
        MutableComponent tooltip = Component.translatable(translationKey).append(": ");

        String weatherKey = "weather.inworldrecipes." + this.weather.name().toLowerCase();
        return tooltip.append(Component.translatable(weatherKey));
    }
}