package com.benbenlaw.inworldrecipes.mixin;

import dev.ftb.mods.ftbultimine.client.CachedEdge;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(FTBUltimineClient.class)
public abstract class FTBUltimineClientMixin {

    @Shadow
    private boolean pressed;
    @Shadow private List<BlockPos> shapeBlocks;
    @Shadow private List<CachedEdge> cachedEdges;

    @Inject(method = "clientTick", at = @At("HEAD"))
    private void clearCacheIfNotPressed(Minecraft mc, CallbackInfo ci) {
        if (!pressed) {
            shapeBlocks = Collections.emptyList();
            cachedEdges = null;
        }
    }
}