package com.benbenlaw.inworldrecipes;

import com.benbenlaw.inworldrecipes.config.InWorldConfig;
import com.benbenlaw.inworldrecipes.item.InWorldRecipesItems;
import com.benbenlaw.inworldrecipes.recipes.InWorldRecipesRecipes;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(InWorldRecipes.MOD_ID)
public class InWorldRecipes {
    public static final String MOD_ID = "inworldrecipes";
    private static final Logger LOGGER = LogUtils.getLogger();

    public InWorldRecipes(IEventBus modEventBus, final ModContainer modContainer) {

        modContainer.registerConfig(ModConfig.Type.COMMON, InWorldConfig.SPEC, "bbl/inworldrecipes/config.toml");

        InWorldRecipesRecipes.SERIALIZER.register(modEventBus);
        InWorldRecipesRecipes.TYPES.register(modEventBus);
        InWorldRecipesItems.ITEMS.register(modEventBus);
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}

