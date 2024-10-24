package com.benbenlaw.inworldrecipes.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

public record RightClickOnBlockTransformsBlockRecipe(SizedIngredient heldItem, Ingredient targetBlock, boolean damageHeldItem, boolean consumeHeldItem, String newBlock) implements Recipe<NoInventoryRecipe> {


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
        return RightClickOnBlockTransformsBlockRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return RightClickOnBlockTransformsBlockRecipe.Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Type implements RecipeType<RightClickOnBlockTransformsBlockRecipe> {
        private Type() { }
        public static final RightClickOnBlockTransformsBlockRecipe.Type INSTANCE = new RightClickOnBlockTransformsBlockRecipe.Type();
    }

    public static class Serializer implements RecipeSerializer<RightClickOnBlockTransformsBlockRecipe> {
        public static final RightClickOnBlockTransformsBlockRecipe.Serializer INSTANCE = new Serializer();

        public final MapCodec<RightClickOnBlockTransformsBlockRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(
                        SizedIngredient.FLAT_CODEC.fieldOf("held_item").forGetter(RightClickOnBlockTransformsBlockRecipe::heldItem),
                        Ingredient.CODEC.fieldOf("target_block").forGetter(RightClickOnBlockTransformsBlockRecipe::targetBlock),
                        Codec.BOOL.fieldOf("damage_held_item").forGetter(RightClickOnBlockTransformsBlockRecipe::damageHeldItem),
                        Codec.BOOL.fieldOf("consume_held_item").forGetter(RightClickOnBlockTransformsBlockRecipe::consumeHeldItem),
                        Codec.STRING.fieldOf("new_block").forGetter(RightClickOnBlockTransformsBlockRecipe::newBlock)
                ).apply(instance, RightClickOnBlockTransformsBlockRecipe::new)
        );

        private final StreamCodec<RegistryFriendlyByteBuf, RightClickOnBlockTransformsBlockRecipe> STREAM_CODEC = StreamCodec.of(
                RightClickOnBlockTransformsBlockRecipe.Serializer::write, RightClickOnBlockTransformsBlockRecipe.Serializer::read);

        @Override
        public @NotNull MapCodec<RightClickOnBlockTransformsBlockRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, RightClickOnBlockTransformsBlockRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static RightClickOnBlockTransformsBlockRecipe read(RegistryFriendlyByteBuf buffer) {

            SizedIngredient heldItem = SizedIngredient.STREAM_CODEC.decode(buffer);
            Ingredient targetBlock = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            boolean damageHeldItem = buffer.readBoolean();
            boolean consumeHeldItem = buffer.readBoolean();
            String newBlock = buffer.readUtf();
            return new RightClickOnBlockTransformsBlockRecipe(heldItem, targetBlock, damageHeldItem, consumeHeldItem, newBlock);
        }

        private static void write(RegistryFriendlyByteBuf buffer, RightClickOnBlockTransformsBlockRecipe recipe) {
            SizedIngredient.STREAM_CODEC.encode(buffer, recipe.heldItem);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.targetBlock);
            buffer.writeBoolean(recipe.damageHeldItem);
            buffer.writeBoolean(recipe.consumeHeldItem);
            buffer.writeUtf(recipe.newBlock);
        }
    }
}
