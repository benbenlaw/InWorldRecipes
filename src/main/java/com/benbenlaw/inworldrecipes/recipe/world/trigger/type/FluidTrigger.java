package com.benbenlaw.inworldrecipes.recipe.world.trigger.type;

import com.benbenlaw.inworldrecipes.recipe.util.FluidTarget;
import com.benbenlaw.inworldrecipes.recipe.util.FluidTargetCodec;
import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerType;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;

public record FluidTrigger(FluidTarget fluid, List<SizedIngredient> items) implements IRecipeTrigger {

    public static final MapCodec<FluidTrigger> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            FluidTargetCodec.CODEC.fieldOf("fluid").forGetter(FluidTrigger::fluid),
            SizedIngredient.NESTED_CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(FluidTrigger::items)
    ).apply(inst, FluidTrigger::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidTrigger> STREAM_CODEC = StreamCodec.composite(
            FluidTargetCodec.STREAM_CODEC, FluidTrigger::fluid,
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), FluidTrigger::items,
            FluidTrigger::new
    );

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        BlockState state = ctx.level().getBlockState(ctx.pos());
        FluidState fluidState = state.getFluidState();

        if (fluidState.isEmpty() || !this.fluid.matches(fluidState.getType())) return false;

        if (this.items.isEmpty()) return true;

        List<ItemEntity> entities = ctx.level().getEntitiesOfClass(ItemEntity.class, new AABB(ctx.pos()));

        for (SizedIngredient required : this.items) {
            int found = entities.stream()
                    .filter(e -> required.test(e.getItem()))
                    .mapToInt(e -> e.getItem().getCount())
                    .sum();

            if (found < required.count()) return false;
        }

        return true;
    }

    @Override
    public TriggerType<?> getType() { return TriggerTypes.FLUID.get(); }

    @Override
    public ItemStack getJeiIcon() {
        if (this.fluid instanceof FluidTarget.Single single) {
            Item bucket = single.fluid().getBucket();
            if (bucket != Items.AIR) {
                return new ItemStack(bucket);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public List<ItemStack> getJeiIcons() {
        List<ItemStack> icons = new ArrayList<>();
        ItemStack fluidIcon = getJeiIcon();
        if (!fluidIcon.isEmpty()) icons.add(fluidIcon);

        for (SizedIngredient sized : this.items) {
            sized.ingredient().items().forEach(holder -> {
                ItemStack stack = new ItemStack(holder.value());
                stack.setCount(sized.count());
                icons.add(stack);
            });
        }

        return icons;
    }

    @Override
    public Component getJeiTooltip() {
        Identifier id = TriggerTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Trigger");

        MutableComponent tooltip = Component.translatable("jei." + id.getNamespace() + "." + id.getPath()).append(": ");

        if (this.fluid instanceof FluidTarget.Single single) {
            Identifier fluidId = BuiltInRegistries.FLUID.getKey(single.fluid());
            tooltip.append(Component.literal(fluidId == null ? "unknown" : fluidId.toString()));
        } else if (this.fluid instanceof FluidTarget.Tag tag) {
            tooltip.append(Component.literal("#" + tag.tag().location()));
        }

        return tooltip;
    }
}
