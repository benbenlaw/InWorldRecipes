package com.benbenlaw.inworldrecipes.recipes;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public sealed interface BlockTarget {
    boolean matches(BlockState state, boolean ignoreBlockState);

    record Single(BlockState blockState) implements BlockTarget {
        @Override
        public boolean matches(BlockState state, boolean ignoreBlockState) {
            if (state.getBlock() != blockState.getBlock()) return false;

            if (ignoreBlockState) {
                return true;
            } else {
                // Compare only the non-default properties
                BlockState defaultState = blockState.getBlock().defaultBlockState();
                for (Property<?> property : blockState.getProperties()) {
                    Comparable<?> recipeValue = blockState.getValue(property);
                    Comparable<?> defaultValue = defaultState.getValue(property);
                    Comparable<?> levelValue = state.getValue(property);

                    if (!recipeValue.equals(defaultValue)) {
                        if (!recipeValue.equals(levelValue)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
    }

    record Tag(TagKey<Block> tag) implements BlockTarget {
        @Override
        public boolean matches(BlockState state, boolean ignoreBlockState) {
            return state.is(tag);
        }
    }
}

