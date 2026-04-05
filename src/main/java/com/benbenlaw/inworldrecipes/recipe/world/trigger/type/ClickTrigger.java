package com.benbenlaw.inworldrecipes.recipe.world.trigger.type;

import com.benbenlaw.inworldrecipes.recipe.util.BlockTarget;
import com.benbenlaw.inworldrecipes.recipe.util.BlockTargetCodec;
import com.benbenlaw.inworldrecipes.recipe.util.ClickType;
import com.benbenlaw.inworldrecipes.recipe.util.ClickTypeCodec;
import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerType;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public record ClickTrigger(ClickType clickType) implements IRecipeTrigger {

    public static final MapCodec<ClickTrigger> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ClickTypeCodec.CLICK_TYPE_CODEC.fieldOf("click_type").forGetter(ClickTrigger::clickType)
    ).apply(inst, ClickTrigger::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, ClickType> CLICK_TYPE_STREAM_CODEC = StreamCodec.of(
            ClickTypeCodec::writeToBuffer,
            ClickTypeCodec::readFromBuffer
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ClickTrigger> STREAM_CODEC =
            CLICK_TYPE_STREAM_CODEC.map(ClickTrigger::new, ClickTrigger::clickType);

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        return ctx.clickType() == this.clickType;
    }

    @Override
    public TriggerType<?> getType() {
        return TriggerTypes.CLICK_TYPE.get();
    }

    @Override
    public Component getJeiTooltip() {
        Identifier id = TriggerTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Trigger");

        String clickName = this.clickType == ClickType.RIGHT_CLICK ? "Right Click" : "Left Click";

        return Component.translatable("jei." + id.getNamespace() + "." + id.getPath())
                .append(": ")
                .append(Component.literal(clickName).withStyle(ChatFormatting.AQUA));
    }
}