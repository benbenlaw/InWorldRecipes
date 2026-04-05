package com.benbenlaw.inworldrecipes.recipe.world.condition.type;

import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionType;
import com.benbenlaw.inworldrecipes.recipe.world.condition.ConditionTypes;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.condition.util.EntityRequirement;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerType;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

public record EntityCondition(List<EntityRequirement> requirements) implements IRecipeCondition {

    public static final MapCodec<EntityCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            EntityRequirement.CODEC.listOf().fieldOf("requirements").forGetter(EntityCondition::requirements)
    ).apply(inst, EntityCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityCondition> STREAM_CODEC =
            EntityRequirement.STREAM_CODEC.apply(ByteBufCodecs.list()).map(EntityCondition::new, EntityCondition::requirements);

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        AABB searchArea = new AABB(ctx.pos()).inflate(10);
        List<Entity> entitiesInArea = ctx.level().getEntities((Entity) null, searchArea, e -> e != ctx.player());

        boolean[] found = new boolean[requirements.size()];
        List<Entity> toDamage = new java.util.ArrayList<>();
        List<Float> damageValues = new java.util.ArrayList<>();

        for (Entity entity : entitiesInArea) {
            for (int i = 0; i < requirements.size(); i++) {
                if (found[i]) continue;

                EntityRequirement req = requirements.get(i);
                if (entity.getType() == req.type()) {

                    boolean nbtMatch = true;
                    if (req.nbt().isPresent()) {
                        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, ctx.level().registryAccess());
                        entity.saveWithoutId(output);
                        nbtMatch = NbtUtils.compareNbt(req.nbt().get(), output.buildResult(), true);
                    }

                    if (nbtMatch) {
                        found[i] = true;
                        if (req.damage() > 0) {
                            toDamage.add(entity);
                            damageValues.add(req.damage());
                        }
                        break;
                    }
                }
            }
        }

        for (boolean b : found) if (!b) return false;

        // Apply individual damage
        for (int i = 0; i < toDamage.size(); i++) {
            toDamage.get(i).hurt(ctx.level().damageSources().generic(), damageValues.get(i));
        }

        return true;
    }

    @Override public ConditionType<?> getType() { return ConditionTypes.ENTITY.get();
    }

    @Override
    public List<ItemStack> getJeiIcons() {
        return requirements.stream()
                .map(req -> {
                    Item egg = SpawnEggItem.byId(req.type()).get().value();
                    if (egg != null) {

                        return new ItemStack(egg != null ? (ItemLike) egg : Items.BAMBOO.asItem());
                    }
                    return new ItemStack(Items.BAMBOO);
                })
                .toList();
    }


    @Override
    public Component getJeiTooltip() {
        MutableComponent tooltip = Component.translatable("jei.inworldrecipes.entity")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": "));

        for (int i = 0; i < requirements.size(); i++) {
            EntityRequirement req = requirements.get(i);
            MutableComponent entry = req.type().getDescription().copy().withStyle(ChatFormatting.YELLOW);

            if (req.nbt().isPresent() && !req.nbt().get().isEmpty()) {
                entry.append(Component.literal("*").withStyle(ChatFormatting.GOLD));
            }
            if (req.damage() > 0) {
                entry.append(Component.literal(" (")
                        .append(Component.translatable("jei.inworldrecipes.damage"))
                        .append(Component.literal(String.valueOf(req.damage())).withStyle(ChatFormatting.RED))
                        .append(Component.literal(")")));
            }

            tooltip.append(entry);

            if (i < requirements.size() - 1) {
                tooltip.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            }
        }

        return tooltip;
    }
}