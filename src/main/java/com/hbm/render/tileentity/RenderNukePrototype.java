package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.bomb.TileEntityNukePrototype;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class RenderNukePrototype extends TileEntitySpecialRenderer<TileEntityNukePrototype>
    implements IItemRendererProvider {

  @Override
  public boolean isGlobalRenderer(TileEntityNukePrototype te) {
    return true;
  }

  @Override
  public void render(
      TileEntityNukePrototype te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.enableLighting();
    GlStateManager.disableCull();
    switch (te.getBlockMetadata()) {
      case 5:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
      case 2:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case 3:
        GlStateManager.rotate(-90, 0F, 1F, 0F);
        break;
    }

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.bomb_prototype_tex);
    ResourceManager.bomb_prototype.renderAll();
    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.enableCull();
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.nuke_prototype);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.scale(2.25, 2.25, 2.25);
      }

      public void renderCommon() {
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.bomb_prototype_tex);
        ResourceManager.bomb_prototype.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
