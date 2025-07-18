package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.turret.TileEntityTurretChekhov;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class RenderTurretChekhov extends RenderTurretBase<TileEntityTurretChekhov>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityTurretChekhov turret,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    Vec3d pos = turret.byHorizontalIndexOffset();

    GL11.glPushMatrix();
    GL11.glTranslated(x + pos.x, y, z + pos.z);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();
    GlStateManager.shadeModel(GL11.GL_SMOOTH);

    this.renderConnectors(turret, true, false, null);

    bindTexture(ResourceManager.turret_base_tex);
    ResourceManager.turret_chekhov.renderPart("Base");
    double yaw =
        -Math.toDegrees(
                turret.lastRotationYaw
                    + (turret.rotationYaw - turret.lastRotationYaw) * partialTicks)
            - 90D;
    double pitch =
        Math.toDegrees(
            turret.lastRotationPitch
                + (turret.rotationPitch - turret.lastRotationPitch) * partialTicks);

    GL11.glRotated(yaw, 0, 1, 0);
    bindTexture(ResourceManager.turret_carriage_tex);
    ResourceManager.turret_chekhov.renderPart("Carriage");

    GL11.glTranslated(0, 1.5, 0);
    GL11.glRotated(pitch, 0, 0, 1);
    GL11.glTranslated(0, -1.5, 0);
    bindTexture(ResourceManager.turret_chekhov_tex);
    ResourceManager.turret_chekhov.renderPart("Body");

    float rot = turret.lastSpin + (turret.spin - turret.lastSpin) * partialTicks;

    GL11.glTranslated(0, 1.5, 0);
    GL11.glRotated(rot, -1, 0, 0);
    GL11.glTranslated(0, -1.5, 0);
    bindTexture(ResourceManager.turret_chekhov_barrels_tex);
    ResourceManager.turret_chekhov.renderPart("Barrels");

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GL11.glPopMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.turret_chekhov);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -3, 0);
        GlStateManager.scale(4, 4, 4);
      }

      public void renderCommon() {
        GlStateManager.translate(-0.75, 0, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.turret_base_tex);
        ResourceManager.turret_chekhov.renderPart("Base");
        bindTexture(ResourceManager.turret_carriage_tex);
        ResourceManager.turret_chekhov.renderPart("Carriage");
        bindTexture(ResourceManager.turret_chekhov_tex);
        ResourceManager.turret_chekhov.renderPart("Body");
        bindTexture(ResourceManager.turret_chekhov_barrels_tex);
        ResourceManager.turret_chekhov.renderPart("Barrels");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
