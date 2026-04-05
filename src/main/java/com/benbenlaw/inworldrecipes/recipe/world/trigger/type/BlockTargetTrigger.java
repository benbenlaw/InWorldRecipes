package com.benbenlaw.inworldrecipes.recipe.world.trigger.type;

import com.benbenlaw.inworldrecipes.recipe.util.BlockTarget;
import com.benbenlaw.inworldrecipes.recipe.util.BlockTargetCodec;
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
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public record BlockTargetTrigger(BlockTarget targetBlock) implements IRecipeTrigger {

    public static final MapCodec<BlockTargetTrigger> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            BlockTargetCodec.CODEC.fieldOf("target_block").forGetter(BlockTargetTrigger::targetBlock)
    ).apply(inst, BlockTargetTrigger::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockTargetTrigger> STREAM_CODEC =
            BlockTargetCodec.STREAM_CODEC.map(BlockTargetTrigger::new, BlockTargetTrigger::targetBlock);

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        BlockState state = ctx.level().getBlockState(ctx.pos());
        return this.targetBlock.matches(state, false);
    }

    @Override
    public TriggerType<?> getType() { return TriggerTypes.BLOCK_TARGET.get(); }

    @Override
    public Component getJeiTooltip() {
        Identifier id = TriggerTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Trigger");

        String translationKey = "jei." + id.getNamespace() + "." + id.getPath();
        MutableComponent tooltip = Component.translatable(translationKey).append(": ");

        if (this.targetBlock instanceof BlockTarget.Single single) {
            tooltip.append(single.blockState().getBlock().getName());
        } else if (this.targetBlock instanceof BlockTarget.Tag tag) {
            tooltip.append(Component.literal("#" + tag.tag().location()));
        }

        return tooltip;
    }
}