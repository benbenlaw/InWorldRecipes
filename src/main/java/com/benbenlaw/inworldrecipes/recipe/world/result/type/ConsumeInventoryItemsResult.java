package com.benbenlaw.inworldrecipes.recipe.world.result.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.condition.type.InventoryItemsCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultType;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

public record ConsumeInventoryItemsResult() implements IRecipeResult {
    public static final MapCodec<ConsumeInventoryItemsResult> CODEC = MapCodec.unit(new ConsumeInventoryItemsResult());
    public static final StreamCodec<RegistryFriendlyByteBuf, ConsumeInventoryItemsResult> STREAM_CODEC = StreamCodec.unit(new ConsumeInventoryItemsResult());

    @Override
    public void apply(WorldRecipeContext ctx, List<IRecipeCondition> conditions) {
        if (ctx.player() == null) return;
        for (IRecipeCondition condition : conditions) {
            if (condition instanceof InventoryItemsCondition(List<SizedIngredient> items)) {
                for (SizedIngredient requirement : items) {
                    shrinkPlayerInventory(ctx.player(), requirement);
                }
            }
        }
    }

    private void shrinkPlayerInventory(Player player, SizedIngredient requirement) {
        int remainingToConsume = requirement.count();
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (!stack.isEmpty() && requirement.ingredient().test(stack)) {
                int countInSlot = stack.getCount();
                int consumeAmount = Math.min(remainingToConsume, countInSlot);

                stack.shrink(consumeAmount);
                remainingToConsume -= consumeAmount;

                if (remainingToConsume <= 0) break;
            }
        }
    }

    @Override
    public ResultType<?> getType() {
        return ResultTypes.CONSUME_INVENTORY_ITEMS.get();
    }
}