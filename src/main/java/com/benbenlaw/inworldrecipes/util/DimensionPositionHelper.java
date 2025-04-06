package com.benbenlaw.inworldrecipes.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record DimensionPositionHelper (ResourceKey<Level> dimension, BlockPos pos) {
}
