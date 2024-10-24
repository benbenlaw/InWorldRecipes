package com.benbenlaw.inworldrecipes.recipes;

import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class NoInventoryRecipe extends RecipeWrapper implements RecipeInput {
    public static final NoInventoryRecipe INSTANCE = new NoInventoryRecipe();

    private NoInventoryRecipe() {
        super(new ItemStackHandler(0));
    }

    @Override
    public int size() {
        return 0;
    }
}

