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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/**
 * Consumes the fluid at the trigger position, with a chance to do so. Removes a plain fluid block
 * entirely, or clears the waterlogged flag on a waterlogged block, leaving the rest of the block intact.
 */
public record ConsumeFluidResult(double chance) implements IRecipeResult {

    public static final MapCodec<ConsumeFluidResult> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.doubleRange(0.0, 1.0).optionalFieldOf("chance", 1.0).forGetter(ConsumeFluidResult::chance)
    ).apply(inst, ConsumeFluidResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConsumeFluidResult> STREAM_CODEC =
            ByteBufCodecs.DOUBLE.map(ConsumeFluidResult::new, ConsumeFluidResult::chance).cast();

    @Override
    public void apply(WorldRecipeContext ctx, List<IRecipeTrigger> triggers, List<IRecipeCondition> conditions) {
        if (ctx.level().getRandom().nextDouble() >= this.chance) return;

        BlockState state = ctx.level().getBlockState(ctx.pos());
        if (state.getFluidState().isEmpty()) return;

        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            ctx.level().setBlockAndUpdate(ctx.pos(), state.setValue(BlockStateProperties.WATERLOGGED, false));
        } else {
            ctx.level().setBlockAndUpdate(ctx.pos(), Blocks.AIR.defaultBlockState());
        }
    }

    @Override
    public ResultType<?> getType() {
        return ResultTypes.CONSUME_FLUID.get();
    }

    @Override
    public Component getJeiTooltip() {
        Identifier id = ResultTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Result Type");

        String translationKey = "jei." + id.getNamespace() + "." + id.getPath();
        MutableComponent tooltip = Component.translatable(translationKey);

        if (this.chance < 1.0) {
            tooltip.append(Component.literal(" (" + (int) Math.round(this.chance * 100) + "%)"));
        }

        return tooltip;
    }
}
