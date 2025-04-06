package com.benbenlaw.inworldrecipes.recipes;

import com.benbenlaw.core.recipe.NoInventoryRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

public record BlockConversionRecipe(String blockToConvert, String convertedBlock, boolean popBlock, boolean requiresSunlight, boolean requiresMoonlight, String dimension, int duration) implements Recipe<NoInventoryRecipe> {

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

    public Block getBlockToConvert() {
        if (!blockToConvert.contains("#")) {
            return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockToConvert));
        }
        return null;
    }

    public TagKey<Block> getBlockToConvertTag() {
        if (blockToConvert.contains("#")) {
            return TagKey.create(BuiltInRegistries.BLOCK.key(), ResourceLocation.parse(blockToConvert.replace("#", "")));
        }
        return null;
    }

    public Block getConvertedBlock() {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(convertedBlock));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BlockConversionRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return BlockConversionRecipe.Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Type implements RecipeType<BlockConversionRecipe> {
        private Type() {
        }

        public static final BlockConversionRecipe.Type INSTANCE = new BlockConversionRecipe.Type();
    }

    public static class Serializer implements RecipeSerializer<BlockConversionRecipe> {
        public static final BlockConversionRecipe.Serializer INSTANCE = new BlockConversionRecipe.Serializer();

        public final MapCodec<BlockConversionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("block_to_convert").forGetter(BlockConversionRecipe::blockToConvert),
                Codec.STRING.fieldOf("converted_block").forGetter(BlockConversionRecipe::convertedBlock),
                Codec.BOOL.optionalFieldOf("pop_block", false).forGetter(BlockConversionRecipe::popBlock),
                Codec.BOOL.optionalFieldOf("requires_sunlight", false).forGetter(BlockConversionRecipe::requiresSunlight),
                Codec.BOOL.optionalFieldOf("requires_moonlight", false).forGetter(BlockConversionRecipe::requiresMoonlight),
                Codec.STRING.optionalFieldOf("dimension", "none").forGetter(BlockConversionRecipe::dimension),
                Codec.INT.optionalFieldOf("duration", 200).forGetter(BlockConversionRecipe::duration)
        ).apply(instance, BlockConversionRecipe::new));

        private final StreamCodec<RegistryFriendlyByteBuf, BlockConversionRecipe> STREAM_CODEC = StreamCodec.of(
                BlockConversionRecipe.Serializer::write, BlockConversionRecipe.Serializer::read);

        @Override
        public @NotNull MapCodec<BlockConversionRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BlockConversionRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static BlockConversionRecipe read(RegistryFriendlyByteBuf buffer) {

            String blockToConvert = buffer.readUtf();
            String convertedBlock = buffer.readUtf();
            boolean popBlock = buffer.readBoolean();
            boolean requiresSunlight = buffer.readBoolean();
            boolean requiresMoonlight = buffer.readBoolean();
            String dimension = buffer.readUtf();
            int duration = buffer.readInt();

            return new BlockConversionRecipe(blockToConvert, convertedBlock, popBlock, requiresSunlight, requiresMoonlight, dimension, duration);
        }

        private static void write(RegistryFriendlyByteBuf buffer, BlockConversionRecipe recipe) {
            buffer.writeUtf(recipe.blockToConvert);
            buffer.writeUtf(recipe.convertedBlock);
            buffer.writeBoolean(recipe.popBlock);
            buffer.writeBoolean(recipe.requiresSunlight);
            buffer.writeBoolean(recipe.requiresMoonlight);
            buffer.writeUtf(recipe.dimension);
            buffer.writeInt(recipe.duration);
        }
    }
}

