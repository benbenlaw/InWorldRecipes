package com.benbenlaw.inworldrecipes.recipes;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.core.recipe.NoInventoryRecipe;
import com.benbenlaw.inworldrecipes.util.ClickType;
import com.benbenlaw.inworldrecipes.util.ClickTypeCodec;
import com.benbenlaw.inworldrecipes.util.WeatherType;
import com.benbenlaw.inworldrecipes.util.WeatherTypeCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public record WorldRecipe(
        Recipe.CommonInfo commonInfo,
        List<Trigger> triggers,
        List<Condition> conditions,
        List<Result> results,
        List<Option> options

) implements Recipe<NoInventoryRecipe> {

    public static final RecipeType<WorldRecipe> TYPE = new RecipeType<>() {};

    public static final MapCodec<WorldRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Recipe.CommonInfo.MAP_CODEC.forGetter(WorldRecipe::commonInfo),
                    Codec.list(Trigger.CODEC).fieldOf("triggers").forGetter(WorldRecipe::triggers),
                    Codec.list(Condition.CODEC).fieldOf("conditions").forGetter(WorldRecipe::conditions),
                    Codec.list(Result.CODEC).fieldOf("results").forGetter(WorldRecipe::results),
                    Codec.list(Option.CODEC).optionalFieldOf("options", List.of()).forGetter(WorldRecipe::options)
            ).apply(instance, WorldRecipe::new
            )
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

    public List<ItemStack> rollResults(RandomSource rand) {
        List<ItemStack> resultsList = new ArrayList<>();

        for (Result result : results) {
            if (result.chanceResults() != null) {
                for (ChanceResult chance : result.chanceResults()) {
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
        // Read common info
        Recipe.CommonInfo commonInfo = Recipe.CommonInfo.STREAM_CODEC.decode(buffer);

        // Read triggers
        int triggerCount = buffer.readVarInt();
        List<Trigger> triggers = new ArrayList<>(triggerCount);
        for (int i = 0; i < triggerCount; i++) {
            // Optional ClickType
            ClickType clickType = buffer.readBoolean() ? ClickTypeCodec.readFromBuffer(buffer) : null;

            // Optional BlockTarget
            BlockTarget targetBlock = null;
            if (buffer.readBoolean()) { // targetBlock exists
                boolean isTag = buffer.readBoolean();
                if (isTag) {
                    targetBlock = new BlockTarget.Tag(TagKey.create(Registries.BLOCK, buffer.readIdentifier()));
                } else {
                    targetBlock = new BlockTarget.Single(Block.stateById(buffer.readInt()));
                }
            }

            boolean lightningStrike = buffer.readBoolean() && buffer.readBoolean();

            // Optional standingOnBlock
            BlockTarget standingOnBlock = null;
            if (buffer.readBoolean()) { // standingOnBlock exists
                boolean isTag = buffer.readBoolean();
                if (isTag) {
                    standingOnBlock = new BlockTarget.Tag(TagKey.create(Registries.BLOCK, buffer.readIdentifier()));
                } else {
                    standingOnBlock = new BlockTarget.Single(Block.stateById(buffer.readInt()));
                }
            }

            //Anvil Land
            boolean anvilLanded = buffer.readBoolean() && buffer.readBoolean();

            triggers.add(new Trigger(clickType, targetBlock, lightningStrike, standingOnBlock, anvilLanded));
        }

        // Read conditions
        int conditionCount = buffer.readVarInt();
        List<Condition> conditions = new ArrayList<>(conditionCount);
        for (int i = 0; i < conditionCount; i++) {
            // Optional held item
            SizedIngredient heldItem = buffer.readBoolean() ? SizedIngredient.STREAM_CODEC.decode(buffer) : null;

            // ignoreBlockState flag
            boolean ignoreBlockState = buffer.readBoolean();

            //Dropped Items
            List<SizedIngredient> droppedItems = null;
            if (buffer.readBoolean()) {
                int droppedCount = buffer.readVarInt();
                droppedItems = new ArrayList<>(droppedCount);
                for (int j = 0; j < droppedCount; j++) {
                    droppedItems.add(SizedIngredient.STREAM_CODEC.decode(buffer));
                }
            }

            //Held Items
            List<SizedIngredient> heldItems = null;
            if (buffer.readBoolean()) {
                int heldCount = buffer.readVarInt();
                heldItems = new ArrayList<>(heldCount);
                for (int j = 0; j < heldCount; j++) {
                    heldItems.add(SizedIngredient.STREAM_CODEC.decode(buffer));
                }
            }

            //Weather
            WeatherType weatherType = buffer.readBoolean() ? WeatherTypeCodec.readFromBuffer(buffer) : null;

            conditions.add(new Condition(heldItem, ignoreBlockState, droppedItems, heldItems, weatherType));

        }

        // Read results
        int resultCount = buffer.readVarInt();
        List<Result> results = new ArrayList<>(resultCount);
        for (int i = 0; i < resultCount; i++) {
            // Optional ChanceResult
            List<ChanceResult> chanceResults = null;
            if (buffer.readBoolean()) {
                int chanceCount = buffer.readVarInt();
                chanceResults = new ArrayList<>(chanceCount);
                for (int j = 0; j < chanceCount; j++) {
                    chanceResults.add(ChanceResult.read(buffer));
                }
            }

            // Per-result flags
            boolean damageHeldItem = buffer.readBoolean();
            boolean consumeHeldItem = buffer.readBoolean();
            boolean popItems = buffer.readBoolean();
            BlockState outputBlockState = buffer.readBoolean() ? Block.stateById(buffer.readInt()) : null;
            boolean consumeHeldItems = buffer.readBoolean();
            boolean consumeDroppedItems = buffer.readBoolean();

            results.add(new Result(chanceResults, damageHeldItem, consumeHeldItem, popItems, outputBlockState, consumeHeldItems, consumeDroppedItems));
        }

        //Read Options

        int optionCount = buffer.readVarInt();
        List<Option> options = new ArrayList<>(optionCount);
        for (int i = 0; i < optionCount; i++) {
            boolean showInJEI = buffer.readBoolean();
            boolean onlyVisualRecipe = buffer.readBoolean();

            options.add(new Option(showInJEI, onlyVisualRecipe));
        }


        // Return fully reconstructed recipe
        return new WorldRecipe(commonInfo, triggers, conditions, results, options);
    }

    private static void write(RegistryFriendlyByteBuf buffer, WorldRecipe recipe) {
        Recipe.CommonInfo.STREAM_CODEC.encode(buffer, recipe.commonInfo());

        // Write Triggers
        buffer.writeVarInt(recipe.triggers().size());
        for (Trigger trigger : recipe.triggers()) {

            // Optional ClickType
            buffer.writeBoolean(trigger.clickType() != null);
            if (trigger.clickType() != null) ClickTypeCodec.writeToBuffer(buffer, trigger.clickType());

            // Optional BlockTarget for clicking
            buffer.writeBoolean(trigger.targetBlock() != null);
            if (trigger.targetBlock() != null) {
                if (trigger.targetBlock() instanceof BlockTarget.Tag tag) {
                    buffer.writeBoolean(true);
                    buffer.writeIdentifier(tag.tag().location());
                } else if (trigger.targetBlock() instanceof BlockTarget.Single single) {
                    buffer.writeBoolean(false);
                    buffer.writeInt(Block.getId(single.blockState()));
                } else {
                    throw new IllegalStateException("Unknown BlockTarget type: " + trigger.targetBlock().getClass());
                }
            }

            // Optional Lightning strike trigger
            buffer.writeBoolean(trigger.lightningStrike() != null);
            if (trigger.lightningStrike() != null) buffer.writeBoolean(trigger.lightningStrike());

            // Optional BlockTarget for standing on
            buffer.writeBoolean(trigger.standingOnBlock() != null);
            if (trigger.standingOnBlock() != null) {
                if (trigger.standingOnBlock() instanceof BlockTarget.Tag tag) {
                    buffer.writeBoolean(true);
                    buffer.writeIdentifier(tag.tag().location());
                } else if (trigger.standingOnBlock() instanceof BlockTarget.Single single) {
                    buffer.writeBoolean(false);
                    buffer.writeInt(Block.getId(single.blockState()));
                } else {
                    throw new IllegalStateException("Unknown BlockTarget type: " + trigger.standingOnBlock().getClass());
                }
            }

            // Anvil Landed
            buffer.writeBoolean(trigger.anvilLanded() != null);
            if (trigger.anvilLanded() != null) buffer.writeBoolean(trigger.anvilLanded());

        }

        // Write conditions
        buffer.writeVarInt(recipe.conditions().size());
        for (Condition condition : recipe.conditions()) {
            buffer.writeBoolean(condition.heldItem() != null);
            if (condition.heldItem() != null) SizedIngredient.STREAM_CODEC.encode(buffer, condition.heldItem());
            buffer.writeBoolean(condition.ignoreBlockState());

            //Dropped items
            buffer.writeBoolean(condition.droppedItems() != null);
            if (condition.droppedItems() != null) {
                buffer.writeVarInt(condition.droppedItems().size());
                for (SizedIngredient dropped : condition.droppedItems()) {
                    SizedIngredient.STREAM_CODEC.encode(buffer, dropped);
                }
            }

            //Held items
            buffer.writeBoolean(condition.inventoryItems() != null);
            if (condition.inventoryItems() != null) {
                buffer.writeVarInt(condition.inventoryItems().size());
                for (SizedIngredient dropped : condition.inventoryItems()) {
                    SizedIngredient.STREAM_CODEC.encode(buffer, dropped);
                }
            }

            //Weather
            buffer.writeBoolean(condition.weatherType() != null);
            if (condition.weatherType() != null) WeatherTypeCodec.writeToBuffer(buffer, condition.weatherType());

        }

        // Write results
        buffer.writeVarInt(recipe.results().size());
        for (Result result : recipe.results()) {

            // Check if the ChanceResult exists
            buffer.writeBoolean(result.chanceResults() != null);
            if (result.chanceResults() != null) {
                buffer.writeVarInt(result.chanceResults().size());
                for (ChanceResult chance : result.chanceResults()) {
                    chance.write(buffer);
                }
            }

            // Write the flags for this result
            buffer.writeBoolean(result.damageHeldItem());
            buffer.writeBoolean(result.consumeHeldItem());
            buffer.writeBoolean(result.popItems());

            buffer.writeBoolean(result.outputBlockState() != null);
            if (result.outputBlockState() != null) buffer.writeInt(Block.getId(result.outputBlockState()));

            buffer.writeBoolean(result.consumeInventoryItems());
            buffer.writeBoolean(result.consumeDroppedItems());

        }

        //Write options
        buffer.writeVarInt(recipe.options().size());
        for (Option option : recipe.options()) {
            buffer.writeBoolean(option.showInJEI());
            buffer.writeBoolean(option.onlyVisualRecipe());
        }
    }
}