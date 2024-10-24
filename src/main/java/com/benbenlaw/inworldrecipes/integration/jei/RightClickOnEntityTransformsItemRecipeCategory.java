package com.benbenlaw.inworldrecipes.integration.jei;

import com.benbenlaw.inworldrecipes.InWorldRecipes;
import com.benbenlaw.inworldrecipes.recipes.RightClickOnEntityTransformsItemRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Arrays;

public class RightClickOnEntityTransformsItemRecipeCategory implements IRecipeCategory<RightClickOnEntityTransformsItemRecipe> {
    public final static ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "right_click_on_entity_transforms_item");
    public final static ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(InWorldRecipes.MOD_ID, "textures/gui/jei_right_click_on_entity_item.png");
    static final RecipeType<RightClickOnEntityTransformsItemRecipe> RECIPE_TYPE = RecipeType.create(InWorldRecipes.MOD_ID, "right_click_on_entity_transforms_item",
            RightClickOnEntityTransformsItemRecipe.class);

    int yOffset = 21;
    int totalMessages;
    private IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper helper;


    @Override
    public @Nullable ResourceLocation getRegistryName(RightClickOnEntityTransformsItemRecipe recipe) {
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RightClickOnEntityTransformsItemRecipe.Type.INSTANCE).stream()
                .filter(recipeHolder -> recipeHolder.value().equals(recipe))
                .map(RecipeHolder::id)
                .findFirst()
                .orElse(null);
    }

    public RightClickOnEntityTransformsItemRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.background = helper.createDrawable(TEXTURE, 0, 0, 139, 18);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.DIAMOND_BLOCK));
    }


    @Override
    public @NotNull RecipeType<RightClickOnEntityTransformsItemRecipe> getRecipeType() {
        return JEIInWorldRecipesPlugin.RIGHT_CLICK_ON_ENTITY_TRANSFORM_ITEM_RECIPE;
    }


    @Override
    public @NotNull Component getTitle() {
        return Component.literal("Right Click On Entity");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RightClickOnEntityTransformsItemRecipe recipe, @NotNull IFocusGroup focusGroup) {

        totalMessages = 0;

        builder.addSlot(RecipeIngredientRole.CATALYST, 4, 2).addItemStacks(Arrays.asList(recipe.heldItem().getItems()));
        //builder.addSlot(RecipeIngredientRole.INPUT, 40, 2).addIngredients(recipe.targetBlock());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 2).addItemStack(recipe.resultItem());

        if (recipe.damageHeldItem()) {
            totalMessages += 1;
        }
        if (recipe.consumeHeldItem()) {
            totalMessages += 1;
        }
        if (recipe.popItem()) {
            totalMessages += 1;
        }
        if (recipe.destroyEntity()) {
            totalMessages += 1;
        }

        background = helper.createDrawable(TEXTURE, 0, 0, 139, 18 + (totalMessages * 10));

    }

    @Override
    public void draw(RightClickOnEntityTransformsItemRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        yOffset = 20;

        assert Minecraft.getInstance().level != null;
        Entity entity = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(recipe.entity())).create(Minecraft.getInstance().level);
        assert entity != null;
        float entityWidth = entity.getDimensions(Pose.STANDING).width();
        float entityHeight = entity.getDimensions(Pose.STANDING).height();
        float scale = Math.min(1.0f / entityWidth, 1.0f / entityHeight) * 20;

        if (entity instanceof EnderDragon) {
            scale = 5.0f;
        }

        renderEntityInCategory(guiGraphics, 40 + 8, 2 + 16, scale, 40, 0, entity);


        if (recipe.damageHeldItem()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Damages Item", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (recipe.consumeHeldItem()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Consumes Item", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (recipe.popItem()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Item Drops In World", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
        if (recipe.destroyEntity()) {
            guiGraphics.drawString(Minecraft.getInstance().font, "Destroys Entity", 0, yOffset, Color.GRAY.getRGB(), false);
            yOffset += 10;
        }
    }


    // This method is from JustEnoughProfessions modified for all entities - Credit @mrbysco///
    // https://github.com/Mrbysco/JustEnoughProfessions/blob/multi/1.21/common/src/main/java/com/mrbysco/justenoughprofessions/RenderHelper.java //
    private void renderEntityInCategory(GuiGraphics guiGraphics, int x, int y, double scale, double yaw, double pitch, Entity entity) {

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 50.0D);
        poseStack.scale((float) scale, (float) scale, (float) scale);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        poseStack.mulPose(Axis.XP.rotationDegrees(((float) Math.atan((-40 / 40.0F))) * 10.0F));

        entity.setYBodyRot((float) -(yaw / 40.F) * 20.0F);
        entity.setYRot((float) -(yaw / 40.F) * 20.0F);
        entity.setYHeadRot(entity.getYRot());
        entity.setXRot((float) -(pitch / 5.F));

        poseStack.translate(0.0F, entity.getVehicleAttachmentPoint(entity).y(), 0.0F);
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityRenderDispatcher.overrideCameraOrientation(new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F));
        entityRenderDispatcher.setRenderShadow(false);
        final MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, bufferSource, 15728880);
        });
        bufferSource.endBatch();
        entityRenderDispatcher.setRenderShadow(true);
        poseStack.popPose();
    }

}