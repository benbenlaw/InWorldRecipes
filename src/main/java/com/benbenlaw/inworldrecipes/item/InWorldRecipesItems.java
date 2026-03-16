package com.benbenlaw.inworldrecipes.item;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InWorldRecipesItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(InWorldRecipes.MOD_ID);

    public static final DeferredItem<Item> TRIGGER = ITEMS.registerSimpleItem("trigger");
    public static final DeferredItem<Item> CONDITION = ITEMS.registerSimpleItem("condition");
    public static final DeferredItem<Item> RESULT = ITEMS.registerSimpleItem("result");


}
