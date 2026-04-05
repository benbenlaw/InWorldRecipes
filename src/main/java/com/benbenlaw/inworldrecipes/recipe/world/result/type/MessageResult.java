package com.benbenlaw.inworldrecipes.recipe.world.result.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultType;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;

import static com.mojang.serialization.codecs.RecordCodecBuilder.*;

public record MessageResult(String message, boolean actionBar) implements IRecipeResult {

    public static final MapCodec<MessageResult> CODEC = mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("message").forGetter(MessageResult::message),
            Codec.BOOL.optionalFieldOf("action_bar", false).forGetter(MessageResult::actionBar)
    ).apply(inst, MessageResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageResult> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MessageResult::message,
            ByteBufCodecs.BOOL, MessageResult::actionBar,
            MessageResult::new
    );

    @Override
    public void apply(WorldRecipeContext ctx, List<IRecipeCondition> conditions) {
        if (ctx.player() != null) {
            ctx.player().sendSystemMessage(
                net.minecraft.network.chat.Component.literal(this.message)
            );
        }
    }

    @Override
    public Component getJeiTooltip() {
        Identifier id = ResultTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Result Type");

        String translationKey = "jei." + id.getNamespace() + "." + id.getPath();
        MutableComponent tooltip = Component.translatable(translationKey).append(": ");

        return tooltip.append(Component.literal(this.message)
                .withStyle(ChatFormatting.GOLD));
    }

    @Override
    public ResultType<?> getType() {
        return ResultTypes.MESSAGE.get();
    }
}