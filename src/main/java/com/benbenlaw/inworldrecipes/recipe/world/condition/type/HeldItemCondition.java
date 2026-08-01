package com.benbenlaw.inworldrecipes.recipe.world.condition.type;

import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionTypes;
import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionType;
import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

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

        Component itemDisplay;
        if (!this.ingredient.ingredient().isCustom()) {
            Either<TagKey<Item>, List<Holder<Item>>> unwrapped = this.ingredient.ingredient().getValues().unwrap();
            itemDisplay = unwrapped.left()
                    .<Component>map(tag -> Component.literal("#" + tag.location()))
                    .orElseGet(() -> this.ingredient.ingredient().items()
                            .findFirst()
                            .map(holder -> holder.value().getDefaultInstance().getHoverName())
                            .orElse(Component.literal("Unknown Item")));
        } else {
            itemDisplay = this.ingredient.ingredient().items()
                    .findFirst()
                    .map(holder -> holder.value().getDefaultInstance().getHoverName())
                    .orElse(Component.literal("Unknown Item"));
        }

        return tooltip.append(Component.literal(this.ingredient.count() + "x "))
                .append(itemDisplay);
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

    @Override
    public List<ItemStack> getJeiIcons() {
        return this.ingredient.ingredient().items()
                .map(holder -> {
                    ItemStack stack = new ItemStack(holder.value());
                    stack.setCount(this.ingredient.count());
                    return stack;
                })
                .toList();
    }

}