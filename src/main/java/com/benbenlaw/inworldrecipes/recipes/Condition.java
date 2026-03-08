package com.benbenlaw.inworldrecipes.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;


public record Condition(
        @Nullable SizedIngredient heldItem,
        boolean ignoreBlockState,
        @Nullable List<SizedIngredient> droppedItems,
        @Nullable List<SizedIngredient> inventoryItems

) {

    public static final Codec<Condition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    SizedIngredient.NESTED_CODEC.optionalFieldOf("held_item").forGetter(condition -> Optional.ofNullable(condition.heldItem)),
                    Codec.BOOL.optionalFieldOf("ignore_block_state").forGetter(condition -> Optional.of(condition.ignoreBlockState)),
                    Codec.list(SizedIngredient.NESTED_CODEC).optionalFieldOf("dropped_items").forGetter(condition -> Optional.ofNullable(condition.droppedItems)),
                    Codec.list(SizedIngredient.NESTED_CODEC).optionalFieldOf("inventory_items").forGetter(condition -> Optional.ofNullable(condition.inventoryItems))

            ).apply(instance, (heldOpt, ignoreBlockStateOpt, droppedItems, heldItems) ->
                    new Condition(
                            heldOpt.orElse(null),
                            ignoreBlockStateOpt.orElse(false),
                            droppedItems.orElse(null),
                            heldItems.orElse(null)
                    ))
    );
}