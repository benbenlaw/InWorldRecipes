package com.benbenlaw.inworldrecipes.recipe.world.trigger.type;

import com.benbenlaw.inworldrecipes.recipe.util.CauldronContent;
import com.benbenlaw.inworldrecipes.recipe.util.CauldronContentCodec;
import com.benbenlaw.inworldrecipes.recipe.util.CauldronContents;
import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerType;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.TriggerTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;

public record CauldronTrigger(CauldronContent content, List<SizedIngredient> items, int consumeLevel) implements IRecipeTrigger {

    public static final MapCodec<CauldronTrigger> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            CauldronContentCodec.CODEC.fieldOf("fluid").forGetter(CauldronTrigger::content),
            SizedIngredient.NESTED_CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(CauldronTrigger::items),
            Codec.intRange(LayeredCauldronBlock.MIN_FILL_LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL)
                    .optionalFieldOf("consume_level", LayeredCauldronBlock.MIN_FILL_LEVEL)
                    .forGetter(CauldronTrigger::consumeLevel)
    ).apply(inst, CauldronTrigger::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CauldronTrigger> STREAM_CODEC = StreamCodec.composite(
            CauldronContentCodec.STREAM_CODEC, CauldronTrigger::content,
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), CauldronTrigger::items,
            ByteBufCodecs.VAR_INT, CauldronTrigger::consumeLevel,
            CauldronTrigger::new
    );

    @Override
    public boolean matches(WorldRecipeContext ctx) {
        BlockState state = ctx.level().getBlockState(ctx.pos());

        CauldronContent actual = CauldronContents.getContent(state.getBlock());
        if (actual != this.content) return false;

        if (CauldronContents.getLevel(state) < this.consumeLevel) return false;

        if (this.items.isEmpty()) return true;

        List<ItemEntity> entities = ctx.level().getEntitiesOfClass(ItemEntity.class, new AABB(ctx.pos()));

        for (SizedIngredient required : this.items) {
            int found = entities.stream()
                    .filter(e -> required.test(e.getItem()))
                    .mapToInt(e -> e.getItem().getCount())
                    .sum();

            if (found < required.count()) return false;
        }

        return true;
    }

    @Override
    public TriggerType<?> getType() { return TriggerTypes.CAULDRON.get(); }

    @Override
    public ItemStack getJeiIcon() {
        Item bucket = bucketFor(this.content);
        return bucket == Items.AIR ? ItemStack.EMPTY : new ItemStack(bucket);
    }

    private static Item bucketFor(CauldronContent content) {
        return switch (content) {
            case WATER -> Items.WATER_BUCKET;
            case LAVA -> Items.LAVA_BUCKET;
            case POWDER_SNOW -> Items.POWDER_SNOW_BUCKET;
        };
    }

    @Override
    public List<ItemStack> getJeiIcons() {
        List<ItemStack> icons = new ArrayList<>();
        ItemStack fluidIcon = getJeiIcon();
        if (!fluidIcon.isEmpty()) icons.add(fluidIcon);

        for (SizedIngredient sized : this.items) {
            sized.ingredient().items().forEach(holder -> {
                ItemStack stack = new ItemStack(holder.value());
                stack.setCount(sized.count());
                icons.add(stack);
            });
        }

        return icons;
    }

    @Override
    public Component getJeiTooltip() {
        Identifier id = TriggerTypes.REGISTRY.getKey(this.getType());
        if (id == null) return Component.literal("Unknown Trigger");

        MutableComponent tooltip = Component.translatable("jei." + id.getNamespace() + "." + id.getPath()).append(": ");

        tooltip.append(Component.literal(this.content.name().toLowerCase()));
        tooltip.append(Component.literal(" (" + this.consumeLevel + "/" + LayeredCauldronBlock.MAX_FILL_LEVEL + ")"));

        return tooltip;
    }
}
