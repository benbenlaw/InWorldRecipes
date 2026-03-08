package com.benbenlaw.inworldrecipes.recipes;

import com.benbenlaw.core.recipe.ChanceResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public record Result(
        @Nullable List<ChanceResult> chanceResults,
        @Nullable boolean damageHeldItem,
        @Nullable boolean consumeHeldItem,
        @Nullable boolean popItems,
        @Nullable BlockState outputBlockState,
        boolean consumeInventoryItems,
        boolean consumeDroppedItems

) {

    public static final Codec<Result> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(ChanceResult.CODEC).optionalFieldOf("results").forGetter(result -> Optional.ofNullable(result.chanceResults)),
                    Codec.BOOL.optionalFieldOf("damage_held_item").forGetter(result -> Optional.of(result.damageHeldItem)),
                    Codec.BOOL.optionalFieldOf("consume_held_item").forGetter(result -> Optional.of(result.consumeHeldItem)),
                    Codec.BOOL.optionalFieldOf("pop_item").forGetter(result -> Optional.of(result.popItems)),
                    BlockState.CODEC.optionalFieldOf("block_state").forGetter(result -> Optional.ofNullable(result.outputBlockState)),
                    Codec.BOOL.optionalFieldOf("consume_inventory_items").forGetter(result -> Optional.of(result.consumeInventoryItems)),
                    Codec.BOOL.optionalFieldOf("consume_dropped_items").forGetter(result -> Optional.of(result.consumeDroppedItems))

            ).apply(instance, (resultOpt, damageHeldOpt, consumeOpt, popOpt, blockOpt, comsumeHeldItemsOpt, consumeDroppedItemsOpt) ->
                    new Result(
                            resultOpt.orElse(null),
                            damageHeldOpt.orElse(false),
                            consumeOpt.orElse(false),
                            popOpt.orElse(false),
                            blockOpt.orElse(null),
                            comsumeHeldItemsOpt.orElse(false),
                            consumeDroppedItemsOpt.orElse(false)
                    ))
    );
}