package com.benbenlaw.inworldrecipes.recipe.world.result;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipe.world.result.type.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ResultTypes {

    public static final ResourceKey<Registry<ResultType<?>>> KEY =
            ResourceKey.createRegistryKey(InWorldRecipes.identifier("recipe_results"));

    public static final DeferredRegister<ResultType<?>> RESULT_TYPES =
            createResultRegister(InWorldRecipes.MOD_ID);

    public static final Registry<ResultType<?>> REGISTRY =
            RESULT_TYPES.makeRegistry(builder -> builder.sync(true));

    public static DeferredRegister<ResultType<?>> createResultRegister(String modId) {
        return DeferredRegister.create(KEY, modId);
    }


    //In World Recipes Results
    public static final Supplier<ResultType<BlockStateResult>> BLOCK_STATE =
            register("block_state", BlockStateResult.CODEC, BlockStateResult.STREAM_CODEC);

    public static final Supplier<ResultType<ConsumeDroppedItemsResult>> CONSUME_DROPPED_ITEMS =
            register("consume_dropped_items", ConsumeDroppedItemsResult.CODEC, ConsumeDroppedItemsResult.STREAM_CODEC);

    public static final Supplier<ResultType<ConsumeHeldItemResult>> CONSUME_HELD_ITEM =
            register("consume_held_item", ConsumeHeldItemResult.CODEC, ConsumeHeldItemResult.STREAM_CODEC);

    public static final Supplier<ResultType<ConsumeInventoryItemsResult>> CONSUME_INVENTORY_ITEMS =
            register("consume_inventory_items", ConsumeInventoryItemsResult.CODEC, ConsumeInventoryItemsResult.STREAM_CODEC);

    public static final Supplier<ResultType<DamageHeldItemResult>> DAMAGE_HELD_ITEM =
            register("damage_held_item", DamageHeldItemResult.CODEC, DamageHeldItemResult.STREAM_CODEC);

    public static final Supplier<ResultType<ChanceResultsResult>> CHANCE_RESULTS =
            register("chance_results", ChanceResultsResult.CODEC, ChanceResultsResult.STREAM_CODEC);

    public static final Supplier<ResultType<MessageResult>> MESSAGE =
            register("message", MessageResult.CODEC, MessageResult.STREAM_CODEC);





    private static <T extends IRecipeResult> Supplier<ResultType<T>> register(String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return RESULT_TYPES.register(name, () -> new ResultType<>(codec, streamCodec));
    }
}