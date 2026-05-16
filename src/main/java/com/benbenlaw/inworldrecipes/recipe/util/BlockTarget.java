package com.benbenlaw.inworldrecipes.recipe.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public sealed interface BlockTarget {
    boolean matches(BlockState state, boolean ignoreBlockState);

    record Single(BlockState blockState) implements BlockTarget {

        @Override
        public boolean matches(BlockState state, boolean ignoreBlockState) {

            if (state.getBlock() != blockState.getBlock()) {
                return false;
            }

            if (ignoreBlockState) {
                return true;
            }

            for (Property<?> property : blockState.getProperties()) {

                if (!state.hasProperty(property)) {
                    return false;
                }

                Comparable<?> expected = blockState.getValue(property);
                Comparable<?> actual = state.getValue(property);

                if (!expected.equals(actual)) {
                    return false;
                }
            }

            return true;
        }
    }

    record Tag(TagKey<Block> tag) implements BlockTarget {
        @Override
        public boolean matches(BlockState state, boolean ignoreBlockState) {
            return state.is(tag);
        }
    }
}

