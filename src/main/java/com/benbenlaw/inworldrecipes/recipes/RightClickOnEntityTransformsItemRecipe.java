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
import java.util.stream.Collectors;

public record RightClickOnEntityTransformsItemRecipe(SizedIngredient heldItem, String entity, boolean damageHeldItem, boolean consumeHeldItem, boolean destroyEntity, boolean popItem, NonNullList<ChanceResult> chanceResults) implements Recipe<NoInventoryRecipe> {

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
        return RightClickOnEntityTransformsItemRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return RightClickOnEntityTransformsItemRecipe.Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Type implements RecipeType<RightClickOnEntityTransformsItemRecipe> {
        private Type() { }
        public static final RightClickOnEntityTransformsItemRecipe.Type INSTANCE = new RightClickOnEntityTransformsItemRecipe.Type();
    }

    public static class Serializer implements RecipeSerializer<RightClickOnEntityTransformsItemRecipe> {
        public static final RightClickOnEntityTransformsItemRecipe.Serializer INSTANCE = new Serializer();

        public final MapCodec<RightClickOnEntityTransformsItemRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(
                        SizedIngredient.FLAT_CODEC.fieldOf("held_item").forGetter(RightClickOnEntityTransformsItemRecipe::heldItem),
                        Codec.STRING.fieldOf("entity").forGetter(RightClickOnEntityTransformsItemRecipe::entity),
                        Codec.BOOL.fieldOf("damage_held_item").forGetter(RightClickOnEntityTransformsItemRecipe::damageHeldItem),
                        Codec.BOOL.fieldOf("consume_held_item").forGetter(RightClickOnEntityTransformsItemRecipe::consumeHeldItem),
                        Codec.BOOL.fieldOf("destroy_entity").forGetter(RightClickOnEntityTransformsItemRecipe::destroyEntity),
                        Codec.BOOL.fieldOf("pop_item").forGetter(RightClickOnEntityTransformsItemRecipe::popItem),
                        Codec.list(ChanceResult.CODEC).fieldOf("results").flatXmap(chanceResults -> {
                            NonNullList<ChanceResult> nonNullList = NonNullList.create();
                            nonNullList.addAll(chanceResults);
                            return DataResult.success(nonNullList);
                        }, DataResult::success).forGetter(RightClickOnEntityTransformsItemRecipe::getRollResults)
                ).apply(instance, RightClickOnEntityTransformsItemRecipe::new)
        );

        private final StreamCodec<RegistryFriendlyByteBuf, RightClickOnEntityTransformsItemRecipe> STREAM_CODEC = StreamCodec.of(
                RightClickOnEntityTransformsItemRecipe.Serializer::write, RightClickOnEntityTransformsItemRecipe.Serializer::read);

        @Override
        public @NotNull MapCodec<RightClickOnEntityTransformsItemRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, RightClickOnEntityTransformsItemRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static RightClickOnEntityTransformsItemRecipe read(RegistryFriendlyByteBuf buffer) {

            SizedIngredient heldItem = SizedIngredient.STREAM_CODEC.decode(buffer);
            String entity = buffer.readUtf();
            boolean damageHeldItem = buffer.readBoolean();
            boolean consumeHeldItem = buffer.readBoolean();
            boolean destroyEntity = buffer.readBoolean();
            boolean popItem = buffer.readBoolean();
            int size = buffer.readVarInt();
            NonNullList<ChanceResult> outputs = NonNullList.withSize(size, ChanceResult.EMPTY);
            outputs.replaceAll(ignored -> ChanceResult.read(buffer));
            
            return new RightClickOnEntityTransformsItemRecipe(heldItem, entity, damageHeldItem, consumeHeldItem, destroyEntity, popItem, outputs);
        }

        private static void write(RegistryFriendlyByteBuf buffer, RightClickOnEntityTransformsItemRecipe recipe) {
            SizedIngredient.STREAM_CODEC.encode(buffer, recipe.heldItem);
            buffer.writeUtf(recipe.entity);
            buffer.writeBoolean(recipe.damageHeldItem);
            buffer.writeBoolean(recipe.consumeHeldItem);
            buffer.writeBoolean(recipe.destroyEntity);
            buffer.writeBoolean(recipe.popItem);
            buffer.writeVarInt(recipe.chanceResults.size());
            for (ChanceResult output : recipe.chanceResults) {
                output.write(buffer);
            }
        }
    }
}
