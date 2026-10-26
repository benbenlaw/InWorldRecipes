package com.benbenlaw.inworldrecipes.recipe.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import javax.annotation.Nullable;

public final class CauldronContents {

    private static final Identifier GLACIAL_BASIN = Identifier.fromNamespaceAndPath("auroral", "glacial_basin");
    private static final String AURA_LEVEL = "aura_level";

    private CauldronContents() {}

    @Nullable
    public static CauldronContent getContent(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.WATER_CAULDRON) return CauldronContent.WATER;
        if (block == Blocks.LAVA_CAULDRON) return CauldronContent.LAVA;
        if (block == Blocks.POWDER_SNOW_CAULDRON) return CauldronContent.POWDER_SNOW;
        if (isGlacialBasin(block) && getLevel(state) > 0) return CauldronContent.LIQUID_AURA;
        return null;
    }

    public static boolean isCauldronWithContent(BlockState state) {
        return getContent(state) != null;
    }

    public static int getLevel(BlockState state) {
        IntegerProperty aura = auraLevelProperty(state);
        if (aura != null) return state.getValue(aura);

        return state.hasProperty(LayeredCauldronBlock.LEVEL)
                ? state.getValue(LayeredCauldronBlock.LEVEL)
                : LayeredCauldronBlock.MAX_FILL_LEVEL;
    }

    public static void consume(BlockState state, Level level, BlockPos pos, int amount) {
        IntegerProperty aura = auraLevelProperty(state);
        if (aura != null) {
            level.setBlockAndUpdate(pos, state.setValue(aura, Math.max(0, state.getValue(aura) - amount)));
            return;
        }

        if (!state.hasProperty(LayeredCauldronBlock.LEVEL)) {
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
            return;
        }

        int newLevel = getLevel(state) - amount;
        if (newLevel <= 0) {
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
        } else {
            level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL, newLevel));
        }
    }

    public static ItemStack getIcon(CauldronContent content) {
        return switch (content) {
            case WATER -> new ItemStack(Items.WATER_BUCKET);
            case LAVA -> new ItemStack(Items.LAVA_BUCKET);
            case POWDER_SNOW -> new ItemStack(Items.POWDER_SNOW_BUCKET);
            case LIQUID_AURA -> BuiltInRegistries.ITEM.getOptional(GLACIAL_BASIN)
                    .map(ItemStack::new)
                    .orElse(ItemStack.EMPTY);
        };
    }

    private static boolean isGlacialBasin(Block block) {
        return BuiltInRegistries.BLOCK.getOptional(GLACIAL_BASIN).filter(basin -> basin == block).isPresent();
    }

    @Nullable
    private static IntegerProperty auraLevelProperty(BlockState state) {
        if (!isGlacialBasin(state.getBlock())) return null;
        return state.getBlock().getStateDefinition().getProperty(AURA_LEVEL) instanceof IntegerProperty property ? property : null;
    }
}
