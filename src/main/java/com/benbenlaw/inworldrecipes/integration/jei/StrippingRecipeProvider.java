package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.inworldrecipes.recipe.Option;
import com.benbenlaw.inworldrecipes.recipe.WorldRecipe;
import com.benbenlaw.inworldrecipes.recipe.util.BlockTarget;
import com.benbenlaw.inworldrecipes.recipe.world.condition.type.HeldItemCondition;
import com.benbenlaw.inworldrecipes.recipe.world.result.type.BlockStateResult;
import com.benbenlaw.inworldrecipes.recipe.world.result.type.DamageHeldItemResult;
import com.benbenlaw.inworldrecipes.recipe.world.trigger.type.BlockTargetTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Strippable;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates JEI-only WorldRecipe entries from NeoForge's Strippable data map
 * (the same data used by AxeItem for real axe-stripping behavior).
 * <p>
 * These recipes exist purely so JEI can show "log -> stripped log" as a relationship.
 * They are marked as onlyVisualRecipe so WorldRecipeEvent never actually runs them —
 * the real stripping is already handled by vanilla's AxeItem#useOn.
 */
public final class StrippingRecipeProvider {

    private StrippingRecipeProvider() {
    }

    public static List<WorldRecipe> getStrippingRecipes() {
        List<WorldRecipe> recipes = new ArrayList<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            Strippable strippable = block.builtInRegistryHolder().getData(NeoForgeDataMaps.STRIPPABLES);
            if (strippable == null) continue;

            Block stripped = strippable.strippedBlock();

            WorldRecipe recipe = new WorldRecipe(
                    new Recipe.CommonInfo(false),
                    List.of(new BlockTargetTrigger(new BlockTarget.Single(block.defaultBlockState()))),
                    List.of(new HeldItemCondition(new SizedIngredient(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ItemTags.AXES)), 1))),
                    List.of(new BlockStateResult(stripped.defaultBlockState()), new DamageHeldItemResult(1)),
                    List.of(new Option(true, true))
            );

            recipes.add(recipe);
        }

        return recipes;
    }
}