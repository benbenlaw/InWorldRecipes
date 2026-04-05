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
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record HeldItemCondition(SizedIngredient ingredient) implements IRecipeCondition {

    public static final MapCodec<HeldItemCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.NESTED_CODEC.fieldOf("ingredient").forGetter(HeldItemCondition::ingredient)
    ).apply(inst, HeldItemCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeldItemCondition> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC, HeldItemCondition::ingredient,
            HeldItemCondition::new
    );

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        if (ctx.hand() == null) return false;
        return ingredient.test(ctx.player().getItemInHand(ctx.hand()));
    }

    @Override
    public ConditionType<?> getType() {
        return ConditionTypes.HELD_ITEM.get();
    }


    @Override
    public Component getJeiTooltip() {
        Identifier id = ConditionTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Condition Type");

        String translationKey = "jei." + id.getNamespace() + "." + id.getPath();
        MutableComponent tooltip = Component.translatable(translationKey).append(": ");

        Component itemName = this.ingredient.ingredient().items()
                .findFirst()
                .map(holder -> holder.value().getDefaultInstance().getHoverName())
                .orElse(Component.literal("Unknown Item"));

        return tooltip.append(Component.literal(this.ingredient.count() + "x "))
                .append(itemName);
    }

    @Override
    public ItemStack getJeiIcon() {
        return this.ingredient.ingredient().items()
                .findFirst()
                .map(holder -> {
                    ItemStack stack = new ItemStack(holder.value());
                    stack.setCount(this.ingredient.count());
                    return stack;
                })
                .orElse(ItemStack.EMPTY);
    }

}