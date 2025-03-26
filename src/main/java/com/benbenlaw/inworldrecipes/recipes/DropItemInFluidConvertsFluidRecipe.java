package com.benbenlaw.inworldrecipes.recipes;

import com.benbenlaw.core.recipe.NoInventoryRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

public record DropItemInFluidConvertsFluidRecipe(SizedIngredient droppedItem, String fluid, String newFluid, boolean destroyItems) implements Recipe<NoInventoryRecipe> {

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
        return DropItemInFluidConvertsFluidRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return DropItemInFluidConvertsFluidRecipe.Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Type implements RecipeType<DropItemInFluidConvertsFluidRecipe> {
        private Type() { }
        public static final DropItemInFluidConvertsFluidRecipe.Type INSTANCE = new DropItemInFluidConvertsFluidRecipe.Type();
    }

    public static class Serializer implements RecipeSerializer<DropItemInFluidConvertsFluidRecipe> {
        public static final DropItemInFluidConvertsFluidRecipe.Serializer INSTANCE = new Serializer();

        public final MapCodec<DropItemInFluidConvertsFluidRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(
                        SizedIngredient.FLAT_CODEC.fieldOf("dropped_item").forGetter(DropItemInFluidConvertsFluidRecipe::droppedItem),
                        Codec.STRING.fieldOf("fluid").forGetter(DropItemInFluidConvertsFluidRecipe::fluid),
                        Codec.STRING.fieldOf("new_fluid").forGetter(DropItemInFluidConvertsFluidRecipe::newFluid),
                        Codec.BOOL.fieldOf("destroy_items").forGetter(DropItemInFluidConvertsFluidRecipe::destroyItems)
                ).apply(instance, DropItemInFluidConvertsFluidRecipe::new)
        );

        private final StreamCodec<RegistryFriendlyByteBuf, DropItemInFluidConvertsFluidRecipe> STREAM_CODEC = StreamCodec.of(
                DropItemInFluidConvertsFluidRecipe.Serializer::write, DropItemInFluidConvertsFluidRecipe.Serializer::read);

        @Override
        public @NotNull MapCodec<DropItemInFluidConvertsFluidRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, DropItemInFluidConvertsFluidRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static DropItemInFluidConvertsFluidRecipe read(RegistryFriendlyByteBuf buffer) {

            SizedIngredient droppedItem = SizedIngredient.STREAM_CODEC.decode(buffer);
            String fluid = buffer.readUtf();
            String newFluid = buffer.readUtf();
            boolean destroyItems = buffer.readBoolean();

            return new DropItemInFluidConvertsFluidRecipe(droppedItem, fluid, newFluid, destroyItems);
        }

        private static void write(RegistryFriendlyByteBuf buffer, DropItemInFluidConvertsFluidRecipe recipe) {
            SizedIngredient.STREAM_CODEC.encode(buffer, recipe.droppedItem);
            buffer.writeUtf(recipe.fluid);
            buffer.writeUtf(recipe.newFluid);
            buffer.writeBoolean(recipe.destroyItems);
        }
    }
}
