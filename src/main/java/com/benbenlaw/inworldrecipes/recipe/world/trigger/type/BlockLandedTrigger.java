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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public record BlockLandedTrigger(Block block) implements IRecipeTrigger {

    public static final MapCodec<BlockLandedTrigger> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockLandedTrigger::block)
    ).apply(inst, BlockLandedTrigger::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockLandedTrigger> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.BLOCK), BlockLandedTrigger::block,
            BlockLandedTrigger::new
    );

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        if (ctx.fallingBlockEntity() == null) return false;
        return ctx.fallingBlockEntity().getBlockState().is(this.block);
    }

    @Override
    public TriggerType<?> getType() {
        return TriggerTypes.BLOCK_LANDED.get();
    }

    @Override
    public Component getJeiTooltip() {
        return Component.translatable("jei.inworldrecipes.block_landed")
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