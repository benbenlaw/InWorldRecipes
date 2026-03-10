package com.benbenlaw.inworldrecipes.mixin;

import com.benbenlaw.inworldrecipes.event.WorldRecipeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin {

    @Inject(method = "tick", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/block/Fallable;onLand(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/item/FallingBlockEntity;)V"))
    private void onAnvilLand(CallbackInfo ci) {
        FallingBlockEntity anvil = (FallingBlockEntity)(Object)this;
        if (anvil.getBlockState().getBlock() != Blocks.ANVIL) return;

        Level level = anvil.level();
        BlockPos below = anvil.blockPosition().below(); // the block below the anvil

        // Trigger recipes for all nearby players (or you can do a "world drop" method)
        for (Player player : level.getEntitiesOfClass(Player.class, anvil.getBoundingBox().inflate(2))) {
            WorldRecipeEvent.handle(level, player, below, InteractionHand.MAIN_HAND, null);
        }

        // Optional: mark anvil to avoid retriggering
        anvil.getPersistentData().putBoolean("RecipeTriggered", true);
    }
}