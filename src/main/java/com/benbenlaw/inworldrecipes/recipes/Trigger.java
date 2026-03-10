package com.benbenlaw.inworldrecipes.recipes;

import com.benbenlaw.inworldrecipes.util.ClickType;
import com.benbenlaw.inworldrecipes.util.ClickTypeCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import javax.annotation.Nullable;
import java.util.Optional;

public record Trigger(
        @Nullable ClickType clickType,
        @Nullable BlockTarget targetBlock,
        @Nullable Boolean lightningStrike,
        @Nullable BlockTarget standingOnBlock,
        @Nullable Boolean anvilLanded

) {

    public static final Codec<Trigger> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ClickTypeCodec.CLICK_TYPE_CODEC.optionalFieldOf("click_type").forGetter(trigger -> Optional.ofNullable(trigger.clickType)),
                    BlockTargetCodec.CODEC.optionalFieldOf("target_block").forGetter(trigger -> Optional.ofNullable(trigger.targetBlock)),
                    Codec.BOOL.optionalFieldOf("lightning_strike").forGetter(trigger -> Optional.ofNullable(trigger.lightningStrike)),
                    BlockTargetCodec.CODEC.optionalFieldOf("standing_on_block").forGetter(trigger -> Optional.ofNullable(trigger.standingOnBlock)),
                    Codec.BOOL.optionalFieldOf("anvil_landed").forGetter(trigger -> Optional.ofNullable(trigger.anvilLanded))
            ).apply(instance, (clickOpt, blockOpt, lightningOp, standingOnOpt, anvilLandedOpt) ->
                    new Trigger(
                            clickOpt.orElse(null),
                            blockOpt.orElse(null),
                            lightningOp.orElse(null),
                            standingOnOpt.orElse(null),
                            anvilLandedOpt.orElse(null)
                    ))
    );
}