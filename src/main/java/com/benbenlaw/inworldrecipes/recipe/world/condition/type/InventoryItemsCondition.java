package com.benbenlaw.inworldrecipes.recipe.world.condition.type;

import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionTypes;
import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionType;
import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;

public record InventoryItemsCondition(List<SizedIngredient> items) implements IRecipeCondition {

    public static final MapCodec<InventoryItemsCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.NESTED_CODEC.listOf().fieldOf("items").forGetter(InventoryItemsCondition::items)
    ).apply(inst, InventoryItemsCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryItemsCondition> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), InventoryItemsCondition::items,
            InventoryItemsCondition::new
    );

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        for (SizedIngredient required : items) {
            boolean found = false;
            for (ItemStack stack : ctx.player().getInventory().getNonEquipmentItems()) {
                if (required.test(stack)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    @Override
    public ConditionType<?> getType() {
        return ConditionTypes.INVENTORY_ITEMS.get();
    }

    @Override
    public Component getJeiTooltip() {
        Identifier id = ConditionTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Condition Type");

        String translationKey = "jei." + id.getNamespace() + "." + id.getPath();
        MutableComponent tooltip = Component.translatable(translationKey).append(": ");

        for (int i = 0; i < this.items.size(); i++) {
            SizedIngredient sized = this.items.get(i);

            Component itemName = sized.ingredient().items()
                    .findFirst()
                    .map(holder -> holder.value().getDefaultInstance().getHoverName())
                    .orElse(Component.literal("Unknown Item"));

            tooltip.append(Component.literal(sized.count() + "x ")).append(itemName);

            if (i < this.items.size() - 1) {
                tooltip.append(Component.literal(", "));
            }
        }
        return tooltip;
    }

    @Override
    public List<ItemStack> getJeiIcons() {
        List<ItemStack> icons = new ArrayList<>();

        for (SizedIngredient sized : this.items) {
            List<ItemStack> stacks = sized.ingredient().items()
                    .map(holder -> {
                        ItemStack stack = new ItemStack(holder.value());
                        stack.setCount(sized.count());
                        return stack;
                    })
                    .toList();

            icons.addAll(stacks);
        }

        if (icons.isEmpty()) {
            icons.add(new ItemStack(net.minecraft.world.item.Items.CHEST));
        }

        return icons;
    }
}