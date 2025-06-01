package com.benbenlaw.inworldrecipes.recipes;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.core.recipe.NoInventoryRecipe;
import com.benbenlaw.inworldrecipes.util.ClickType;
import com.benbenlaw.inworldrecipes.util.ClickTypeCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record BlockInteractionRecipe(
        ClickType clickType,
        BlockState targetBlockState,
        SizedIngredient heldItem,
        BlockState outputBlockState,
        NonNullList<ChanceResult> chanceResults,
        boolean damageHeldItem,
        boolean consumeHeldItem,
        boolean popItems

) implements Recipe<NoInventoryRecipe> {

    @Override
    public boolean matches(NoInventoryRecipe p_346065_, Level p_345375_) {
        return true;
    }

    @Override
    public ItemStack assemble(NoInventoryRecipe p_345149_, HolderLookup.Provider p_346030_) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider p_336125_) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BlockInteractionRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return BlockInteractionRecipe.Type.INSTANCE;
    }

    public NonNullList<ChanceResult> getRollResults() {
        return this.chanceResults;
    }
    public List<ItemStack> rollResults(RandomSource rand) {
        List<ItemStack> results = new ArrayList<>();
        List<ChanceResult> rollResults = getRollResults();
        for (ChanceResult output : rollResults) {
            ItemStack stack = output.rollOutput(rand);
            if (!stack.isEmpty())
                results.add(stack);
        }
        return results;
    }


    @Override
    public boolean isSpecial() {
        return true;
    }
    public static class Type implements RecipeType<BlockInteractionRecipe> {
        private Type() { }
        public static final BlockInteractionRecipe.Type INSTANCE = new BlockInteractionRecipe.Type();
    }

    public static class Serializer implements RecipeSerializer<BlockInteractionRecipe> {
        public static final BlockInteractionRecipe.Serializer INSTANCE = new BlockInteractionRecipe.Serializer();

        public final MapCodec<BlockInteractionRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(
                        ClickTypeCodec.CLICK_TYPE_CODEC.fieldOf("click_type").forGetter(BlockInteractionRecipe::clickType),
                        BlockState.CODEC.fieldOf("target_block_state").forGetter(BlockInteractionRecipe::targetBlockState),
                        SizedIngredient.FLAT_CODEC.fieldOf("held_item").forGetter(BlockInteractionRecipe::heldItem),
                        BlockState.CODEC.optionalFieldOf("output_block_state").forGetter(r -> Optional.ofNullable(r.outputBlockState)),
                        Codec.list(ChanceResult.CODEC)
                                .optionalFieldOf("results", List.of()).flatXmap(
                                        list -> {
                                            NonNullList<ChanceResult> nonNullList = NonNullList.create();
                                            nonNullList.addAll(list);
                                            return DataResult.success(nonNullList);
                                        },
                                        DataResult::success
                                ).forGetter(BlockInteractionRecipe::getRollResults),
                        Codec.BOOL.fieldOf("damage_held_item").forGetter(BlockInteractionRecipe::damageHeldItem),
                        Codec.BOOL.fieldOf("consume_held_item").forGetter(BlockInteractionRecipe::consumeHeldItem),
                        Codec.BOOL.fieldOf("pop_items").forGetter(BlockInteractionRecipe::popItems)

                ).apply(instance, (clickType, blockState, heldItem, outputOpt, results, damageHeldItem, consumeHeldItem, popItems) ->
                        new BlockInteractionRecipe(clickType, blockState, heldItem, outputOpt.orElse(null), results, damageHeldItem, consumeHeldItem, popItems)
                )
        );

        private final StreamCodec<RegistryFriendlyByteBuf, BlockInteractionRecipe> STREAM_CODEC = StreamCodec.of(
                BlockInteractionRecipe.Serializer::write, BlockInteractionRecipe.Serializer::read);

        @Override
        public @NotNull MapCodec<BlockInteractionRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BlockInteractionRecipe> streamCodec() {
            return STREAM_CODEC;
        }


        private static BlockInteractionRecipe read(RegistryFriendlyByteBuf buffer) {

            ClickType clickType = ClickTypeCodec.readFromBuffer(buffer);
            BlockState blockState = Block.stateById(buffer.readInt());
            SizedIngredient heldItem = SizedIngredient.STREAM_CODEC.decode(buffer);
            BlockState outputBlockState = null;
            if (buffer.readBoolean()) {
                outputBlockState = Block.stateById(buffer.readInt());
            }
            int size = buffer.readVarInt();
            NonNullList<ChanceResult> outputs = NonNullList.withSize(size, ChanceResult.EMPTY);
            outputs.replaceAll(ignored -> ChanceResult.read(buffer));
            boolean damageHeldItem = buffer.readBoolean();
            boolean consumeHeldItem = buffer.readBoolean();
            boolean popItem = buffer.readBoolean();

            return new BlockInteractionRecipe(clickType, blockState, heldItem, outputBlockState, outputs, damageHeldItem, consumeHeldItem, popItem);
        }

        private static void write(RegistryFriendlyByteBuf buffer, BlockInteractionRecipe recipe) {

            ClickTypeCodec.writeToBuffer(buffer, recipe.clickType);
            buffer.writeInt(Block.getId(recipe.targetBlockState));
            SizedIngredient.STREAM_CODEC.encode(buffer, recipe.heldItem);
            buffer.writeBoolean(recipe.outputBlockState != null);
            if (recipe.outputBlockState != null) {
                buffer.writeInt(Block.getId(recipe.outputBlockState));
            }
            buffer.writeVarInt(recipe.chanceResults.size());
            for (ChanceResult output : recipe.chanceResults) {
                output.write(buffer);
            }
            buffer.writeBoolean(recipe.damageHeldItem);
            buffer.writeBoolean(recipe.consumeHeldItem);
            buffer.writeBoolean(recipe.popItems);


        }

    }
}
