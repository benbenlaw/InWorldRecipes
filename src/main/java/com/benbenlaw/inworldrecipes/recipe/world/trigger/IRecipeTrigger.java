package com.benbenlaw.inworldrecipes.recipe.world.trigger;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionType;
import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IRecipeTrigger {
    boolean matches(WorldRecipeContext ctx);
    TriggerType<?> getType();

    default ItemStack getJeiIcon() {
        return ItemStack.EMPTY;
    }

    default List<ItemStack> getJeiIcons() {
        ItemStack single = getJeiIcon(); // Keep the old method for backward compatibility
        return single.isEmpty() ? List.of() : List.of(single);
    }

    default Component getJeiTooltip() {

        Identifier id = TriggerTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Trigger Type");

        String translationKey = "jei." + id.getNamespace() + "." + id.getPath();

        return Component.translatable(translationKey);
    }
}