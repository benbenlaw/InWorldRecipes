package com.benbenlaw.inworldrecipes.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class InWorldConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.ConfigValue<Boolean> DISABLE_DRAGON_EGG_TELEPORT;


    static {
        BUILDER.comment("In-World Recipes Config for In-World Recipes")
                .push("In-World Recipes Config");

        DISABLE_DRAGON_EGG_TELEPORT = BUILDER
                .comment("If true, disables the teleportation of the dragon egg.")
                .define("disable_dragon_egg_teleport", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
