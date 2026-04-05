package com.benbenlaw.inworldrecipes.mixin;

import com.benbenlaw.inworldrecipes.event.WorldRecipeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin {

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Fallable;onLand(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/item/FallingBlockEntity;)V"))

    private void onBlockLand(CallbackInfo ci) {
        FallingBlockEntity fallingEntity = (FallingBlockEntity)(Object)this;

        Level level = fallingEntity.level();
        if (level.isClientSide()) return;

        BlockPos landPos = fallingEntity.blockPosition();
        Player closestPlayer = level.getNearestPlayer(fallingEntity, 10.0);

        WorldRecipeEvent.handle(level, closestPlayer, landPos, null, null, false, fallingEntity, "");
    }
}