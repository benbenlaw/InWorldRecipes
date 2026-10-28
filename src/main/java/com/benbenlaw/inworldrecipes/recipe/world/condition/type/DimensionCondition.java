package com.benbenlaw.inworldrecipes.recipe.world.condition.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionType;
import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionTypes;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record DimensionCondition(Identifier dimension) implements IRecipeCondition {

    public static final MapCodec<DimensionCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(DimensionCondition::dimension)
    ).apply(inst, DimensionCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DimensionCondition> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, DimensionCondition::dimension,
            DimensionCondition::new
    );

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        return ctx.level().dimension().identifier().equals(this.dimension);
    }

    @Override
    public ConditionType<?> getType() {
        return ConditionTypes.DIMENSION.get();
    }

    @Override
    public Component getJeiTooltip() {
        Identifier id = ConditionTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Condition Type");

        return Component.translatable("jei." + id.getNamespace() + "." + id.getPath())
                .append(": ")
                .append(Component.literal(this.dimension.toString()));
    }
}
