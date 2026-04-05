package com.benbenlaw.inworldrecipes.recipe.world.result.type;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultType;
import com.benbenlaw.inworldrecipes.recipe.world.result.ResultTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public record ChanceResultsResult(List<ChanceResult> results, boolean addToInventory) implements IRecipeResult {

    public static final MapCodec<ChanceResultsResult> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ChanceResult.CODEC.listOf().fieldOf("results").forGetter(ChanceResultsResult::results),
            Codec.BOOL.optionalFieldOf("add_to_inventory", false).forGetter(ChanceResultsResult::addToInventory)
    ).apply(inst, ChanceResultsResult::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, ChanceResult> CHANCE_STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> value.write(buffer),
            ChanceResult::read
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ChanceResultsResult> STREAM_CODEC = StreamCodec.composite(
            CHANCE_STREAM_CODEC.apply(ByteBufCodecs.list()), ChanceResultsResult::results,
            ByteBufCodecs.BOOL, ChanceResultsResult::addToInventory,
            ChanceResultsResult::new
    );

    @Override
    public void apply(WorldRecipeContext ctx, List<IRecipeCondition> conditions) {
        for (ChanceResult chanceResult : results) {
            ItemStack output = chanceResult.rollOutput(ctx.level().getRandom());

            if (!output.isEmpty()) {
                if (this.addToInventory && ctx.player() != null) {
                    ctx.player().getInventory().placeItemBackInInventory(output);
                } else {
                    ItemEntity entity = new ItemEntity(ctx.level(),
                            ctx.pos().getX() + 0.5, ctx.pos().getY() + 1.1, ctx.pos().getZ() + 0.5,
                            output);
                    ctx.level().addFreshEntity(entity);
                }
            }
        }
    }

    @Override
    public ResultType<?> getType() {
        return ResultTypes.CHANCE_RESULTS.get();
    }

    @Override
    public Component getJeiTooltip() {
        Identifier id = ResultTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Result Type");

        String translationKey = "jei." + id.getNamespace() + "." + id.getPath();
        MutableComponent tooltip = Component.translatable(translationKey).append(": ");

        for (int i = 0; i < this.results.size(); i++) {
            ChanceResult result = this.results.get(i);
            ItemStack stack = result.template().create();
            int chancePercent = (int) (result.chance() * 100);

            tooltip.append(Component.literal(stack.getCount() + "x "))
                    .append(stack.getHoverName())
                    .append(Component.literal(" (" + chancePercent + "%)").withStyle(ChatFormatting.GRAY));

            if (i < this.results.size() - 1) {
                tooltip.append(Component.literal(", "));
            }
        }

        if (this.addToInventory) {
            tooltip.append(Component.literal(" [").append(Component.translatable("jei.inworldrecipes.to_inventory")).append("]").withStyle(ChatFormatting.ITALIC));
        }

        return tooltip;
    }

    @Override
    public List<ItemStack> getJeiIcons() {
        return this.results.stream()
                .map(cr -> cr.template().create())
                .filter(stack -> !stack.isEmpty())
                .toList();
    }
}