package com.benbenlaw.inworldrecipes.recipe.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;

public final class CauldronFluids {

    private CauldronFluids() {}

    @Nullable
    public static Fluid getFluid(Block block) {
        if (block == Blocks.WATER_CAULDRON) return Fluids.WATER;
        if (block == Blocks.LAVA_CAULDRON) return Fluids.LAVA;
        return null;
    }

    public static boolean isCauldronWithFluid(BlockState state) {
        return getFluid(state.getBlock()) != null;
    }

    public static int getLevel(BlockState state) {
        return state.hasProperty(LayeredCauldronBlock.LEVEL)
                ? state.getValue(LayeredCauldronBlock.LEVEL)
                : LayeredCauldronBlock.MAX_FILL_LEVEL;
    }

    /**
     * Lowers the cauldron's fluid level by {@code amount}, emptying it back to a plain cauldron
     * once the level reaches zero. Lava cauldrons have no partial levels, so any consumption empties them.
     */
    public static void consume(BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, int amount) {
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
