package com.hbm.render.item;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemAssemblyTemplate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

public class TemplateItemRenderer extends TileEntityItemStackRenderer {

    public static final TemplateItemRenderer INSTANCE = new TemplateItemRenderer();
    static ItemStack stackToRender = ItemStack.EMPTY;

    private static void render(ItemStack stack, IBakedModel finalModel) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0.5F, 0F);
        RenderHelper.enableGUIStandardItemLighting();

        finalModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(finalModel, TransformType.GUI, false);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, finalModel);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public void renderByItem(@NotNull ItemStack ignored) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            ItemStack overrideStack = getOverrideStack(stackToRender);
            if (!overrideStack.isEmpty()) {
                IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(overrideStack, null, null);
                render(overrideStack, model);
            }
        } else {
            renderOriginalModel(stackToRender);
        }
    }

    private void renderOriginalModel(ItemStack stack) {
        if (stack.isEmpty()) return;

        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);

        IBakedModel finalModel = model;
        if (model instanceof TemplateBakedModel wrapper) {
            IBakedModel original = wrapper.originalModel;
            finalModel = original.getOverrides().handleItemState(original, stack, null, null);
        }
        render(stack, finalModel);
    }

    private ItemStack getOverrideStack(ItemStack originalStack) {
        if (originalStack.isEmpty()) return ItemStack.EMPTY;
        Item item = originalStack.getItem();

        if (item instanceof ItemAssemblyTemplate) {
            ComparableStack output = ItemAssemblyTemplate.getRecipeOutput(originalStack);
            return (output != null) ? output.toStack() : ItemStack.EMPTY;
        } else if (item == ModItems.chemistry_template) {
            return new ItemStack(ModItems.chemistry_icon, 1, originalStack.getMetadata());
        } else if (item == ModItems.crucible_template) {
            if (CrucibleRecipes.indexMapping.containsKey(originalStack.getMetadata())) {
                return CrucibleRecipes.indexMapping.get(originalStack.getMetadata()).icon;
            }
        }
        return ItemStack.EMPTY;
    }
}