package com.benbenlaw.inworldrecipes.recipe.world.result.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultType;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultTypes;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public record DimensionBlockStateResult(Identifier dimension, BlockState state, boolean consumeSource) implements IRecipeResult {

    public static final MapCodec<DimensionBlockStateResult> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(DimensionBlockStateResult::dimension),
            BlockState.CODEC.fieldOf("block_state").forGetter(DimensionBlockStateResult::state),
            Codec.BOOL.optionalFieldOf("consume_source", true).forGetter(DimensionBlockStateResult::consumeSource)
    ).apply(inst, DimensionBlockStateResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DimensionBlockStateResult> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, DimensionBlockStateResult::dimension,
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), DimensionBlockStateResult::state,
            ByteBufCodecs.BOOL, DimensionBlockStateResult::consumeSource,
            DimensionBlockStateResult::new
    );

    @Override
    public void apply(WorldRecipeContext ctx, List<IRecipeTrigger> triggers, List<IRecipeCondition> conditions) {
        if (ctx.level().getServer() == null) return;

        ServerLevel target = ctx.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, this.dimension));
        if (target == null || target == ctx.level()) return;

        BlockPos pos = ctx.pos();
        if (target.isOutsideBuildHeight(pos)) return;
        if (target.getBlockState(pos).getDestroySpeed(target, pos) < 0) return;

        target.setBlockAndUpdate(pos, this.state);

        if (this.consumeSource) {
            ctx.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    @Override
    public ResultType<?> getType() {
        return ResultTypes.DIMENSION_BLOCK_STATE.get();
    }

    @Override
    public Component getJeiTooltip() {
        Identifier id = ResultTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Result Type");

        MutableComponent tooltip = Component.translatable("jei." + id.getNamespace() + "." + id.getPath()).append(": ");

        return tooltip.append(this.state.getBlock().getName())
                .append(Component.literal(" (" + this.dimension + ")"));
    }

    @Override
    public ItemStack getJeiIcon() {
        return new ItemStack(this.state.getBlock().asItem());
    }
}
