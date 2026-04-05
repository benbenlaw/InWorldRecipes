package com.benbenlaw.inworldrecipes.event;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipe.WorldRecipe;
import com.benbenlaw.inworldrecipes.recipe.InWorldRecipesRecipes;
import com.benbenlaw.inworldrecipes.recipe.world.WorldRecipeContext;
import com.benbenlaw.inworldrecipes.recipe.util.ClickType;
import com.benbenlaw.inworldrecipes.recipe.world.condition.IRecipeCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.IRecipeResult;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.IRecipeTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import javax.annotation.Nullable;
import java.util.List;

@EventBusSubscriber(modid = InWorldRecipes.MOD_ID)
public class WorldRecipeEvent {


    @SubscribeEvent
    public static void rightClickOnBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || event.getHand() == InteractionHand.OFF_HAND) return;
        handle(event.getLevel(), event.getEntity(), event.getPos(), event.getHand(), ClickType.RIGHT_CLICK, false, null, null);
    }

    @SubscribeEvent
    public static void leftClickOnBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        handle(event.getLevel(), event.getEntity(), event.getPos(), event.getHand(), ClickType.LEFT_CLICK, false, null, null);
    }

    @SubscribeEvent
    public static void onLightningStrike(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof LightningBolt lightning) {
            BlockPos pos = lightning.blockPosition();
            Player nearestPlayer = event.getLevel().getNearestPlayer(lightning, 10.0);
            handle(event.getLevel(), nearestPlayer, pos, null, null, true, null, null);
        }
    }

    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        if (event.getPlayer().level().isClientSide()) return;
        handle(event.getPlayer().level(), event.getPlayer(), event.getPlayer().blockPosition(),
                InteractionHand.MAIN_HAND, null, false, null, event.getRawText().trim());
    }

    public static void handle(Level level, @Nullable Player player, BlockPos pos,
                              @Nullable InteractionHand hand, @Nullable ClickType clickType,
                              boolean lightningStrike, @Nullable FallingBlockEntity fallingBlock,
                              @Nullable String chatMessage) {

        WorldRecipeContext context = new WorldRecipeContext(
                level, player, pos, hand, clickType, lightningStrike, fallingBlock, chatMessage
        );

        for (RecipeHolder<WorldRecipe> recipeHolder : getAllWorldRecipes(level)) {
            if (execute(context, recipeHolder.value())) {
                break;
            }
        }
    }

    private static boolean execute(WorldRecipeContext ctx, WorldRecipe recipe) {
        // Trigger Check (At least one trigger must match the current context)
        boolean triggerMatched = false;
        for (IRecipeTrigger trigger : recipe.triggers()) {
            if (trigger.matches(ctx)) {
                triggerMatched = true;
                break;
            }
        }
        if (!triggerMatched) return false;

        // Conditions Check (All conditions must be met)
        for (IRecipeCondition condition : recipe.conditions()) {
            if (!condition.matches(ctx)) return false;
        }

        // Apply Results
        for (IRecipeResult result : recipe.results()) {
            result.apply(ctx, recipe.conditions());
        }

        return true;
    }

    private static List<RecipeHolder<WorldRecipe>> getAllWorldRecipes(Level level) {
        return level.getServer()
                .getRecipeManager()
                .recipeMap()
                .values()
                .stream()
                .filter(r -> r.value().getType() == InWorldRecipesRecipes.WORLD_RECIPE_TYPE.get())
                .map(r -> (RecipeHolder<WorldRecipe>) r)
                .toList();
    }
}