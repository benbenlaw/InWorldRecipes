package com.benbenlaw.inworldrecipes.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

public record RightClickOnBlockTransformsItemRecipe(SizedIngredient heldItem, Ingredient targetBlock, boolean damageHeldItem, boolean consumeHeldItem, boolean destroyTargetBlock, boolean popItem, ItemStack resultItem) implements Recipe<NoInventoryRecipe> {

    @Override
    public boolean matches(NoInventoryRecipe p_346065_, Level p_345375_) {
        return true;
    }

    @Override
    public ItemStack assemble(NoInventoryRecipe p_345149_, HolderLookup.Provider p_346030_) {
        return resultItem;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider p_336125_) {
        return resultItem;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RightClickOnBlockTransformsItemRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return RightClickOnBlockTransformsItemRecipe.Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Type implements RecipeType<RightClickOnBlockTransformsItemRecipe> {
        private Type() { }
        public static final RightClickOnBlockTransformsItemRecipe.Type INSTANCE = new RightClickOnBlockTransformsItemRecipe.Type();
    }

    public static class Serializer implements RecipeSerializer<RightClickOnBlockTransformsItemRecipe> {
        public static final RightClickOnBlockTransformsItemRecipe.Serializer INSTANCE = new Serializer();

        public final MapCodec<RightClickOnBlockTransformsItemRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(
                        SizedIngredient.FLAT_CODEC.fieldOf("held_item").forGetter(RightClickOnBlockTransformsItemRecipe::heldItem),
                        Ingredient.CODEC.fieldOf("target_block").forGetter(RightClickOnBlockTransformsItemRecipe::targetBlock),
                        Codec.BOOL.fieldOf("damage_held_item").forGetter(RightClickOnBlockTransformsItemRecipe::damageHeldItem),
                        Codec.BOOL.fieldOf("consume_held_item").forGetter(RightClickOnBlockTransformsItemRecipe::consumeHeldItem),
                        Codec.BOOL.fieldOf("destroy_target_block").forGetter(RightClickOnBlockTransformsItemRecipe::destroyTargetBlock),
                        Codec.BOOL.fieldOf("pop_item").forGetter(RightClickOnBlockTransformsItemRecipe::popItem),
                        ItemStack.CODEC.fieldOf("result").forGetter(RightClickOnBlockTransformsItemRecipe::resultItem)
                ).apply(instance, RightClickOnBlockTransformsItemRecipe::new)
        );

        private final StreamCodec<RegistryFriendlyByteBuf, RightClickOnBlockTransformsItemRecipe> STREAM_CODEC = StreamCodec.of(
                RightClickOnBlockTransformsItemRecipe.Serializer::write, RightClickOnBlockTransformsItemRecipe.Serializer::read);

        @Override
        public @NotNull MapCodec<RightClickOnBlockTransformsItemRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, RightClickOnBlockTransformsItemRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static RightClickOnBlockTransformsItemRecipe read(RegistryFriendlyByteBuf buffer) {

            SizedIngredient heldItem = SizedIngredient.STREAM_CODEC.decode(buffer);
            Ingredient targetBlock = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            boolean damageHeldItem = buffer.readBoolean();
            boolean consumeHeldItem = buffer.readBoolean();
            boolean destroyTargetBlock = buffer.readBoolean();
            boolean popItem = buffer.readBoolean();
            ItemStack resultItem = ItemStack.STREAM_CODEC.decode(buffer);
            
            return new RightClickOnBlockTransformsItemRecipe(heldItem, targetBlock, damageHeldItem, consumeHeldItem, destroyTargetBlock, popItem, resultItem);
        }

        private static void write(RegistryFriendlyByteBuf buffer, RightClickOnBlockTransformsItemRecipe recipe) {
            SizedIngredient.STREAM_CODEC.encode(buffer, recipe.heldItem);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.targetBlock);
            buffer.writeBoolean(recipe.damageHeldItem);
            buffer.writeBoolean(recipe.consumeHeldItem);
            buffer.writeBoolean(recipe.destroyTargetBlock);
            buffer.writeBoolean(recipe.popItem);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.resultItem);
        }
    }
}
