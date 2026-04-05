package com.benbenlaw.inworldrecipes.recipe.world.result.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultType;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public record BlockStateResult(BlockState state) implements IRecipeResult {

    public static final MapCodec<BlockStateResult> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            net.minecraft.world.level.block.state.BlockState.CODEC.fieldOf("block_state").forGetter(BlockStateResult::state)
    ).apply(inst, BlockStateResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockStateResult> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), BlockStateResult::state,
            BlockStateResult::new
    );

    @Override
    public void apply(WorldRecipeContext ctx, List<IRecipeCondition> conditions) {
        ctx.level().setBlockAndUpdate(ctx.pos(), state);
    }

    @Override
    public ResultType<?> getType() { return ResultTypes.BLOCK_STATE.get();
    }

    @Override
    public Component getJeiTooltip() {
        Identifier id = ResultTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Result Type");

        String translationKey = "jei." + id.getNamespace() + "." + id.getPath();
        MutableComponent tooltip = Component.translatable(translationKey).append(": ");

        return tooltip.append(this.state.getBlock().getName());
    }

    @Override
    public ItemStack getJeiIcon() {
        return new ItemStack(this.state.getBlock().asItem());
    }
}