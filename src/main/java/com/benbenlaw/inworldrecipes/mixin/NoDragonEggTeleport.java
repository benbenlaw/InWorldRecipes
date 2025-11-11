package com.benbenlaw.inworldrecipes.mixin;


import com.benbenlaw.inworldrecipes.config.InWorldConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DragonEggBlock.class)
public class NoDragonEggTeleport {


    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    public void stopUseWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (InWorldConfig.DISABLE_DRAGON_EGG_TELEPORT.get()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void stopAttack(BlockState state, Level level, BlockPos pos, Player player, CallbackInfo ci) {
        if (InWorldConfig.DISABLE_DRAGON_EGG_TELEPORT.get()) {
            ci.cancel();
        }
    }
}
