package com.benbenlaw.inworldrecipes.recipe.world.trigger;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipe.world.condition.type.EntityCondition;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.type.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TriggerTypes {

    public static final ResourceKey<Registry<TriggerType<?>>> KEY =
            ResourceKey.createRegistryKey(InWorldRecipes.identifier("recipe_triggers"));

    public static final DeferredRegister<TriggerType<?>> TRIGGER_TYPES =
            createTriggerRegister(InWorldRecipes.MOD_ID);

    public static final Registry<TriggerType<?>> REGISTRY =
            TRIGGER_TYPES.makeRegistry(builder -> builder.sync(true));

    public static DeferredRegister<TriggerType<?>> createTriggerRegister(String modId) {
        return DeferredRegister.create(KEY, modId);
    }

    //In World Recipes Triggers
    public static final Supplier<TriggerType<BlockLandedTrigger>> BLOCK_LANDED =
            register("block_landed", BlockLandedTrigger.CODEC, BlockLandedTrigger.STREAM_CODEC);

    public static final Supplier<TriggerType<BlockTargetTrigger>> BLOCK_TARGET =
            register("block_target", BlockTargetTrigger.CODEC, BlockTargetTrigger.STREAM_CODEC);

    public static final Supplier<TriggerType<ClickTrigger>> CLICK_TYPE =
            register("click_type", ClickTrigger.CODEC, ClickTrigger.STREAM_CODEC);

    public static final Supplier<TriggerType<LightningTrigger>> LIGHTNING =
            register("lightning", LightningTrigger.CODEC, LightningTrigger.STREAM_CODEC);

    public static final Supplier<TriggerType<StandingOnTrigger>> STANDING_ON =
            register("standing_on", StandingOnTrigger.CODEC, StandingOnTrigger.STREAM_CODEC);

    public static final Supplier<TriggerType<ChatMessageTrigger>> CHAT_MESSAGE =
            register("chat_message", ChatMessageTrigger.CODEC, ChatMessageTrigger.STREAM_CODEC);


    private static <T extends IRecipeTrigger> Supplier<TriggerType<T>> register(String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return TRIGGER_TYPES.register(name, () -> new TriggerType<>(codec, streamCodec));
    }
}
