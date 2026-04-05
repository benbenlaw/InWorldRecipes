package com.benbenlaw.inworldrecipes.recipe.world.result;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionTypes;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IRecipeResult {
    void apply(WorldRecipeContext ctx, List<IRecipeCondition> conditions);
    ResultType<?> getType();

    default ItemStack getJeiIcon() {
        return ItemStack.EMPTY;
    }

    default List<ItemStack> getJeiIcons() {
        ItemStack single = getJeiIcon(); // Keep the old method for backward compatibility
        return single.isEmpty() ? List.of() : List.of(single);
    }

    default Component getJeiTooltip() {

        Identifier id = ResultTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Result Type");

        String translationKey = "jei." + id.getNamespace() + "." + id.getPath();

        return Component.translatable(translationKey);
    }
}