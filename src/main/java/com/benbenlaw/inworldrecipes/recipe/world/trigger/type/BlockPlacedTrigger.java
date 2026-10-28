package com.benbenlaw.inworldrecipes.recipe.world.trigger.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerType;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public record BlockPlacedTrigger(Block block) implements IRecipeTrigger {

    public static final MapCodec<BlockPlacedTrigger> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockPlacedTrigger::block)
    ).apply(inst, BlockPlacedTrigger::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPlacedTrigger> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.BLOCK), BlockPlacedTrigger::block,
            BlockPlacedTrigger::new
    );

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        return ctx.blockPlaced() && ctx.level().getBlockState(ctx.pos()).is(this.block);
    }

    @Override
    public TriggerType<?> getType() {
        return TriggerTypes.BLOCK_PLACED.get();
    }

    @Override
    public Component getJeiTooltip() {
        return Component.translatable("jei.inworldrecipes.block_placed")
                .append(": ")
                .append(this.block.getName());
    }

    @Override
    public ItemStack getJeiIcon() {
        Item item = this.block.asItem();
        if (item == Items.AIR) {
            return new ItemStack(Items.BARRIER);
        }
        return new ItemStack(item);
    }
}
