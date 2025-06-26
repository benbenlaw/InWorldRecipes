package com.benbenlaw.inworldrecipes.recipes;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.core.recipe.NoInventoryRecipe;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;
import org.jline.terminal.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record DropItemInFluidRecipe(NonNullList<SizedIngredient> droppedItems, String fluid, boolean consumeFluidBlock, NonNullList<ChanceResult> chanceResults) implements Recipe<NoInventoryRecipe> {

    @Override
    public boolean matches(NoInventoryRecipe p_346065_, Level p_345375_) {
        return true;
    }

    @Override
    public ItemStack assemble(NoInventoryRecipe p_345149_, HolderLookup.Provider p_346030_) {
        return chanceResults.get(0).stack().copy();
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider p_336125_) {
        return chanceResults.get(0).stack();
    }

    public List<ItemStack> getResults() {
        return getRollResults().stream()
                .map(ChanceResult::stack)
                .collect(Collectors.toList());
    }

    public NonNullList<ChanceResult> getRollResults() {
        return this.chanceResults;
    }
    public NonNullList<SizedIngredient> getDroppedItems() {
        return this.droppedItems;
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
    public RecipeSerializer<?> getSerializer() {
        return DropItemInFluidRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return DropItemInFluidRecipe.Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Type implements RecipeType<DropItemInFluidRecipe> {
        private Type() { }
        public static final DropItemInFluidRecipe.Type INSTANCE = new DropItemInFluidRecipe.Type();
    }

    public static class Serializer implements RecipeSerializer<DropItemInFluidRecipe> {
        public static final DropItemInFluidRecipe.Serializer INSTANCE = new Serializer();

        public final MapCodec<DropItemInFluidRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(
                        Codec.list(SizedIngredient.FLAT_CODEC).fieldOf("dropped_items").flatXmap(sizedIngredients -> {
                            NonNullList<SizedIngredient> nonNullList = NonNullList.create();
                            nonNullList.addAll(sizedIngredients);
                            return DataResult.success(nonNullList);
                        }, DataResult::success).forGetter(DropItemInFluidRecipe::getDroppedItems),
                        Codec.STRING.fieldOf("fluid").forGetter(DropItemInFluidRecipe::fluid),
                        Codec.BOOL.fieldOf("consume_fluid").forGetter(DropItemInFluidRecipe::consumeFluidBlock),
                        Codec.list(ChanceResult.CODEC).fieldOf("results").flatXmap(chanceResults -> {
                            NonNullList<ChanceResult> nonNullList = NonNullList.create();
                            nonNullList.addAll(chanceResults);
                            return DataResult.success(nonNullList);
                        }, DataResult::success).forGetter(DropItemInFluidRecipe::getRollResults)
                ).apply(instance, DropItemInFluidRecipe::new)
        );

        private final StreamCodec<RegistryFriendlyByteBuf, DropItemInFluidRecipe> STREAM_CODEC = StreamCodec.of(
                DropItemInFluidRecipe.Serializer::write, DropItemInFluidRecipe.Serializer::read);

        @Override
        public @NotNull MapCodec<DropItemInFluidRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, DropItemInFluidRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static DropItemInFluidRecipe read(RegistryFriendlyByteBuf buffer) {
            int droppedItemSize = buffer.readVarInt();
            NonNullList<SizedIngredient> droppedItems = NonNullList.create();
            for (int i = 0; i < droppedItemSize; i++) {
                droppedItems.add(SizedIngredient.STREAM_CODEC.decode(buffer));
            }

            String fluid = buffer.readUtf();
            boolean consumeFluidBlock = buffer.readBoolean();

            int size = buffer.readVarInt();
            NonNullList<ChanceResult> outputs = NonNullList.create();
            for (int i = 0; i < size; i++) {
                outputs.add(ChanceResult.read(buffer));
            }

            return new DropItemInFluidRecipe(droppedItems, fluid, consumeFluidBlock, outputs);
        }


        private static void write(RegistryFriendlyByteBuf buffer, DropItemInFluidRecipe recipe) {
            buffer.writeVarInt(recipe.droppedItems.size());
            for (SizedIngredient output : recipe.droppedItems) {
                SizedIngredient.STREAM_CODEC.encode(buffer, output);
            }
            buffer.writeUtf(recipe.fluid);
            buffer.writeBoolean(recipe.consumeFluidBlock);
            buffer.writeVarInt(recipe.chanceResults.size());
            for (ChanceResult output : recipe.chanceResults) {
                output.write(buffer);
            }
        }
    }
}
