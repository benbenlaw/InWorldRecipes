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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record LightningCraftingRecipe(SizedIngredient droppedItem, NonNullList<ChanceResult> chanceResults) implements Recipe<NoInventoryRecipe> {

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
    public ItemStack getResultItem(HolderLookup.Provider p_336125_) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return LightningCraftingRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return LightningCraftingRecipe.Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Type implements RecipeType<LightningCraftingRecipe> {
        private Type() { }
        public static final LightningCraftingRecipe.Type INSTANCE = new LightningCraftingRecipe.Type();
    }

    public static class Serializer implements RecipeSerializer<LightningCraftingRecipe> {
        public static final LightningCraftingRecipe.Serializer INSTANCE = new Serializer();

        public final MapCodec<LightningCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(
                        SizedIngredient.FLAT_CODEC.fieldOf("dropped_item").forGetter(LightningCraftingRecipe::droppedItem),
                        Codec.list(ChanceResult.CODEC)
                                .optionalFieldOf("results", List.of()).flatXmap(
                                        list -> {
                                            NonNullList<ChanceResult> nonNullList = NonNullList.create();
                                            nonNullList.addAll(list);
                                            return DataResult.success(nonNullList);
                                        },
                                        DataResult::success
                                ).forGetter(LightningCraftingRecipe::getRollResults)
                ).apply(instance, LightningCraftingRecipe::new)
        );

        private final StreamCodec<RegistryFriendlyByteBuf, LightningCraftingRecipe> STREAM_CODEC = StreamCodec.of(
                LightningCraftingRecipe.Serializer::write, LightningCraftingRecipe.Serializer::read);

        @Override
        public @NotNull MapCodec<LightningCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, LightningCraftingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static LightningCraftingRecipe read(RegistryFriendlyByteBuf buffer) {

            SizedIngredient droppedItem = SizedIngredient.STREAM_CODEC.decode(buffer);
            int size = buffer.readVarInt();
            NonNullList<ChanceResult> outputs = NonNullList.withSize(size, ChanceResult.EMPTY);
            outputs.replaceAll(ignored -> ChanceResult.read(buffer));

            return new LightningCraftingRecipe(droppedItem, outputs);
        }

        private static void write(RegistryFriendlyByteBuf buffer, LightningCraftingRecipe recipe) {
            SizedIngredient.STREAM_CODEC.encode(buffer, recipe.droppedItem);
            buffer.writeVarInt(recipe.chanceResults.size());
            for (ChanceResult output : recipe.chanceResults) {
                output.write(buffer);
            }
        }
    }
}
