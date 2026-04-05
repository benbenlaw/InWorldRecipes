package com.benbenlaw.inworldrecipes.recipe;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.core.recipe.NoInventoryRecipe;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionType;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultType;
import com.benbenlaw.inworldrecipes.recipe.world.result.type.ChanceResultsResult;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public record WorldRecipe(
        Recipe.CommonInfo commonInfo,
        List<IRecipeTrigger> triggers, // Updated to IRecipeTrigger
        List<IRecipeCondition> conditions,
        List<IRecipeResult> results,
        List<Option> options

) implements Recipe<NoInventoryRecipe> {

    public static final RecipeType<WorldRecipe> TYPE = new RecipeType<>() {};

    public static final MapCodec<WorldRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Recipe.CommonInfo.MAP_CODEC.forGetter(WorldRecipe::commonInfo),
                    TriggerType.CODEC.listOf().fieldOf("triggers").forGetter(WorldRecipe::triggers), // Modular Triggers
                    ConditionType.CODEC.listOf().fieldOf("conditions").forGetter(WorldRecipe::conditions),
                    ResultType.CODEC.listOf().fieldOf("results").forGetter(WorldRecipe::results),
                    Codec.list(Option.CODEC).optionalFieldOf("options", List.of()).forGetter(WorldRecipe::options)
            ).apply(instance, WorldRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, WorldRecipe> STREAM_CODEC =
            StreamCodec.of(WorldRecipe::write, WorldRecipe::read);

    public static final RecipeSerializer<WorldRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public boolean matches(NoInventoryRecipe recipeInput, Level level) {
        return true;
    }

    @Override
    public ItemStack assemble(NoInventoryRecipe noInventoryRecipe) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<NoInventoryRecipe>> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<NoInventoryRecipe>> getType() {
        return TYPE;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return commonInfo.showNotification();
    }

    @Override
    public String group() {
        return "";
    }

    /**
     * Iterates through results to find ChanceResults and roll items.
     */
    public List<ItemStack> rollResults(RandomSource rand) {
        List<ItemStack> resultsList = new ArrayList<>();
        for (IRecipeResult result : results) {
            if (result instanceof ChanceResultsResult chanceResults) {
                for (ChanceResult chance : chanceResults.results()) {
                    ItemStack stack = chance.rollOutput(rand);
                    if (!stack.isEmpty()) {
                        resultsList.add(stack);
                    }
                }
            }
        }
        return resultsList;
    }

    private static WorldRecipe read(RegistryFriendlyByteBuf buffer) {
        Recipe.CommonInfo commonInfo = Recipe.CommonInfo.STREAM_CODEC.decode(buffer);

        // Use the list codec provided by Minecraft/NeoForge
        List<IRecipeTrigger> triggers = TriggerType.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
        List<IRecipeCondition> conditions = ConditionType.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
        List<IRecipeResult> results = ResultType.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);

        // Options is now empty or removed
        return new WorldRecipe(commonInfo, triggers, conditions, results, List.of());
    }

    private static void write(RegistryFriendlyByteBuf buffer, WorldRecipe recipe) {
        Recipe.CommonInfo.STREAM_CODEC.encode(buffer, recipe.commonInfo());

        // Use the list codec to encode
        TriggerType.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.triggers());
        ConditionType.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.conditions());
        ResultType.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.results());
    }

}