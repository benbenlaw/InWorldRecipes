package com.benbenlaw.inworldrecipes.recipe.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public final class CauldronContents {

    private CauldronContents() {}

    @Nullable
    public static CauldronContent getContent(Block block) {
        if (block == Blocks.WATER_CAULDRON) return CauldronContent.WATER;
        if (block == Blocks.LAVA_CAULDRON) return CauldronContent.LAVA;
        if (block == Blocks.POWDER_SNOW_CAULDRON) return CauldronContent.POWDER_SNOW;
        return null;
    }

    public static boolean isCauldronWithContent(BlockState state) {
        return getContent(state.getBlock()) != null;
    }

    public static int getLevel(BlockState state) {
        return state.hasProperty(LayeredCauldronBlock.LEVEL)
                ? state.getValue(LayeredCauldronBlock.LEVEL)
                : LayeredCauldronBlock.MAX_FILL_LEVEL;
    }

    /**
     * Lowers the cauldron's level by {@code amount}, emptying it back to a plain cauldron
     * once the level reaches zero. Lava cauldrons have no partial levels, so any consumption empties them.
     */
    public static void consume(BlockState state, Level level, BlockPos pos, int amount) {
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
}
