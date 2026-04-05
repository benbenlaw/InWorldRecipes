package com.benbenlaw.inworldrecipes.recipe.world.condition;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IRecipeCondition {
    boolean matches(WorldRecipeContext ctx);
    ConditionType<?> getType();

    default List<ItemStack> getJeiIcons() {
        ItemStack single = getJeiIcon(); // Keep the old method for backward compatibility
        return single.isEmpty() ? List.of() : List.of(single);
    }

    default ItemStack getJeiIcon() {
        return ItemStack.EMPTY;
    }

    default Component getJeiTooltip() {

        Identifier id = ConditionTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Condition Type");

        String translationKey = "jei." + id.getNamespace() + "." + id.getPath();

        return Component.translatable(translationKey);
    }
}

