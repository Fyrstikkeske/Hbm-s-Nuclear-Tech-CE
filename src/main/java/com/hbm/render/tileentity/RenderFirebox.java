package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityHeaterFirebox;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderFirebox extends TileEntitySpecialRenderer<TileEntityHeaterFirebox>
    implements IItemRendererProvider {
  @Override
  public boolean isGlobalRenderer(TileEntityHeaterFirebox te) {
    return true;
  }

  @Override
  public void render(
          TileEntityHeaterFirebox tile,
          double x,
          double y,
          double z,
          float partialTicks,
          int destroyStage,
          float alpha) {

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();

    switch (tile.getBlockMetadata() - BlockDummyable.offset) {
      case 3:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 2:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
    }

    GlStateManager.rotate(-90, 0F, 1F, 0F);

    TileEntityHeaterFirebox firebox = tile;

    bindTexture(ResourceManager.heater_firebox_tex);
    ResourceManager.heater_firebox.renderPart("Main");

    GlStateManager.pushMatrix();
    float door = firebox.prevDoorAngle + (firebox.doorAngle - firebox.prevDoorAngle) * partialTicks;
    GlStateManager.translate(1.375, 0, 0.375);
    GlStateManager.rotate(door, 0F, -1F, 0F);
    GlStateManager.translate(-1.375, 0, -0.375);
    ResourceManager.heater_firebox.renderPart("Door");
    GlStateManager.popMatrix();

    if (firebox.wasOn) {
      GlStateManager.pushMatrix();
      GlStateManager.pushAttrib();

      GlStateManager.disableLighting();
      GlStateManager.disableCull();

      // Full brightness
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

      ResourceManager.heater_firebox.renderPart("InnerBurning");

      GlStateManager.enableLighting();
      GlStateManager.popAttrib();
      GlStateManager.popMatrix();
    } else {
      ResourceManager.heater_firebox.renderPart("InnerEmpty");
    }

    GlStateManager.popMatrix();
  }


  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.heater_firebox);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -1, 0);
        GlStateManager.scale(3.25, 3.25, 3.25);
      }

      public void renderCommon() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.heater_firebox_tex);
        ResourceManager.heater_firebox.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
