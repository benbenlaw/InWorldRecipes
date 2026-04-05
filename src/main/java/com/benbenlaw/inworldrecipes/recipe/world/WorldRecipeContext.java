package com.benbenlaw.inworldrecipes.recipe.world;

import com.benbenlaw.inworldrecipes.recipe.util.ClickType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public record WorldRecipeContext(
        Level level,
        @Nullable Player player,
        BlockPos pos,
        @Nullable InteractionHand hand,
        @Nullable ClickType clickType,
        boolean lightningStrike,
        @Nullable FallingBlockEntity fallingBlockEntity,
        @Nullable String chatMessage
) {

    public boolean isBlockLanding() {
        return fallingBlockEntity != null;
    }
}