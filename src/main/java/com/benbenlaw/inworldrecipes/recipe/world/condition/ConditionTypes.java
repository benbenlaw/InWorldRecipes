package com.benbenlaw.inworldrecipes.recipe.world.condition;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipe.world.condition.type.*;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ConditionTypes {

    public static final ResourceKey<Registry<ConditionType<?>>> KEY =
            ResourceKey.createRegistryKey(InWorldRecipes.identifier("recipe_conditions"));

    public static final DeferredRegister<ConditionType<?>> CONDITION_TYPES =
            createConditionRegister(InWorldRecipes.MOD_ID);

    public static final Registry<ConditionType<?>> REGISTRY =
            CONDITION_TYPES.makeRegistry(builder -> builder.sync(true));

    public static DeferredRegister<ConditionType<?>> createConditionRegister(String modId) {
        return DeferredRegister.create(KEY, modId);
    }


    //In World Recipes Conditions
    public static final Supplier<ConditionType<HeldItemCondition>> HELD_ITEM =
            register("held_item", HeldItemCondition.CODEC, HeldItemCondition.STREAM_CODEC);

    public static final Supplier<ConditionType<DroppedItemsCondition>> DROPPED_ITEMS =
            register("dropped_items", DroppedItemsCondition.CODEC, DroppedItemsCondition.STREAM_CODEC);

    public static final Supplier<ConditionType<InventoryItemsCondition>> INVENTORY_ITEMS =
            register("inventory_items", InventoryItemsCondition.CODEC, InventoryItemsCondition.STREAM_CODEC);

    public static final Supplier<ConditionType<WeatherCondition>> WEATHER =
            register("weather", WeatherCondition.CODEC, WeatherCondition.STREAM_CODEC);

    public static final Supplier<ConditionType<EntityCondition>> ENTITY =
            register("entity", EntityCondition.CODEC, EntityCondition.STREAM_CODEC);



    private static <T extends IRecipeCondition> Supplier<ConditionType<T>> register(String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return CONDITION_TYPES.register(name, () -> new ConditionType<>(codec, streamCodec));
    }
}