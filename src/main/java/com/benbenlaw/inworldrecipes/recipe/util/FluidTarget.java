package com.benbenlaw.inworldrecipes.recipe.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public sealed interface FluidTarget {
    boolean matches(Fluid fluid);

    record Single(Fluid fluid) implements FluidTarget {
        @Override
        public boolean matches(Fluid fluid) {
            return this.fluid == fluid;
        }
    }

    record Tag(TagKey<Fluid> tag) implements FluidTarget {
        @Override
        @SuppressWarnings("deprecation")
        public boolean matches(Fluid fluid) {
            return fluid.is(tag);
        }
    }
}
