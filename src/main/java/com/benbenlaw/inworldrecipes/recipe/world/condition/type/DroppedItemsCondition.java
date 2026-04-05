package com.benbenlaw.inworldrecipes.recipe.world.condition.type;

import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionTypes;
import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionType;
import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record DroppedItemsCondition(List<SizedIngredient> items) implements IRecipeCondition {

    public static final MapCodec<DroppedItemsCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.NESTED_CODEC.listOf().fieldOf("items").forGetter(DroppedItemsCondition::items)
    ).apply(inst, DroppedItemsCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DroppedItemsCondition> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), DroppedItemsCondition::items,
            DroppedItemsCondition::new
    );

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        BlockPos pos = ctx.pos();

        var searchArea = new AABB(pos).inflate(5);
        var entities = ctx.level().getEntitiesOfClass(ItemEntity.class, searchArea);

        for (SizedIngredient required : items) {
            int found = entities.stream()
                    .filter(e -> required.test(e.getItem()))
                    .mapToInt(e -> e.getItem().getCount())
                    .sum();

            if (found < required.count()) return false;
        }
        return true;
    }

    @Override
    public ConditionType<?> getType() {
        return ConditionTypes.DROPPED_ITEMS.get();
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

        return icons;
    }

}