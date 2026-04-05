package com.benbenlaw.inworldrecipes.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record Option(
        boolean showInJEI,
        boolean onlyVisualRecipe

) {

    public static final Codec<Option> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.optionalFieldOf("show_in_jei").forGetter(option -> Optional.of(option.showInJEI)),
                    Codec.BOOL.optionalFieldOf("only_visual_recipe").forGetter(option -> Optional.of(option.onlyVisualRecipe))

                    ).apply(instance, (showInJEI, onlyVisualRecipe) ->
                    new Option(
                            showInJEI.orElse(true),
                            onlyVisualRecipe.orElse(false)
                    ))
    );
}