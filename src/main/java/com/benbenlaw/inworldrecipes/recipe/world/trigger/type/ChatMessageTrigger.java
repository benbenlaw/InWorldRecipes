package com.benbenlaw.inworldrecipes.recipe.world.trigger.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerType;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record ChatMessageTrigger(String message) implements IRecipeTrigger {

    public static final MapCodec<ChatMessageTrigger> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("message").forGetter(ChatMessageTrigger::message)
    ).apply(inst, ChatMessageTrigger::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChatMessageTrigger> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(ChatMessageTrigger::new, ChatMessageTrigger::message).cast();

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        return ctx.chatMessage() != null && ctx.chatMessage().equalsIgnoreCase(this.message);
    }

    @Override
    public TriggerType<?> getType() {
        return TriggerTypes.CHAT_MESSAGE.get();
    }

    @Override
    public Component getJeiTooltip() {
        Identifier id = TriggerTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Trigger");

        return Component.translatable("jei." + id.getNamespace() + "." + id.getPath())
                .append(": ")
                .append(Component.literal(this.message).withStyle(ChatFormatting.YELLOW));
    }
}