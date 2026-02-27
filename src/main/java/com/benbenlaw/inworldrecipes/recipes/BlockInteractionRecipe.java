package com.benbenlaw.inworldrecipes.recipes;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.core.recipe.NoInventoryRecipe;
import com.benbenlaw.inworldrecipes.util.ClickType;
import com.benbenlaw.inworldrecipes.util.ClickTypeCodec;
import net.minecraft.core.NonNullList;
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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record BlockInteractionRecipe(
        Recipe.CommonInfo commonInfo,
        ClickType clickType,
        BlockTarget targetBlock,
        SizedIngredient heldItem,
        @Nullable BlockState outputBlockState,
        NonNullList<ChanceResult> chanceResults,
        boolean damageHeldItem,
        boolean consumeHeldItem,
        boolean popItems,
        boolean ignoreBlockState
) implements Recipe<NoInventoryRecipe> {

    public static final RecipeType<BlockInteractionRecipe> TYPE = new RecipeType<>() {};

    public static final MapCodec<BlockInteractionRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Recipe.CommonInfo.MAP_CODEC.forGetter(BlockInteractionRecipe::commonInfo),
                    ClickTypeCodec.CLICK_TYPE_CODEC.fieldOf("click_type").forGetter(BlockInteractionRecipe::clickType),
                    BlockTargetCodec.CODEC.fieldOf("target_block_state").forGetter(BlockInteractionRecipe::targetBlock),
                    SizedIngredient.NESTED_CODEC.fieldOf("held_item").forGetter(BlockInteractionRecipe::heldItem),
                    BlockState.CODEC.optionalFieldOf("output_block_state")
                            .forGetter(r -> Optional.ofNullable(r.outputBlockState)),
                    Codec.list(ChanceResult.CODEC)
                            .optionalFieldOf("results", List.of())
                            .flatXmap(list -> {
                                NonNullList<ChanceResult> nonNullList = NonNullList.create();
                                nonNullList.addAll(list);
                                return DataResult.success(nonNullList);
                            }, DataResult::success)
                            .forGetter(BlockInteractionRecipe::chanceResults),
                    Codec.BOOL.fieldOf("damage_held_item").forGetter(BlockInteractionRecipe::damageHeldItem),
                    Codec.BOOL.fieldOf("consume_held_item").forGetter(BlockInteractionRecipe::consumeHeldItem),
                    Codec.BOOL.fieldOf("pop_items").forGetter(BlockInteractionRecipe::popItems),
                    Codec.BOOL.optionalFieldOf("ignore_block_state", false).forGetter(BlockInteractionRecipe::ignoreBlockState)
            ).apply(instance, (commonInfo, clickType, targetBlock, heldItem, outputOpt,
                               chanceResults, damage, consume, pop, ignore) ->
                    new BlockInteractionRecipe(
                            commonInfo,
                            clickType,
                            targetBlock,
                            heldItem,
                            outputOpt.orElse(null), // <--- Fix: convert Optional<BlockState> to BlockState
                            chanceResults,
                            damage,
                            consume,
                            pop,
                            ignore
                    )
            )
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockInteractionRecipe> STREAM_CODEC =
            StreamCodec.of(BlockInteractionRecipe::write, BlockInteractionRecipe::read);

    public static final RecipeSerializer<BlockInteractionRecipe> SERIALIZER =
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
        List<ItemStack> results = new ArrayList<>();
        for (ChanceResult output : chanceResults) {
            ItemStack stack = output.rollOutput(rand);
            if (!stack.isEmpty()) results.add(stack);
        }
        return results;
    }

    private static BlockInteractionRecipe read(RegistryFriendlyByteBuf buffer) {
        Recipe.CommonInfo commonInfo = Recipe.CommonInfo.STREAM_CODEC.decode(buffer);
        ClickType clickType = ClickTypeCodec.readFromBuffer(buffer);

        boolean isTag = buffer.readBoolean();
        BlockTarget targetBlock = isTag
                ? new BlockTarget.Tag(TagKey.create(Registries.BLOCK, buffer.readIdentifier()))
                : new BlockTarget.Single(Block.stateById(buffer.readInt()));

        SizedIngredient heldItem = SizedIngredient.STREAM_CODEC.decode(buffer);

        BlockState outputBlockState = buffer.readBoolean() ? Block.stateById(buffer.readInt()) : null;

        int size = buffer.readVarInt();
        NonNullList<ChanceResult> chanceResults = NonNullList.withSize(size, ChanceResult.EMPTY);
        chanceResults.replaceAll(i -> ChanceResult.read(buffer));

        boolean damageHeldItem = buffer.readBoolean();
        boolean consumeHeldItem = buffer.readBoolean();
        boolean popItems = buffer.readBoolean();
        boolean ignoreBlockState = buffer.readBoolean();

        return new BlockInteractionRecipe(commonInfo, clickType, targetBlock, heldItem, outputBlockState,
                chanceResults, damageHeldItem, consumeHeldItem, popItems, ignoreBlockState);
    }

    private static void write(RegistryFriendlyByteBuf buffer, BlockInteractionRecipe recipe) {
        Recipe.CommonInfo.STREAM_CODEC.encode(buffer, recipe.commonInfo);

        ClickTypeCodec.writeToBuffer(buffer, recipe.clickType);

        if (recipe.targetBlock instanceof BlockTarget.Tag tag) {
            buffer.writeBoolean(true);
            buffer.writeIdentifier(tag.tag().location());
        } else if (recipe.targetBlock instanceof BlockTarget.Single(BlockState blockState)) {
            buffer.writeBoolean(false);
            buffer.writeInt(Block.getId(blockState));
        }

        SizedIngredient.STREAM_CODEC.encode(buffer, recipe.heldItem);

        buffer.writeBoolean(recipe.outputBlockState != null);
        if (recipe.outputBlockState != null) buffer.writeInt(Block.getId(recipe.outputBlockState));

        buffer.writeVarInt(recipe.chanceResults.size());
        for (ChanceResult output : recipe.chanceResults) output.write(buffer);

        buffer.writeBoolean(recipe.damageHeldItem);
        buffer.writeBoolean(recipe.consumeHeldItem);
        buffer.writeBoolean(recipe.popItems);
        buffer.writeBoolean(recipe.ignoreBlockState);
    }
}