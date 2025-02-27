package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.RenderSparks;
import com.hbm.tileentity.machine.TileEntityMachineReactorBreeding;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

public class RenderBreeder extends TileEntitySpecialRenderer<TileEntityMachineReactorBreeding> {

    @Override
    public boolean isGlobalRenderer(TileEntityMachineReactorBreeding te) {
        return true;
    }

    @Override
    public void render(TileEntityMachineReactorBreeding breeder, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();

        GL11.glRotatef(90, 0F, 1F, 0F);

        switch(breeder.getBlockMetadata() - BlockDummyable.offset)
        {
            case 2: GL11.glRotatef(0, 0F, 1F, 0F); break;
            case 4: GL11.glRotatef(90, 0F, 1F, 0F); break;
            case 3: GL11.glRotatef(180, 0F, 1F, 0F); break;
            case 5: GL11.glRotatef(270, 0F, 1F, 0F); break;
        }

        if(breeder.progress > 0.0F)
            for(int i = 0; i < 3; i++) {
                GL11.glPushMatrix();
                GL11.glRotatef((float) (Math.PI * i), 0F, 1F, 0F);
                RenderSparks.renderSpark((int) ((System.currentTimeMillis() % 10000) / 100 + i), 0, 1.875, 0, 0.15F, 3, 4, 0x00ff00, 0xffffff);
                GL11.glPopMatrix();
            }

        GL11.glScaled(0.5, 0.5, 0.5);

        bindTexture(ResourceManager.breeder_tex);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.breeder.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.enableCull();

        GL11.glPopMatrix();
    }
}
