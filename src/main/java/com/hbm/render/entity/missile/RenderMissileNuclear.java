package com.hbm.render.entity.missile;

import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.entity.missile.EntityMissileTier4;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
// PRACTICALLY - TIER 4 MISSILES
@AutoRegister(entity = EntityMissileTier4.EntityMissileN2.class, factory = "FACTORY")
@AutoRegister(entity = EntityMissileTier4.EntityMissileNuclear.class, factory = "FACTORY")
@AutoRegister(entity = EntityMissileTier4.EntityMissileMirv.class, factory = "FACTORY")
@AutoRegister(entity = EntityMissileTier4.EntityMissileVolcano.class, factory = "FACTORY")
public class RenderMissileNuclear extends Render<EntityMissileBaseNT> {

	public static final IRenderFactory<EntityMissileBaseNT> FACTORY = (RenderManager man) -> {return new RenderMissileNuclear(man);};
	
	protected RenderMissileNuclear(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityMissileBaseNT missile, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		GlStateManager.enableLighting();
        double[] renderPos = NTMRenderHelper.getRenderPosFromMissile(missile, partialTicks);
        x = renderPos[0];
        y = renderPos[1];
        z = renderPos[2];
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(missile.prevRotationYaw + (missile.rotationYaw - missile.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(missile.prevRotationPitch + (missile.rotationPitch - missile.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(1.5F, 1.5F, 1.5F);

        if(missile instanceof EntityMissileTier4.EntityMissileVolcano)
			bindTexture(ResourceManager.missileVolcano_tex);
		else if(missile instanceof EntityMissileTier4.EntityMissileNuclear)
			bindTexture(ResourceManager.missileNuclear_tex);
		else if(missile instanceof EntityMissileTier4.EntityMissileMirv)
			bindTexture(ResourceManager.missileMIRV_tex);
		else
			bindTexture(ResourceManager.missileN2_tex);
        ResourceManager.missileNuclear.renderAll();
        GL11.glPopAttrib();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMissileBaseNT entity) {
		return ResourceManager.missileNuclear_tex;
	}

}
